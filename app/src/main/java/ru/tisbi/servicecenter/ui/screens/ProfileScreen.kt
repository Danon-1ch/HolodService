package ru.tisbi.servicecenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tisbi.servicecenter.data.Role
import ru.tisbi.servicecenter.ui.MainScaffold
import ru.tisbi.servicecenter.ui.MainViewModel
import ru.tisbi.servicecenter.ui.components.InfoRow
import ru.tisbi.servicecenter.ui.components.SectionCard
import ru.tisbi.servicecenter.ui.components.money
import ru.tisbi.servicecenter.ui.theme.BrandAccent
import ru.tisbi.servicecenter.ui.theme.BrandPrimary
import ru.tisbi.servicecenter.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    vm: MainViewModel,
    role: Role,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val employee by vm.session.collectAsState()
    val branch by remember { vm.branch() }.collectAsState(initial = null)
    val engineerLoad by remember { vm.engineerLoad() }.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordMessage by remember { mutableStateOf<String?>(null) }

    val me = employee ?: return
    val myLoad = engineerLoad.firstOrNull { it.engineerId == me.id }

    MainScaffold(
        title = "Профиль",
        subtitle = branch?.name,
        role = role,
        currentRoute = "profile",
        onNavigate = onNavigate
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.size(78.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${me.lastName.first()}${me.firstName.first()}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(me.fullName, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(6.dp))
                        RoleChip(me.role)
                        Spacer(Modifier.height(6.dp))
                        Text(branch?.name ?: "Филиал", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }

            if (role == Role.ENGINEER && myLoad != null) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(
                            Modifier.weight(1f),
                            title = "Выполнено",
                            value = "${myLoad.doneCount}",
                            accent = BrandPrimary
                        )
                        StatCard(
                            Modifier.weight(1f),
                            title = "Сумма работ",
                            value = myLoad.revenue.money(),
                            accent = BrandAccent
                        )
                    }
                }
            }

            item {
                SectionCard(title = "Учётные данные") {
                    InfoRow("Логин", me.login)
                    InfoRow("Телефон", me.phone)
                    InfoRow("Должность", me.role.title)
                    InfoRow("Филиал", branch?.name ?: "—")
                    if (branch != null) {
                        InfoRow("Адрес филиала", branch!!.address)
                    }
                }
            }

            item {
                SectionCard(title = "Безопасность") {
                    OutlinedButton(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Сменить пароль")
                    }
                    if (passwordMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            passwordMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (passwordMessage!!.startsWith("Пароль")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item {
                SectionCard(title = "О приложении") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Storefront, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ХолодСервис · версия 1.0", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Мобильное рабочее место сотрудника сети сервисных центров по ремонту холодильного оборудования. Данные хранятся в локальной базе филиала.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        vm.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти из учётной записи")
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { old, new, repeat ->
                if (new != repeat) {
                    passwordMessage = "Новые пароли не совпадают"
                    showPasswordDialog = false
                } else {
                    scope.launch {
                        val result = vm.changePassword(old, new)
                        passwordMessage = result.fold(
                            onSuccess = { "Пароль изменён" },
                            onFailure = { it.message }
                        )
                        showPasswordDialog = false
                    }
                }
            }
        )
    }
}

@Composable
private fun StatCard(modifier: Modifier, title: String, value: String, accent: androidx.compose.ui.graphics.Color) {
    SectionCard(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = accent, fontWeight = FontWeight.SemiBold)
        Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Смена пароля") },
        text = {
            Column {
                OutlinedTextField(
                    value = old,
                    onValueChange = { old = it },
                    label = { Text("Текущий пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = new,
                    onValueChange = { new = it },
                    label = { Text("Новый пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = repeat,
                    onValueChange = { repeat = it },
                    label = { Text("Повторите новый пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (new.length < 5) {
                    error = "Пароль должен содержать не менее 5 символов"
                } else if (new != repeat) {
                    error = "Пароли не совпадают"
                } else {
                    onConfirm(old, new, repeat)
                }
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
