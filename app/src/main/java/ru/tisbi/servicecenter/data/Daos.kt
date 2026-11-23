package ru.tisbi.servicecenter.data

// Запросы к базе. Для экранов объявлены проекции:
// строка списка собирается одним запросом, без N+1.

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class RequestRow(
    val id: Long,
    val number: String,
    val status: RequestStatus,
    val priority: Priority,
    val type: RequestType,
    val createdAt: Long,
    val scheduledAt: Long,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String,
    val address: String,
    val equipmentTitle: String,
    val engineerId: Long?,
    val engineerName: String?,
    val description: String,
    val total: Double
)

data class ClientRow(
    val id: Long,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val phone: String,
    val email: String?,
    val address: String,
    val equipmentCount: Int,
    val requestCount: Int
)

data class WorkRow(
    val id: Long,
    val serviceName: String,
    val quantity: Int,
    val price: Double,
    val performedAt: Long
)

data class PartRow(
    val id: Long,
    val partName: String,
    val article: String,
    val quantity: Int,
    val price: Double
)

data class StatusCount(val status: RequestStatus, val count: Int)

data class EngineerLoad(
    val engineerId: Long,
    val name: String,
    val doneCount: Int,
    val revenue: Double
)

data class HistoryRow(
    val id: Long,
    val number: String,
    val createdAt: Long,
    val status: RequestStatus
)

@Dao
// Филиалы.
interface BranchDao {
    @Query("SELECT * FROM branches ORDER BY id")
    fun observeAll(): Flow<List<Branch>>

    @Query("SELECT * FROM branches WHERE id = :id")
    fun observe(id: Long): Flow<Branch?>

    @Query("SELECT * FROM branches WHERE id = :id")
    suspend fun get(id: Long): Branch?

    @Query("SELECT COUNT(*) FROM branches")
    suspend fun count(): Int

    @Insert
    suspend fun insert(branch: Branch): Long
}

@Dao
// Сотрудники: поиск по логину, инженеры филиала.
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE login = :login LIMIT 1")
    suspend fun findByLogin(login: String): Employee?

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getById(id: Long): Employee?

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun count(): Int

    @Query("SELECT * FROM employees WHERE branchId = :branchId ORDER BY isActive DESC, lastName")
    fun observeByBranch(branchId: Long): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE branchId = :branchId AND role = 'ENGINEER' AND isActive = 1 ORDER BY lastName")
    fun observeEngineers(branchId: Long): Flow<List<Employee>>

    @Query("UPDATE employees SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Insert
    suspend fun insert(employee: Employee): Long

    @Update
    suspend fun update(employee: Employee)
}

@Dao
// Клиенты: поиск и счётчики техники и заявок.
interface ClientDao {
    @Query(
        """
        SELECT c.id, c.lastName, c.firstName, c.middleName, c.phone, c.email, c.address,
               (SELECT COUNT(*) FROM equipment e WHERE e.clientId = c.id) AS equipmentCount,
               (SELECT COUNT(*) FROM service_requests r WHERE r.clientId = c.id) AS requestCount
        FROM clients c
        WHERE c.branchId = :branchId
          AND (:query = '' OR c.lastName LIKE '%' || :query || '%'
               OR c.firstName LIKE '%' || :query || '%'
               OR c.phone LIKE '%' || :query || '%'
               OR c.address LIKE '%' || :query || '%')
        ORDER BY c.lastName, c.firstName
        """
    )
    fun observeClients(branchId: Long, query: String): Flow<List<ClientRow>>

    @Query("SELECT * FROM clients WHERE id = :id")
    fun observeClient(id: Long): Flow<Client?>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClient(id: Long): Client?

    @Query("SELECT COUNT(*) FROM clients WHERE branchId = :branchId")
    suspend fun countByBranch(branchId: Long): Int

    @Insert
    suspend fun insert(client: Client): Long

    @Update
    suspend fun update(client: Client)
}

@Dao
// Техника клиента.
interface EquipmentDao {
    @Query("SELECT * FROM equipment WHERE clientId = :clientId ORDER BY id")
    fun observeByClient(clientId: Long): Flow<List<Equipment>>

    @Query("SELECT * FROM equipment WHERE clientId = :clientId ORDER BY id")
    suspend fun getByClient(clientId: Long): List<Equipment>

    @Insert
    suspend fun insert(equipment: Equipment): Long

    @Update
    suspend fun update(equipment: Equipment)
}

@Dao
// Заявки: список, карточка, смена статуса.
interface RequestDao {
    @Query(
        """
        SELECT r.id, r.number, r.status, r.priority, r.type, r.createdAt, r.scheduledAt,
               r.clientId,
               c.lastName || ' ' || c.firstName || ' ' || COALESCE(c.middleName, '') AS clientName,
               c.phone AS clientPhone, r.address,
               e.kind || ' ' || e.brand || ' ' || e.model AS equipmentTitle,
               r.engineerId,
               emp.lastName || ' ' || emp.firstName AS engineerName,
               r.description,
               COALESCE((SELECT SUM(w.price * w.quantity) FROM work_items w WHERE w.requestId = r.id), 0.0) AS total
        FROM service_requests r
        JOIN clients c ON c.id = r.clientId
        JOIN equipment e ON e.id = r.equipmentId
        LEFT JOIN employees emp ON emp.id = r.engineerId
        WHERE r.branchId = :branchId
          AND (:engineerId IS NULL OR r.engineerId = :engineerId)
          AND (:query = '' OR r.number LIKE '%' || :query || '%'
               OR c.lastName LIKE '%' || :query || '%'
               OR c.phone LIKE '%' || :query || '%'
               OR r.address LIKE '%' || :query || '%'
               OR e.brand LIKE '%' || :query || '%'
               OR e.model LIKE '%' || :query || '%')
        ORDER BY CASE r.status
                   WHEN 'IN_PROGRESS' THEN 0 WHEN 'ASSIGNED' THEN 1 WHEN 'NEW' THEN 2
                   WHEN 'WAITING_PARTS' THEN 3 WHEN 'DONE' THEN 4 ELSE 5 END,
                 r.scheduledAt
        """
    )
    fun observeRequests(branchId: Long, engineerId: Long?, query: String): Flow<List<RequestRow>>

