package bank.restmodule.service

import bank.restmodule.datasource.UserDataSource
import bank.restmodule.model.UserToChange
import bank.restmodule.model.UserToCreate
import bank.restmodule.model.UserToShow
import bank.restmodule.model.UserToTransfer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class UserService(
    @Qualifier("mainSource") private val dataSource: UserDataSource,
) {

    fun retrieveUsers(): Collection<UserToShow> = dataSource.retrieveUsers()

    fun retrieveUser(number: Int, password: String): UserToShow = dataSource.retrieveUser(number, password)

    fun addUser(userToCreate: UserToCreate): UserToCreate =
        dataSource.createUser(userToCreate)

    fun changeMoneyAmount(userNumber : Int, userToChange: UserToChange): UserToShow =
        dataSource.changeMoneyAmount(userNumber, userToChange)

    fun transfer(userToTransfer: UserToTransfer): UserToShow =
        dataSource.transfer(userToTransfer)
}
