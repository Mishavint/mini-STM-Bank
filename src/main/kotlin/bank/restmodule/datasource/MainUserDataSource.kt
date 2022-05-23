package bank.restmodule.datasource

import bank.restmodule.exceptions.ForbiddenAccess
import bank.restmodule.log
import bank.restmodule.model.*
import bank.restmodule.numberOfThreads
import org.springframework.stereotype.Repository
import stmmanager.TVar
import stmmanager.atomic
import java.math.BigInteger
import java.security.MessageDigest

@Repository("mainSource")
class MainUserDataSource : UserDataSource {

    private var maxNumberOfUser = 1

    // main list of users, that contains everything we need
    private val users = mutableListOf(
        (User(1, "defaultName", md5("1234"), 1_000_000))
    )

    private val listOfUsersToShow = mutableListOf(
        (UserToShow(1, "defaultName", 1_000_000))
    )

    // map of accounts numbers to its moneyAmounts
    private val mapOfChecks = mutableMapOf(1 to TVar(1_000_000L))

    /* list of Users for few threads.
    Every thread can change itы own UserToCreate
    Pair account number to UserToCreate */
    private val listOfUsersToCreate = MutableList(numberOfThreads + 1) {
        Pair(0, UserToCreate("qwerty", "qwerty"))
    }

    val lock = Any()

    // Only used for Admin
    override fun retrieveUsers(): Collection<UserToShow> {
        log("retrieving users")
        return listOfUsersToShow
    }

    /* Firstly checks that we this account number exists
       Then checks that password is correct */
    override fun retrieveUser(userNumber: Int, password: String): UserToShow {

        val result = listOfUsersToShow.firstOrNull { it.number == userNumber }
            ?: run {
                log("Retrieve user. User with $userNumber number was not found")
                throw NoSuchElementException("Could not find a user with $userNumber number")
            }

        users.firstOrNull { it.number == result.number }!!.let {
            if (it.password != md5(password)) {
                log("Retrieve user. Incorrect password for user with $userNumber number")
                throw ForbiddenAccess("Incorrect password")
            }
        }

        log("Successfully found user with $userNumber number")
        return result
    }

    /* Synchronisation lets us write different account numbers
    firstly writes userToCreate to map so different threads can work together without data condition
    Then adding new user to different lists and return itself */
    override fun createUser(userToCreate: UserToCreate): UserToCreate {

        log("Adding new user ${userToCreate.name}")

        synchronized(lock) {
            listOfUsersToCreate[getId()] = Pair(maxNumberOfUser + 1, userToCreate)
            maxNumberOfUser += 1
        }

        listOfUsersToShow.add(
            UserToShow(listOfUsersToCreate[getId()].first, listOfUsersToCreate[getId()].second.name, 0))

        mapOfChecks[listOfUsersToCreate[getId()].first] = TVar(0)

        users.add(User(listOfUsersToCreate[getId()].first, listOfUsersToCreate[getId()].second.name,
            md5(listOfUsersToCreate[getId()].second.password), 0))

        log("Successfully added new user: ${userToCreate.name} with ${listOfUsersToCreate[getId()].first} number")

        return listOfUsersToCreate[getId()].second
    }

    /* After checks, it changes values in atomic blocks */
    override fun changeMoneyAmount(userNumber: Int, userToChange: UserToChange): UserToShow {

        // Check that this user exists and password is correct
        retrieveUser(userNumber, userToChange.password)

        atomic {
            log("changing amount for $userNumber for ${userToChange.amount} amount. Current amount is " +
                    "${mapOfChecks[userNumber]!!.read()}")
        }

        atomic {
            mapOfChecks[userNumber]!!.let {
                val temp = it.read()

                if (temp + userToChange.amount < 0.0) {
                    abort()
                    throw IllegalArgumentException("No enough moneys")
                }

                it.write(temp + userToChange.amount)
            }
        }

        atomic {
            listOfUsersToShow.first { it.number == userNumber }.moneyAmount = mapOfChecks[userNumber]!!.read()
            users.first { it.number == userNumber }.moneyAmount = mapOfChecks[userNumber]!!.read()
        }

        atomic {
            log("Changed amount for $userNumber. Current amount is ${mapOfChecks[userNumber]!!.read()}")
        }

        return listOfUsersToShow.firstOrNull { it.number == userNumber }!!
    }

    /* After checks, it changes values for two users in atomic blocks */
    override fun transfer(userToTransfer: UserToTransfer): UserToShow {

        // Check that this user exists and password is correct
        retrieveUser(userToTransfer.number1, userToTransfer.password)

        // Check that 2nd account exists
        listOfUsersToShow.firstOrNull { it.number == userToTransfer.number2 }
            ?: run {
                log("Retrieve user. User with ${userToTransfer.number2} number was not found")
                throw NoSuchElementException("Could not find a user with ${userToTransfer.number2} number")
            }

        log("Transfer ${userToTransfer.amount} from ${userToTransfer.number1} to ${userToTransfer.number2}")

        if (userToTransfer.amount < 0) throw IllegalArgumentException("You have to transfer positive number")

        atomic {
            mapOfChecks[userToTransfer.number1]!!.let { firstCheck ->
                val temp = firstCheck.read()

                if (temp - userToTransfer.amount < 0.0) {
                    abort()
                    throw IllegalArgumentException("No enough money")
                }
                firstCheck.write(temp - userToTransfer.amount) // Change money amount to first user

                mapOfChecks[userToTransfer.number2]!!.let { secondCheck ->
                    val temp2 = secondCheck.read()
                    secondCheck.write(temp2 + userToTransfer.amount) // Change money amount for second user
                }
            }

        }

        /* ----------------- Changing lists ----------------------- */

        atomic {
            log("Transfer was successful")
            listOfUsersToShow.first { it.number == userToTransfer.number1 }.moneyAmount =
                mapOfChecks[userToTransfer.number1]!!.read()
            users.first { it.number == userToTransfer.number1 }.moneyAmount =
                mapOfChecks[userToTransfer.number1]!!.read()
        }

        atomic {
            listOfUsersToShow.first { it.number == userToTransfer.number2 }.moneyAmount =
                mapOfChecks[userToTransfer.number2]!!.read()
            users.first { it.number == userToTransfer.number2 }.moneyAmount =
                mapOfChecks[userToTransfer.number2]!!.read()
        }

        return listOfUsersToShow.firstOrNull { it.number == userToTransfer.number1 }!!
    }

    // Function that create hash from string, so passwords can be protected
    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    // Function, that returns thread id, very useful for lists for few threads
    private fun getId(): Int = (Thread.currentThread().id % numberOfThreads + 1).toInt()
}
