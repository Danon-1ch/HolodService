package ru.tisbi.servicecenter.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tisbi.servicecenter.data.Employee
import ru.tisbi.servicecenter.data.Part
import ru.tisbi.servicecenter.data.Priority
import ru.tisbi.servicecenter.data.RequestStatus
import ru.tisbi.servicecenter.data.Role
import ru.tisbi.servicecenter.data.Service
import ru.tisbi.servicecenter.ui.DetailScaffold
import ru.tisbi.servicecenter.ui.MainViewModel
import ru.tisbi.servicecenter.ui.components.ConfirmDialog
import ru.tisbi.servicecenter.ui.components.InfoRow
import ru.tisbi.servicecenter.ui.components.PriorityChip
import ru.tisbi.servicecenter.ui.components.QuantityStepper
import ru.tisbi.servicecenter.ui.components.SectionCard
import ru.tisbi.servicecenter.ui.components.SelectCard
import ru.tisbi.servicecenter.ui.components.StatusChip
import ru.tisbi.servicecenter.ui.components.asDate
import ru.tisbi.servicecenter.ui.components.asDateTime
import ru.tisbi.servicecenter.ui.components.money
import ru.tisbi.servicecenter.ui.theme.BrandPrimary
import ru.tisbi.servicecenter.ui.theme.TextSecondary
import java.util.concurrent.TimeUnit

