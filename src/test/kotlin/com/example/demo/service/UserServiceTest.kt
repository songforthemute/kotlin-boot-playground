package com.example.demo.service

import com.example.demo.entity.User
import com.example.demo.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UserServiceTest {
    private val userRepository: UserRepository = mockk()
    private val userService = UserService(userRepository)

    @Test
    fun `should get all users`() {
        // given
        val users = listOf(
            User(1, "홍길동", "hong@test.com", 30),
            User(2, "김철수", "kim@test.com", 25)
        )
        every { userRepository.findAll() } returns users

        // when
        val result = userService.getAllUsers()

        // then
        assertEquals(users, result)
        verify { userRepository.findAll() }
    }

    @Test
    fun `should get user by id`() {
        // given
        val user = User(1, "홍길동", "hong@test.com", 30)
        every { userRepository.findById(1) } returns Optional.of(user)

        // when
        val result = userService.getUserById(1)

        // then
        assertEquals(user, result)
        verify { userRepository.findById(1) }
    }

    @Test
    fun `should throw exception when user not found`() {
        // given
        every { userRepository.findById(1) } returns Optional.empty()

        // when & then
        assertThrows<NoSuchElementException> {
            userService.getUserById(1)
        }
    }

    @Test
    fun `should create user`() {
        // given
        val user = User(name = "홍길동", email = "hong@test.com", age = 30)
        val savedUser = user.copy(id = 1)
        every { userRepository.save(user) } returns savedUser

        // when
        val result = userService.createUser(user)

        // then
        assertEquals(savedUser, result)
        verify { userRepository.save(user) }
    }
} 