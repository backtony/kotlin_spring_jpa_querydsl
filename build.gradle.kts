import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.8"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.21"

    // https://cheese10yun.github.io/spring-kotlin/
    // all-open 플러그인과 동일한 프로젝트
    // @Component, @Async, @Transactional(aop를 위해 override가 필요), @Cacheable, @SpringBootTest, @Configuration
    // @Controller, @RestController, @Service, @Repository, @Component
    // 위 애노테이션이 붙은 클래스를 open 시켜준다.(코틀린은 기본적으로 전부 상속이 불가능하도록 final)
    kotlin("plugin.spring") version "1.6.21"

    // https://cheese10yun.github.io/spring-kotlin/
    // @Entity, @Embeddable, @MappedSuperclass 의 경우 noArg(기본생성자)를 만들어준다.
    kotlin("plugin.jpa") version "1.6.21"

    // querydsl
    kotlin("kapt") version "1.6.21"
}

// proxy lazy fetching을 완전히 이용하려면 클래스가 상속 가능해야 한다. 하지만 코틀린은 기본적으로 전부 final이다.
// all open = plugin.spring으로 열리지 않은 jpa final 클래스들을 수동으로 open으로 열어준다.
allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // kotlin reflection 허용
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    // jackson은 자바만을 고려된 모듈이라 코틀린도 잘 이해할 수 있도록 하는 모듈
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")

    // querydsl
    implementation("com.querydsl:querydsl-jpa:5.0.0")
    kapt("com.querydsl:querydsl-apt:5.0.0:jpa")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
