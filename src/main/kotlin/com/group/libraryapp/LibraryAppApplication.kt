package com.group.libraryapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LibraryAppApplication

// 코틀린에서는 top level에 여러 클래스와 함수를 만들 수 있고
// 함수를 만들 경우 static 함수 취급된다.
fun main(args: Array<String>) {
    runApplication<LibraryAppApplication>(*args)
}