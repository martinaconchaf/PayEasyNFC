package cl.payeasynfc.app

data class Seller(
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val passwordSalt: String
)

data class SellerAccountOption(
    val id: Long,
    val name: String,
    val email: String
)