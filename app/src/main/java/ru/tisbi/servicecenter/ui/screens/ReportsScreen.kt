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
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.tisbi.servicecenter.data.RequestStatus
import ru.tisbi.servicecenter.data.Role
import ru.tisbi.servicecenter.ui.MainScaffold
import ru.tisbi.servicecenter.ui.MainViewModel
import ru.tisbi.servicecenter.ui.components.EmptyState
import ru.tisbi.servicecenter.ui.components.SectionCard
import ru.tisbi.servicecenter.ui.components.color
import ru.tisbi.servicecenter.ui.components.money
import ru.tisbi.servicecenter.ui.theme.BrandAccent
import ru.tisbi.servicecenter.ui.theme.BrandPrimary
import ru.tisbi.servicecenter.ui.theme.StatusDone
import ru.tisbi.servicecenter.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun ReportsScreen(
    vm: MainViewModel,
    role: Role,
    onNavigate: (String) -> Unit
) {
    val employee by vm.session.collectAsState()
    val statusCounts by remember { vm.statusCounts() }.collectAsState(initial = emptyList())
    val engineerLoad by remember { vm.engineerLoad() }.collectAsState(initial = emptyList())
    val revenue by remember { vm.revenue() }.collectAsState(initial = 0.0)
    val allRequests by remember { vm.requests("", false) }.collectAsState(initial = emptyList())

    val total = statusCounts.sumOf { it.count }
    val done = statusCounts.firstOrNull { it.status == RequestStatus.DONE }?.count ?: 0
    val average = if (done > 0) revenue / done else 0.0
    val maxCount = (statusCounts.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    val maxRevenue = (engineerLoad.maxOfOrNull { it.revenue } ?: 1.0).coerceAtLeast(1.0)

    MainScaffold(
        title = "Отчёты",
        subtitle = employee?.let { "Филиал · ${it.shortName}" },
        role = role,
        currentRoute = "reports",
        onNavigate = onNavigate
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        Modifier.weight(1f),
                        icon = Icons.Outlined.WorkOutline,
                        title = "Заявки",
                        value = total.toString(),
                        tint = BrandPrimary
                    )
                    KpiCard(
                        Modifier.weight(1f),
                        icon = Icons.Outlined.TaskAlt,
                        title = "Выполнено",
                        value = done.toString(),
                        tint = StatusDone
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        Modifier.weight(1f),
                        icon = Icons.Outlined.Payments,
                        title = "Выручка",
                        value = revenue.money(),
                        tint = BrandAccent
                    )
                    KpiCard(
                        Modifier.weight(1f),
                        icon = Icons.Outlined.BarChart,
                        title = "Средний чек",
                        value = average.money(),
                        tint = BrandPrimary
                    )
                }
            }

            item {
                SectionCard(title = "Заявки по статусам") {
                    if (statusCounts.isEmpty()) {
                        Text("Данных пока нет", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    } else {
                        RequestStatus.entries.forEach { status ->
                            val count = statusCounts.firstOrNull { it.status == status }?.count ?: 0
                            if (count > 0) {
                                StatusBar(
                                    label = status.title,
                                    count = count,
                                    fraction = count.toFloat() / maxCount,
                                    color = status.color()
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Загрузка инженеров") {
                    if (engineerLoad.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.Engineering,
                            title = "Нет инженеров",
                            text = "Добавьте сотрудников, чтобы видеть статистику"
                        )
                    } else {
                        engineerLoad.forEachIndexed { index, load ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(34.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = BrandPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(load.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.height(5.dp))
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                                    ) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth((load.revenue / maxRevenue).toFloat().coerceIn(0f, 1f))
                                                .height(7.dp)
                                                .background(BrandPrimary, RoundedCornerShape(50))
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Выполнено заявок: ${load.doneCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    load.revenue.money(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = BrandPrimary
                                )
                            }
                            if (index < engineerLoad.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Структура заявок") {
                    val open = allRequests.count { it.status == RequestStatus.NEW || it.status == RequestStatus.ASSIGNED }
                    val inWork = allRequests.count { it.status == RequestStatus.IN_PROGRESS || it.status == RequestStatus.WAITING_PARTS }
                    val cancelled = allRequests.count { it.status == RequestStatus.CANCELED }
                    RatioRow("В очереди на назначение", open, total)
                    RatioRow("В работе у инженеров", inWork, total)
                    RatioRow("Отменено", cancelled, total)
                    RatioRow("Выполнено", done, total)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Доля выполненных заявок: ${if (total > 0) (done * 100.0 / total).roundToInt() else 0} %",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    tint: Color
) {
    SectionCard(modifier = modifier) {
        Box(
            Modifier
                .size(36.dp)
                .background(tint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = tint)
        Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun StatusBar(label: String, count: Int, fraction: Float, color: Color) {
    Column(Modifier.padding(vertical = 5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("$count", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                    .height(8.dp)
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun RatioRow(label: String, count: Int, total: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            if (total > 0) "$count · ${(count * 100.0 / total).roundToInt()} %" else "0",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
