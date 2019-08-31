package co.touchlab.sessionize

import co.touchlab.sessionize.api.AnalyticsApi
import co.touchlab.sessionize.api.NotificationsApi
import co.touchlab.sessionize.api.SessionizeApi
import co.touchlab.sessionize.db.DateAdapter
import co.touchlab.sessionize.db.SessionizeDbHelper
import co.touchlab.sessionize.mocks.FeedbackApiMock
import co.touchlab.sessionize.mocks.NotificationsApiMock
import co.touchlab.sessionize.platform.Concurrent
import co.touchlab.sessionize.platform.TestConcurrent
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

abstract class EventModelTest:KoinComponent {
    private val sessionizeApiMock = SessionizeApiMock()
    private val analyticsApiMock = AnalyticsApiMock()
    private val notificationsApiMock = NotificationsApiMock()
    private val feedbackApiMock = FeedbackApiMock()

    private val timeZone = "-0800"

    @BeforeTest
    fun setup() {
        val commonModule = module {
            threadLocal<SessionizeApi> { sessionizeApiMock }
            single<Concurrent> { TestConcurrent }
        }

        val platformModule = module {
            single<AnalyticsApi> { analyticsApiMock }
            single<SqlDriver> { testDbConnection() }
            single<CoroutineDispatcher> { Dispatchers.Main }
            single { TestSettings() }
            single(named("timeZone")){
                timeZone
            }
            single<NotificationsApi> { notificationsApiMock }
            single<PlatformFileLoader>(named("PlatformFileLoader")) { { filePrefix, fileType ->
                when (filePrefix) {
                    "sponsors" -> SPONSORS
                    "speakers" -> SPEAKERS
                    "schedule" -> SCHEDULE
                    else -> SCHEDULE
                }
            } }
            single { { s: String -> Unit } }
            single {{ e:Throwable, message:String -> println(message)}}

        }

        startKoin {
            modules(listOf(commonModule, platformModule))
        }

        AppContext.initAppContext()
    }

    @Test
    fun testRsvpAndAnalytics() = runTest {
        val eventModel = EventModel("67316")
        val sessions = SessionizeDbHelper.sessionQueries.allSessions().executeAsList()
        if(sessions.isNotEmpty()) {
            val session = sessions.first()
            val si = collectSessionInfo(session)
            eventModel.toggleRsvpSuspend(si)
            assertTrue { sessionizeApiMock.rsvpCalled }
            assertTrue { analyticsApiMock.logCalled }
            assertTrue { notificationsApiMock.reminderCalled }
        }
    }

    @Test
    fun testFeedbackModel() = runTest {
        val fbModel = feedbackApiMock.getFeedbackModel()
        fbModel.showFeedbackForPastSessions(feedbackApiMock)

        assertTrue { feedbackApiMock.feedbackError != null }
    }

    @Test
    fun testPSTTimeZoneCorrect(){
        val timeStr = "2019-04-12T08:00:00"
        val correctMillis = 1555084800000

        val timeStrWithZone = timeStr + timeZone

        val dateAdapter = DateAdapter()
        val timeDate = dateAdapter.decode(timeStrWithZone)
        val newTimeStr = dateAdapter.encode(timeDate)

        assertTrue { newTimeStr == timeStr }
        //assertTrue { timeDate.toLongMillis() == correctMillis }
    }
}

class AnalyticsApiMock : AnalyticsApi {
    var logCalled = false

    override fun logEvent(name: String, params: Map<String, Any>) {
        logCalled = true
    }
}

class SessionizeApiMock : SessionizeApi {
    override suspend fun sendFeedback(sessionId: String, rating: Int, comment: String?): Boolean {
        return true
    }

    var rsvpCalled = false
    override suspend fun getSpeakersJson(): String {
        return ""
    }

    override suspend fun getSessionsJson(): String {
        return ""
    }

    override suspend fun getSponsorJson(): String {
        return ""
    }

    override suspend fun getSponsorSessionJson(): String {
        return ""
    }

    override suspend fun recordRsvp(methodName: String, sessionId: String): Boolean {
        rsvpCalled = true
        return true
    }
}