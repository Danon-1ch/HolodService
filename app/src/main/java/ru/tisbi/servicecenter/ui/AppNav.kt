package ru.tisbi.servicecenter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.tisbi.servicecenter.ui.screens.ClientDetailScreen
import ru.tisbi.servicecenter.ui.screens.ClientEditScreen
import ru.tisbi.servicecenter.ui.screens.ClientsScreen
import ru.tisbi.servicecenter.ui.screens.EmployeeEditScreen
import ru.tisbi.servicecenter.ui.screens.EmployeesScreen
import ru.tisbi.servicecenter.ui.screens.LoginScreen
import ru.tisbi.servicecenter.ui.screens.NewRequestScreen
import ru.tisbi.servicecenter.ui.screens.ProfileScreen
import ru.tisbi.servicecenter.ui.screens.ReportsScreen
import ru.tisbi.servicecenter.ui.screens.RequestDetailScreen
import ru.tisbi.servicecenter.ui.screens.RequestsScreen

private fun NavHostController.openTab(route: String) {
    navigate(route) {
        popUpTo("requests") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNav(vm: MainViewModel = viewModel()) {
    val session by vm.session.collectAsState()
    val role = session?.role

    if (role == null) {
        LoginScreen(vm, onLoggedIn = {})
        return
    }

    val nav = rememberNavController()
    val message by vm.actionMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message!!)
            vm.consumeMessage()
        }
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = "requests") {
            composable("requests") {
                RequestsScreen(
                    vm = vm,
                    role = role,
                    onOpenRequest = { nav.navigate("request/$it") },
                    onNewRequest = { nav.navigate("newRequest") },
                    onNavigate = { nav.openTab(it) }
                )
            }
            composable("clients") {
                ClientsScreen(
                    vm = vm,
                    role = role,
                    onOpenClient = { nav.navigate("client/$it") },
                    onAddClient = { nav.navigate("clientEdit/0") },
                    onNavigate = { nav.openTab(it) }
                )
            }
            composable("employees") {
                EmployeesScreen(
                    vm = vm,
                    onOpenEmployee = { nav.navigate("employeeEdit/$it") },
                    onAddEmployee = { nav.navigate("employeeEdit/0") },
                    onNavigate = { nav.openTab(it) }
                )
            }
            composable("reports") {
                ReportsScreen(vm = vm, role = role, onNavigate = { nav.openTab(it) })
            }
            composable("profile") {
                ProfileScreen(vm = vm, role = role, onNavigate = { nav.openTab(it) }, onLogout = { vm.logout() })
            }
            composable("request/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                RequestDetailScreen(
                    vm = vm,
                    requestId = id,
                    role = role,
                    onBack = { nav.popBackStack() },
                    onOpenClient = { nav.navigate("client/$it") }
                )
            }
            composable("newRequest") {
                NewRequestScreen(
                    vm = vm,
                    onBack = { nav.popBackStack() },
                    onCreated = { id ->
                        nav.navigate("request/$id") {
                            popUpTo("newRequest") { inclusive = true }
                        }
                    }
                )
            }
            composable("client/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                ClientDetailScreen(
                    vm = vm,
                    clientId = id,
                    role = role,
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate("clientEdit/$id") },
                    onOpenRequest = { nav.navigate("request/$it") }
                )
            }
            composable("clientEdit/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                ClientEditScreen(
                    vm = vm,
                    clientId = id,
                    onBack = { nav.popBackStack() },
                    onSaved = { nav.popBackStack() }
                )
            }
            composable("employeeEdit/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                EmployeeEditScreen(
                    vm = vm,
                    employeeId = id,
                    onBack = { nav.popBackStack() },
                    onSaved = { nav.popBackStack() }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)
        )
    }
}
