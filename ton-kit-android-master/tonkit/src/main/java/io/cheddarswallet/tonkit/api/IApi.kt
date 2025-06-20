package io.cheddarswallet.tonkit.api

import io.cheddarswallet.tonkit.Address
import io.cheddarswallet.tonkit.models.Account
import io.cheddarswallet.tonkit.models.Event
import io.cheddarswallet.tonkit.models.Jetton
import io.cheddarswallet.tonkit.models.JettonBalance
import io.tonapi.models.EmulateMessageToWalletRequestParamsInner
import java.math.BigInteger

interface IApi {
    suspend fun getAccount(address: Address): Account
    suspend fun getAccountJettonBalances(address: Address): List<JettonBalance>
    suspend fun getEvents(
        address: Address,
        beforeLt: Long?,
        startTimestamp: Long?,
        limit: Int,
    ): List<Event>
    suspend fun getAccountSeqno(address: Address): Int
    suspend fun getJettonInfo(address: Address): Jetton
    suspend fun getRawTime(): Int
    suspend fun estimateFee(boc: String, params: List<EmulateMessageToWalletRequestParamsInner>?): BigInteger
    suspend fun send(boc: String)
    suspend fun getAccountSeqno(address: String): Int
}
