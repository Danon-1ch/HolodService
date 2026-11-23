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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tisbi.servicecenter.data.ClientRow
import ru.tisbi.servicecenter.data.Equipment
import ru.tisbi.servicecenter.data.Priority
import ru.tisbi.servicecenter.data.RequestType
import ru.tisbi.servicecenter.ui.DetailScaffold
import ru.tisbi.servicecenter.ui.MainViewModel
import ru.tisbi.servicecenter.ui.components.SectionCard
import ru.tisbi.servicecenter.ui.components.SelectCard
import ru.tisbi.servicecenter.ui.components.asDate
import ru.tisbi.servicecenter.ui.theme.BrandPrimary
import ru.tisbi.servicecenter.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestScreen(vm: MainViewModel, onBack: () -> Unit, onCreated: (Long) -> Unit) {
    val clients by remember { vm.clients("") }.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var client by remember { mutableStateOf<ClientRow?>(null) }
    var equipment by remember { mutableStateOf<Equipment?>(null) }
    var equipmentList by remember { mutableStateOf<List<Equipment>>(emptyList()) }
    var type by remember { mutableStateOf(RequestType.REPAIR) }
    var priority by remember { mutableStateOf(Priority.NORMAL) }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var scheduledAt by remember {
        mutableStateOf(
            LocalDate.now().plusDays(1).atTime(10, 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    DetailScaffold(
        title = "Новая заявка",
        subtitle = "Приём заявки диспетчером",
        onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        val selectedClient = client
                        val selectedEquipment = equipment
                        if (selectedClient == null) {
                            error = "Выберите клиента"
                        } else if (selectedEquipment == null) {
                            error = "Выберите оборудование"
                        } else if (description.isBlank()) {
                            error = "Опишите неисправность"
                        } else {
                            saving = true
                            scope.launch {
                                val id = vm.createRequest(
                                    clientId = selectedClient.id,
                                    equipmentId = selectedEquipment.id,
                                    type = type,
                                    priority = priority,
                                    description = description.trim(),
                                    address = address.trim(),
                                    scheduledAt = scheduledAt
                                )
                                saving = false
                                if (id != null) onCreated(id) else error = "Не удалось создать заявку"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                    enabled = !saving,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text(if (saving) "Сохранение…" else "Создать заявку")
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
            SectionCard(title = "Клиент и оборудование") {
                SelectCard(
                    label = "Клиент",
                    selected = client,
                    options = clients,
                    optionLabel = { "${it.lastName} ${it.firstName} ${it.middleName.orEmpty()} · ${it.phone}" },
                    onSelect = { selected ->
                        client = selected
                        address = selected.address
                        equipment = null
                        equipmentList = emptyList()
                        scope.launch {
                            equipmentList = vm.equipmentForClient(selected.id)
                            if (equipmentList.size == 1) equipment = equipmentList.first()
                        }
                    },
                    placeholder = "Выберите клиента"
                )
                Spacer(Modifier.height(10.dp))
                SelectCard(
                    label = "Оборудование",
                    selected = equipment,
                    options = equipmentList,
                    optionLabel = { "${it.title} · ${it.serialNumber}" },
                    onSelect = { equipment = it },
                    placeholder = if (client == null) "Сначала выберите клиента" else "Выберите оборудование"
                )
                if (client != null && equipmentList.isEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "У клиента нет зарегистрированной техники. Добавьте её в карточке клиента.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            SectionCard(title = "Характер работ") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RequestType.entries.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { option ->
                                FilterChip(
                                    selected = type == option,
                                    onClick = { type = option },
                                    label = { Text(option.title, style = MaterialTheme.typography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Приоритет", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { option ->
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(option.title, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            SectionCard(title = "Описание неисправности") {
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Что произошло, какие признаки неисправности") },
                    minLines = 3,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
            }

            SectionCard(title = "Выезд") {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Адрес выезда") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                androidx.compose.material3.OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Плановый выезд: ${scheduledAt.asDate()}")
                }
            }

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = scheduledAt)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { scheduledAt = it }
                    showDatePicker = false
                }) { Text("Выбрать") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
        ) {
            DatePicker(state = state)
        }
    }
}
