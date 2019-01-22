package com.exoscale.circuitbreaker.shop

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard
import org.springframework.web.bind.annotation.*

@EnableHystrixDashboard
@EnableCircuitBreaker
@SpringBootApplication
class ShopApplication

fun main(args: Array<String>) {
    SpringApplication.run(ShopApplication::class.java, *args)
}

@RestController
class FetchQuoteController(private val service: FetchQuoteService) {

    @GetMapping("/product/{id}")
    fun fetch(@PathVariable id: String) = service.getQuote(id)
}