package com.brewkery.data.cart

import com.brewkery.data.model.MenuItem
import java.util.UUID

/**
 * One line in the cart: an item + the exact customization the user picked.
 * Same item with a different size/milk/sugar counts as a separate line.
 */
data class CartLine(
    val item: MenuItem,
    val sizeLabel: String,
    val sizeExtra: Double,
    val milkName: String,
    val milkExtra: Double,
    val sugar: String,
    val quantity: Int,
    val lineId: String = UUID.randomUUID().toString()
)

/**
 * Pure billing math (no Android dependency, so it is unit-testable).
 * Rule from the prototype: 8% tax applies on SUBTOTAL ONLY, not on delivery fee.
 * Prototype check: subtotal $9.40 x 8% = $0.752 -> $0.75 shown. Correct.
 */
object CartCalculator {

    fun lineTotal(line: CartLine): Double =
        (line.item.basePrice + line.sizeExtra + line.milkExtra) * line.quantity

    fun subtotal(lines: List<CartLine>): Double =
        lines.sumOf { lineTotal(it) }

    fun tax(subtotal: Double, taxRatePercent: Double): Double =
        subtotal * taxRatePercent / 100.0

    fun total(subtotal: Double, deliveryFee: Double, tax: Double): Double =
        subtotal + deliveryFee + tax

    fun format(amount: Double, symbol: String = "$"): String =
        symbol + String.format("%.2f", amount)
}

/** Simple in-memory cart. No database needed: cart resets when the app closes. */
class CartManager {

    private val lines = mutableListOf<CartLine>()

    fun snapshot(): List<CartLine> = lines.toList()

    fun add(line: CartLine) {
        val index = lines.indexOfFirst {
            it.item.id == line.item.id &&
                it.sizeLabel == line.sizeLabel &&
                it.milkName == line.milkName &&
                it.sugar == line.sugar
        }
        if (index == -1) {
            lines.add(line)
        } else {
            // Replace with a NEW object (never mutate in place): StateFlow only
            // emits when the list content actually differs by equality.
            val existing = lines[index]
            lines[index] = existing.copy(quantity = existing.quantity + line.quantity)
        }
    }

    fun setQuantity(lineId: String, quantity: Int) {
        val index = lines.indexOfFirst { it.lineId == lineId }
        if (index == -1) return
        if (quantity <= 0) {
            lines.removeAt(index)
        } else {
            lines[index] = lines[index].copy(quantity = quantity)
        }
    }

    fun remove(lineId: String) {
        lines.removeAll { it.lineId == lineId }
    }

    fun clear() {
        lines.clear()
    }

    fun itemCount(): Int = lines.sumOf { it.quantity }
}
