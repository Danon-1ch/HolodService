package ru.tisbi.servicecenter.ui

// Состояние экранов: сессия сотрудника, готовность базы,
// ошибки входа и сообщения о действиях.

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import ru.tisbi.servicecenter.ServiceCenterApp
import ru.tisbi.servicecenter.data.Branch
import ru.tisbi.servicecenter.data.Client
import ru.tisbi.servicecenter.data.ClientRow
import ru.tisbi.servicecenter.data.Employee
import ru.tisbi.servicecenter.data.EngineerLoad
import ru.tisbi.servicecenter.data.Equipment
import ru.tisbi.servicecenter.data.HistoryRow
import ru.tisbi.servicecenter.data.Part
import ru.tisbi.servicecenter.data.PartRow
import ru.tisbi.servicecenter.data.Repository
import ru.tisbi.servicecenter.data.RequestRow
import ru.tisbi.servicecenter.data.RequestStatus
import ru.tisbi.servicecenter.data.RequestType
import ru.tisbi.servicecenter.data.Priority
import ru.tisbi.servicecenter.data.Service
import ru.tisbi.servicecenter.data.StatusCount
import ru.tisbi.servicecenter.data.WorkRow

// Единственная ViewModel приложения.
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: Repository = (app as ServiceCenterApp).repository

    private val _session = MutableStateFlow<Employee?>(null)
    val session = _session.asStateFlow()

    private val _ready = MutableStateFlow(false)
    val ready = _ready.asStateFlow()

    private val _loginLoading = MutableStateFlow(false)
    val loginLoading = _loginLoading.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage = _actionMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repo.ensureSeeded()
            _ready.value = true
        }
    }

    // Вход выполняется в корутине, результат приходит в StateFlow.
    fun login(login: String, password: String, onSuccess: () -> Unit) {
        if (login.isBlank() || password.isBlank()) {
            _loginError.value = "Введите логин и пароль"
            return
        }
        _loginLoading.value = true
        _loginError.value = null
        viewModelScope.launch {
            val employee = repo.login(login, password)
            _loginLoading.value = false
            if (employee == null) {
                _loginError.value = "Неверный логин или пароль, либо учётная запись отключена"
            } else {
                _session.value = employee
                onSuccess()
            }
        }
    }

    fun logout() {
        _session.value = null
        _actionMessage.value = null
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun showMessage(text: String) {
        _actionMessage.value = text
    }

    fun consumeMessage() {
        _actionMessage.value = null
    }

    private val branchId: Long? get() = _session.value?.branchId

    fun branch(): Flow<Branch?> {
        val id = branchId ?: return flowOf(null)
        return repo.branch(id)
    }

    fun requests(query: String, onlyMine: Boolean): Flow<List<RequestRow>> {
        val employee = _session.value ?: return flowOf(emptyList())
        return repo.requests(employee.branchId, if (onlyMine) employee.id else null, query)
    }

    fun request(id: Long): Flow<RequestRow?> = repo.request(id)
    fun works(id: Long): Flow<List<WorkRow>> = repo.works(id)
    fun partUsages(id: Long): Flow<List<PartRow>> = repo.partUsages(id)
    fun services(): Flow<List<Service>> = repo.services()
    fun partsCatalog(): Flow<List<Part>> = repo.parts()

    fun clients(query: String): Flow<List<ClientRow>> {
        val id = branchId ?: return flowOf(emptyList())
        return repo.clients(id, query)
    }

    fun client(id: Long): Flow<Client?> = repo.client(id)
    fun clientRequests(id: Long): Flow<List<HistoryRow>> = repo.clientRequests(id)
    fun equipment(clientId: Long): Flow<List<Equipment>> = repo.equipment(clientId)

    fun employees(): Flow<List<Employee>> {
        val id = branchId ?: return flowOf(emptyList())
        return repo.employees(id)
    }

    fun engineers(): Flow<List<Employee>> {
        val id = branchId ?: return flowOf(emptyList())
        return repo.engineers(id)
    }

    fun statusCounts(): Flow<List<StatusCount>> {
        val id = branchId ?: return flowOf(emptyList())
        return repo.statusCounts(id)
    }

    fun engineerLoad(): Flow<List<EngineerLoad>> {
        val id = branchId ?: return flowOf(emptyList())
        return repo.engineerLoad(id)
    }

    fun revenue(): Flow<Double> {
        val id = branchId ?: return flowOf(0.0)
        return repo.revenue(id)
    }

    suspend fun createRequest(
        clientId: Long,
        equipmentId: Long,
        type: RequestType,
        priority: Priority,
        description: String,
        address: String,
        scheduledAt: Long
    ): Long? {
        val employee = _session.value ?: return null
        return repo.createRequest(
            branchId = employee.branchId,
            clientId = clientId,
            equipmentId = equipmentId,
            createdById = employee.id,
            type = type,
            priority = priority,
            description = description,
            address = address,
            scheduledAt = scheduledAt
        )
    }

    suspend fun assignEngineer(requestId: Long, engineerId: Long) = repo.assignEngineer(requestId, engineerId)
    suspend fun setStatus(requestId: Long, status: RequestStatus) = repo.setStatus(requestId, status)
    suspend fun cancelRequest(requestId: Long, reason: String) = repo.cancelRequest(requestId, reason)
    suspend fun addWork(requestId: Long, serviceId: Long, quantity: Int): Boolean {
        val employee = _session.value ?: return false
        return repo.addWork(requestId, serviceId, employee.id, quantity)
    }

    suspend fun addPart(requestId: Long, partId: Long, quantity: Int): Boolean =
        repo.addPart(requestId, partId, quantity)

    suspend fun saveClient(client: Client): Long = repo.saveClient(client)
    suspend fun saveEquipment(equipment: Equipment): Long = repo.saveEquipment(equipment)
    suspend fun equipmentForClient(clientId: Long): List<Equipment> = repo.equipmentForClient(clientId)
    suspend fun saveEmployee(employee: Employee, password: String?): Result<Unit> =
        repo.saveEmployee(employee, password)

    suspend fun setEmployeeActive(id: Long, active: Boolean) = repo.setEmployeeActive(id, active)

    suspend fun changePassword(old: String, new: String): Result<Unit> {
        val employee = _session.value ?: return Result.failure(IllegalStateException("Сессия не найдена"))
        return repo.changePassword(employee.id, old, new)
    }
}
