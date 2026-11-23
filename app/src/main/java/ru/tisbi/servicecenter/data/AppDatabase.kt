package ru.tisbi.servicecenter.data

// Сборка базы Room: сущности, версия схемы, DAO.

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Branch::class,
        Employee::class,
        Client::class,
        Equipment::class,
        ServiceRequest::class,
        Service::class,
        WorkItem::class,
        Part::class,
        PartUsage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun branchDao(): BranchDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun clientDao(): ClientDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun requestDao(): RequestDao
    abstract fun workDao(): WorkDao
    abstract fun catalogDao(): CatalogDao
    abstract fun reportDao(): ReportDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "holodservice.db")
                .fallbackToDestructiveMigration(false)
                .build()
    }
}
