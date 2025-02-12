# Kotlin으로 배우는 Spring Boot 프레임워크

이 가이드는 Kotlin 개발자가 Spring Boot를 처음 배울 때 필요한 기본 개념과 실습을 포함합니다.

## 목차

1. [Spring Boot 소개](#spring-boot-소개)
2. [프로젝트 설정](#프로젝트-설정)
3. [기본 구조 이해하기](#기본-구조-이해하기)
4. [REST API 개발하기](#rest-api-개발하기)
5. [데이터베이스 연동](#데이터베이스-연동)
6. [테스트 작성하기](#테스트-작성하기)
7. [실습: CRUD 애플리케이션 만들기](#실습-crud-애플리케이션-만들기)

## Spring Boot 소개

Spring Boot는 Java/Kotlin 기반의 애플리케이션 프레임워크로, 다음과 같은 특징을 가집니다:

-   빠른 개발 환경 구성
-   자동 설정(Auto Configuration)
-   내장 서버
-   의존성 관리 자동화

### 주요 개념

1. **의존성 주입(Dependency Injection)**

    - 객체 간의 의존 관계를 외부에서 주입
    - 느슨한 결합도와 높은 응집도 달성

    ```kotlin
    @Service
    class UserService(private val userRepository: UserRepository)  // 생성자 주입
    ```

2. **어노테이션 기반 설정**
    - `@SpringBootApplication`: 스프링 부트 애플리케이션 시작점
    - `@RestController`: REST API 컨트롤러 정의
    - `@Service`: 비즈니스 로직 처리 계층
    - `@Repository`: 데이터 접근 계층

## 프로젝트 설정

### 1. 프로젝트 생성

[Spring Initializr](https://start.spring.io/)에서 다음 설정으로 프로젝트 생성:

-   Project: Gradle - Kotlin
-   Language: Kotlin
-   Spring Boot: 3.3.0
-   Dependencies:
    -   Spring Web
    -   Spring Data JPA
    -   H2 Database

### 2. build.gradle.kts 설정

```kotlin
plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.h2database:h2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

## 기본 구조 이해하기

### 1. 애플리케이션 구조

```
src/
├── main/
│   ├── kotlin/
│   │   └── com/example/demo/
│   │       ├── DemoApplication.kt
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       └── entity/
│   └── resources/
│       └── application.properties
└── test/
    └── kotlin/
```

### 2. 각 계층의 역할

1. **Entity**: 데이터 모델 정의

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue
    val id: Long = 0,
    var name: String,
    var email: String
)
```

2. **Repository**: 데이터 접근 계층

```kotlin
@Repository
interface UserRepository : JpaRepository<User, Long>
```

3. **Service**: 비즈니스 로직 처리

```kotlin
@Service
class UserService(private val repository: UserRepository) {
    fun getAllUsers() = repository.findAll()
}
```

4. **Controller**: HTTP 요청 처리

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(private val service: UserService) {
    @GetMapping
    fun getUsers() = service.getAllUsers()
}
```

## REST API 개발하기

### 1. Controller 작성

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(private val service: UserService) {
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>> =
        ResponseEntity.ok(service.getAllUsers())

    @PostMapping
    fun createUser(@RequestBody user: User): ResponseEntity<User> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.createUser(user))
}
```

### 2. HTTP 메서드와 어노테이션

-   `@GetMapping`: 조회
-   `@PostMapping`: 생성
-   `@PutMapping`: 수정
-   `@DeleteMapping`: 삭제
-   `@RequestBody`: HTTP 요청 본문을 객체로 변환
-   `@PathVariable`: URL 경로 변수 추출

## 데이터베이스 연동

### 1. JPA 설정

application.properties:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

### 2. Entity 설계

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(unique = true)
    var email: String
)
```

## 테스트 작성하기

### 1. 단위 테스트 (Service)

```kotlin
@Test
fun `should get user by id`() {
    // given
    val user = User(1, "홍길동", "hong@test.com", 30)
    every { userRepository.findById(1) } returns Optional.of(user)

    // when
    val result = userService.getUserById(1)

    // then
    assertEquals(user, result)
}
```

### 2. 통합 테스트 (Controller)

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    @Test
    fun `should create user`() {
        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"홍길동","email":"hong@test.com"}"""))
            .andExpect(status().isCreated)
    }
}
```

## 실습: CRUD 애플리케이션 만들기

### 1. 프로젝트 생성

1. Spring Initializr에서 프로젝트 생성
2. 의존성 추가:
    - Spring Web
    - Spring Data JPA
    - H2 Database

### 2. Entity 클래스 작성

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue
    val id: Long = 0,
    var name: String,
    var email: String,
    var age: Int
)
```

### 3. Repository 인터페이스 생성

```kotlin
@Repository
interface UserRepository : JpaRepository<User, Long>
```

### 4. Service 클래스 구현

```kotlin
@Service
class UserService(private val userRepository: UserRepository) {
    fun getAllUsers() = userRepository.findAll()
    fun getUserById(id: Long) = userRepository.findById(id)
        .orElseThrow { NoSuchElementException("User not found") }
    fun createUser(user: User) = userRepository.save(user)
    fun updateUser(id: Long, user: User): User {
        val existingUser = getUserById(id)
        existingUser.name = user.name
        existingUser.email = user.email
        return userRepository.save(existingUser)
    }
    fun deleteUser(id: Long) = userRepository.deleteById(id)
}
```

### 5. Controller 구현

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(private val service: UserService) {
    @GetMapping
    fun getAllUsers() = service.getAllUsers()

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long) = service.getUserById(id)

    @PostMapping
    fun createUser(@RequestBody user: User) = service.createUser(user)

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody user: User) =
        service.updateUser(id, user)

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) = service.deleteUser(id)
}
```

### 6. API 테스트

```bash
# 사용자 생성
curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{"name":"홍길동","email":"hong@test.com","age":30}'

# 사용자 조회
curl http://localhost:8080/api/users

# 특정 사용자 조회
curl http://localhost:8080/api/users/1

# 사용자 정보 수정
curl -X PUT http://localhost:8080/api/users/1 \
     -H "Content-Type: application/json" \
     -d '{"name":"홍길동2","email":"hong2@test.com","age":31}'

# 사용자 삭제
curl -X DELETE http://localhost:8080/api/users/1
```

## 추가 학습 자료

-   [Spring 공식 문서](https://spring.io/guides)
-   [Kotlin Spring Boot 튜토리얼](https://spring.io/guides/tutorials/spring-boot-kotlin/)
-   [Spring Data JPA 문서](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
