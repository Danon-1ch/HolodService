package ru.tisbi.servicecenter.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tisbi.servicecenter.data.Equipment
import ru.tisbi.servicecenter.data.HistoryRow
import ru.tisbi.servicecenter.data.Role
import ru.tisbi.servicecenter.ui.DetailScaffold
import ru.tisbi.servicecenter.ui.MainViewModel
import ru.tisbi.servicecenter.ui.components.Avatar
import ru.tisbi.servicecenter.ui.components.InfoRow
import ru.tisbi.servicecenter.ui.components.SectionCard
import ru.tisbi.servicecenter.ui.components.SelectCard
import ru.tisbi.servicecenter.ui.components.StatusChip
import ru.tisbi.servicecenter.ui.components.asDate
import ru.tisbi.servicecenter.ui.components.asDateTime
import ru.tisbi.servicecenter.ui.components.asShortDate
import ru.tisbi.servicecenter.ui.theme.BrandPrimary
import ru.tisbi.servicecenter.ui.theme.StatusDone
import ru.tisbi.servicecenter.ui.theme.TextSecondary

@Composable
fun ClientDetailScreen(
    vm: MainViewModel,
    clientId: Long,
    role: Role,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenRequest: (Long) -> Unit
) {
    val client by remember(clientId) { vm.client(clientId) }.collectAsState(initial = null)
    val equipment by remember(clientId) { vm.equipment(clientId) }.collectAsState(initial = emptyList())
    val history by remember(clientId) { vm.clientRequests(clientId) }.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showEquipmentDialog by remember { mutableStateOf(false) }

    val data = client
    if (data == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    DetailScaffold(
        title = "${data.lastName} ${data.firstName}",
        subtitle = "Карточка клиента",
        onBack = onBack,
        actions = {
            if (role != Role.ENGINEER) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Редактировать")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar("${data.lastName} ${data.firstName}", 56)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${data.lastName} ${data.firstName} ${data.middleName.orEmpty()}".trim(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Клиент с ${data.createdAt.asDate()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        InfoRow("Телефон", data.phone)
                        InfoRow("Адрес", data.address)
                        if (!data.email.isNullOrBlank()) InfoRow("E-mail", data.email!!)
                        Spacer(Modifier.height(10.dp))
                        val context = LocalContext.current
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${data.phone}")))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Позвонить")
                            }
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(data.address)}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Outlined.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Маршрут")
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "Оборудование",
                    action = {
                        if (role != Role.ENGINEER) {
                            TextButton(onClick = { showEquipmentDialog = true }) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Добавить")
                            }
                        }
                    }
                ) {
                    if (equipment.isEmpty()) {
                        Text("Оборудование не зарегистрировано", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    } else {
                        equipment.forEachIndexed { index, item ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Engineering, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text(
                                        "Серийный номер: ${item.serialNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    item.warrantyUntil?.let { until ->
                                        val onWarranty = until > System.currentTimeMillis()
                                        Text(
                                            if (onWarranty) "На гарантии до ${until.asDate()}" else "Гарантия истекла ${until.asDate()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (onWarranty) StatusDone else TextSecondary
                                        )
                                    }
                                }
                            }
                            if (index < equipment.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            item {
                SectionCard(title = "История заявок") {
                    if (history.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.History, contentDescription = null, tint = TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Заявок ещё не было", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    } else {
                        history.forEachIndexed { index, item ->
                            HistoryRowView(item = item, onClick = { onOpenRequest(item.id) })
                            if (index < history.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }

    if (showEquipmentDialog) {
        AddEquipmentDialog(
            onDismiss = { showEquipmentDialog = false },
            onConfirm = { kind, brand, model, serial ->
                scope.launch {
                    vm.saveEquipment(
                        Equipment(
                            clientId = clientId,
                            kind = kind,
                            brand = brand,
                            model = model,
                            serialNumber = serial,
                            warrantyUntil = null
                        )
                    )
                    vm.showMessage("Оборудование добавлено")
                }
            }
        )
    }
}

@Composable
private fun HistoryRowView(item: HistoryRow, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.number, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${item.createdAt.asShortDate()} · ${item.createdAt.asDateTime().substringAfter(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        StatusChip(item.status)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AddEquipmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    val kinds = listOf("Холодильник", "Морозильник", "Витрина", "Ларь морозильный")
    var kind by remember { mutableStateOf(kinds.first()) }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое оборудование") },
        text = {
            Column {
                SelectCard(
                    label = "Тип",
                    selected = kind,
                    options = kinds,
                    optionLabel = { it },
                    onSelect = { kind = it }
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Марка") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Модель") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = serial,
                    onValueChange = { serial = it },
                    label = { Text("Серийный номер") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (brand.isBlank() || model.isBlank() || serial.isBlank()) {
                    error = "Заполните марку, модель и серийный номер"
                } else {
                    onConfirm(kind, brand.trim(), model.trim(), serial.trim())
                    onDismiss()
                }
            }) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
