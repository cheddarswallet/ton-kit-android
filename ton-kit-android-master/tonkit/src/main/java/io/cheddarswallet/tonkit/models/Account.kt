package io.cheddarswallet.tonkit.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.cheddarswallet.tonkit.Address

@Entity
data class Account(
    @PrimaryKey
    val address: Address,
    val balance: Long,
    val status: AccountStatus,
)
