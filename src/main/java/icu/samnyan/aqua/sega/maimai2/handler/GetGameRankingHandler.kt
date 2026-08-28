package icu.samnyan.aqua.sega.maimai2.handler

import ext.int
import ext.logger
import ext.long
import ext.thread
import icu.samnyan.aqua.sega.allnet.TokenChecker
import icu.samnyan.aqua.sega.general.BaseHandler
import jakarta.persistence.EntityManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.Volatile

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Component("Maimai2GetGameRankingHandler")
class GetGameRankingHandler(
    private val em: EntityManager
) : BaseHandler {
    private data class MusicRankingItem(val musicId: Int, val weight: Long)

    @Volatile
    private var musicRankingCache: List<MusicRankingItem> = emptyList()

    init {
        // To make sure the cache is initialized before the first request,
        // not using `initialDelay = 0` in `@Scheduled`.
        thread { refreshMusicRankingCache() }
    }

    @Scheduled(fixedDelay = 3600_000)
    private fun refreshMusicRankingCache() {
        // Get the play count of each music in the last N days
        val queryAfter = LocalDateTime.now().minusDays(LOOK_BACK_DAYS)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val queryAfterStr = queryAfter.format(formatter)

        val results = em.createQuery(
            "SELECT p.musicId, count(distinct p.user.id) FROM Mai2UserPlaylog p WHERE p.userPlayDate >= :queryAfter GROUP BY p.musicId ORDER BY count(distinct p.user.id) DESC",
            Array<Any>::class.java
        )
            .setParameter("queryAfter", queryAfterStr)
            .setMaxResults(QUERY_LIMIT.toInt())
            .resultList

        musicRankingCache = results.map { row ->
            MusicRankingItem(row[0].int, row[1].long)
        }

        log.info("Refreshed music ranking cache: ${musicRankingCache.size} items")
    }

    override fun handle(request: Map<String, Any>): Any = mapOf(
        "type" to request["type"],
        "gameRankingList" to when(request["type"]) {
            1 -> {
                val opts = TokenChecker.getCurrentSession()?.user?.gameOptions
                // If is null or true, return the ranking list
                if (opts?.enableMusicRank == false)
                    emptyList()
                else
                    musicRankingCache.map { mapOf("id" to it.musicId, "point" to it.weight, "userName" to "") }
            }
            else -> emptyList()
        }
    )

    companion object {
        val log = logger()
        
        const val LOOK_BACK_DAYS: Long = 7
        const val QUERY_LIMIT: Long = 50
    }
}
