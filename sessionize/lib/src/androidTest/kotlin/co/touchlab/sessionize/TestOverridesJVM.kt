package co.touchlab.sessionize

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.touchlab.sessionize.platform.AndroidAppContext
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.BeforeTest

@RunWith(AndroidJUnit4::class)
class StaticFileLoaderTestJVM : StaticFileLoaderTest() {
    @BeforeTest
    fun androidSetup() {

        startKoin {
            modules(setUp())
            module {
                single<PlatformFileLoader>(named("PlatformFileLoader")) { { name, type ->
                    loadAsset("$name.$type")
                } }
                single { { s: String -> Unit } }
                single {{e:Throwable, message:String ->
                    Log.e("StaticFileLoaderTest", message, e)
                }}
            }
        }

        AndroidAppContext.app = ApplicationProvider.getApplicationContext()
    }
}

@RunWith(AndroidJUnit4::class)
class EventModelTestJVM : EventModelTest()

private fun loadAsset(name: String) = AndroidAppContext.app.assets
        .open(name, Context.MODE_PRIVATE)
        .bufferedReader()
        .use { it.readText() }

@RunWith(AndroidJUnit4::class)
class SettingsModelTestJVM : SettingsModelTest()