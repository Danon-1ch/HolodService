package ru.tisbi.servicecenter.data

import java.util.Locale

/**
 * Демонстрационное наполнение базы данных филиала.
 * Записи создаются при первом запуске приложения, чтобы экраны и отчёты
 * были наполнены реалистичными данными без ручного ввода.
 */
object SeedData {

    private const val DAY = 86_400_000L
    private const val HOUR = 3_600_000L

    private data class BranchSeed(val name: String, val address: String, val phone: String, val email: String)

    private data class EmployeeSeed(
        val last: String,
        val first: String,
        val middle: String,
        val role: Role,
        val login: String,
        val password: String,
        val phone: String
    )

    private data class EquipmentSeed(
        val kind: String,
        val brand: String,
        val model: String,
        val serial: String,
        val warrantyDays: Long? = null
    )

    private data class ClientSeed(
        val last: String,
        val first: String,
        val middle: String,
        val phone: String,
        val email: String,
        val address: String,
        val equipment: List<EquipmentSeed>
    )

    private data class ClientInfo(val client: Client, val branchId: Long, val equipment: List<Equipment>)

    private val branches = listOf(
        BranchSeed("Центральный офис", "г. Казань, ул. Баумана, 44", "+7 (843) 292-10-01", "central@holodservice.ru"),
        BranchSeed("Филиал «Северный»", "г. Казань, пр. Ямашева, 71", "+7 (843) 292-10-02", "north@holodservice.ru"),
        BranchSeed("Филиал «Южный»", "г. Казань, ул. Юлиуса Фучика, 90", "+7 (843) 292-10-03", "south@holodservice.ru")
    )

    private val employees = listOf(
        listOf(
            EmployeeSeed("Сафин", "Рустем", "Ильдарович", Role.ADMIN, "admin", "admin123", "+7 (843) 292-10-01"),
            EmployeeSeed("Петрова", "Анна", "Сергеевна", Role.DISPATCHER, "petrova", "disp123", "+7 (917) 231-08-44"),
            EmployeeSeed("Иванов", "Сергей", "Петрович", Role.ENGINEER, "ivanov", "eng123", "+7 (917) 245-77-12"),
            EmployeeSeed("Галеев", "Марат", "Ринатович", Role.ENGINEER, "galeev", "eng123", "+7 (927) 404-16-90")
        ),
        listOf(
            EmployeeSeed("Волкова", "Ольга", "Дмитриевна", Role.DISPATCHER, "volkova", "disp123", "+7 (917) 260-55-31"),
            EmployeeSeed("Смирнова", "Ольга", "Викторовна", Role.ENGINEER, "smirnova", "eng123", "+7 (927) 418-22-07"),
            EmployeeSeed("Хайруллин", "Ильдар", "Наилевич", Role.ENGINEER, "hairullin", "eng123", "+7 (917) 903-44-18")
        ),
        listOf(
            EmployeeSeed("Кузнецов", "Андрей", "Викторович", Role.DISPATCHER, "kuznetsov", "disp123", "+7 (843) 292-10-03"),
            EmployeeSeed("Нигматуллин", "Руслан", "Маратович", Role.ENGINEER, "nigmatullin", "eng123", "+7 (927) 612-39-54"),
            EmployeeSeed("Фролова", "Екатерина", "Андреевна", Role.ENGINEER, "frolova", "eng123", "+7 (917) 388-70-26"),
            EmployeeSeed("Ахметов", "Дамир", "Ильшатович", Role.ENGINEER, "ahmetov", "eng123", "+7 (927) 501-14-63"),
            EmployeeSeed("Данилов", "Кирилл", "Олегович", Role.ENGINEER, "danilov", "eng123", "+7 (917) 774-90-05")
        )
    )

