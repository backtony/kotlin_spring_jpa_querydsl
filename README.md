## Kotlin + Spring + JPA + Querydsl

### build.gradle 설정
```groovy
plugins {
    id 'org.springframework.boot' version '2.6.8'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.6.21'

    // https://cheese10yun.github.io/spring-kotlin/
    // all-open 플러그인과 동일한 프로젝트
    // @Component, @Async, @Transactional(aop를 위해 override가 필요), @Cacheable, @SpringBootTest, @Configuration
    // @Controller, @RestController, @Service, @Repository, @Component
    // 위 애노테이션이 붙은 클래스를 open 시켜준다.(코틀린은 기본적으로 전부 상속이 불가능하도록 final)
    id 'org.jetbrains.kotlin.plugin.spring' version '1.6.21'

    // https://cheese10yun.github.io/spring-kotlin/
    // @Entity, @Embeddable, @MappedSuperclass 의 경우 noArg(기본생성자)를 만들어준다.
    id 'org.jetbrains.kotlin.plugin.jpa' version '1.6.21'

    // querydsl
    id 'org.jetbrains.kotlin.kapt' version '1.6.21'
}

// proxy lazy fetching을 완전히 이용하려면 클래스가 상속 가능해야 한다. 하지만 코틀린은 기본적으로 전부 final이다.
// all open = plugin.spring으로 열리지 않은 jpa final 클래스들을 수동으로 open으로 열어준다.
allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

group = 'com.group'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    runtimeOnly 'com.h2database:h2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8'

    // kotlin reflection 허용
    implementation 'org.jetbrains.kotlin:kotlin-reflect:1.6.21'
    // jackson은 자바만을 고려된 모듈이라 코틀린도 잘 이해할 수 있도록 하는 모듈
    implementation 'com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3'

    //querydsl
    implementation 'com.querydsl:querydsl-jpa:5.0.0'
    kapt("com.querydsl:querydsl-apt:5.0.0:jpa")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.named('test') {
    useJUnitPlatform()
}

compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}

compileTestKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}
```

<br>

### optional 제거하기
```kotlin
interface UserRepository : JpaRepository<User, Long>, UserRepositoryCustom {

    // 자바의 경우 optional을 사용했지만,
    // kotlin의 경우 ?를 사용하여 null을 받고
    // 동작을 시켜야 하는 경우 safe call을
    // null인 경우 exception을 던져야 한다면(orElseThrow) 엘비스 연산자로 처리하면
    // optional을 제거할 수 있다.
    fun findByName(name: String) : User?
}

@Transactional
fun deleteUser(name: String) {
    val user = userRepository.findByName(name) ?: fail()
    userRepository.delete(user)
}
```

<Br>

### JPA named 함수에서 optional 제거
```kotlin
// CRUD repo에서 findById로 반환하는 optional은 컨트롤할 수 없는데
// 코틀린의 확장함수를 통해 제어할 수 있다.
// 코틀린은 CRUD Repo과 함께 사용을 대비해서 CrudRepositoryExtensions에서
// findByIdOrNull을 제공한다.
@Transactional
fun updateUserName(request: UserUpdateRequest) {
    val user = userRepository.findByIdOrNull(request.id) ?: fail()
    user.updateName(request.name)
}
```
<br>

```kotlin
// .kt 파일은 코틀린의 파일 형태
// 여기서 함수를 만들면 자바로 변환시 final 클래스의 static final 함수로 만들어진다.

fun fail(): Nothing {
    throw IllegalArgumentException()
}

// findByIdOrNull에서 엘비스 연산자로 fail exception 중복이 발생한다면 확장함수로 한번 더 래핑해서 처리해도 된다.
fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID): T {
    return this.findByIdOrNull(id) ?: fail()
}

@Transactional
fun updateUserName(request: UserUpdateRequest) {
    val user = userRepository.findByIdOrThrow(request.id) // 위 확장 함수를 한번 더 확장함수로 감싸서 만들었다.
    user.updateName(request.name)
}
```

<Br>

### Querydsl 조건 null처리
```kotlin
@Repository
class UserLoanHistoryQuerydslRepository(
    private val query: JPAQueryFactory
) {

    fun find(bookName: String, status: UserLoanStatus? = null) : UserLoanHistory? {
        return query.select(userLoanHistory)
            .from(userLoanHistory)
            .where(
                userLoanHistory.bookName.eq(bookName),
                //  non-null 값에 대해서만 code block을 실행시킬 때 사용하는 let을 사용하여 처리한다.
                status?.let { userLoanHistory.status.eq(status) }
            )
            .limit(1)
            .fetchOne()
    }
}
```

<br>

### Init 메서드로 인자 validation 처리
```kotlin
@Entity
class User(
    var name: String,
    val age: Int?,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userLoanHistories: MutableList<UserLoanHistory> = mutableListOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    init {
        if(name.isBlank()) {
            throw java.lang.IllegalArgumentException("이름은 비어 있을 수 없습니다.")
        }
    }
}
```

<br>

### 컨벤션
```kotlin
// default 파라미터는 가장 마지막에 작성하는 것이 관례
// companion object인 정적 팩토리 메서드 또한 가장 마지막에 작성하는 것이 관례
@Entity
class Book(
    var name: String,

    @Enumerated(EnumType.STRING)
    val type: BookType,

    // 디폴트 파라미터는 가장 마지막에 주는 것이 관례
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    init {
        if (name.isBlank()) {
            throw java.lang.IllegalArgumentException("이름은 비어 있을 수 없습니다.")
        }
    }

    // 정적팩토리 메서드를 가장 아래 놓는 것이 컨벤션
    // test를 위한 객체 생성 -> test fixture
    companion object {
        fun fixture(
            name: String = "책 이름",
            type: BookType = BookType.COMPUTER,
            id: Long? = null,
        ): Book {
            return Book(
                name = name,
                type = type,
                id = id,
            )
        }
    }
}
```

