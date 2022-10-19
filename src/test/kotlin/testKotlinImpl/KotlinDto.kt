package testKotlinImpl

import sqlObjectMapper.Column

data class Order(
    @Column
    val itemId: Long
)