    private val clients = listOf(
        listOf(
            ClientSeed("Гараева", "Лейсан", "Ильдаровна", "+7 (917) 260-14-22", "l.garaeva@mail.ru", "ул. Профсоюзная, 14, кв. 35", listOf(
                EquipmentSeed("Холодильник", "Indesit", "ITR 4180 W", "IND-4820193"),
                EquipmentSeed("Морозильник", "Atlant", "М-7104", "ATL-771203", 240)
            )),
            ClientSeed("Шарипов", "Айдар", "Рустемович", "+7 (927) 441-06-78", "sharipov.aydar@yandex.ru", "ул. Достоевского, 52, кв. 7", listOf(
                EquipmentSeed("Холодильник", "LG", "GW-B459SQJL", "LG-6612084")
            )),
            ClientSeed("Николаева", "Елена", "Викторовна", "+7 (917) 815-32-40", "e.nikolaeva@gmail.com", "ул. Лево-Булачная, 24, кв. 112", listOf(
                EquipmentSeed("Холодильник", "Bosch", "KGV39VW22R", "BS-3301294"),
                EquipmentSeed("Витрина", "Liebherr", "D 5100", "LB-990312")
            )),
            ClientSeed("Хусаинов", "Булат", "Маратович", "+7 (927) 330-91-57", "b.husainov@mail.ru", "ул. Татарстан, 8, кв. 19", listOf(
                EquipmentSeed("Холодильник", "Samsung", "RB-30 J3200SA", "SM-5520173", 95)
            )),
            ClientSeed("Фёдорова", "Ирина", "Александровна", "+7 (917) 502-77-19", "irina.f@inbox.ru", "ул. Пушкина, 12, кв. 61", listOf(
                EquipmentSeed("Морозильник", "Позис", "F 180", "PZ-114509")
            )),
            ClientSeed("Юсупов", "Тимур", "Ринатович", "+7 (927) 664-28-03", "t.yusupov@mail.ru", "ул. Московская, 31, кв. 4", listOf(
                EquipmentSeed("Холодильник", "Haier", "A2F636C", "HR-884210")
            ))
        ),
        listOf(
            ClientSeed("Савельев", "Дмитрий", "Олегович", "+7 (917) 903-11-26", "d.savelyev@yandex.ru", "пр. Ямашева, 36, кв. 205", listOf(
                EquipmentSeed("Холодильник", "Indesit", "DS 4160", "IND-5520881"),
                EquipmentSeed("Морозильник", "Bosch", "GSN36VW3P", "BS-774511")
            )),
            ClientSeed("Ким", "Алина", "Сергеевна", "+7 (927) 712-45-88", "a.kim@gmail.com", "ул. Амирхана, 12, кв. 88", listOf(
                EquipmentSeed("Витрина", "Позис", "ВХС-0,7", "PZ-661102")
            )),
            ClientSeed("Валеев", "Ильназ", "Фаритович", "+7 (917) 448-60-31", "v.ilnaz@mail.ru", "ул. Чуйкова, 58, кв. 14", listOf(
                EquipmentSeed("Холодильник", "Atlant", "ХМ-4208", "ATL-330912")
            )),
            ClientSeed("Морозова", "Наталья", "Петровна", "+7 (927) 205-73-64", "n.morozova@inbox.ru", "пр. Победы, 108, кв. 33", listOf(
                EquipmentSeed("Холодильник", "LG", "GC-B459SQCL", "LG-9982011", 150)
            )),
            ClientSeed("Абдуллин", "Ранис", "Ильгизович", "+7 (917) 631-02-19", "r.abdullin@mail.ru", "ул. Лаврентьева, 9, кв. 47", listOf(
                EquipmentSeed("Морозильник", "Haier", "HEF-134", "HR-552390")
            )),
            ClientSeed("Соколова", "Марина", "Андреевна", "+7 (927) 884-56-02", "m.sokolova@yandex.ru", "ул. Волгоградская, 25, кв. 90", listOf(
                EquipmentSeed("Холодильник", "Liebherr", "CN 4015", "LB-220145")
            ))
        ),
        listOf(
            ClientSeed("Гайнуллин", "Азат", "Наилевич", "+7 (917) 720-33-48", "a.gainullin@mail.ru", "ул. Юлиуса Фучика, 74, кв. 21", listOf(
                EquipmentSeed("Холодильник", "Samsung", "RL-34 EBSW", "SM-773401")
            )),
            ClientSeed("Лебедева", "Оксана", "Юрьевна", "+7 (927) 559-18-71", "o.lebedeva@gmail.com", "ул. Рихарда Зорге, 55, кв. 8", listOf(
                EquipmentSeed("Витрина", "Liebherr", "D 6100", "LB-551209"),
                EquipmentSeed("Холодильник", "Bosch", "KGN39VL21R", "BS-112098")
            )),
            ClientSeed("Тимофеев", "Павел", "Сергеевич", "+7 (917) 306-92-15", "p.timofeev@inbox.ru", "ул. Кул Гали, 15, кв. 62", listOf(
                EquipmentSeed("Холодильник", "Indesit", "ITR 4180 W", "IND-771245")
            )),
            ClientSeed("Хайрутдинова", "Гузель", "Рустемовна", "+7 (927) 470-61-39", "g.hairutdinova@mail.ru", "ул. Мавлютова, 17, кв. 103", listOf(
                EquipmentSeed("Морозильник", "Atlant", "М-7186", "ATL-552108")
            )),
            ClientSeed("Орлов", "Никита", "Владимирович", "+7 (917) 588-27-04", "n.orlov@yandex.ru", "ул. Комиссара Габишева, 4, кв. 12", listOf(
                EquipmentSeed("Холодильник", "Haier", "CE-107", "HR-110293", 60)
            )),
            ClientSeed("Пименова", "Дарья", "Алексеевна", "+7 (927) 340-09-82", "d.pimenova@gmail.com", "ул. Отрадная, 20, кв. 5", listOf(
                EquipmentSeed("Холодильник", "Позис", "РД-149", "PZ-887311")
            ))
        )
    )

