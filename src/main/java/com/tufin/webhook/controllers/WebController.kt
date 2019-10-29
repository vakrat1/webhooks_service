package com.tufin.webhook.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

import java.security.Principal

@Controller
class WebController {

//    @Autowired
//    private val customerDAO: CustomerDAO? = null

    @GetMapping(path = ["/"])
    fun index(): String {
        return "external"
    }

    @GetMapping(path = ["/customers"])
    fun customers(principal: Principal, model: Model): String {
        val customers = addCustomers()
        model.addAttribute("customers", customers)
        model.addAttribute("username", principal.name)
        return "customers"
    }

    // add customers for demonstration
    fun addCustomers() : List<Customer>{
        val customers = mutableListOf<Customer>()

        val customer1 = Customer()
        customer1.address = "1111 foo blvd"
        customer1.name = "Foo Industries"
        customer1.serviceRendered = "Important services"
        customers.add(customer1)

        val customer2 = Customer()
        customer2.address = "2222 bar street"
        customer2.name = "Bar LLP"
        customer2.serviceRendered = "Important services"
        customers.add(customer2)

        val customer3 = Customer()
        customer3.address = "33 main street"
        customer3.name = "Big LLC"
        customer3.serviceRendered = "Important services"
        customers.add(customer3)

        return customers
    }
}
