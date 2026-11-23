package ru.tisbi.servicecenter

import android.app.Application
import ru.tisbi.servicecenter.data.AppDatabase
import ru.tisbi.servicecenter.data.Repository

class ServiceCenterApp : Application() {
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = Repository(AppDatabase.build(this))
    }
}
