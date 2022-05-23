package bank.restmodule.controller

import bank.restmodule.exceptions.ForbiddenAccess
import bank.restmodule.model.UserToChange
import bank.restmodule.model.UserToCreate
import bank.restmodule.model.UserToTransfer
import bank.restmodule.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ApiController(private val service: UserService) {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<String> =
        ResponseEntity(e.message, HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<String> =
        ResponseEntity(e.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(ForbiddenAccess::class)
    fun handleForbidden(e: ForbiddenAccess): ResponseEntity<String> =
        ResponseEntity(e.message, HttpStatus.FORBIDDEN)

    @GetMapping("/{accountNumber}")
    fun getUser(@PathVariable accountNumber: Int, @RequestBody password: String) =
        service.retrieveUser(accountNumber, password)

    @PostMapping
    fun addUser(@RequestBody userToCreate: UserToCreate): UserToCreate =
        service.addUser(userToCreate)

    @PatchMapping("/{userNumber}")
    fun refill(@PathVariable userNumber: Int, @RequestBody userToChange: UserToChange) =
        service.changeMoneyAmount(userNumber, userToChange)

    @PatchMapping
    fun transfer(@RequestBody userToTransfer: UserToTransfer) =
        service.transfer(userToTransfer)
}
