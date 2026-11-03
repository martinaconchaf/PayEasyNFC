
package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.concurrent.Executors

private val ProfileBlue = Color(0xFF173B65)
private val ProfileBackground = Color(0xFFF3F6FA)
private val ProfileGreen = Color(0xFF24834A)

@Composable
fun ProfileScreen(
    sellerEmail: String,
    onProfileUpdated: (String) -> Unit,
    onOpenBank: () -> Unit,
    onSwitchAccount: () -> Unit,
    onOpenSale: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStatistics: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { SellerDatabase(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val handler = remember { Handler(Looper.getMainLooper()) }

    var sellerId by remember { mutableStateOf<Long?>(null) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(sellerEmail) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var savingProfile by remember { mutableStateOf(false) }
    var changingPassword by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            database.close()
        }
    }

    LaunchedEffect(sellerEmail) {
        loading = true

        executor.execute {
            val seller = database.getSellerByEmail(sellerEmail)

            handler.post {
                if (seller != null) {
                    sellerId = seller.id
                    name = seller.name
                    email = seller.email
                } else {
                    message = "No se pudieron cargar los datos del vendedor"
                }

                loading = false
            }
        }
    }

    ProfileContent(
        name = name,
        email = email,
        currentPassword = currentPassword,
        newPassword = newPassword,
        confirmPassword = confirmPassword,
        message = message,
        loading = loading,
        savingProfile = savingProfile,
        changingPassword = changingPassword,

        onNameChange = {
            name = it
            message = null
        },
        onEmailChange = {
            email = it
            message = null
        },
        onCurrentPasswordChange = {
            currentPassword = it
            message = null
        },
        onNewPasswordChange = {
            newPassword = it
            message = null
        },
        onConfirmPasswordChange = {
            confirmPassword = it
            message = null
        },

        onSaveProfile = {
            message = null
            val id = sellerId

            when {
                name.trim().length < 2 -> {
                    message = "El nombre debe tener al menos 2 caracteres"
                }

                !Patterns.EMAIL_ADDRESS
                    .matcher(email.trim())
                    .matches() -> {
                    message = "Ingresa un correo electrónico válido"
                }

                id == null -> {
                    message = "No se encontró el vendedor"
                }

                else -> {
                    savingProfile = true

                    executor.execute {
                        val updated = database.updateSellerProfile(
                            sellerId = id,
                            name = name.trim(),
                            email = email.trim()
                        )

                        handler.post {
                            savingProfile = false

                            if (updated) {
                                onProfileUpdated(
                                    email.trim().lowercase()
                                )
                                message = "Perfil actualizado correctamente"
                            } else {
                                message =
                                    "No se pudo actualizar. Puede que el correo ya esté registrado."
                            }
                        }
                    }
                }
            }
        },

        onChangePassword = {
            message = null
            val id = sellerId

            when {
                currentPassword.isBlank() -> {
                    message = "Ingresa tu contraseña actual"
                }

                newPassword.length < 8 -> {
                    message =
                        "La nueva contraseña debe tener al menos 8 caracteres"
                }

                newPassword != confirmPassword -> {
                    message = "Las contraseñas nuevas no coinciden"
                }

                currentPassword == newPassword -> {
                    message = "La nueva contraseña debe ser diferente"
                }

                id == null -> {
                    message = "No se encontró el vendedor"
                }

                else -> {
                    changingPassword = true

                    executor.execute {
                        val changed = database.changeSellerPassword(
                            sellerId = id,
                            currentPassword = currentPassword,
                            newPassword = newPassword
                        )

                        handler.post {
                            changingPassword = false

                            if (changed) {
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                                message =
                                    "Contraseña actualizada correctamente"
                            } else {
                                message =
                                    "La contraseña actual es incorrecta"
                            }
                        }
                    }
                }
            }
        },

        onOpenBank = onOpenBank,
        onOpenSale = onOpenSale,
        onOpenHistory = onOpenHistory,
        onOpenStatistics = onOpenStatistics,
        onSwitchAccount = onSwitchAccount,
        onLogout = onLogout
    )
}

@Composable
private fun ProfileContent(
    name: String,
    email: String,
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    message: String?,
    loading: Boolean,
    savingProfile: Boolean,
    changingPassword: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onOpenBank: () -> Unit,
    onOpenSale: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStatistics: () -> Unit,
    onSwitchAccount: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    ProfileBlue,
                    RoundedCornerShape(
                        bottomStart = 28.dp,
                        bottomEnd = 28.dp
                    )
                )
                .padding(24.dp)
        ) {
            Text(
                text = "PayEasy NFC",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Panel del vendedor",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = if (name.isNotBlank()) name else "Mi perfil",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = email,
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ProfileBlue)
                }
            } else {
                // Datos personales
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Datos personales",
                            style = MaterialTheme.typography.titleLarge,
                            color = ProfileBlue,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = onNameChange,
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = onEmailChange,
                            label = { Text("Correo electrónico") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onSaveProfile,
                            enabled = !savingProfile,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfileBlue
                            )
                        ) {
                            Text(
                                if (savingProfile) "Guardando..."
                                else "Guardar perfil"
                            )
                        }
                    }
                }

                // Seguridad
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Seguridad",
                            style = MaterialTheme.typography.titleLarge,
                            color = ProfileBlue,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Actualiza tu contraseña para proteger tu cuenta.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = onCurrentPasswordChange,
                            label = { Text("Contraseña actual") },
                            visualTransformation =
                                PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = onNewPasswordChange,
                            label = { Text("Nueva contraseña") },
                            visualTransformation =
                                PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            label = {
                                Text("Confirmar nueva contraseña")
                            },
                            visualTransformation =
                                PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = onChangePassword,
                            enabled = !changingPassword,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                if (changingPassword) "Actualizando..."
                                else "Cambiar contraseña"
                            )
                        }
                    }
                }

                // Acciones principales
                Text(
                    text = "Operaciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ProfileBlue
                )

                Button(
                    onClick = onOpenSale,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileBlue
                    )
                ) {
                    Text(
                        text = "＋  Nuevo cobro",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                ProfileMenuButton(
                    title = "Historial de ventas",
                    description = "Consulta tus operaciones anteriores",
                    onClick = onOpenHistory
                )

                ProfileMenuButton(
                    title = "Estadísticas de ventas",
                    description = "Revisa tus resultados y totales",
                    onClick = onOpenStatistics
                )

                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ProfileBlue
                )

                ProfileMenuButton(
                    title = "Cuenta bancaria",
                    description = "Administra los datos de tu cuenta",
                    onClick = onOpenBank
                )

                ProfileMenuButton(
                    title = "Cambiar de cuenta",
                    description = "Accede con otro vendedor",
                    onClick = onSwitchAccount
                )

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (message != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (
                            message.contains("correctamente")
                        ) {
                            Color(0xFFE7F5EC)
                        } else {
                            Color(0xFFFCEAEA)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = if (
                            message.contains("correctamente")
                        ) {
                            ProfileGreen
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                color = ProfileBlue,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileContent(
            name = "Juan Pérez",
            email = "juan@ejemplo.com",
            currentPassword = "",
            newPassword = "",
            confirmPassword = "",
            message = null,
            loading = false,
            savingProfile = false,
            changingPassword = false,
            onNameChange = {},
            onEmailChange = {},
            onCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSaveProfile = {},
            onChangePassword = {},
            onOpenBank = {},
            onOpenSale = {},
            onOpenHistory = {},
            onOpenStatistics = {},
            onSwitchAccount = {},
            onLogout = {}
        )
    }
}
