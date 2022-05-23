package bank.restmodule.datasource

import bank.restmodule.model.UserToChange
import bank.restmodule.model.UserToCreate
import bank.restmodule.model.UserToShow
import bank.restmodule.model.UserToTransfer

interface UserDataSource {

    fun retrieveUsers(): Collection<UserToShow>

    fun retrieveUser(userNumber: Int, password: String): UserToShow

    fun createUser(userToCreate: UserToCreate): UserToCreate

    fun changeMoneyAmount(userNumber : Int, userToChange: UserToChange): UserToShow

    fun transfer(userToTransfer: UserToTransfer): UserToShow
}
