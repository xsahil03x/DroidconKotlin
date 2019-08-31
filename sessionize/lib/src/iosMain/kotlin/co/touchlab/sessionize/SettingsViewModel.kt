package co.touchlab.sessionize

import co.touchlab.sessionize.platform.NotificationsModel
import org.koin.core.KoinComponent

class SettingsViewModel() {
    val settingsModel = SettingsModel(NotificationsModel)
}