package bank.restmodule.model

// Basic type. Password must be encrypted
data class User(
    val number: Int,
    val name: String,
    val password: String,
    var moneyAmount: Long
)

// When someone wants to check the account, this will be shown
data class UserToShow(
    val number: Int,
    val name: String,
    var moneyAmount: Long
)

// When someone wants to create new user, they will send request with json with that content
data class UserToCreate(
    val name: String,
    val password: String
)

// When someone wants to change balance, they will send request with json with that content
data class UserToChange(
    val amount: Long,
    val password: String
)

// When someone wants to do transfer, they will send request with json with that content
data class UserToTransfer(
    val number1: Int,
    val number2: Int,
    val amount: Long,
    val password: String
)
