@file:Suppress("UNCHECKED_CAST")

package stmmanager

import java.util.concurrent.atomic.AtomicReference

private val rootTx = Transaction().apply { commit() }

class TVar<T>(initial: T) {
    private val loc = AtomicReference(Loc(initial, initial, rootTx))

    fun openIn(tx: Transaction, update: (T) -> T): T {
        while (true) {
            val curLoc = loc.get()
            val curValue = curLoc.valueIn(tx) { owner -> contentionPolicy(tx, owner) }

            if (curValue === TxStatus.ACTIVE) continue
            val updValue = update(curValue as T)

            if (loc.compareAndSet(curLoc, Loc(curValue, updValue, tx))) {
                if (tx.status == TxStatus.ABORTED) throw AbortException()

                return updValue
            }
        }
    }

    fun readIn(tx: Transaction) = openIn(tx) { it }
    fun writeIn(tx: Transaction, x: T) = openIn(tx) { x }
}

fun contentionPolicy(tx: Transaction, owner: Transaction) {
    owner.abort()
}