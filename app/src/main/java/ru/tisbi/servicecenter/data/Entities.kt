package ru.tisbi.servicecenter.data

// Сущности Room: девять таблиц и четыре перечисления.
// Имена полей совпадают с колонками базы.

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Роли определяют права в приложении.
enum class Role(val title: String) {
    ADMIN("Администратор"),
    DISPATCHER("Диспетчер"),
    ENGINEER("Инженер")
}

// Шесть состояний заявки. Room хранит их строками, чтобы в базе было видно значение.
enum class RequestStatus(val title: String) {
    NEW("Новая"),
    ASSIGNED("Назначена"),
    IN_PROGRESS("В работе"),
    WAITING_PARTS("Ожидает запчасти"),
    DONE("Выполнена"),
    CANCELED("Отменена")
}

// Вид работ по заявке.
enum class RequestType(val title: String) {
    DIAGNOSTICS("Диагностика"),
    REPAIR("Ремонт"),
    MAINTENANCE("Техобслуживание"),
    WARRANTY("Гарантийный ремонт")
}

// Приоритет влияет на порядок заявок в списке.
enum class Priority(val title: String) {
    LOW("Низкий"),
    NORMAL("Обычный"),
    HIGH("Высокий")
}

@Entity(tableName = "branches")
data class Branch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val phone: String,
    val email: String
)

@Entity(
    // Сотрудник: учётная запись и кадровые данные вместе.
    tableName = "employees",
    foreignKeys = [ForeignKey(
        entity = Branch::class,
        parentColumns = ["id"],
        childColumns = ["branchId"]
    )],
    indices = [Index("branchId"), Index(value = ["login"], unique = true)]
)
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val branchId: Long,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val role: Role,
    val login: String,
    val passwordHash: String,
    val phone: String,
    val isActive: Boolean = true
) {
    val fullName: String get() = "$lastName $firstName ${middleName.orEmpty()}".trim()
    val shortName: String get() = "$lastName ${firstName.firstOrNull()?.let { "$it." } ?: ""}".trim()
}

@Entity(
    // Клиент привязан к филиалу.
    tableName = "clients",
    foreignKeys = [ForeignKey(
        entity = Branch::class,
        parentColumns = ["id"],
        childColumns = ["branchId"]
    )],
    indices = [Index("branchId")]
)
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val branchId: Long,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val phone: String,
    val email: String?,
    val address: String,
    val createdAt: Long
) {
    val fullName: String get() = "$lastName $firstName ${middleName.orEmpty()}".trim()
}

@Entity(
    // Единица техники клиента.
    tableName = "equipment",
    foreignKeys = [ForeignKey(
        entity = Client::class,
        parentColumns = ["id"],
        childColumns = ["clientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("clientId")]
)
data class Equipment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val kind: String,
    val brand: String,
    val model: String,
    val serialNumber: String,
    val warrantyUntil: Long?
) {
    val title: String get() = "$kind $brand $model".trim()
}

@Entity(
    // Заявка на ремонт — главная таблица системы.
    tableName = "service_requests",
    foreignKeys = [
        ForeignKey(entity = Branch::class, parentColumns = ["id"], childColumns = ["branchId"]),
        ForeignKey(entity = Client::class, parentColumns = ["id"], childColumns = ["clientId"]),
        ForeignKey(entity = Equipment::class, parentColumns = ["id"], childColumns = ["equipmentId"]),
        ForeignKey(entity = Employee::class, parentColumns = ["id"], childColumns = ["engineerId"])
    ],
    indices = [Index("branchId"), Index("clientId"), Index("equipmentId"), Index("engineerId")]
)
data class ServiceRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val branchId: Long,
    val number: String,
    val clientId: Long,
    val equipmentId: Long,
    val engineerId: Long?,
    val createdById: Long,
    val type: RequestType,
    val priority: Priority,
    val status: RequestStatus,
    val description: String,
    val address: String,
    val createdAt: Long,
    val scheduledAt: Long,
    val completedAt: Long?,
    val cancelReason: String? = null
)

@Entity(
    // Прайс-лист работ.
    tableName = "services",
    indices = [Index(value = ["name"], unique = true)]
)
data class Service(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val durationMin: Int
)

@Entity(
    // Выполненные работы по заявке.
    tableName = "work_items",
    foreignKeys = [
        ForeignKey(entity = ServiceRequest::class, parentColumns = ["id"], childColumns = ["requestId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Service::class, parentColumns = ["id"], childColumns = ["serviceId"]),
        ForeignKey(entity = Employee::class, parentColumns = ["id"], childColumns = ["engineerId"])
    ],
    indices = [Index("requestId"), Index("serviceId"), Index("engineerId")]
)
data class WorkItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: Long,
    val serviceId: Long,
    val engineerId: Long,
    val quantity: Int,
    val price: Double,
    val performedAt: Long
)

@Entity(tableName = "parts")
data class Part(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val article: String,
    val price: Double
)

@Entity(
    // Израсходованные запчасти по заявке.
    tableName = "part_usages",
    foreignKeys = [
        ForeignKey(entity = ServiceRequest::class, parentColumns = ["id"], childColumns = ["requestId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Part::class, parentColumns = ["id"], childColumns = ["partId"])
    ],
    indices = [Index("requestId"), Index("partId")]
)
data class PartUsage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: Long,
    val partId: Long,
    val quantity: Int,
    val price: Double
)
