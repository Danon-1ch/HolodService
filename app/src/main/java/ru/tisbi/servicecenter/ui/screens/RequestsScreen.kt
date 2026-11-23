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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.tisbi.servicecenter.data.Priority
import ru.tisbi.servicecenter.data.RequestRow
import ru.tisbi.servicecenter.data.RequestStatus
import ru.tisbi.servicecenter.data.Role
import ru.tisbi.servicecenter.ui.MainViewModel
import ru.tisbi.servicecenter.ui.MainScaffold
import ru.tisbi.servicecenter.ui.components.EmptyState
import ru.tisbi.servicecenter.ui.components.PriorityChip
import ru.tisbi.servicecenter.ui.components.SearchField
import ru.tisbi.servicecenter.ui.components.StatusChip
import ru.tisbi.servicecenter.ui.components.asShortDate
import ru.tisbi.servicecenter.ui.components.money
import ru.tisbi.servicecenter.ui.theme.BrandPrimary
import ru.tisbi.servicecenter.ui.theme.TextPrimary
import ru.tisbi.servicecenter.ui.theme.TextSecondary

private enum class RequestFilter(val title: String) {
    ACTIVE("Активные"),
    ALL("Все"),
    NEW("Новые"),
    IN_PROGRESS("В работе"),
    DONE("Выполненные")
}

@Composable
fun RequestsScreen(
    vm: MainViewModel,
    role: Role,
    onOpenRequest: (Long) -> Unit,
    onNewRequest: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val session by vm.session.collectAsState()
    val employee = session ?: return

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(if (role == Role.ENGINEER) RequestFilter.ACTIVE else RequestFilter.ALL) }

    val rows by remember(query) {
        vm.requests(query, onlyMine = role == Role.ENGINEER)
    }.collectAsState(initial = emptyList())

    val visible = when (filter) {
        RequestFilter.ACTIVE -> rows.filter { it.status != RequestStatus.DONE && it.status != RequestStatus.CANCELED }
        RequestFilter.ALL -> rows
        RequestFilter.NEW -> rows.filter { it.status == RequestStatus.NEW }
        RequestFilter.IN_PROGRESS -> rows.filter {
            it.status == RequestStatus.ASSIGNED || it.status == RequestStatus.IN_PROGRESS || it.status == RequestStatus.WAITING_PARTS
        }
        RequestFilter.DONE -> rows.filter { it.status == RequestStatus.DONE }
    }

    MainScaffold(
        title = if (role == Role.ENGINEER) "Мои заявки" else "Заявки филиала",
        subtitle = "${employee.shortName} · ${role.title}",
        role = role,
        currentRoute = "requests",
        onNavigate = onNavigate,
        floatingActionButton = {
            if (role != Role.ENGINEER) {
                ExtendedFloatingActionButton(
                    onClick = onNewRequest,
                    containerColor = BrandPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Новая заявка") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Номер, клиент, телефон, адрес, модель"
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    val filters = if (role == Role.ENGINEER) {
                        listOf(RequestFilter.ACTIVE, RequestFilter.DONE, RequestFilter.ALL)
                    } else {
                        listOf(RequestFilter.ALL, RequestFilter.NEW, RequestFilter.IN_PROGRESS, RequestFilter.DONE)
                    }
                    filters.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = { Text(option.title, style = MaterialTheme.typography.labelMedium) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
            if (visible.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Assignment,
                        title = "Заявок не найдено",
                        text = if (query.isBlank()) "В выбранной категории пока нет заявок" else "Попробуйте изменить запрос или сбросить фильтр"
                    )
                }
            }
            items(visible, key = { it.id }) { row ->
                RequestCard(row = row, onClick = { onOpenRequest(row.id) })
            }
        }
    }
}

@Composable
private fun RequestCard(row: RequestRow, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.number,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )
                Spacer(Modifier.weight(1f))
                PriorityChip(row.priority)
                if (row.priority != Priority.NORMAL) Spacer(Modifier.width(6.dp))
                StatusChip(row.status)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                row.clientName.trim(),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Engineering, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(row.equipmentTitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            }
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(row.address, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
                Text(row.scheduledAt.asShortDate(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier
                        .size(3.dp)
                        .background(TextSecondary.copy(alpha = 0.6f), CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    row.engineerName ?: "Инженер не назначен",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (row.engineerName == null) MaterialTheme.colorScheme.tertiary else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    row.total.money(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