    private val services = listOf(
        Service(name = "Диагностика холодильного оборудования", price = 900.0, durationMin = 40),
        Service(name = "Заправка фреоном R600a", price = 3200.0, durationMin = 90),
        Service(name = "Замена компрессора", price = 6500.0, durationMin = 180),
        Service(name = "Замена термостата", price = 2100.0, durationMin = 60),
        Service(name = "Ремонт платы управления", price = 4500.0, durationMin = 120),
        Service(name = "Чистка конденсатора", price = 1500.0, durationMin = 60),
        Service(name = "Замена уплотнителя двери", price = 1900.0, durationMin = 50),
        Service(name = "Техобслуживание витрины", price = 5000.0, durationMin = 150),
        Service(name = "Замена вентилятора обдува", price = 2300.0, durationMin = 70),
        Service(name = "Устранение утечки фреона", price = 3800.0, durationMin = 120)
    )

    private val parts = listOf(
        Part(name = "Компрессор Danfoss NLE8.5", article = "DF-NLE8.5", price = 7500.0),
        Part(name = "Фреон R600a, баллон 1 кг", article = "FR-600A", price = 900.0),
        Part(name = "Термостат K59", article = "TH-K59", price = 1200.0),
        Part(name = "Уплотнитель двери 600 мм", article = "SL-600", price = 1400.0),
        Part(name = "Плата управления Indesit", article = "PB-IND42", price = 5200.0),
        Part(name = "Вентилятор обдува 120 мм", article = "FN-120", price = 1600.0),
        Part(name = "Фильтр-осушитель", article = "FD-08", price = 700.0),
        Part(name = "Реле пусковое 1/4", article = "RL-025", price = 800.0),
        Part(name = "Термодатчик NTC 10 кОм", article = "NTC-10", price = 650.0),
        Part(name = "Петля двери универсальная", article = "LN-UNI", price = 1100.0)
    )

    private val descriptions = listOf(
        "Не морозит верхняя камера, компрессор работает непрерывно и не отключается.",
        "На задней стенке холодильной камеры образуется сильная наледь, вода стоит под ящиками.",
        "Не закрывается дверь: уплотнитель рассохся, остаётся зазор по всей высоте.",
        "При работе появился посторонний шум и вибрация, особенно при запуске компрессора.",
        "После скачка напряжения холодильник не включается, индикация на панели отсутствует.",
        "Морозильная камера работает, а в холодильном отделении температура около 15 градусов.",
        "Витрина не держит температуру, на стекле постоянно конденсат.",
        "Требуется плановое обслуживание перед сезоном: чистка конденсатора, проверка фреона.",
        "Холодильник выключается через 10-15 минут работы и включается снова через час.",
        "Под холодильником собирается лужа, на полу вода.",
        "Дверца морозильника не прилегает, в камере намерзает лёд.",
        "На панели мигает ошибка, компрессор не запускается.",
        "Повреждён уплотнитель нижней двери, требуется замена.",
        "В холодильной камере слишком холодно, продукты замерзают у задней стенки.",
        "Гарантийный случай: компрессор шумит и не создаёт давление.",
        "Не работает вентилятор обдува, в камере неравномерная температура.",
        "Плата управления не реагирует на кнопки, подсветка не включается.",
        "Регулярно намерзает лёд на испарителе, требуется диагностика системы оттайки."
    )

