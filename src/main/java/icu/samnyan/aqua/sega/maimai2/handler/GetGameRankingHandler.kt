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
        try {
            // Get the play count of each music in the last N days (or all time if sparse)
            val queryAfter = LocalDateTime.now().minusDays(LOOK_BACK_DAYS)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val queryAfterStr = queryAfter.format(formatter)

            var results = em.createQuery(
                "SELECT p.musicId, count(p.id) FROM Mai2UserPlaylog p WHERE p.userPlayDate >= :queryAfter GROUP BY p.musicId ORDER BY count(p.id) DESC",
                Array<Any>::class.java
            )
                .setParameter("queryAfter", queryAfterStr)
                .setMaxResults(QUERY_LIMIT.toInt())
                .resultList

            if (results.isEmpty()) {
                results = em.createQuery(
                    "SELECT p.musicId, count(p.id) FROM Mai2UserPlaylog p GROUP BY p.musicId ORDER BY count(p.id) DESC",
                    Array<Any>::class.java
                )
                    .setMaxResults(QUERY_LIMIT.toInt())
                    .resultList
            }

            musicRankingCache = results.map { row ->
                MusicRankingItem(row[0].int, row[1].long)
            }

            log.info("Refreshed music ranking cache: ${musicRankingCache.size} items")
        } catch (e: Exception) {
            log.error("Failed to refresh music ranking cache", e)
        }
    }

    override fun handle(request: Map<String, Any>): Any {
        val type = (request["type"] as? Number)?.toInt() ?: 1
        val opts = TokenChecker.getCurrentSession()?.user?.gameOptions
        val rankingList = if (opts?.enableMusicRank == false) {
            emptyList()
        } else if (musicRankingCache.isNotEmpty()) {
            musicRankingCache.map { mapOf("id" to it.musicId, "point" to it.weight, "userName" to "") }
        } else {
            DEFAULT_RANKINGS.mapIndexed { idx, mid ->
                mapOf("id" to mid, "point" to (1000L - idx * 50), "userName" to "")
            }
        }
        return mapOf(
            "type" to type,
            "gameRankingList" to rankingList
        )
    }

    companion object {
        val log = logger()
        
        const val LOOK_BACK_DAYS: Long = 30
        const val QUERY_LIMIT: Long = 50

        val DEFAULT_RANKINGS = listOf(
            11760, // 勇者 (YOASOBI)
            11353, // グッバイ宣言 (Chinozo)
            11369, // 廻廻奇譚 (Eve)
            11568, // INTERNET OVERDOSE
            125,   // 千本桜
            853,   // 前前前世
            227,   // Garakuta Doll Play
            834,   // PANDORA PARADOXXX
            799,   // QZKago Requiem
            750    // 初音ミクの激唱
        )
    }
}
