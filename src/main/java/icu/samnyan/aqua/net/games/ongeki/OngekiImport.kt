package icu.samnyan.aqua.net.games.ongeki

import ext.API
import ext.returns
import ext.vars
import icu.samnyan.aqua.net.games.IExportClass
import icu.samnyan.aqua.net.games.ImportClass
import icu.samnyan.aqua.net.games.ImportController
import icu.samnyan.aqua.sega.ongeki.model.*
import icu.samnyan.aqua.sega.ongeki.OngekiRepos
import icu.samnyan.aqua.sega.ongeki.OngekiUserLinked
import icu.samnyan.aqua.sega.ongeki.OngekiUserRepos
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.full.declaredMembers

@RestController
@API("api/v2/game/ongeki")
class OngekiImport(
    val repos: OngekiRepos,
) : ImportController<OngekiDataExport, UserData>(
    "SDDT", "ongeki", OngekiDataExport::class,
    exportFields = OngekiDataExport::class.vars().associateBy {
        it.name.replace("List", "").lowercase()
    },
    exportRepos = OngekiDataExport::class.vars()
        .filter { f -> f.name !in setOf("gameId", "userData") }
        .associateWith { OngekiUserRepos::class.declaredMembers
            .filter { f -> f returns OngekiUserLinked::class }
            .firstOrNull { f -> f.name == it.name
                || f.name == (it.name.substring(4, 5).lowercase() + it.name.substring(5)) // strip user
                || f.name == it.name.replace("List", "")
            }
            ?.call(repos) as OngekiUserLinked<*>? ?: error("No matching field found for ${it.name}")
        },
    artemisRenames = mapOf() // TODO (almost nobody uses this so it's very low priority)
) {
    override fun createEmpty() = OngekiDataExport()
    override val userDataRepo = repos.u.data
}


data class OngekiDataExport(
    override var gameId: String = "SDDT",
    override var userData: UserData,
    var userActivity: List<UserActivity>,
    var userBoss: List<UserBoss>,
    var userCard: List<UserCard>,
    var userChapter: List<UserChapter>,
    var userCharacter: List<UserCharacter>,
    var userDeck: List<UserDeck>,
    var userEventMusic: List<UserEventMusic>,
    var userEventPoint: List<UserEventPoint>,
    var userGeneralData: List<UserGeneralData>,
    var userItem: List<UserItem>,
    var userKop: List<UserKop>,
    var userLoginBonus: List<UserLoginBonus>,
    var userMemoryChapter: List<UserMemoryChapter>,
    var userMissionPoint: List<UserMissionPoint>,
    var userMusicDetail: List<UserMusicDetail>,
    var userMusicItem: List<UserMusicItem>,
    var userOption: UserOption,
    var userPlaylog: List<UserPlaylog>,
    var userRival: List<UserRival>,
    var userScenario: List<UserScenario>,
    var userStory: List<UserStory>,
    var userTechCount: List<UserTechCount>,
    var userTechEvent: List<UserTechEvent>,
    var userTradeItem: List<UserTradeItem>,
    var userTrainingRoom: List<UserTrainingRoom>,
    var userEventMap: List<UserEventMap>,
    var userSkin: List<UserSkin>,
    var userRegions: List<UserRegions>,
    var userGacha: List<UserGacha>,
): IExportClass<UserData> {
    constructor() : this("SDDT", UserData(), ArrayList(), ArrayList(), ArrayList(), ArrayList(),ArrayList(), ArrayList(), ArrayList(), ArrayList(), ArrayList(),ArrayList(), ArrayList(), ArrayList(), ArrayList(), ArrayList(),ArrayList(), ArrayList(), UserOption(), ArrayList(), ArrayList(),ArrayList(), ArrayList(), ArrayList(), ArrayList(), ArrayList(),ArrayList(), ArrayList(), ArrayList(), ArrayList(), ArrayList())
}
