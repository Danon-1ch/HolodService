package ru.tisbi.servicecenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import ru.tisbi.servicecenter.data.Employee
import ru.tisbi.servicecenter.data.Role
import ru.tisbi.servicecenter.ui.MainScaffold
import ru.tisbi.servicecenter.ui.MainViewModel
import ru.tisbi.servicecenter.ui.components.Avatar
import ru.tisbi.servicecenter.ui.components.EmptyState
import ru.tisbi.servicecenter.ui.components.SearchField
import ru.tisbi.servicecenter.ui.theme.BrandAccent
import ru.tisbi.servicecenter.ui.theme.BrandPrimary
import ru.tisbi.servicecenter.ui.theme.StatusDone
import ru.tisbi.servicecenter.ui.theme.TextSecondary

fun Role.color() = when (this) {
    Role.ADMIN -> BrandAccent
    Role.DISPATCHER -> BrandPrimary
    Role.ENGINEER -> StatusDone
}

@Composable
fun RoleChip(role: Role) {
    val color = role.color()
    Surface(color = color.copy(alpha = 0.12f), contentColor = color, shape = RoundedCornerShape(50)) {
        Text(
            role.title,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmployeesScreen(
    vm: MainViewModel,
    onOpenEmployee: (Long) -> Unit,
    onAddEmployee: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val session by vm.session.collectAsState()
    var query by remember { mutableStateOf("") }
    val employees by remember { vm.employees() }.collectAsState(initial = emptyList())
    val visible = employees.filter {
        query.isBlank() ||
            it.fullName.contains(query, ignoreCase = true) ||
            it.phone.contains(query) ||
            it.login.contains(query, ignoreCase = true)
    }

    MainScaffold(
        title = "Сотрудники",
        subtitle = session?.let { "Филиал: ${it.shortName}" },
        role = Role.ADMIN,
        currentRoute = "employees",
        onNavigate = onNavigate,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddEmployee,
                containerColor = BrandPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Сотрудник") }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SearchField(value = query, onValueChange = { query = it }, placeholder = "ФИО, телефон, логин")
            }
            if (visible.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Badge,
                        title = "Сотрудники не найдены",
                        text = "Проверьте запрос или добавьте нового сотрудника"
                    )
                }
            }
            items(visible, key = { it.id }) { employee ->
                EmployeeCard(employee = employee, onClick = { onOpenEmployee(employee.id) })
            }
        }
    }
}

@Composable
private fun EmployeeCard(employee: Employee, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(employee.fullName, 46)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(employee.fullName, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(employee.phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoleChip(employee.role)
                    Spacer(Modifier.width(8.dp))
                    if (!employee.isActive) {
                        Text(
                            "Отключён",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("Логин: ${employee.login}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                }
            }
        }
    }
}
