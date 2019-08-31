package co.touchlab.sessionize


import co.touchlab.droidcon.db.DroidconDb
import co.touchlab.sessionize.api.AnalyticsApi
import co.touchlab.sessionize.api.NotificationsApi
import co.touchlab.sessionize.api.SessionizeApi
import co.touchlab.sessionize.api.SessionizeApiImpl
import co.touchlab.sessionize.mocks.NotificationsApiMock
import co.touchlab.sessionize.platform.Concurrent
import co.touchlab.sessionize.platform.MainConcurrent
import co.touchlab.sessionize.platform.TestConcurrent
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

abstract class StaticFileLoaderTest : KoinComponent {

    private val staticFileLoader : PlatformFileLoader by inject(named("PlatformFileLoader"))

    internal fun setUp(): Module = module {
        single<SqlDriver> { testDbConnection() }
        single<CoroutineDispatcher> { Dispatchers.Main }
        single { TestSettings() }
        single<AnalyticsApi> { AnalyticsApiMock() }
        single(named("timeZone")){"-0400"}
        single<NotificationsApi> { NotificationsApiMock() }
    }

    @AfterTest
    fun tearDown() {
    }

    @Test
    fun testAbout() {
        val about = staticFileLoader("about", "json")
        about?.let {

            val aboutJson = Json.nonstrict.parseJson(it).jsonArray
            assertNotEquals(aboutJson.size, 0, "empty about.json or none found")
            assertTrue(aboutJson[0].jsonObject.containsKey("icon"))
            assertTrue(aboutJson[0].jsonObject.containsKey("title"))
            assertTrue(aboutJson[0].jsonObject.containsKey("detail"))

        }
    }

    @Test
    fun testSchedule() {
        val schedule = staticFileLoader("schedule", "json")
        schedule?.let {
            val scheduleJson = Json.nonstrict.parseJson(it).jsonArray
        assertNotEquals(scheduleJson.size, 0, "empty schedule.json or none found")
        assertTrue(scheduleJson[0].jsonObject.containsKey("date"))
        assertTrue(scheduleJson[0].jsonObject.containsKey("rooms"))
        }
    }

    @Test
    fun testSpeakers() {
        val speakers = staticFileLoader("speakers", "json")
        speakers?.let {
            val speakersJson = Json.nonstrict.parseJson(it).jsonArray
            assertNotEquals(speakersJson.size, 0, "empty speakers.json or none found")
            assertTrue(speakersJson[0].jsonObject.containsKey("id"))
            assertTrue(speakersJson[0].jsonObject.containsKey("firstName"))
            assertTrue(speakersJson[0].jsonObject.containsKey("lastName"))
        }
    }

}

