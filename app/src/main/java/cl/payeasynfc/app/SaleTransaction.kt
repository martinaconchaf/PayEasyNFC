package cl.payeasynfc.app

data class SaleTransaction(
    val id: Long,
    val transactionCode: String,
    val amount: Long,
    val paymentMethod: String,
    val status: String,
    val createdAt: Long
)