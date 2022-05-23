package bank.restmodule.controller

import bank.restmodule.model.UserToShow
import bank.restmodule.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class AdminController(private val service: UserService) {
    @GetMapping
    fun getUsers(): Collection<UserToShow> = service.retrieveUsers()
}
