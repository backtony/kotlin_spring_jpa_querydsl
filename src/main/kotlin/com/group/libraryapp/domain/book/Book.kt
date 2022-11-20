package com.group.libraryapp.domain.book

import javax.persistence.*

// jpa르 사용하기 위해서는 argument가 없는 기본 생성자가 필요하다.
// 이에 대한 설정은 매번 entity에서 하기 번거로우니 plugin을 사용한다.(build.gradle)
@Entity
class Book(
    var name: String,

    @Enumerated(EnumType.STRING)
    val type: BookType,

    // 디폴트 파라미터는 가장 마지막에 주는 거이 관례
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

