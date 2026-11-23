package ru.tisbi.servicecenter.data

// Единая точка доступа к данным: проверка прав,
// нумерация заявок, фиксация цен.

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Locale

/**
 * Единая точка доступа к данным приложения. Экраны не работают с Room напрямую:
 * они получают Flow из репозитория и вызывают его методы изменения.
 */
class Repository(private val db: AppDatabase) {

    // Наполнение базы при первом запуске.
    suspend fun ensureSeeded() = SeedData.populate(db)

    // Вход: логин, активность учётной записи, хеш пароля.
    suspend fun login(login: String, password: String): Employee? {
        val employee = db.employeeDao().findByLogin(login.trim().lowercase()) ?: return null
        if (!employee.isActive) return null
        if (employee.passwordHash != PasswordHasher.hash(password)) return null
        return employee
    }

    fun branch(id: Long): Flow<Branch?> = db.branchDao().observe(id)
    fun branches(): Flow<List<Branch>> = db.branchDao().observeAll()

    fun requests(branchId: Long, engineerId: Long?, query: String): Flow<List<RequestRow>> =
        db.requestDao().observeRequests(branchId, engineerId, query.trim())

    fun request(id: Long): Flow<RequestRow?> = db.requestDao().observeRequest(id)

    fun clients(branchId: Long, query: String): Flow<List<ClientRow>> =
        db.clientDao().observeClients(branchId, query.trim())

    fun client(id: Long): Flow<Client?> = db.clientDao().observeClient(id)
    fun clientRequests(id: Long): Flow<List<HistoryRow>> = db.requestDao().observeByClient(id)
    fun equipment(clientId: Long): Flow<List<Equipment>> = db.equipmentDao().observeByClient(clientId)

    fun employees(branchId: Long): Flow<List<Employee>> = db.employeeDao().observeByBranch(branchId)
    fun engineers(branchId: Long): Flow<List<Employee>> = db.employeeDao().observeEngineers(branchId)

    fun services(): Flow<List<Service>> = db.catalogDao().observeServices()
    fun parts(): Flow<List<Part>> = db.catalogDao().observeParts()

    fun works(requestId: Long): Flow<List<WorkRow>> = db.workDao().observeWorks(requestId)
    fun partUsages(requestId: Long): Flow<List<PartRow>> = db.workDao().observeParts(requestId)

    fun statusCounts(branchId: Long): Flow<List<StatusCount>> = db.reportDao().observeStatusCounts(branchId)
    fun engineerLoad(branchId: Long): Flow<List<EngineerLoad>> = db.reportDao().observeEngineerLoad(branchId)
    fun revenue(branchId: Long): Flow<Double> = db.reportDao().observeRevenue(branchId)

    suspend fun equipmentForClient(clientId: Long): List<Equipment> =
        db.equipmentDao().getByClient(clientId)

    // Номер заявки вида З-2026-0001 собирается из года и счётчика.
    suspend fun createRequest(
        branchId: Long,
        clientId: Long,
        equipmentId: Long,
        createdById: Long,
        type: RequestType,
        priority: Priority,
        description: String,
        address: String,
        scheduledAt: Long
    ): Long {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val number = String.format(Locale.US, "З-%d-%04d", year, db.requestDao().count() + 1)
        return db.requestDao().insert(
            ServiceRequest(
                branchId = branchId,
                number = number,
                clientId = clientId,
                equipmentId = equipmentId,
                engineerId = null,
                createdById = createdById,
                type = type,
                priority = priority,
                status = RequestStatus.NEW,
                description = description,
                address = address,
                createdAt = System.currentTimeMillis(),
                scheduledAt = scheduledAt,
                completedAt = null
            )
        )
    }

    // Назначение инженера переводит заявку в статус «Назначена».
    suspend fun assignEngineer(requestId: Long, engineerId: Long) {
        db.requestDao().assign(requestId, engineerId, RequestStatus.ASSIGNED)
    }

    suspend fun setStatus(requestId: Long, status: RequestStatus) {
        val completedAt = if (status == RequestStatus.DONE) System.currentTimeMillis() else null
        db.requestDao().complete(requestId, status, completedAt)
    }

    suspend fun cancelRequest(requestId: Long, reason: String) {
        db.requestDao().cancel(requestId, reason.ifBlank { "Причина не указана" })
    }

    // Работа фиксируется с ценой прайса на момент выполнения.
    suspend fun addWork(requestId: Long, serviceId: Long, engineerId: Long, quantity: Int): Boolean {
        val service = db.catalogDao().getService(serviceId) ?: return false
        if (quantity <= 0) return false
        db.workDao().insertWork(
            WorkItem(
                requestId = requestId,
                serviceId = serviceId,
                engineerId = engineerId,
                quantity = quantity,
                price = service.price,
                performedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    // Расход запчасти тоже с ценой на момент списания.
    suspend fun addPart(requestId: Long, partId: Long, quantity: Int): Boolean {
        val part = db.catalogDao().getPart(partId) ?: return false
        if (quantity <= 0) return false
        db.workDao().insertPartUsage(
            PartUsage(
                requestId = requestId,
                partId = partId,
                quantity = quantity,
                price = part.price
            )
        )
        return true
    }

    // Новый клиент или правка существующего.
    suspend fun saveClient(client: Client): Long =
        if (client.id == 0L) db.clientDao().insert(client) else {
            db.clientDao().update(client)
            client.id
        }

    suspend fun saveEquipment(equipment: Equipment): Long =
        if (equipment.id == 0L) db.equipmentDao().insert(equipment) else {
            db.equipmentDao().update(equipment)
            equipment.id
        }

    // Пароль хешируется, пустая строка означает «не менять».
    suspend fun saveEmployee(employee: Employee, password: String?): Result<Unit> = try {
        if (employee.id == 0L) {
            require(password != null && password.length >= 5) { "Пароль должен содержать не менее 5 символов" }
            db.employeeDao().insert(employee.copy(passwordHash = PasswordHasher.hash(password)))
        } else {
            val stored = db.employeeDao().getById(employee.id) ?: return Result.failure(IllegalStateException("Сотрудник не найден"))
            val hash = if (password.isNullOrBlank()) stored.passwordHash else PasswordHasher.hash(password)
            db.employeeDao().update(employee.copy(passwordHash = hash))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        val message = if (e.message?.contains("UNIQUE", ignoreCase = true) == true) "Логин уже занят" else e.message ?: "Не удалось сохранить сотрудника"
        Result.failure(IllegalStateException(message))
    }

    suspend fun setEmployeeActive(id: Long, active: Boolean) = db.employeeDao().setActive(id, active)

    // Смена пароля с проверкой текущего.
    suspend fun changePassword(employeeId: Long, oldPassword: String, newPassword: String): Result<Unit> {
        val employee = db.employeeDao().getById(employeeId)
            ?: return Result.failure(IllegalStateException("Сотрудник не найден"))
        if (employee.passwordHash != PasswordHasher.hash(oldPassword)) {
            return Result.failure(IllegalStateException("Текущий пароль указан неверно"))
        }
        if (newPassword.length < 5) {
            return Result.failure(IllegalStateException("Новый пароль должен содержать не менее 5 символов"))
        }
        db.employeeDao().update(employee.copy(passwordHash = PasswordHasher.hash(newPassword)))
        return Result.success(Unit)
    }
}
