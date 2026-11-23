package ru.tisbi.servicecenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tisbi.servicecenter.data.Employee
import ru.tisbi.servicecenter.data.Role
import ru.tisbi.servicecenter.ui.DetailScaffold
import ru.tisbi.servicecenter.ui.MainViewModel
import ru.tisbi.servicecenter.ui.components.InfoRow
import ru.tisbi.servicecenter.ui.components.SectionCard
import ru.tisbi.servicecenter.ui.components.SelectCard
import ru.tisbi.servicecenter.ui.theme.BrandPrimary
import ru.tisbi.servicecenter.ui.theme.TextSecondary

@Composable
fun EmployeeEditScreen(
    vm: MainViewModel,
    employeeId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val session by vm.session.collectAsState()
    val employees by remember { vm.employees() }.collectAsState(initial = emptyList())
    val existing = employees.firstOrNull { it.id == employeeId }
    val scope = rememberCoroutineScope()

    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.ENGINEER) }
    var isActive by remember { mutableStateOf(true) }
    var loaded by remember { mutableStateOf(employeeId == 0L) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(existing) {
        val data = existing
        if (!loaded && data != null) {
            lastName = data.lastName
            firstName = data.firstName
            middleName = data.middleName.orEmpty()
            phone = data.phone
            login = data.login
            role = data.role
            isActive = data.isActive
            loaded = true
        }
    }

    DetailScaffold(
        title = if (employeeId == 0L) "Новый сотрудник" else "Сотрудник",
        subtitle = if (employeeId == 0L) "Создание учётной записи" else existing?.fullName,
        onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        val branchId = existing?.branchId ?: session?.branchId
                        if (branchId == null) return@Button
                        if (lastName.isBlank() || firstName.isBlank() || phone.isBlank() || login.isBlank()) {
                            error = "Заполните фамилию, имя, телефон и логин"
                            return@Button
                        }
                        if (employeeId == 0L && password.length < 5) {
                            error = "Пароль должен содержать не менее 5 символов"
                            return@Button
                        }
                        saving = true
                        scope.launch {
                            val result = vm.saveEmployee(
                                Employee(
                                    id = existing?.id ?: 0L,
                                    branchId = branchId,
                                    lastName = lastName.trim(),
                                    firstName = firstName.trim(),
                                    middleName = middleName.trim().ifBlank { null },
                                    role = role,
                                    login = login.trim().lowercase(),
                                    passwordHash = existing?.passwordHash.orEmpty(),
                                    phone = phone.trim(),
                                    isActive = isActive
                                ),
                                password.ifBlank { null }
                            )
                            saving = false
                            result.fold(
                                onSuccess = {
                                    vm.showMessage(if (employeeId == 0L) "Сотрудник добавлен" else "Данные сохранены")
                                    onSaved()
                                },
                                onFailure = { error = it.message }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                    enabled = !saving,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text(if (saving) "Сохранение…" else "Сохранить")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionCard(title = "Личные данные") {
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it; error = null },
                    label = { Text("Фамилия") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it; error = null },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = middleName,
                    onValueChange = { middleName = it },
                    label = { Text("Отчество") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; error = null },
                    label = { Text("Телефон") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            SectionCard(title = "Доступ в систему") {
                SelectCard(
                    label = "Роль",
                    selected = role,
                    options = Role.entries,
                    optionLabel = { it.title },
                    onSelect = { role = it }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it; error = null },
                    label = { Text("Логин") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text(if (employeeId == 0L) "Пароль" else "Новый пароль") },
                    supportingText = {
                        if (employeeId != 0L) Text("Оставьте пустым, чтобы не менять пароль")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Учётная запись активна", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Отключённый сотрудник не сможет войти в приложение",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }

            if (existing != null) {
                SectionCard(title = "Служебная информация") {
                    InfoRow("Филиал", session?.let { "Филиал №${it.branchId}" } ?: "—")
                    InfoRow("Логин", existing.login)
                    InfoRow("Роль", existing.role.title)
                }
            }

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
