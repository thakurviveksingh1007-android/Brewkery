package com.brewkery.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.brewkery.data.cart.CartCalculator
import com.brewkery.data.cart.CartLine
import com.brewkery.ui.CartViewModel
import com.brewkery.ui.theme.BrewOrange
import com.brewkery.ui.theme.CoffeeBrown
import com.brewkery.ui.theme.CreamChip
import com.brewkery.ui.theme.InkGray
import com.brewkery.ui.theme.PaleBorder
import com.brewkery.ui.theme.PriceRed

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val lines by cartViewModel.lines.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleBackButton(onClick = onBack)
                Spacer(Modifier.weight(1f))
                Text("YOUR CART", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                if (lines.isNotEmpty()) {
                    Text(
                        "Clear Cart",
                        color = PriceRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { cartViewModel.clearCart() }
                    )
                } else {
                    Spacer(Modifier.width(64.dp))
                }
            }
        }
    ) { padding ->
        if (lines.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                // Empty-state card exactly like the prototype.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, PaleBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(CreamChip),
                            contentAlignment = Alignment.Center
                        ) { Text("\uD83D\uDED2", fontSize = 28.sp) }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Your in-memory cart is empty.",
                            fontSize = 13.sp,
                            color = InkGray
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                BillCard(
                    cartViewModel = cartViewModel,
                    onOrderPlaced = onOrderPlaced,
                    enabled = false
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(Modifier.height(2.dp)) }
                    items(lines, key = { it.lineId }) { line ->
                        CartLineCard(
                            line = line,
                            currency = { amount -> cartViewModel.money(amount) },
                            onInc = { cartViewModel.changeQuantity(line, line.quantity + 1) },
                            onDec = { cartViewModel.changeQuantity(line, line.quantity - 1) }
                        )
                    }
                }
                BillCard(
                    cartViewModel = cartViewModel,
                    onOrderPlaced = onOrderPlaced,
                    enabled = true
                )
            }
        }
    }
}

@Composable
private fun CircleBackButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PaleBorder)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) { Text("\u2039", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    currency: (Double) -> String,
    onInc: () -> Unit,
    onDec: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PaleBorder)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = line.item.imageUrl,
                contentDescription = line.item.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(line.item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "${line.sizeLabel} \u2022 ${line.milkName}",
                    fontSize = 12.sp,
                    color = InkGray
                )
                Text(
                    currency(CartCalculator.lineTotal(line)),
                    color = PriceRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            // Qty stepper pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF6EFE6))
                    .padding(horizontal = 2.dp)
            ) {
                TextButton(onClick = onDec) { Text("-", fontWeight = FontWeight.Bold) }
                Text("${line.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                TextButton(onClick = onInc) { Text("+", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun BillCard(
    cartViewModel: CartViewModel,
    onOrderPlaced: () -> Unit,
    enabled: Boolean
) {
    val liveSubtotal by cartViewModel.subtotal.collectAsStateWithLifecycle()
    // Prototype shows the fee row statically but $0.00 totals on empty cart.
    val subtotal = if (enabled) liveSubtotal else 0.0
    val tax = if (enabled) cartViewModel.tax() else 0.0
    val total = if (enabled) cartViewModel.total() else 0.0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, PaleBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            BillRow("Subtotal", cartViewModel.money(subtotal))
            BillRow("Delivery Fee", cartViewModel.money(cartViewModel.deliveryFee))
            BillRow(
                "Est. Tax (${cartViewModel.taxRatePercent}%)",
                cartViewModel.money(tax)
            )
            Spacer(Modifier.height(8.dp))
            DashedDivider()
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Payable", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    cartViewModel.money(total),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = BrewOrange
                )
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (enabled) {
                            Brush.horizontalGradient(listOf(BrewOrange, CoffeeBrown))
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    BrewOrange.copy(alpha = 0.45f),
                                    CoffeeBrown.copy(alpha = 0.45f)
                                )
                            )
                        }
                    )
                    .clickable(enabled = enabled) {
                        cartViewModel.placeOrder()
                        onOrderPlaced()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "\uD83D\uDEF5 Place Order Now \u2022 ${cartViewModel.money(total)}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BillRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = InkGray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DashedDivider() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = PaleBorder,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        )
    }
}
