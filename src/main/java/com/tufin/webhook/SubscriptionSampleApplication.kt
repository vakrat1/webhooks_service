package com.tufin.webhook

//import kotlin.jvm.JvmStatic;
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

//@SpringBootApplication(scanBasePackages = ["com.tufin.publishers", "com.tufin.resolvers"])
@SpringBootApplication
class SubscriptionSampleApplication

    //  @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(SubscriptionSampleApplication::class.java, *args)
    }
