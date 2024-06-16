package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.util.*


@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    println("hello world")
    runApplication<DemoApplication>(*args)
}

@RestController
class MessageController(val service: MessageService) {
    @GetMapping("/")
    fun index(): List<Message> = service.findMessages()

    @GetMapping("/{id}")
    fun index(@PathVariable id: String): List<Message> = service.findMessageById(id)

    @PostMapping("/")
    fun post(@RequestBody message: Message) {
        if (message.id === null) {
            message.id = UUID.randomUUID().toString()
        }

        println("Requested Message is, ${message.text}")
        println("Requested Id is, ${message.id}")

        service.save(message)
    }
}

interface MessageRepository : CrudRepository<Message, String>

@Table(name = "MESSAGES")
data class Message(@Id var id: String? = UUID.randomUUID().toString(), val text: String)

@Service
class MessageService(val db: MessageRepository) {
    fun findMessages(): List<Message> = db.findAll().toList()

    fun findMessageById(id: String): List<Message> = db.findById(id).toList()

    fun save(message: Message) {
        db.save(message)
    }

    fun <T : Any> Optional<out T>.toList(): List<T> = if (isPresent) listOf(get()) else emptyList()
}