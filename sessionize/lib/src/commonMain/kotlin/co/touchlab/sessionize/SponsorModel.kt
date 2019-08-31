package co.touchlab.sessionize

import co.touchlab.firebase.firestore.DocumentSnapshot
import co.touchlab.firebase.firestore.collection
import co.touchlab.firebase.firestore.data_
import co.touchlab.firebase.firestore.documents_
import co.touchlab.firebase.firestore.getFirebaseInstance
import co.touchlab.firebase.firestore.get_
import co.touchlab.firebase.firestore.id
import co.touchlab.firebase.firestore.orderBy
import co.touchlab.sessionize.api.AnalyticsApi
import co.touchlab.sessionize.jsondata.Sponsor
import co.touchlab.sessionize.jsondata.SponsorGroup
import org.koin.core.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.get
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object SponsorsModel : BaseModel() , KoinComponent {

    fun loadSponsors(
            proc: (sponsors: List<SponsorGroup>) -> Unit,
            error: (ex: Throwable) -> Unit
    ) {
        getFirebaseInstance()
                .collection("sponsors")
                .orderBy("displayOrder")
                .get_()
                .addListeners({
                    proc(sponsorGroupsFrom(it.documents_))
                },{
                    get<PlatformCrashlyticsException>().invoke(it, "loadSponsorsFromServer failed")
                    error(it)
                })
    }

    private fun sponsorGroupsFrom(documentSnapshots: List<DocumentSnapshot>): List<SponsorGroup> {
        return documentSnapshots.map {
            sponsorGroupFrom(it)
        }
    }

    private fun sponsorGroupFrom(documentSnapshot: DocumentSnapshot): SponsorGroup {
        val groupName = documentSnapshot.id.capitalize()
        val level = documentSnapshot.data_()

        @Suppress("UNCHECKED_CAST")
        val sponsorsList = level?.get("sponsors") as List<Map<String, String>>
        val sponsors = sponsorsList.map {
            Sponsor(
                    name = it["name"] as String,
                    groupName = groupName,
                    url = it["url"] as String,
                    icon = it["icon"] as String,
                    sponsorId = it["sponsorId"]
            )
        }

        return SponsorGroup(groupName, sponsors)
    }
}

fun sponsorClicked(sponsor: Sponsor){
    GlobalContext.get().koin.get<AnalyticsApi>().logEvent("sponsor_clicked", mapOf(Pair("id", sponsor.sponsorId.toString()), Pair("name", sponsor.name)))
}