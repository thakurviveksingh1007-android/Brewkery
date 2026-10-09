package com.brewkery

import com.brewkery.data.cart.CartCalculator
import com.brewkery.data.cart.CartLine
import com.brewkery.data.model.Customizations
import com.brewkery.data.model.MenuItem
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the cart billing math.
 * Pure JVM tests (no Android framework needed) - run with:
 * ./gradlew testDebugUnitTest
 */
class CartCalculatorTest {

    private fun fakeItem(basePrice: Double): MenuItem = MenuItem(
        id = 1,
        categoryId = "cat_hot_coffee",
        name = "Test Coffee",
        tagline = "",
        description = "",
        basePrice = basePrice,
        rating = 4.9,
        reviewCount = 10,
        prepTime = "",
        calories = 0,
        imageUrl = "",
        badge = "",
        ingredients = emptyList(),
        customizations = Customizations(
            sizes = emptyList(),
            sugarLevels = emptyList(),
            milkOptions = emptyList()
        )
    )

    private fun line(
        base: Double = 4.85,
        sizeExtra: Double = 0.65,
        milkExtra: Double = 0.50,
        qty: Int = 1
    ): CartLine = CartLine(
        item = fakeItem(base),
        sizeLabel = "Grande",
        sizeExtra = sizeExtra,
        milkName = "Almond",
        milkExtra = milkExtra,
        sugar = "Standard",
        quantity = qty
    )

    @Test
    fun lineTotal_addsExtras_andMultipliesByQuantity() {
        // (4.85 + 0.65 + 0.50) x 2 = 12.00
        assertEquals(12.00, CartCalculator.lineTotal(line(qty = 2)), 0.001)
    }

    @Test
    fun tax_appliesOnSubtotalOnly_notOnDeliveryFee() {
        // Prototype proof: subtotal $9.40 x 8% = $0.752 -> $0.75 shown in design.
        // If tax wrongly included the $2.50 delivery fee, it would be $0.95.
        assertEquals(0.75, CartCalculator.tax(9.40, 8.0), 0.01)
    }

    @Test
    fun total_matchesPrototypeBill() {
        // Prototype bill: $9.40 + $2.50 + $0.75 = $12.65
        val total = CartCalculator.total(
            subtotal = 9.40,
            deliveryFee = 2.50,
            tax = 0.75
        )
        assertEquals(12.65, total, 0.001)
    }

    @Test
    fun format_alwaysShowsTwoDecimals() {
        assertEquals("$12.65", CartCalculator.format(12.65))
        assertEquals("$2.50", CartCalculator.format(2.5))
    }
}
