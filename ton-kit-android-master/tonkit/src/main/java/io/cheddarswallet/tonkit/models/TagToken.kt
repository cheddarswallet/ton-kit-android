package io.cheddarswallet.tonkit.models

import io.cheddarswallet.tonkit.Address

data class TagToken(
    val platform: Tag.Platform,
    val jettonAddress: Address?,
)
