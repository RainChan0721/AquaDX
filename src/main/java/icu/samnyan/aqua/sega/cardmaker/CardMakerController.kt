package icu.samnyan.aqua.sega.cardmaker

import ext.API
import ext.logger
import ext.long
import ext.parsing
import icu.samnyan.aqua.sega.allnet.TokenChecker
import icu.samnyan.aqua.sega.util.BasicMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@RestController
@RequestMapping(path = [
    "/g/SDED/{version}/CardMakerServlet", "/g/SDED/{version}/CardMakerServlet/",
    "/g/SDED/{version}", "/g/SDED/{version}/",
    "/CardMakerServlet", "/CardMakerServlet/"
])
class CardMakerController(
    val mapper: BasicMapper,
    val allNetProps: icu.samnyan.aqua.sega.allnet.AllNetProps,
    @param:Value("\${server.port:}") val SERVER_PORT: String
) {
    val logger = logger()

    @API("GetGameSettingApi", "GetGameSetting")
    fun getGameSetting(@ModelAttribute request: MutableMap<String, Any>): Any? {
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
        val rebootStartTime = LocalDateTime.now().minusHours(3)
        val rebootEndTime = LocalDateTime.now().minusHours(2)

        val gameSetting = mapOf(
            "dataVersion" to "1.35.0",
            "ongekiCmVersion" to "1.32.0",
            "chuniCmVersion" to "1.30.0",
            "maimaiCmVersion" to "1.45.0",
            "isMaintenance" to false,
            "requestInterval" to 1,
            "rebootStartTime" to rebootStartTime.format(formatter),
            "rebootEndTime" to rebootEndTime.format(formatter),
            "isBackgroundDistribute" to false,
            "maxCountCharacter" to 300,
            "maxCountItem" to 300,
            "maxCountCard" to 300,
            "watermark" to false
        )

        val json = mapper.write(mapOf(
            "gameSetting" to gameSetting,
            "isDumpUpload" to false,
            "isAou" to false
        ))

        logger.info("Response: {}", json)
        return json
    }

    fun gameConnect(modelKind: Int, type: Int, titleUri: String) =
    mapOf("modelKind" to modelKind, "type" to type, "titleUri" to titleUri)

    @API("GetGameConnectApi", "GetGameConnect")
    fun getGameConnect(@ModelAttribute request: MutableMap<String, Any>): Any? {
        val version = request["version"]?.toString() ?: "1.30.00"
        val session = TokenChecker.getCurrentSession()

        val host = allNetProps.host.ifBlank { null } ?:
        try { InetAddress.getLocalHost().hostAddress }
        catch (_: UnknownHostException) { "localhost" }
        val port = allNetProps.port?.toString() ?: SERVER_PORT
        val protocol = if (allNetProps.tls) "https" else "http"
        val hostPort = if (allNetProps.hidePort || port.isBlank()) host else "$host:$port"

        val base = if (session == null) "/g" else "/gs/" + session.token
        val json = mapper.write(mapOf(
            "length" to 3,
            "gameConnectList" to listOf(
                gameConnect(0, 1, "$protocol://$hostPort$base/SDHD/$version/"),
                gameConnect(1, 1, "$protocol://$hostPort$base/SDEZ/$version/"),
                gameConnect(2, 1, "$protocol://$hostPort$base/SDDT/$version/")
            )
        ))

        logger.info("Response: $json")
        return json
    }

    @API("GetClientBookkeepingApi", "GetClientBookkeeping")
    fun getClientBookkeeping(@ModelAttribute request: MutableMap<String, Any>): Any? {
        val placeId = request["placeId"]?.let { parsing { it.long } } ?: 0L
        val json = mapper.write(mapOf(
            "placeId" to placeId,
            "length" to 0,
            "clientBookkeepingList" to mutableListOf<Any>()
        ))

        logger.info("Response: $json")
        return json
    }

    @API("UpsertClientBookkeepingApi", "UpsertClientBookkeeping")
    fun upsertClientBookkeeping() = "{\"returnCode\":1,\"apiName\":\"UpsertClientBookkeepingApi\"}"

    @API("UpsertClientSettingApi", "UpsertClientSetting")
    fun upsertClientSetting() = "{\"returnCode\":1,\"apiName\":\"UpsertClientSettingApi\"}"

    @API("UpsertClientTestmodeApi", "UpsertClientTestmode")
    fun upsertClientTestmode() = "{\"returnCode\":1,\"apiName\":\"UpsertClientTestmodeApi\"}"

    @API("GetGameTitleStopApi", "GetGameTitleStop")
    fun getGameTitleStop(@ModelAttribute request: MutableMap<String, Any>): Any? {
        val json = mapper.write(mapOf(
            "length" to 0,
            "gameTitleStopList" to emptyList<Any>()
        ))
        logger.info("Response: $json")
        return json
    }

    @API("Ping")
    fun ping() = "{\"returnCode\":1,\"apiName\":\"Ping\"}"
}
