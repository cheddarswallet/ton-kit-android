package com.tonapps.blockchain.ton

enum class TonSendMode(val value: Int) {
    CARRY_ALL_REMAINING_BALANCE(128),
    CARRY_ALL_REMAINING_INCOMING_VALUE(64),
    DESTROY_ACCOUNT_IF_ZERO(32),
    PAY_GAS_SEPARATELY(1),
    IGNORE_ERRORS(2),
    NONE(0)
}
