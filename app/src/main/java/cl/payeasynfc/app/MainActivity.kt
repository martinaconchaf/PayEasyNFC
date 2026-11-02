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
                                    transactionId = "PAY-${System.currentTimeMillis()}"
                                }

                                pantalla = "resultadoPago"
                            },
                            onBack = {
                                pantalla = "metodoPago"
                            }
                        )

                        "comprobante" -> PaymentReceiptScreen(
                            transactionId = transactionId,
                            amount = montoVenta,
                            paymentMethod = metodoPago,
                            onFinish = {
                                montoVenta = 0L
                                metodoPago = ""
                                transactionId = ""
                                pantalla = "perfil"
                            }
                        )

                        "resultadoPago" -> PaymentResultScreen(
                            approved = pagoAprobado,
                            message = mensajeResultado,
                            onContinue = {
                                if (pagoAprobado) {
                                    pantalla = "comprobante"
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