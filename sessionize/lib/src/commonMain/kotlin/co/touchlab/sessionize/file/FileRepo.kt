package co.touchlab.sessionize.file

import co.touchlab.sessionize.PlatformFileLoader
import co.touchlab.sessionize.db.SessionizeDbHelper
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object FileRepo : KoinComponent{
    val staticFileLoader: PlatformFileLoader by inject(named("PlatformFileLoader"))

    fun seedFileLoad() {

        val speakerJson = staticFileLoader("speakers", "json")
        val scheduleJson = staticFileLoader("schedule", "json")
        val sponsorSessionJson = staticFileLoader("sponsor_session", "json")

        if (speakerJson != null && scheduleJson != null && sponsorSessionJson != null) {
            SessionizeDbHelper.primeAll(
                    speakerJson,
                    scheduleJson,
                    sponsorSessionJson
            )
        } else {
            //This should only ever happen in dev
            throw NullPointerException("Couldn't load static files")
        }
    }
}