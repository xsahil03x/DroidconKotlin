package co.touchlab.sessionize.feedback

import android.content.Intent
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import co.touchlab.droidcon.db.MyPastSession
import co.touchlab.sessionize.FeedbackModel
import co.touchlab.sessionize.PlatformCrashlyticsException
import co.touchlab.sessionize.api.FeedbackApi
import co.touchlab.sessionize.platform.AndroidAppContext
import co.touchlab.sessionize.platform.NotificationsModel.setFeedbackEnabled
import org.koin.core.KoinComponent
import org.koin.core.inject

class FeedbackManager : FeedbackApi, KoinComponent {

    private val softExceptionCallback: PlatformCrashlyticsException by inject()
    private var fragmentManager:FragmentManager? = null
    private var feedbackModel:FeedbackModel = FeedbackModel()
    private var feedbackDialog:FeedbackDialog? = null

    fun setFragmentManager(fragmentManager: FragmentManager){
        this.fragmentManager = fragmentManager
    }

    fun showFeedbackForPastSessions(){
        feedbackModel.showFeedbackForPastSessions(this)
    }

    fun close(){
        this.fragmentManager = null
        try {
            feedbackDialog?.dismiss()
        } catch (e: Exception) {
            softExceptionCallback(e, "Failed closing FeedbackManager")
        }
        feedbackDialog = null
    }

    fun disableFeedback(){
        setFeedbackEnabled(false)
        LocalBroadcastManager.getInstance(AndroidAppContext.app).sendBroadcast(Intent(FeedbackDisabledNotificationName))
    }

    override fun generateFeedbackDialog(session: MyPastSession){
        feedbackDialog = FeedbackDialog.newInstance(sessionId = session.id, sessionTitle = session.title, feedbackManager = this)
        try {
            fragmentManager?.let {
                feedbackDialog?.showNow(it, "FeedbackDialog")
            }
        } catch (e: Exception) {
            softExceptionCallback(e, "Failed generating feedback dialog. Probably closing context.")
        }
    }

    fun finishedFeedback(sessionId:String, rating:Int, comment: String) {
        feedbackModel.finishedFeedback(sessionId,rating,comment)
    }

    override fun onError(error: FeedbackApi.FeedBackError) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object{
        const val FeedbackDisabledNotificationName = "FeedbackDisabled"
    }
}