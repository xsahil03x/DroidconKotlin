package co.touchlab.sessionize

import co.touchlab.sessionize.api.NotificationsApi
import co.touchlab.sessionize.platform.INotificationsModel
import kotlinx.coroutines.launch
import org.koin.core.inject


class SettingsModel(private val notificationsModel: INotificationsModel) : BaseModel() {

    private val notificationsApi: NotificationsApi by inject()

    fun setRemindersSettingEnabled(enabled:Boolean) = launch {
        notificationsModel.setRemindersEnabled(enabled)

        if (enabled && !notificationsModel.notificationsEnabled()) {
            notificationsApi.initializeNotifications {
                notificationsModel.recreateReminderNotifications()
            }
        }else{
            notificationsModel.recreateReminderNotifications()
        }
    }

    fun setFeedbackSettingEnabled(enabled:Boolean) = launch {
        notificationsModel.setFeedbackEnabled(enabled)

        if (enabled && !notificationsModel.notificationsEnabled()) {
            notificationsApi.initializeNotifications {
                notificationsModel.recreateFeedbackNotifications()
            }
        }else{
            notificationsModel.recreateFeedbackNotifications()
        }
    }
}