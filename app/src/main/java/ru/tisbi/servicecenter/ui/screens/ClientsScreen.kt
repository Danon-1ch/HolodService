package ru.tisbi.servicecenter.ui.screens

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
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
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
import ru.tisbi.servicecenter.data.ClientRow
import ru.tisbi.servicecenter.data.Role
import ru.tisbi.servicecenter.ui.MainScaffold
import ru.tisbi.servicecenter.ui.MainViewModel
import ru.tisbi.servicecenter.ui.components.Avatar
import ru.tisbi.servicecenter.ui.components.EmptyState
import ru.tisbi.servicecenter.ui.components.SearchField
import ru.tisbi.servicecenter.ui.theme.BrandPrimary
import ru.tisbi.servicecenter.ui.theme.TextSecondary

@Composable
fun ClientsScreen(
    vm: MainViewModel,
    role: Role,
    onOpenClient: (Long) -> Unit,
    onAddClient: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val session by vm.session.collectAsState()
    var query by remember { mutableStateOf("") }
    val clients by remember(query) { vm.clients(query) }.collectAsState(initial = emptyList())

    MainScaffold(
        title = "Клиенты",
        subtitle = session?.let { "База филиала · ${it.shortName}" },
        role = role,
        currentRoute = "clients",
        onNavigate = onNavigate,
        floatingActionButton = {
            if (role != Role.ENGINEER) {
                ExtendedFloatingActionButton(
                    onClick = onAddClient,
                    containerColor = BrandPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Новый клиент") }
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
                    placeholder = "Фамилия, телефон, адрес"
                )
            }
            item {
                Text(
                    "Найдено: ${clients.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            if (clients.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.People,
                        title = "Клиенты не найдены",
                        text = "Измените условия поиска или добавьте нового клиента"
                    )
                }
            }
            items(clients, key = { it.id }) { client ->
                ClientCard(client = client, onClick = { onOpenClient(client.id) })
            }
        }
    }
}

@Composable
fun ClientCard(client: ClientRow, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Avatar("${client.lastName} ${client.firstName}", 44)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${client.lastName} ${client.firstName} ${client.middleName.orEmpty()}".trim(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(client.phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(client.address, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Engineering, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Техники: ${client.equipmentCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Заявок: ${client.requestCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
