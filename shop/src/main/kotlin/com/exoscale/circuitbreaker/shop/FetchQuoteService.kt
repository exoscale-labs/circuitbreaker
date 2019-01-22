package com.exoscale.circuitbreaker.shop

import com.exoscale.circuitbreaker.shop.Origin.Cache
import com.exoscale.circuitbreaker.shop.Origin.Srv
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import kotlin.system.measureTimeMillis

@Service
class FetchQuoteService {

    private val template = RestTemplate()
    private val cache = mutableMapOf<String, Double?>()

    @Value("\${app.services.pricing.url}")
    private lateinit var quoteUrl: String

    @HystrixCommand(fallbackMethod = "getQuoteFromCache",
        commandProperties = [
            HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "25"),
            HystrixProperty(name = "metrics.rollingPercentile.timeInMilliseconds", value = "1200")],
        threadPoolProperties = [HystrixProperty(name = "coreSize", value = "2")])
    fun getQuote(productId: String) = wrapInResult(productId, Srv) {
        template.getForObject<Double>(quoteUrl, Double::class.java, it)
            .apply { cache[it] = this }
    }

    @Suppress("UNUSED")
    fun getQuoteFromCache(productId: String) = wrapInResult(productId, Cache) {
        cache[it]
    }

    private fun wrapInResult(productId: String,
                             origin: Origin,
                             quote: (String) -> Double?): Result {
        var price: Double? = null
        val duration = measureTimeMillis {
            price = quote(productId)
        }
        println("Quote took $duration from $origin")
        return Result(origin, price)
    }
}

class Result(@Suppress("UNUSED") val origin: Origin,
             @Suppress("UNUSED") val price: Double?)

enum class Origin {
    Cache, Srv
}