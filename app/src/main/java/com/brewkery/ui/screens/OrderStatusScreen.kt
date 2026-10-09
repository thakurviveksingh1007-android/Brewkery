package com.brewkery.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewkery.ui.PlacedOrder
import com.brewkery.ui.theme.BrewOrange
import com.brewkery.ui.theme.DarkBar
import com.brewkery.ui.theme.InkGray
import com.brewkery.ui.theme.LeafGreen
import com.brewkery.ui.theme.PaleBorder
import com.brewkery.ui.theme.PrepPill

@Composable
fun OrderStatusScreen(
    order: PlacedOrder?,
    estimatedTime: String,
    onBackToMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        // Cup illustration in circle
        // Cup illustration: white circle with a thin orange ring, like the design.
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, BrewOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("\u2615", fontSize = 48.sp) }

        Spacer(Modifier.height(14.dp))
        Text(
            "ORDER DISPATCHED",
            color = BrewOrange,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Brewing in Progress!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Your ticket was dispatched to our barista.",
            fontSize = 13.sp,
            color = InkGray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))
        TicketCard(
            ticketId = order?.ticketId ?: "PENDING",
            itemCount = order?.itemCount ?: 0,
            estimatedTime = estimatedTime
        )

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onBackToMenu,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkBar)
        ) {
            Text("Back to Menu", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TicketCard(ticketId: String, itemCount: Int, estimatedTime: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, PaleBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ORDER TICKET", fontSize = 10.sp, color = InkGray)
                    Text(
                        "#$ticketId",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                BlinkingPill(text = "PREPARING")
            }
            Spacer(Modifier.height(10.dp))
            TicketRow(label = "Estimated Wait:", value = estimatedTime, valueOrange = true)
            TicketRow(label = "Items Ordered:", value = "$itemCount Items", valueOrange = false)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Status:", fontSize = 13.sp, color = InkGray)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Barista accepted your order!",
                    fontSize = 13.sp,
                    color = LeafGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Live status pill: gently blinks so the PREPARING state feels alive,
// exactly like the prototype's animated indicator.
@Composable
private fun BlinkingPill(text: String) {
    val transition = rememberInfiniteTransition(label = "prepBlink")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = BrewOrange,
        modifier = Modifier
            .alpha(alpha)
            .clip(RoundedCornerShape(12.dp))
            .background(PrepPill)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun TicketRow(label: String, value: String, valueOrange: Boolean) {    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = InkGray)
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (valueOrange) BrewOrange else MaterialTheme.colorScheme.onSurface
        )
    }
}
