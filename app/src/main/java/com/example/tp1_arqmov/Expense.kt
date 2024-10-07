package com.example.tp1_arqmov

import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val category: Category,
    val amount: Double
) {
    constructor() : this(UUID.randomUUID().toString(), "", Category.FOOD, 0.0)

}