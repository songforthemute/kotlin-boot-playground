package com.example.demo.controller

import com.example.demo.entity.User
import com.example.demo.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun `should create user`() {
        val user = User(
            name = "홍길동",
            email = "hong@test.com",
            age = 30
        )
        
        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value(user.name))
            .andExpect(jsonPath("$.email").value(user.email))
            .andExpect(jsonPath("$.age").value(user.age))
    }

    @Test
    fun `should get all users`() {
        val user1 = userRepository.save(User(name = "홍길동", email = "hong@test.com", age = 30))
        val user2 = userRepository.save(User(name = "김철수", email = "kim@test.com", age = 25))

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value(user1.name))
            .andExpect(jsonPath("$[1].name").value(user2.name))
    }

    @Test
    fun `should get user by id and return 404 when not found`() {
        // Success case
        val savedUser = userRepository.save(User(
            name = "홍길동",
            email = "hong@test.com",
            age = 30
        ))

        mockMvc.perform(get("/api/users/${savedUser.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value(savedUser.name))

        // Not found case
        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should update user`() {
        val savedUser = userRepository.save(User(name = "홍길동", email = "hong@test.com", age = 30))
        val updatedUser = User(name = "홍길동2", email = "hong2@test.com", age = 31)

        mockMvc.perform(put("/api/users/${savedUser.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatedUser)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("홍길동2"))
            .andExpect(jsonPath("$.email").value("hong2@test.com"))
            .andExpect(jsonPath("$.age").value(31))
    }

    @Test
    fun `should delete user`() {
        val savedUser = userRepository.save(User(name = "홍길동", email = "hong@test.com", age = 30))

        mockMvc.perform(delete("/api/users/${savedUser.id}"))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/api/users/${savedUser.id}"))
            .andExpect(status().isNotFound)
    }
} 