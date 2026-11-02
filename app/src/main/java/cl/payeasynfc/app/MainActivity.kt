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
                                pantalla = "perfil"
                            },
                            onBack = {
                                pantalla = "cobro"
                            }
                        )
                    }
                }
            }
        }
    }
}