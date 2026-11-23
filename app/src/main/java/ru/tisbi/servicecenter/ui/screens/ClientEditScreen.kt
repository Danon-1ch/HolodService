package ru.tisbi.servicecenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tisbi.servicecenter.data.Client
import ru.tisbi.servicecenter.ui.DetailScaffold
import ru.tisbi.servicecenter.ui.MainViewModel
import ru.tisbi.servicecenter.ui.components.SectionCard
import ru.tisbi.servicecenter.ui.theme.BrandPrimary
import ru.tisbi.servicecenter.ui.theme.TextSecondary

@Composable
fun ClientEditScreen(
    vm: MainViewModel,
    clientId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val session by vm.session.collectAsState()
    val existing by remember(clientId) { vm.client(clientId) }.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(clientId == 0L) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(existing) {
        val data = existing
        if (!loaded && data != null) {
            lastName = data.lastName
            firstName = data.firstName
            middleName = data.middleName.orEmpty()
            phone = data.phone
            email = data.email.orEmpty()
            address = data.address
            loaded = true
        }
    }

    DetailScaffold(
        title = if (clientId == 0L) "Новый клиент" else "Редактирование клиента",
        subtitle = "Контактные данные",
        onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        val branchId = existing?.branchId ?: session?.branchId
                        if (branchId == null) return@Button
                        if (lastName.isBlank() || firstName.isBlank() || phone.isBlank() || address.isBlank()) {
                            error = "Заполните фамилию, имя, телефон и адрес"
                            return@Button
                        }
                        saving = true
                        scope.launch {
                            vm.saveClient(
                                Client(
                                    id = existing?.id ?: 0L,
                                    branchId = branchId,
                                    lastName = lastName.trim(),
                                    firstName = firstName.trim(),
                                    middleName = middleName.trim().ifBlank { null },
                                    phone = phone.trim(),
                                    email = email.trim().ifBlank { null },
                                    address = address.trim(),
                                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                                )
                            )
                            saving = false
                            vm.showMessage(if (clientId == 0L) "Клиент добавлен" else "Данные клиента обновлены")
                            onSaved()
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
            SectionCard(title = "ФИО") {
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
            }
            SectionCard(title = "Контакты") {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; error = null },
                    label = { Text("Телефон") },
                    placeholder = { Text("+7 (___) ___-__-__") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; error = null },
                    label = { Text("Адрес") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "Клиент привязывается к филиалу, в котором зарегистрирован. Изменения сразу видны диспетчеру и инженерам.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
