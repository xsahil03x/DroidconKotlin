package co.touchlab.sessionize

import co.touchlab.sessionize.platform.backgroundSuspend
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object AboutModel : BaseModel() {
    private val clLogCallback : PlatformCrashlyticsLog by inject()
    fun loadAboutInfo(proc: (aboutInfo: List<AboutInfo>) -> Unit) = launch {
        clLogCallback("loadAboutInfo AboutModel()")
        proc(backgroundSuspend { AboutProc.parseAbout() })
    }
}

internal object AboutProc : KoinComponent{
    private val staticFileLoader : PlatformFileLoader by inject(named("PlatformFileLoader"))
    fun parseAbout(): List<AboutInfo> {
        val aboutJsonString = staticFileLoader("about", "json")!!
        return Json.nonstrict.parse(AboutInfo.serializer().list, aboutJsonString)
    }
}

@Serializable
data class AboutInfo(val icon: String, val title: String, val detail: String)