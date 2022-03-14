package com.example.demo

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Database {
    @Bean
    fun initDatabase(adminsRepository: AdminsRepository):CommandLineRunner{
        return CommandLineRunner{
            adminsRepository.save(Admin("DAM2","123456"))
        }
    }

}