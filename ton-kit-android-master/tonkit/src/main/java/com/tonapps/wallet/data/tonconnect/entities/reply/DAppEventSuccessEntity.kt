package com.tonapps.wallet.data.tonconnect.entities.reply

import org.json.JSONObject

data class DAppEventSuccessEntity(
    val event: String = "connect",
    val id: Long = System.currentTimeMillis(),
    val payload: DAppReplyPayloadEntity,
): DAppReply() {

    constructor(items: List<DAppReply>, appName: String, appVersion: String, maxMessages: Int): this(payload = DAppReplyPayloadEntity(
        items,
        DAppDeviceEntity(appName = appName, appVersion = appVersion, maxMessages = maxMessages)
    ))

    override fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("event", event)
        json.put("id", id)
        json.put("payload", payload.toJSON())
        return json
    }

}
