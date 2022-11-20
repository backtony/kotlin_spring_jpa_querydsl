package com.group.libraryapp.util

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull

// .kt 파일은 코틀린의 파일 형태
// 여기서 함수를 만들면 자바로 변환시 final 클래스의 static final 함수로 만들어진다.

fun fail(): Nothing {
    throw IllegalArgumentException()
}

fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID): T {
    return this.findByIdOrNull(id) ?: fail()
}