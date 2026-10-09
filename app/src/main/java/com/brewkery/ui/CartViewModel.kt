package com.brewkery.ui

import androidx.lifecycle.ViewModel
import com.brewkery.data.cart.CartCalculator
import com.brewkery.data.cart.CartLine
import com.brewkery.data.cart.CartManager
import com.brewkery.data.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

data class PlacedOrder(
    val ticketId: String,
    val itemCount: Int
)

/**
 * Activity-scoped cart holder: shared by Menu, Detail, Cart and Status screens.
 * Also carries shop billing config once the menu loads (fee, tax %, ETA, symbol).
 */
class CartViewModel : ViewModel() {

    private val manager = CartManager()

    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines

    private val _itemCount = MutableStateFlow(0)
    val itemCount: StateFlow<Int> = _itemCount

    private val _subtotal = MutableStateFlow(0.0)
    val subtotal: StateFlow<Double> = _subtotal

    private val _lastOrder = MutableStateFlow<PlacedOrder?>(null)
    val lastOrder: StateFlow<PlacedOrder?> = _lastOrder

    var deliveryFee: Double = 2.50
        private set
    var taxRatePercent: Double = 8.0
        private set
    var estimatedTime: String = "20 - 30 mins"
        private set
    var currencySymbol: String = "$"
        private set

    fun setShopConfig(fee: Double, taxRate: Double, eta: String, symbol: String) {
        deliveryFee = fee
        taxRatePercent = taxRate
        estimatedTime = eta
        currencySymbol = symbol
    }

    fun addToCart(
        item: MenuItem,
        sizeLabel: String,
        sizeExtra: Double,
        milkName: String,
        milkExtra: Double,
        sugar: String,
        quantity: Int
    ) {
        manager.add(
            CartLine(item, sizeLabel, sizeExtra, milkName, milkExtra, sugar, quantity)
        )
        refresh()
    }

    fun changeQuantity(line: CartLine, quantity: Int) {
        manager.setQuantity(line.lineId, quantity)
        refresh()
    }

    fun removeLine(line: CartLine) {
        manager.remove(line.lineId)
        refresh()
    }

    fun clearCart() {
        manager.clear()
        refresh()
    }

    fun tax(): Double = CartCalculator.tax(_subtotal.value, taxRatePercent)

    fun total(): Double =
        CartCalculator.total(_subtotal.value, deliveryFee, tax())

    fun money(amount: Double): String =
        CartCalculator.format(amount, currencySymbol)

    /** Places the order: generates ticket, snapshots count, clears cart. */
    fun placeOrder(): PlacedOrder {
        val ticket = "BK-" + Random.nextInt(10000, 99999).toString()
        val order = PlacedOrder(ticket, _itemCount.value)
        manager.clear()
        refresh()
        _lastOrder.value = order
        return order
    }

    private fun refresh() {
        _lines.value = manager.snapshot()
        _itemCount.value = manager.itemCount()
        _subtotal.value = CartCalculator.subtotal(_lines.value)
    }
}
