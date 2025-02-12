package com.example.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    var name: String = "",
    
    @Column(nullable = false, unique = true)
    var email: String = "",
    
    var age: Int = 0
) {
    // JPA를 위한 기본 생성자
    constructor() : this(0, "", "", 0)
} 