@Composable
fun RequestDetailScreen(
    vm: MainViewModel,
    requestId: Long,
    role: Role,
    onBack: () -> Unit,
    onOpenClient: (Long) -> Unit
) {
    val row by remember(requestId) { vm.request(requestId) }.collectAsState(initial = null)
    val works by remember(requestId) { vm.works(requestId) }.collectAsState(initial = emptyList())
    val usedParts by remember(requestId) { vm.partUsages(requestId) }.collectAsState(initial = emptyList())
    val engineers by remember { vm.engineers() }.collectAsState(initial = emptyList())
    val services by remember { vm.services() }.collectAsState(initial = emptyList())
    val parts by remember { vm.partsCatalog() }.collectAsState(initial = emptyList())
    val me by vm.session.collectAsState()

    val scope = rememberCoroutineScope()
    var showWorkDialog by remember { mutableStateOf(false) }
    var showPartDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    val request = row
    if (request == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val employee = me
    val isMine = employee != null && request.engineerId == employee.id
    val isDispatcher = role == Role.DISPATCHER || role == Role.ADMIN
    val canEditWorks = request.status != RequestStatus.DONE && request.status != RequestStatus.CANCELED && (isDispatcher || isMine)
    val total = works.sumOf { it.price * it.quantity }
    val partsTotal = usedParts.sumOf { it.price * it.quantity }

    DetailScaffold(
        title = request.number,
        subtitle = "${request.type.title} · ${request.status.title}",
        onBack = onBack,
        bottomBar = {
            RequestActions(
                role = role,
                status = request.status,
                isMine = isMine,
                onStart = { scope.launch { vm.setStatus(requestId, RequestStatus.IN_PROGRESS) } },
                onWaiting = { scope.launch { vm.setStatus(requestId, RequestStatus.WAITING_PARTS) } },
                onComplete = { showCompleteDialog = true },
                onCancel = { showCancelDialog = true }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(request.status)
                        Spacer(Modifier.width(8.dp))
                        PriorityChip(request.priority)
                        Spacer(Modifier.weight(1f))
                        Text(
                            total.money(),
                            style = MaterialTheme.typography.titleLarge,
                            color = BrandPrimary
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        request.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                SectionCard(title = "Клиент") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = TextSecondary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(request.clientName.trim(), style = MaterialTheme.typography.titleMedium)
                            Text(request.clientPhone, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        val context = LocalContext.current
                        OutlinedButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${request.clientPhone}")))
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Позвонить", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    InfoRow("Адрес выезда", request.address)
                    TextButton(onClick = { onOpenClient(request.clientId) }, contentPadding = PaddingValues(0.dp)) {
                        Text("Открыть карточку клиента")
                    }
                }
            }

            item {
                SectionCard(title = "Оборудование") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Engineering, contentDescription = null, tint = BrandPrimary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(request.equipmentTitle, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            if (isDispatcher) {
                item {
                    SectionCard(title = "Исполнитель") {
                        SelectCard(
                            label = "Инженер",
                            selected = engineers.firstOrNull { it.id == request.engineerId },
                            options = engineers,
                            optionLabel = { "${it.shortName} · ${it.phone}" },
                            onSelect = { engineer ->
                                scope.launch { vm.assignEngineer(requestId, engineer.id) }
                            },
                            placeholder = "Назначьте инженера"
                        )
                        if (request.engineerId == null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "После назначения заявка перейдёт в статус «Назначена»",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else if (request.engineerName != null) {
                item {
                    SectionCard(title = "Исполнитель") {
                        Text(request.engineerName!!, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            item {
                SectionCard(
                    title = "Работы",
                    action = {
                        if (canEditWorks) {
                            TextButton(onClick = { showWorkDialog = true }) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Добавить")
                            }
                        }
                    }
                ) {
                    if (works.isEmpty()) {
                        Text("Работы ещё не внесены", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    } else {
                        works.forEach { work ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(work.serviceName, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${work.quantity} × ${work.price.money()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Text((work.price * work.quantity).money(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Итого по работам", style = MaterialTheme.typography.titleSmall)
                            Text((total + partsTotal).money(), style = MaterialTheme.typography.titleMedium, color = BrandPrimary)
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "Запчасти",
                    action = {
                        if (canEditWorks) {
                            TextButton(onClick = { showPartDialog = true }) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Добавить")
                            }
                        }
                    }
                ) {
                    if (usedParts.isEmpty()) {
                        Text("Запчасти не использовались", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    } else {
                        usedParts.forEach { part ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(part.partName, style = MaterialTheme.typography.bodyMedium)
                                    Text("${part.quantity} шт. · ${part.article}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Text((part.price * part.quantity).money(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Сведения о заявке") {
                    InfoRow("Тип работ", request.type.title)
                    InfoRow("Создана", request.createdAt.asDateTime())
                    InfoRow("Плановый выезд", request.scheduledAt.asDateTime())
                    if (request.status == RequestStatus.DONE) {
                        InfoRow("Выполнена", request.scheduledAt.asDate())
                    }
                    InfoRow("Приоритет", request.priority.title)
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showWorkDialog) {
        AddWorkDialog(
            services = services,
            onDismiss = { showWorkDialog = false },
            onConfirm = { service, quantity ->
                scope.launch { vm.addWork(requestId, service.id, quantity) }
            }
        )
    }

    if (showPartDialog) {
        AddPartDialog(
            parts = parts,
            onDismiss = { showPartDialog = false },
            onConfirm = { part, quantity ->
                scope.launch { vm.addPart(requestId, part.id, quantity) }
            }
        )
    }

    if (showCompleteDialog) {
        ConfirmDialog(
            title = "Завершить заявку?",
            text = "Заявка будет переведена в статус «Выполнена». Стоимость работ и запчастей сохранится в истории клиента.",
            confirmLabel = "Завершить",
            onConfirm = {
                scope.launch {
                    vm.setStatus(requestId, RequestStatus.DONE)
                    vm.showMessage("Заявка ${request.number} выполнена")
                }
            },
            onDismiss = { showCompleteDialog = false }
        )
    }

    if (showCancelDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Отменить заявку") },
            text = {
                Column {
                    Text("Укажите причину отмены — она сохранится в карточке заявки.")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Причина") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        vm.cancelRequest(requestId, reason)
                        showCancelDialog = false
                    }
                }) { Text("Отменить заявку", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Назад") }
            }
        )
    }
}

@Composable
private fun RequestActions(
    role: Role,
    status: RequestStatus,
    isMine: Boolean,
    onStart: () -> Unit,
    onWaiting: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val showForEngineer = role == Role.ENGINEER && isMine &&
        (status == RequestStatus.ASSIGNED || status == RequestStatus.IN_PROGRESS || status == RequestStatus.WAITING_PARTS)
    val showForDispatcher = role != Role.ENGINEER &&
        status != RequestStatus.DONE && status != RequestStatus.CANCELED
    if (!showForEngineer && !showForDispatcher) return

    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showForEngineer) {
                when (status) {
                    RequestStatus.ASSIGNED -> Button(
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Принять в работу")
                    }
                    RequestStatus.IN_PROGRESS -> {
                        OutlinedButton(onClick = onWaiting, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Ждём запчасть")
                        }
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                        ) {
                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Завершить")
                        }
                    }
                    RequestStatus.WAITING_PARTS -> Button(
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text("Вернуть в работу")
                    }
                    else -> Unit
                }
            } else if (showForDispatcher) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Отменить заявку", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AddWorkDialog(
    services: List<Service>,
    onDismiss: () -> Unit,
    onConfirm: (Service, Int) -> Unit
) {
    var selected by remember { mutableStateOf<Service?>(null) }
    var quantity by remember { mutableStateOf(1) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить работу") },
        text = {
            Column {
                SelectCard(
                    label = "Услуга",
                    selected = selected,
                    options = services,
                    optionLabel = { "${it.name} · ${it.price.money()}" },
                    onSelect = { selected = it },
                    placeholder = "Выберите услугу"
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Количество", style = MaterialTheme.typography.bodyMedium)
                    QuantityStepper(quantity = quantity, onChange = { quantity = it })
                }
                if (selected != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "К оплате: ${(selected!!.price * quantity).money()}",
                        style = MaterialTheme.typography.titleSmall,
                        color = BrandPrimary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selected?.let { onConfirm(it, quantity) }
                    onDismiss()
                },
                enabled = selected != null
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun AddPartDialog(
    parts: List<Part>,
    onDismiss: () -> Unit,
    onConfirm: (Part, Int) -> Unit
) {
    var selected by remember { mutableStateOf<Part?>(null) }
    var quantity by remember { mutableStateOf(1) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить запчасть") },
        text = {
            Column {
                SelectCard(
                    label = "Запчасть",
                    selected = selected,
                    options = parts,
                    optionLabel = { "${it.name} · ${it.price.money()}" },
                    onSelect = { selected = it },
                    placeholder = "Выберите запчасть"
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Количество", style = MaterialTheme.typography.bodyMedium)
                    QuantityStepper(quantity = quantity, onChange = { quantity = it })
                }
                if (selected != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Стоимость: ${(selected!!.price * quantity).money()}",
                        style = MaterialTheme.typography.titleSmall,
                        color = BrandPrimary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selected?.let { onConfirm(it, quantity) }
                    onDismiss()
                },
                enabled = selected != null
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
