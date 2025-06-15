package io.cheddarswallet.tonkit.api

import io.cheddarswallet.tonkit.Address
import kotlinx.coroutines.flow.Flow

interface IApiListener {
    val transactionFlow: Flow<String>

    fun start(address: Address)
    fun stop()

}
