package co.touchlab.sessionize.api

import co.touchlab.sessionize.BaseModel
import co.touchlab.sessionize.Durations
import co.touchlab.sessionize.PlatformCrashlyticsException
import co.touchlab.sessionize.SettingsKeys
import co.touchlab.sessionize.db.SessionizeDbHelper
import co.touchlab.sessionize.platform.NotificationsModel.createNotifications
import co.touchlab.sessionize.platform.NotificationsModel.notificationsEnabled
import co.touchlab.sessionize.platform.backgroundSuspend
import co.touchlab.sessionize.platform.currentTimeMillis
import co.touchlab.sessionize.platform.logException
import com.russhwolf.settings.Settings
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.native.concurrent.ThreadLocal


@ThreadLocal
object NetworkRepo : KoinComponent{
    private val sessionizeApi : SessionizeApi by inject()
    private val appSettings : Settings by inject()
    private val softExceptionCallback : PlatformCrashlyticsException by inject()

    fun dataCalls() = CoroutineScope().launch {
        try {
            val api = sessionizeApi
            val networkSpeakerJson = api.getSpeakersJson()
            val networkSessionJson = api.getSessionsJson()
            val networkSponsorSessionJson =  api.getSponsorSessionJson()

            backgroundSuspend {
                SessionizeDbHelper.primeAll(networkSpeakerJson, networkSessionJson, networkSponsorSessionJson)
                appSettings.putLong(SettingsKeys.KEY_LAST_LOAD, currentTimeMillis())
            }

            //If we do some kind of data re-load after a user logs in, we'll need to update this.
            //We assume for now that when the app first starts, you have nothing rsvp'd
            if (notificationsEnabled()) {
                createNotifications()
            }
        } catch (e: Exception) {
            logException(e)
        }
    }

    fun refreshData() {
        if (!appSettings.getBoolean(SettingsKeys.KEY_FIRST_RUN, true)) {
            val lastLoad = appSettings.getLong(SettingsKeys.KEY_LAST_LOAD)
            if (lastLoad < (currentTimeMillis() - (Durations.TWO_HOURS_MILLIS.toLong()))) {
                dataCalls()
            }
        }
    }

    fun sendFeedback() = CoroutineScope().launch {
        try {
            SessionizeDbHelper.sendFeedback()
        } catch (e: Throwable) {
            softExceptionCallback(e, "Feedback Send Failed")
        }
    }

    private class CoroutineScope() : BaseModel()
}