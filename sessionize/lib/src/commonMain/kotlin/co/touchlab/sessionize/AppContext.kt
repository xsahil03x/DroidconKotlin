package co.touchlab.sessionize

import co.touchlab.sessionize.SettingsKeys.KEY_FIRST_RUN
import co.touchlab.sessionize.api.NetworkRepo
import co.touchlab.sessionize.api.NotificationsApi
import co.touchlab.sessionize.db.SessionizeDbHelper
import co.touchlab.sessionize.file.FileRepo
import co.touchlab.sessionize.platform.Concurrent
import co.touchlab.sessionize.platform.NotificationsModel
import co.touchlab.sessionize.platform.logException
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject

typealias PlatformFileLoader = (filePrefix: String, fileType: String) -> String?
typealias PlatformCrashlyticsLog = (s: String) -> Unit
typealias PlatformCrashlyticsException = (e:Throwable, message:String) ->Unit

object AppContext :KoinComponent {
    //Workaround for https://github.com/Kotlin/kotlinx.serialization/issues/441
    private val primeJson = Json.nonstrict
    private val appSettings : Settings by inject()

    fun initAppContext(networkRepo: NetworkRepo = NetworkRepo,
                       fileRepo: FileRepo = FileRepo,
                       dbHelper: SessionizeDbHelper = SessionizeDbHelper,
                       notificationsModel: NotificationsModel = NotificationsModel) {

        dbHelper.initDatabase(get())

        val notificationsApi : NotificationsApi = get()
        val concurrent : Concurrent = get()

        notificationsApi.initializeNotifications { success ->
            concurrent.backgroundTask({ success }, {
                if (it) {
                    notificationsModel.createNotifications()
                } else {
                    notificationsModel.cancelNotifications()
                }
            })
        }

        concurrent.backgroundTask({ maybeLoadSeedData(fileRepo) }) {
            networkRepo.refreshData()
        }
    }

    private fun maybeLoadSeedData(fileRepo: FileRepo) {
        try {
            if (firstRun()) {
                fileRepo.seedFileLoad()
                updateFirstRun()
            }
        } catch (e: Exception) {
            logException(e)
        }
    }

    private fun firstRun(): Boolean = appSettings.getBoolean(KEY_FIRST_RUN, true)

    private fun updateFirstRun() {
        appSettings.putBoolean(KEY_FIRST_RUN, false)
    }
}
