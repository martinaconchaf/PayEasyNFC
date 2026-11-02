package cl.payeasynfc.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var pantalla by remember { mutableStateOf("registro") }
            var vendedorEmail by remember { mutableStateOf("") }
            var montoVenta by remember { mutableStateOf(0L) }
            var metodoPago by remember { mutableStateOf("") }
            var transactionId by remember { mutableStateOf("") }
            var pagoAprobado by remember { mutableStateOf(false) }
            var mensajeResultado by remember { mutableStateOf("") }
            val sellerDatabase = remember {
                SellerDatabase(this@MainActivity)
            }
            var transactionDate by remember {
                mutableStateOf(0L)
            }
            var transactionSaved by remember {
                mutableStateOf(false)
            }
            val context = androidx.compose.ui.platform.LocalContext.current

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (pantalla) {
                        "registro" -> RegisterScreen(
                            onGoToLogin = {
                                pantalla = "login"
                            }
                        )

                        "login" -> LoginScreen(
                            onBackToRegister = {
                                pantalla = "registro"
                            },
                            onLoginSuccess = { email ->
                                vendedorEmail = email
                                pantalla = "perfil"
                            }
                        )

                        "perfil" -> ProfileScreen(
                            sellerEmail = vendedorEmail,
                            onProfileUpdated = { nuevoEmail ->
                                vendedorEmail = nuevoEmail
                            },
                            onOpenBank = {
                                pantalla = "banco"
                            },
                            onSwitchAccount = {
                                pantalla = "cambiarCuenta"
                            },
                            onOpenSale = {
                                pantalla = "cobro"
                            },
                            onLogout = {
                                vendedorEmail = ""
                                pantalla = "login"
                            }


                        )

                        "banco" -> BankAccountScreen(
                            sellerEmail = vendedorEmail,
                            onBackToLogin = {
                                pantalla = "perfil"
                            }
                        )

                        "cambiarCuenta" -> AccountSwitcherScreen(
                            currentEmail = vendedorEmail,
                            onAccountChanged = { nuevoEmail ->
                                vendedorEmail = nuevoEmail
                                pantalla = "perfil"
                            },
                            onBack = {
                                pantalla = "perfil"
                            }
                        )

                        "cobro" -> SaleAmountScreen(
                            onAmountConfirmed = { monto ->
                                montoVenta = monto
                                pantalla = "metodoPago"
                            },
                            onBack = {
                                pantalla = "perfil"
                            }
                        )

                        "metodoPago" -> PaymentMethodScreen(
                            amount = montoVenta,
                            onMethodSelected = { metodo ->
                                metodoPago = metodo
                                pantalla = "procesando"
                            },
                            onBack = {
                                pantalla = "cobro"
                            }
                        )


                        "procesando" -> PaymentProcessingScreen(
                            amount = montoVenta,
                            paymentMethod = metodoPago,


                            onResult = { aprobado, mensaje ->
                                pagoAprobado = aprobado
                                mensajeResultado = mensaje

                                if (aprobado) {
                                    transactionId =
                                        "PAY-${System.currentTimeMillis()}"

                                    transactionDate = System.currentTimeMillis()

                                    transactionSaved = sellerDatabase.saveTransaction(
                                        sellerEmail = vendedorEmail,
                                        transactionCode = transactionId,
                                        amount = montoVenta,
                                        paymentMethod = metodoPago,
                                        status = "APPROVED",
                                        createdAt = transactionDate
                                    )
                                } else {
                                    transactionSaved = false
                                }

                                pantalla = "resultadoPago"
                            },

                            onBack = {
                                pantalla = "metodoPago"
                            },

                            onCancel = {
                                val codigoCancelacion =
                                    "CAN-${System.currentTimeMillis()}"

                                sellerDatabase.saveTransaction(
                                    sellerEmail = vendedorEmail,
                                    transactionCode = codigoCancelacion,
                                    amount = montoVenta,
                                    paymentMethod = metodoPago,
                                    status = "CANCELLED",
                                    createdAt = System.currentTimeMillis()
                                )

                                montoVenta = 0L
                                metodoPago = ""
                                pantalla = "perfil"
                            }
                        )

                        "comprobante" -> PaymentReceiptScreen(
                            transactionId = transactionId,
                            amount = montoVenta,
                            paymentMethod = metodoPago,
                            createdAt = transactionDate,

                            onSaveReceipt = {
                                try {
                                    val pdf = ReceiptPdf.create(
                                        context = context,
                                        transactionId = transactionId,
                                        amount = montoVenta,
                                        paymentMethod = metodoPago,
                                        createdAt = transactionDate
                                    )

                                    val saved = ReceiptPdf.saveToDownloads(context, pdf)

                                    android.widget.Toast.makeText(
                                        context,
                                        if (saved) {
                                            "Comprobante guardado en Descargas"
                                        } else {
                                            "No se pudo guardar el comprobante"
                                        },
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()

                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Error al generar el PDF",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            },

                            onShareReceipt = {
                                try {
                                    val pdf = ReceiptPdf.create(
                                        context = context,
                                        transactionId = transactionId,
                                        amount = montoVenta,
                                        paymentMethod = metodoPago,
                                        createdAt = transactionDate
                                    )

                                    ReceiptPdf.share(context, pdf)

                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "No se pudo compartir el comprobante",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            },

                            onShareSellerCopy = {
                                try {
                                    val pdf = ReceiptPdf.create(
                                        context = context,
                                        transactionId = transactionId,
                                        amount = montoVenta,
                                        paymentMethod = metodoPago,
                                        createdAt = transactionDate
                                    )

                                    ReceiptPdf.shareToSeller(
                                        context = context,
                                        file = pdf,
                                        sellerEmail = vendedorEmail
                                    )

                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "No se pudo preparar la copia para el vendedor",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            },

                            onFinish = {
                                montoVenta = 0L
                                metodoPago = ""
                                transactionId = ""
                                transactionDate = 0L
                                transactionSaved = false
                                pantalla = "perfil"
                            }
                        )

                        "resultadoPago" -> PaymentResultScreen(
                            approved = pagoAprobado,
                            message = mensajeResultado,
                            onContinue = {
                                if (pagoAprobado && transactionSaved) {
                                    pantalla = "comprobante"
                                } else if (pagoAprobado) {
                                    mensajeResultado =
                                        "El pago fue simulado como aprobado, pero no se pudo guardar el registro."
                                    pantalla = "perfil"
                                } else {
                                    pantalla = "metodoPago"
                                }
                            },
                            onBack = {
                                pantalla = "perfil"
                            }
                        )

                    }
                }
            }
        }
    }
}