    suspend fun populate(db: AppDatabase) {
        if (db.employeeDao().count() > 0) return

        val now = System.currentTimeMillis()
        val year = java.util.Calendar.getInstance().apply { timeInMillis = now }.get(java.util.Calendar.YEAR)

        val branchIds = branches.map { db.branchDao().insert(Branch(name = it.name, address = it.address, phone = it.phone, email = it.email)) }

        val dispatchers = mutableMapOf<Long, Long>()
        val engineers = mutableMapOf<Long, MutableList<Long>>()
        employees.forEachIndexed { branchIndex, list ->
            val branchId = branchIds[branchIndex]
            list.forEach { seed ->
                val id = db.employeeDao().insert(
                    Employee(
                        branchId = branchId,
                        lastName = seed.last,
                        firstName = seed.first,
                        middleName = seed.middle,
                        role = seed.role,
                        login = seed.login,
                        passwordHash = PasswordHasher.hash(seed.password),
                        phone = seed.phone
                    )
                )
                when (seed.role) {
                    Role.DISPATCHER -> dispatchers[branchId] = id
                    Role.ENGINEER -> engineers.getOrPut(branchId) { mutableListOf() }.add(id)
                    Role.ADMIN -> Unit
                }
            }
        }

        val clientInfos = mutableListOf<ClientInfo>()
        clients.forEachIndexed { branchIndex, list ->
            val branchId = branchIds[branchIndex]
            list.forEachIndexed { index, seed ->
                val clientId = db.clientDao().insert(
                    Client(
                        branchId = branchId,
                        lastName = seed.last,
                        firstName = seed.first,
                        middleName = seed.middle,
                        phone = seed.phone,
                        email = seed.email,
                        address = seed.address,
                        createdAt = now - (210 - index * 7) * DAY
                    )
                )
                val equipment = seed.equipment.map { eq ->
                    val equipmentId = db.equipmentDao().insert(
                        Equipment(
                            clientId = clientId,
                            kind = eq.kind,
                            brand = eq.brand,
                            model = eq.model,
                            serialNumber = eq.serial,
                            warrantyUntil = eq.warrantyDays?.let { now + it * DAY }
                        )
                    )
                    Equipment(equipmentId, clientId, eq.kind, eq.brand, eq.model, eq.serial, eq.warrantyDays?.let { now + it * DAY })
                }
                clientInfos += ClientInfo(Client(clientId, branchId, seed.last, seed.first, seed.middle, seed.phone, seed.email, seed.address, now), branchId, equipment)
            }
        }

        val insertedServices = services.map { seed ->
            seed.copy(id = db.catalogDao().insertService(seed))
        }
        val insertedParts = parts.map { seed ->
            seed.copy(id = db.catalogDao().insertPart(seed))
        }

        val statusPattern = listOf(
            RequestStatus.DONE, RequestStatus.DONE, RequestStatus.IN_PROGRESS,
            RequestStatus.ASSIGNED, RequestStatus.DONE, RequestStatus.NEW,
            RequestStatus.WAITING_PARTS, RequestStatus.DONE, RequestStatus.ASSIGNED,
            RequestStatus.IN_PROGRESS, RequestStatus.DONE, RequestStatus.CANCELED
        )
        val typePattern = listOf(
            RequestType.REPAIR, RequestType.DIAGNOSTICS, RequestType.MAINTENANCE,
            RequestType.REPAIR, RequestType.WARRANTY
        )

        for (i in 0 until 36) {
            val info = clientInfos[i % clientInfos.size]
            val branchId = info.branchId
            val engineerList = engineers[branchId].orEmpty()
            val status = statusPattern[i % statusPattern.size]
            val engineerId = if (status == RequestStatus.NEW) null else engineerList[i % engineerList.size]
            val createdAt = now - ((i % 28) + 1) * DAY - ((i * 3) % 9) * HOUR
            val scheduled = createdAt + DAY / 2
            val type = typePattern[i % typePattern.size]
            val priority = when {
                i % 11 == 0 -> Priority.HIGH
                i % 5 == 0 -> Priority.LOW
                else -> Priority.NORMAL
            }
            val number = String.format(Locale.US, "З-%d-%04d", year, i + 1)
            val completedAt = if (status == RequestStatus.DONE) minOf(now - HOUR, createdAt + 2 * DAY) else null

            val requestId = db.requestDao().insert(
                ServiceRequest(
                    branchId = branchId,
                    number = number,
                    clientId = info.client.id,
                    equipmentId = info.equipment[i % info.equipment.size].id,
                    engineerId = engineerId,
                    createdById = dispatchers[branchId] ?: 1L,
                    type = type,
                    priority = priority,
                    status = status,
                    description = descriptions[i % descriptions.size],
                    address = info.client.address,
                    createdAt = createdAt,
                    scheduledAt = scheduled,
                    completedAt = completedAt,
                    cancelReason = if (status == RequestStatus.CANCELED) "Клиент перенёс ремонт на другой месяц" else null
                )
            )

            if (engineerId != null && status != RequestStatus.CANCELED) {
                val workCount = when (status) {
                    RequestStatus.DONE -> if (i % 3 == 0) 2 else 1
                    RequestStatus.IN_PROGRESS -> 1
                    else -> 0
                }
                repeat(workCount) { w ->
                    val service = insertedServices[(i + w * 3) % insertedServices.size]
                    db.workDao().insertWork(
                        WorkItem(
                            requestId = requestId,
                            serviceId = service.id,
                            engineerId = engineerId,
                            quantity = 1,
                            price = service.price,
                            performedAt = completedAt ?: (createdAt + (w + 1) * HOUR)
                        )
                    )
                }
                if (status == RequestStatus.DONE && i % 3 == 0) {
                    val part = insertedParts[i % insertedParts.size]
                    db.workDao().insertPartUsage(
                        PartUsage(
                            requestId = requestId,
                            partId = part.id,
                            quantity = if (i % 6 == 0) 2 else 1,
                            price = part.price
                        )
                    )
                }
            }
        }
    }
}
