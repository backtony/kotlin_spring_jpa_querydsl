package com.group.libraryapp.domain.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>, UserRepositoryCustom {

    // 자바의 경우 optional을 사용했지만,
    // kotlin의 경우 ?를 사용하여 null을 받고
    // 동작을 시켜야 하는 경우 safe call을
    // null인 경우 exception을 던져야 한다면(orElseThrow) 엘비스 연산자로 처리하면
    // optional을 제거할 수 있다.
    fun findByName(name: String) : User?

    // 하지만 CRUD repo에서 findById로 반환하는 optional은 컨트롤할 수 없는데
    // 코틀린의 확장함수를 통해 제어할 수 있다.
    // 코틀린은 CRUD Repo과 함께 사용을 대비해서 CrudRepositoryExtensions에서
    // findByIdOrNull을 제공한다.
}