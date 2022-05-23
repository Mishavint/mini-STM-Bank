package bank.restmodule

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

const val numberOfThreads = 4

@SpringBootApplication
class StmBankApplication

fun main(args: Array<String>) {
    runApplication<StmBankApplication>(*args)
}
