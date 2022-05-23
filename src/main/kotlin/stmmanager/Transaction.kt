package stmmanager

import java.util.concurrent.atomic.AtomicReference

class Transaction {
    private val _status = AtomicReference(TxStatus.ACTIVE)
    val status: TxStatus
        get() = _status.get()

    fun commit() {
        _status.compareAndSet(TxStatus.ACTIVE, TxStatus.COMMITED)
    }

    fun abort() {
        _status.compareAndSet(TxStatus.ACTIVE, TxStatus.ABORTED)
    }

    fun <T> TVar<T>.read(): T = readIn(this@Transaction)
    fun <T> TVar<T>.write(x: T) = writeIn(this@Transaction, x)
}

fun <T> atomic(block: Transaction.() -> T): T {
    while (true) {
        val transaction = Transaction()
        try {
            val result = block(transaction)
            transaction.commit()
            return result
        } catch (e: AbortException) {
            transaction.abort()
        }
    }
}