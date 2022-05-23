package bank.restmodule

import java.time.Clock
import java.time.LocalTime
import java.time.ZoneOffset

val utc: Clock = Clock.systemUTC()
fun time(): LocalTime = LocalTime.ofInstant(utc.instant(), ZoneOffset.UTC)
fun log(message: String) = println("[${time()}] [${Thread.currentThread().name}] $message")
