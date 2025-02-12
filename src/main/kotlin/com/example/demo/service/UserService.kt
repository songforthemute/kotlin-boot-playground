package com.example.demo.service

import com.example.demo.entity.User
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {

    fun getAllUsers(): List<User> = userRepository.findAll()

    fun getUserById(id: Long): User {
        return userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User not found with id: $id") }
    }

    @Transactional
    fun createUser(user: User): User = userRepository.save(user)

    @Transactional
    fun updateUser(id: Long, user: User): User {
        val existingUser = getUserById(id)
        existingUser.name = user.name
        existingUser.email = user.email
        existingUser.age = user.age
        return userRepository.save(existingUser)
    }

    @Transactional
    fun deleteUser(id: Long) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id)
        } else {
            throw NoSuchElementException("User not found with id: $id")
        }
    }
} 