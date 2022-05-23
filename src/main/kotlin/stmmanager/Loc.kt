package stmmanager

class Loc<T>(
    val oldValue: T,
    val newValue: T,
    val owner: Transaction,
) {
    fun valueIn(tx: Transaction, onActive: (Transaction) -> Unit): Any? =
        if (owner === tx) newValue
        else when (owner.status) {
            TxStatus.ABORTED -> oldValue
            TxStatus.COMMITED -> newValue
            TxStatus.ACTIVE -> {
                onActive(owner)
                TxStatus.ACTIVE
            }
        }
}