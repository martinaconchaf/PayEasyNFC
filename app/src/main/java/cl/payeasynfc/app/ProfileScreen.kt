package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.util.concurrent.Executors

@Composable
fun ProfileScreen(
    sellerEmail: String,
    onProfileUpdated: (String) -> Unit,
    onOpenBank: () -> Unit,
    onSwitchAccount: () -> Unit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Mi perfil",
            style = MaterialTheme.typography.headlineSmall
        )

        if (loading) {
            CircularProgressIndicator()
        } else {
            Text("Datos personales", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    message = null
                    val id = sellerId

                    when {
                        name.trim().length < 2 ->
                            message = "El nombre debe tener al menos 2 caracteres"

                        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                            message = "Ingresa un correo electrónico válido"

                        id == null ->
                            message = "No se encontró el vendedor"

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
                                    message = if (updated) {
                                        onProfileUpdated(email.trim().lowercase())
                                        "Perfil actualizado correctamente"
                                    } else {
                                        "No se pudo actualizar. Puede que el correo ya esté registrado."
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = !savingProfile && !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (savingProfile) "Guardando..." else "Guardar perfil")
            }

            HorizontalDivider()

            Text("Cambiar contraseña", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Contraseña actual") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nueva contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar nueva contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    message = null
                    val id = sellerId

                    when {
                        currentPassword.isBlank() ->
                            message = "Ingresa tu contraseña actual"

                        newPassword.length < 8 ->
                            message = "La nueva contraseña debe tener al menos 8 caracteres"

                        newPassword != confirmPassword ->
                            message = "Las contraseñas nuevas no coinciden"

                        currentPassword == newPassword ->
                            message = "La nueva contraseña debe ser diferente"

                        id == null ->
                            message = "No se encontró el vendedor"

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
                                    message = if (changed) {
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        "Contraseña actualizada correctamente"
                                    } else {
                                        "La contraseña actual es incorrecta"
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = !changingPassword && !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (changingPassword) "Actualizando..."
                    else "Cambiar contraseña"
                )
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = onOpenBank,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Administrar cuenta bancaria")
            }

            OutlinedButton(
                onClick = onSwitchAccount,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cambiar de cuenta")
            }

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }

        message?.let {
            Text(
                text = it,
                color = if (
                    it.contains("correctamente")
                ) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    }
}