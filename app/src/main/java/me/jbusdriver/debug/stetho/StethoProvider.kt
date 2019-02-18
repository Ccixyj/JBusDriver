/*
package me.jbusdriver.debug.stetho

import android.content.Context
import com.facebook.stetho.InspectorModulesProvider
import com.facebook.stetho.Stetho
import com.facebook.stetho.inspector.database.DatabaseFilesProvider
import com.facebook.stetho.inspector.database.DefaultDatabaseConnectionProvider
import com.facebook.stetho.inspector.database.SqliteDatabaseDriver
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain
import me.jbusdriver.db.DB
import java.io.File


fun initializeStetho(context: Context) {
    // See also: Stetho.initializeWithDefaults(Context)
    Stetho.initialize(
        Stetho.newInitializerBuilder(context)
            .enableWebKitInspector(ExtInspectorModulesProvider(context))
            .build()
    )
}

private class ExtInspectorModulesProvider internal constructor(private val mContext: Context) :
    InspectorModulesProvider {

    override fun get(): Iterable<ChromeDevtoolsDomain> {
        return Stetho.DefaultInspectorModulesBuilder(mContext)
            .provideDatabaseDriver(createCollectDBDriver(mContext))
            .finish()
    }

    private fun createCollectDBDriver(context: Context): SqliteDatabaseDriver {
        return SqliteDatabaseDriver(context, DatabaseFilesProvider {
            context.databaseList().map { File(it) } + listOf(File(DB.collectDataBase.readableDatabase.path))
        }, DefaultDatabaseConnectionProvider())
    }

}*/
