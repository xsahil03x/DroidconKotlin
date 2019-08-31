package co.touchlab.sessionize

import co.touchlab.sessionize.platform.printThrowable
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.BeforeTest

class StaticFileLoaderTestDarwin : StaticFileLoaderTest() {
    @BeforeTest
    fun androidSetup(){
        startKoin {
            modules(setUp())
            module {
                single<PlatformFileLoader>(named("PlatformFileLoader")) { { name, type ->
//                    loadAsset("$name.$type")
                    TODO()
                } }
                single { { s: String -> Unit } }
                single {{e:Throwable, message:String ->
                    printThrowable(e)
                }}
            }
        }
    }
}

class EventModelTestJVM: EventModelTest()