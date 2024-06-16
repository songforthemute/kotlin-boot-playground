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
    - [Update your application](#update-your-application)
3. Add database support for the Spring Boot project
    - [Add database support](#add-database-support)
    - [Update the MessageController class](#update-the-messagecontroller-class)
    - [Update the MessageService class](#update-the-messageservice-class)
    - [Configure the database](#configure-the-database)
    - [Add messages to database via HTTP request](#add-messages-to-database-via-http-request)
    - [Retrieve messages by id](#retrieve-messages-by-id)
4. Use Spring Data CrudRepository for the database access
    - [Update your application](#update-your-application-1)

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

---

## Update your application

```kotlin
// DemoApplication.kt
data class Message(val id: String?, val text: String)

@RestContrller
class MessageController {
    @GetMapping("/")
    fun index() = listOf(
        Message("1", "Hello!"),
        Message("2", "Bonjour!"),
        Message("3", "Privet!"),
    )
}
```

- `Message` 클래스는 데이터 전송에 사용
    - 직렬화된 `Message` 객체들은 컨트롤러가 브라우저 요청에 응답할 JSON 문서를 구성
- **Data classes - `data class Message`**
    - Kotlin에서 `data class`의 주 목적은 데이터를 보유하는 것
    - 이런 클래스는 `data` 키워드로 표시되며, 일부 표준 기능과 일부 유틸리티 함수는 클래스 구조에서 기계적으로 파생 가능한 경우가 많음
- Spring 애플리케이션의 모든 컨트롤러는 `Jackson` 라이브러리가 클래스 경로에 있는 경우, 기본적으로 JSON 응답을 렌더링
    - `build.gradle.kts` 파일에서 `spring-boot-starter-web` 종속성을 지정하면 전이적 종속성으로 `Jackson`을 받음
    - 따라서 엔드포인트가 JSON으로 직렬화할 수 있는 데이터 구조를 반환하면 애플리케이션은 JSON 문서로 응답
- `Unresolved reference: data`
    - `build.gradle.kts` 파일의 의존성에 다음의 패키지 추가
    - `implementation("org.springframework.boot:spring-boot-starter-data-jpa")` 추가

### Run the application

- `http://localhost:8080`

```kotlin
/**
Description:
Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.

Reason: Failed to determine a suitable driver class


Action:

Consider the following:
If you want an embedded database (H2, HSQL or Derby), please put it on the classpath.
If you have database settings to be loaded from a particular profile you may need to activate it (no profiles are currently active).
 */
```

- Spring Boot가 데이터 소스를 자동 구성하려 시도했으나 필요한 구성을 찾지 못해 발생한 에러
- 데이터 소스가 필요하지 않은 경우, 다음과 같이 자동 구성을 사용하지 않도록 설정 가능

```kotlin
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class DemoApplication
```

---

## Add database support

- Spring 프레임워크 기반 애플리케이션의 일반적인 관행은 소위 서비스 계층(비즈니스 로직이 있는 곳)에서 데이터베이스 액세스 로직을 구현하는 것
- Spring에서는 클래스가 애플리케이션의 서비스 계층에 속한다는 것을 암시하기 위해 `@Service` 어노테이션으로 클래스 표시

```kotlin
import org.springframework.stereotype.Service
import org.springframework.jdbc.core.JdbcTemplate

@Service
class MessageService(val db: JdbcTemplate) {
    fun findMessages(): List<Message> = db.query("select * from messages") { response, _ ->
        Message(response.getString("id"), response.getString("text"))
    }

    fun save(message: Message) {
        db.update(
            "insert into messages values ( ?, ? )",
            message.id, message.text
        )
    }
}
```

- **Constructor argument and dependency inject - `(val db: JdbcTemplate)`**
    - Kotlin의 클래스에는 기본 생성자가 존재하며, 기본 생성자는 클래스 헤더의 일부
    - 또한, 하나 이상의 보조 생성자를 가질 수 있음
- **Trailing lambda and SAM conversion**
    - `db.query("...", RowMapper { ... })`
        - `findMessages()` 함수는 `JdbcTemplate` 클래스의 `query()` 함수 호출
        - `query()` 함수는 두 개의 인수를 받는데, 하나는 문자열 인스턴스로서의 SQL 쿼리, 다른 하나는 row당 하나의 객체를 매핑하는 콜백
    - `db.query("...", { ... })`
        - `RowMapper` 인터페이스는 메서드를 하나만 선언하므로, 인터페이스 이름을 생략해 람다 표현식을 통해 구현 가능
        - 람다 식을 함수 호출의 매개변수로 사용하기 때문에 Kotlin 컴파일러는 람다 식을 변환해야 하는 인터페이스를 인지하고 있음
        - 이를 Kotlin에서는 'SAM conversion'이라고 부름
    - `db.query("...") { ... }`
        - SAM conversion 후 query 함수는 첫 번째 위치에 문자열이, 마지막 위치에 람다 표현식이 있는 두 개의 인수를 갖게 됨
        - Kotlin 규칙에 따르면 함수의 마지막 매개변수가 함수인 경우, 해당 인수로 전달된 람다 표현식을 괄호 밖에 배치할 수 있음(Trailing lambda, 후행
          람다)
- **Underscore for unused lambda argument**
    - 여러 개의 매개변수가 있는 람다의 경우, 언더스코어 문자를 사용해 사용하지 않는 매개변수의 이름 변경 가능

## Update the MessageController class

```kotlin
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PostMapping

@RestController
class MesasgeController(val service: MessageService) {
    @GetMapping("/")
    fun index(): List<Message> = service.findMessages()

    @PostMapping("/")
    fun post(@RequestBody message: Message) {
        service.save(message)
    }
}
```

- **`@PostMapping` 어노테이션**
    - HTTP POST 요청을 처리하는 메서드에 추가하는 어노테이션
- **`@RequestBody` 어노테이션**
    - HTTP Body의 콘텐츠로 전송되 JSON을 객체로 변환하기 위해 사용하는 어노테이션
    - 애플리케이션의 classpath에 존재하는 `Jackson` 라이브러리로 인해 자동으로 변환이 이루어짐

## Update the MessageService class

```kotlin
import java.util.UUID

@Service
class MessageService(val db: JdbcTemplate) {
    fun findMessages(): List<Message> = db.query("select * from messages") { response, _ ->
        Message(response.getString("id"), response.getString("text"))
    }

    fun save(message: Message) {
        val id = message.id ?: UUID.randomUUID().toString()
        db.update(
            "insert into messages values ( ?, ? )",
            id, message.text
        )
    }
}
```

- `Message` 클래스의 `id`가 nullable 문자열로 선언됨
    - 하지만 데이터베이스에 `null`값을 `id`값으로 저장하는 것은 올바르지 않으므로 이 상황을 처리할 필요가 있음
- 이로써 애플리케이션 코드가 데이터베이스와 함께 작동할 준비가 완료되었으므로 데이터 소스를 구성해야 함

## Configure the database

1. `src/main/resources` 디렉토리에 `schema.sql` 파일 생성
    - 데이터베이스 객체 정의가 저장되는 곳
2. `src/main/resources/schema.sql` 파일 작성
    ```roomsql
    CREATE TABLE IF NOT EXISTS message (
        id  VARCHAR(60) PRIMARY KEY,
        text    VARCHAR NOT NULL
    );
    ```
    - `id`, `text` 두 개의 열로 `messages` 테이블 생성(`Message` 클래스 구조와 일치)
3. `src/main/resources` 폴더의 `application.properties` 파일에 애플리케이션 속성 추가
    ```properties
    spring.datasource.driver-class-name=org.h2.Driver
    spring.datasource.url=jdbc:h2:file:./data/testdb
    spring.datasource.username=name
    spring.datasource.password=password
    spring.sql.init.schema-locations=classpath:schema.sql
    spring.sql.init.mode=always
    ```
    - 이 설정은 Spring Boot 애플리케이션의 데이터베이스를 활성화

## Add messages to database via HTTP request

- 이전에 생성한 엔드포인트로 작업하기 위해서는 HTTP 클라이언트를 사용해야 함
- IntelliJ IDEA에서는 임베디드 HTTP 클라이언트 사용

1. 애플리케이션을 실행하면, POST 요청을 실행하여 데이터베이스에 메시지를 저장할 수 있음. `src/main/resources` 폴더에 `requests.http` 파일을
   생성하고 다음 HTTP 요청 추가
    ```http
    ### POST "Hello!"
    POST http://localhost:8080/
    Content-Type: application/json
    
    {
        "text": "Hello!"
    }
    
    ### Post "Bonjour!"
    POST http://localhost:8080/
    Content-Type: application/json
    
    {
        "text": "Bonjour!"
    }
   
    ### POST "Privet!"
    POST http://localhost:8080/
    Content-Type: application/json
    
    {
        "text": "Privet!"
    }
   
    ### Get all the messages
    GET http://localhost:8080/
    ```[testdb.mv.db](data%2Ftestdb.mv.db)

2. 모든 POST 요청을 실행하면 텍스트 메세지를 데이터베이스에 기록

- _cf. 다른 방법으로 requests를 실행하는 방법_
    ```shell
    curl -X POST --location "http://localhost:8080" -H "Content-Type: application/json" -d "{ \"text\" : \"Hello\" }"
    curl -X POST --location "http://localhost:8080" -H "Content-Type: application/json" -d "{ \"text\" : \"Bonjour\" }"
    curl -X POST --location "http://localhost:8080" -H "Content-Type: application/json" -d "{ \"text\" : \"Privet\" }"
    curl -X POST --location "http://localhost:8080" -H "Content-Type: application/json" -d "{ \"text\" : \"Hello world\!\" }"
    curl -X GET --location "http://localhost:8080"
    ```
- _cf. 애플리케이션 실행이 안될 때_
    ```shell
    Description:
    Parameter 0 of constructor in com.example.demo.MessageService required a bean of type 'org.springframework.jdbc.core.JdbcTemplate' that could not be found.

    Action:
    Consider defining a bean of type 'org.springframework.jdbc.core.JdbcTemplate' in your configuration.
    ```
    - `build.gradle.kts` 파일에서 `implementation("com.h2database:h2")` 종속성 추가
    - `DemoApplication.kt`
      파일의 `@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])` 어노테이션에서 `exclude`
      인수를 제거

## Retrieve messages by id

- 애플리케이션 기능을 확장해 ID별로 개별 메시지 검색 가능

1. `MessageService` 클래스에서 새로운 함수 `findMessageById(id: String)`를 추가
    ```kotlin
    @Service
    class MessageService(val db: JdbcTemplate) {
      
        fun findMessages(): List<Message> = db.query("select * from messages") { response, _ ->
            Message(response.getString("id"), response.getString("text"))
        }
      
        fun findMessageById(id: String): List<Message> = db.query("select * from messages where id = ?", id) { response, _ ->
            Message(response.getString("id"), response.getString("text"))
        }
      
        fun save(message: Message) {
            val id = message.id ?: UUID.randomUUID().toString()
            db.update(
                "insert into messages values ( ?, ? )",
                id, message.text
            )
        }
    }
    ```
    - id로 메시지를 가져오는 데 사용되는 `.query()` 함수는 Spring 프레임워크에서 제공하는 Kotlin 확장 함수.
    - 이 함수를 사용하려면 `import org.springframework.jdbc.core.query`를 추가로 import 할 수 있음

2. `id` 매개 변수가 있는 `index(...)` 함수를 `MessageController` 클래스에 추가
    ```kotlin
    import org.springframework.web.bind.annotation.*
    
    @RestController
    class MessageController(val service: MessageService) {
        @GetMapping("/")
        fun index(): List<Message> = service.findMessages()
    
        @GetMapping("/{id}")
        fun index(@PathVariable id: String): List<Message> =
            service.findMessageById(id)
    
        @PostMapping("/")
        fun post(@RequestBody message: Message) {
            service.save(message)
        }
    }
    ```
    - **Retrieving a value from the context path**
        - 새 함수에 `@GetMapping("/{id}")`으로 주석을 달면, Spring 프레임워크가 컨텍스트 경로에서 message id를 검색
        - 함수 인수로 `@PathVariable` 어노테이션을 넣으면 프레임워크에서 검색된 값을 함수 인수로 사용하도록 지시
    - **vararg argument position in the parameter list**
        - `query()` 함수는 세 개의 인수를 받음
            - SQL 쿼리 문자열
            - 문자열 타입 매개변수 `id`
            - RowMapper 인스턴스(람다 표현식)
        - `query()` 함수의 두 번째 매개변수는 가변 인수(vararg)로 선언됨
            - Kotlin에서는 가변 인수 매개변수의 위치가 매개변수 목록의 마지막에 위치할 필요는 없음

---

## Update your application

1. `Message` 클래스에 `@Table` 어노테이션을 추가해 데이터베이스 테이블에 대한 매핑 선언
    - `id` 필드 앞에 `@id` 어노테이션 추가
    - 어노테이션을 추가하는 것 외에도, 데이터베이스에 새 객체를 삽입할 때 `CrudRepository`가 작동하는 방식에 따라 `id`를 변경 가능(`var`)으로 만들어야
      함.
    - _cf. `@Table` 어노테이션을 추가하려면, `import org.springframework.data.relational.core.mapping.Table`
      패키지를 import 해오면 되는데, 해당 패키지는 `build.gradle.kt`
      파일에 `implementation 'org.springframework.boot:spring-boot-starter-jdbc'`의존성을 추가하면 됨_
    ```kotlin
    import org.springframework.data.annotation.Id
    import org.springframework.data.relational.core.mapping.Table
    
    @Table("MESSAGES")
    data class Message(@Id var id: String?, val text: String)
    ```

2. `Message` 데이터 클래스와 함께 작동할 `CrudRepository`의 인터페이스 선언
    ```kotlin
    import org.springframework.data.repository.CrudRepository
    
    interface MessageRepository : CrudRepository<Message, String>
    ```

3. `MessageService` 클래스를 업데이트
    - 이제 SQL 쿼리를 실행하는 대신 `MessageRepository` 호출
    ```kotlin
    import java.util.*
    
    @Service
    class MessageService(val db: MessageRepository) {
        fun findMessages(): List<Message> = db.findAll().toList()
        
        fun findMessageById(id: String): List<Message> = db.findById(id).toList() 
        
        fun save(message: Message) {
            db.save(message)
        }
   
        // Optional 클래스에 .toList() 확장 함수 추가
        fun <T : Any> Optional<out T>.toList(): List<T> =
            if (isPresent) listOf(get()) else emptyList()
    }
    ```
    - **Extension functions**
        - `CrudRepository` 인터페이스의 `findById()` 함수의 반환 유형은 `Optional` 클래스의 인스턴스
        - 그러나 일관성을 위해 단일 메시지가 포함된 List를 반환하는 것이 편리
        - 그러기 위해서는 `Optional` 값이 있는 경우, 래핑을 해제하고 해당 값이 포함된 리스트를 반환해야 하는데 `Optional` 타입의 확장 함수로 구현 가능
        - `Optional<out T>.toList()`에서 `.toList()`는 `Optional`의 확장 함수인데, 확장 함수를 사용하면 모든 클래스에 추가 함수를
          작성할 수 잇으므로 일부 라이브러리 클래스의 기능을 확장하려는 경우에 유용
    - **CrudRepository `save()` function**
        - 이 함수는 데이터베이스에 id가 없는 새 객체가 있다는 가정하에 작동하므로 insert 하려면 id가 `null`이어야 함
        - id가 null이 아닌 경우, CrudReposition는 객체가 데이터베이스에 이미 존재한다고 가정하며, 이는 insert 작업이 아닌 update 작업
        - insert 작업이 끝나면 데이터 저장소에서 `id`가 생성되어 메시지 인스턴스에 다시 할당됨
        - 따라서 `id` 속성은 `var` 키워드를 사용해서 선언해야 함

4. 메시지 테이블 정의를 업데이트해 삽입된 객체에 대한 id 생성
    - id는 문자열이므로 기본적으로 `RANDOM_UUID()` 함수를 사용해 생성할 수 있음
    ```roomsql
    CREATE TABLE IF NOT EXISTS messages (
      id  VARCHAR(60) DEFAULT RANDOM_UUID() PRIMARY KEY,
      text VARCHAR  NOT NULL
    )
    ```

5. `src/main/resources` 폴더의 `application.properties`의 데이터베이스명 변경
    ```properties
    spring.datasource.driver-class-name=org.h2.Driver
    spring.datasource.url=jdbc:h2:file:./data/testdb2
    spring.datasource.username=name
    spring.datasource.password=password
    spring.sql.init.schema-locations=classpath:schema.sql
    spring.sql.init.mode=always
    ```