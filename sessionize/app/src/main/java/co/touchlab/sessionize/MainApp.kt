package co.touchlab.sessionize

import android.app.Application
import android.content.Context
import android.util.Log
import co.touchlab.droidcon.db.DroidconDb
import co.touchlab.sessionize.api.AnalyticsApi
import co.touchlab.sessionize.api.NetworkRepo
import co.touchlab.sessionize.api.NotificationsApi
import co.touchlab.sessionize.api.SessionizeApi
import co.touchlab.sessionize.api.SessionizeApiImpl
import co.touchlab.sessionize.platform.AndroidAppContext
import co.touchlab.sessionize.platform.Concurrent
import co.touchlab.sessionize.platform.MainConcurrent
import com.google.firebase.analytics.FirebaseAnalytics
import com.russhwolf.settings.AndroidSettings
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.get
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidAppContext.app = this

        val commonModule = module {
            threadLocal<SessionizeApi> { SessionizeApiImpl }
            single<Concurrent> { MainConcurrent }
        }

        val platformModule = module {
            single<AnalyticsApi> { AnalyticsApiImpl(FirebaseAnalytics.getInstance(this@MainApp)) }
            single<SqlDriver> { AndroidSqliteDriver(DroidconDb.Schema, this@MainApp, "droidcondb") }
            single<CoroutineDispatcher> { Dispatchers.Main }
            single<CoroutineContext> { Dispatchers.Main }
            single { AndroidSettings.Factory(this@MainApp).create("DROIDCON_SETTINGS") }
            single(named("timeZone")){
                BuildConfig.TIME_ZONE
            }
            single<NotificationsApi> { NotificationsApiImpl() }
            single<PlatformFileLoader>(named("PlatformFileLoader")) { this@MainApp::loadAsset }
            single<PlatformCrashlyticsLog> { { Log.w("MainApp", it) } }
            single<PlatformCrashlyticsException> { {e:Throwable, message:String ->
                Log.e("MainApp", message, e)
            } }

        }

        startKoin {
            modules(listOf(commonModule, platformModule))
        }

        AppContext.initAppContext()

        NetworkRepo.sendFeedback()

        @Suppress("ConstantConditionIf")
        if(BuildConfig.FIREBASE_ENABLED) {
            FirebaseMessageHandler.init()
        }else{
            Log.d("MainApp","Firebase json not found: Firebased Not Enabled")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        val notificationsApi : NotificationsApi = get()
        notificationsApi.deinitializeNotifications()
    }

    private fun loadAsset(fileName: String, filePrefix: String): String? =
            assets.open("$fileName.$filePrefix", Context.MODE_PRIVATE)
                    .bufferedReader()
                    .use { it.readText() }
}
