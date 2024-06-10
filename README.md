# Spring Boot on Kotlin

- [Create a RESTful web service with a dataabse using Spring Boot](https://kotlinlang.org/docs/jvm-create-project-with-spring-boot.html)
- [Spring initializr](https://start.spring.io)

## 목차

1. Create a Spring Boot project with Kotlin
    - [Explore the project Gradle build file](#explore-the-project-gradle-build-file)
    - [Explore the generated Spring Boot application](#explore-the-generated-spring-boot-application)
    - [Create a controller](#create-a-controller)
    - [Run the application](#run-the-application)
2. Add a data class to the Spring Boot project
3. Add database support for the Spring Boot project
4. Use Spring Data CrudRepository for the database access

---

## Explore the project Gradle build file

- `build.gradle.kts`
    - 애플리케이션에 필요한 종속성 목록이 포함된 Gradle Kotlin 빌드 스크립트
    - Gradle 파일은 Spring Boot의 표준이지만, kotlin-spring Gradle 플러그인인 `kotlin("plugin.spring")`을 비롯한 필수
      Kotlin 종속 요소도 포함

```kts
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile // For `KotlinCompile` task below

plugins {
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.2"
    kotlin("jvm") version "2.0.0" // 사용할 Kotlin 버전
    kotlin("plugin.spring") version "2.0.0" // Kotlin Spring 플러그인
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // JSON 작업을 위한 Kotlin용 Jackson 익스텐션
    implementation("org.jetbrains.kotlin:kotlin-reflect") // Spring 작업에 필요한 Kotlin reflection library
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> { // `KotlinCompile` 작업을 위한 설정
    kotlinOptions { // Kotlin 컴파일러 옵션
        freeCompilerArgs =
            listOf("-Xjsr305=strict") // `-Xjsr305=strict` enables the strict mode for JSR-305 annotations
        jvmTarget = "17" // 생성된 JVM 바이트코드의 타겟 버전 명시 
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

1. `plugins` 블록의 Kotlin 아티팩트
    - `kotlin("jvm")` - 프로젝트에서 사용할 Kotlin 버전 정의
    - `kotlin("plugin.spring")` 스프링 프레임워크 기능과 호환되도록 하기 위해 Kotlin 클래스에 `open` 수정자를 추가하기 위한 Kotlin
      Spring 컴파일러 플러그인
2. `dependencies` 블록의 Kotlin 관련 모듈들
    - `com.fasterxml.jackson.module:jackson-module-kotlin` Kotlin 클래스 및 데이터 클래스의 직렬화 및 역직렬화 지원 추가
    - `org.jetbrains.kotlin:kotlin-reflect` Kotlin 리플렉션 라이브러리
3. 종속성 섹션 뒤의 `KotlinCompile` 작업 구성 블록이 있는데, 여기에서 컴파일러에 엑스트라 인수를 추가해 다양한 언어 기능을 활성화 또는 비활성화 가능

---

## Explore the generated Spring Boot application

```kotlin
// DemoApplication.kt
package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
```

- **클래스 선언 - `Class DemoApplication`**
    - 패키지 선언과 import 문 바로 뒤에, 첫 번째 선언인 DemoApplication 클래스를 볼 수 있음
    - Kotlin에서는 클래스에 멤버(속성 혹은 함수)가 포함되지 않은 경우 클래스 본문(`{}`)을 생략 가능

- **`@SpringBootApplication` 어노테이션**
    - Spring Boot 애플리케이션의 편의성 어노테이션 ([
      _docs_](https://docs.spring.io/spring-boot/reference/using/using-the-springbootapplication-annotation.html#using.using-the-springbootapplication-annotation))
    - Spring
      Boot의 [자동 구성](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html#using.auto-configuration),
      [구성 요소 스캔](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/ComponentScan.html)
      을 활성화하고, '애플리케이션 클래스'에 대한 추가 구성을 정의할 수 있음

- **Program entry porint - `main()`**
    - 애플리케이션의 진입점
    - `DemoApplication` 클래스 외부의 [최상위 함수](https://kotlinlang.org/docs/functions.html#local-functions)
      로
      선언됨
    - `main()` 함수는 Spring의 `runApplication(*arg)` 함수를 호출해 Spring 프레임워크에서 애플리케이션을 시작

- **가변 인수 - `args: Array<String>`**
    - `runApplication()` 함수의 선언을 확인하면, 함수의
      매개변수에 [`vararg`](https://kotlinlang.org/docs/functions.html#explicit-return-types)
      수정자(`vararg arg: String`)가 표시되어 있음
    - 이는 함수의 가변적인 수의 문자열 인수를 전달할 수 있음을 의미

- **스프레드 연산자 - `(*args)`**
    - `args`는 문자열 배열로 선언된 `main()` 함수에 대한 매개변수
    - 문자열 배열이 있고, 그 내용을 함수에 전달하려면 스프레드 연산자 사용(배열 앞에 별표 기호 `*`를 붙임)

### Create a controller

```kotlin
// DemoApplication.kt
@RestController
class MessageController {
    @GetMapping("/")
    fun index(@RequestParam("name") name: String) = "Hello, $name!"
}
```

- Spring 애플리케이션에서 컨트롤러는 웹 요청을 처리하는 데 사용
- **`@RestController` 어노테이션**
    - Spring에게 `MessageController`가 REST 컨트롤러임을 알리는 역할,
      _cf. [Unresolved reference](https://medium.com/@songforthemute/unresolved-reference-web-6fd6ca60139c)_
    - 이 어노테이션은 이 클래스가 `DemoApplication` 클래스와 동일한 패키지에 있기 때문에 컴포넌트 스캔에서 선택된다는 것을 의미
- **`@GetMapping` 어노테이션**
    - HTTP GET 호출에 해당하는 엔드포인트를 구현하는 REST 컨트롤러의 함수 표시
- **`@RequestParam` 어노테이션**
    - 함수 매개변수 이름에는 `@RequestParam` 어노테이션이 표시되어 있는데, 이는 메서드 매개변수가 웹 요펑 매개변수에 바인딩되어야 함을 표시
    - 따라서 루트에서 애플리케이션에 액세스하여 `/?name=example`과 같이 `name`이라는 요청 매개변수를 제공하면 매개변수 값이 `index()` 함수를 호출할
      때 인수로 사용됨
- **Single-expression functions - `index()`**
    - `index()` 함수는 하나의 문만 포함하므로 단일 표현식 함수로 선언 가능

### Run the application

1. `main()` 함수를 실행하면, 로컬 서버가 컴퓨터에서 실행
    - 또는 터미널에서 `./gradlew bootRun` 커맨드를 실행해도 가능
2. `http://localhost:8080?name=Jenny`