    @Query(
        """
        SELECT r.id, r.number, r.status, r.priority, r.type, r.createdAt, r.scheduledAt,
               r.clientId,
               c.lastName || ' ' || c.firstName || ' ' || COALESCE(c.middleName, '') AS clientName,
               c.phone AS clientPhone, r.address,
               e.kind || ' ' || e.brand || ' ' || e.model AS equipmentTitle,
               r.engineerId,
               emp.lastName || ' ' || emp.firstName AS engineerName,
               r.description,
               COALESCE((SELECT SUM(w.price * w.quantity) FROM work_items w WHERE w.requestId = r.id), 0.0) AS total
        FROM service_requests r
        JOIN clients c ON c.id = r.clientId
        JOIN equipment e ON e.id = r.equipmentId
        LEFT JOIN employees emp ON emp.id = r.engineerId
        WHERE r.id = :id
        """
    )
    fun observeRequest(id: Long): Flow<RequestRow?>

    @Query("SELECT * FROM service_requests WHERE id = :id")
    suspend fun getRequest(id: Long): ServiceRequest?

    @Query("SELECT id, number, createdAt, status FROM service_requests WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun observeByClient(clientId: Long): Flow<List<HistoryRow>>

    @Query("SELECT COUNT(*) FROM service_requests")
    suspend fun count(): Int

    @Insert
    suspend fun insert(request: ServiceRequest): Long

    @Query("UPDATE service_requests SET status = :status, engineerId = :engineerId WHERE id = :id")
    suspend fun setStatus(id: Long, status: RequestStatus, engineerId: Long?)

    @Query("UPDATE service_requests SET engineerId = :engineerId, status = :status WHERE id = :id")
    suspend fun assign(id: Long, engineerId: Long, status: RequestStatus)

    @Query("UPDATE service_requests SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun complete(id: Long, status: RequestStatus, completedAt: Long?)

    @Query("UPDATE service_requests SET status = 'CANCELED', cancelReason = :reason WHERE id = :id")
    suspend fun cancel(id: Long, reason: String)
}

@Dao
// Работы и расход запчастей по заявке.
interface WorkDao {
    @Query(
        """
        SELECT w.id, s.name AS serviceName, w.quantity, w.price, w.performedAt
        FROM work_items w JOIN services s ON s.id = w.serviceId
        WHERE w.requestId = :requestId
        ORDER BY w.performedAt DESC
        """
    )
    fun observeWorks(requestId: Long): Flow<List<WorkRow>>

    @Query(
        """
        SELECT p.id, pt.name AS partName, pt.article, p.quantity, p.price
        FROM part_usages p JOIN parts pt ON pt.id = p.partId
        WHERE p.requestId = :requestId
        ORDER BY p.id DESC
        """
    )
    fun observeParts(requestId: Long): Flow<List<PartRow>>

    @Insert
    suspend fun insertWork(item: WorkItem): Long

    @Insert
    suspend fun insertPartUsage(usage: PartUsage): Long
}

@Dao
// Справочники услуг и запчастей.
interface CatalogDao {
    @Query("SELECT * FROM services ORDER BY name")
    fun observeServices(): Flow<List<Service>>

    @Query("SELECT * FROM parts ORDER BY name")
    fun observeParts(): Flow<List<Part>>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getService(id: Long): Service?

    @Query("SELECT * FROM parts WHERE id = :id")
    suspend fun getPart(id: Long): Part?

    @Insert
    suspend fun insertService(item: Service): Long

    @Insert
    suspend fun insertPart(item: Part): Long
}

@Dao
// Отчёты по филиалу.
interface ReportDao {
    @Query(
        """
        SELECT status, COUNT(*) AS count FROM service_requests
        WHERE branchId = :branchId GROUP BY status
        """
    )
    fun observeStatusCounts(branchId: Long): Flow<List<StatusCount>>

    @Query(
        """
        SELECT emp.id AS engineerId, emp.lastName || ' ' || emp.firstName AS name,
               COUNT(DISTINCT CASE WHEN r.status = 'DONE' THEN r.id END) AS doneCount,
               COALESCE(SUM(w.price * w.quantity), 0.0) AS revenue
        FROM employees emp
        LEFT JOIN service_requests r ON r.engineerId = emp.id
        LEFT JOIN work_items w ON w.requestId = r.id
        WHERE emp.branchId = :branchId AND emp.role = 'ENGINEER'
        GROUP BY emp.id
        ORDER BY revenue DESC
        """
    )
    fun observeEngineerLoad(branchId: Long): Flow<List<EngineerLoad>>

    @Query("SELECT COALESCE(SUM(w.price * w.quantity), 0.0) FROM work_items w JOIN service_requests r ON r.id = w.requestId WHERE r.branchId = :branchId")
    fun observeRevenue(branchId: Long): Flow<Double>
}
