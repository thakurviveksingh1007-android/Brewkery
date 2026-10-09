package com.brewkery.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.brewkery.data.model.MenuItem
import com.brewkery.ui.CartViewModel
import com.brewkery.ui.DetailUiState
import com.brewkery.ui.DetailViewModel
import com.brewkery.ui.theme.BrewOrange
import com.brewkery.ui.theme.InkGray
import com.brewkery.ui.theme.PaleBorder
import com.brewkery.ui.theme.StarAmber

@Composable
fun DetailScreen(
    itemId: Int,
    detailViewModel: DetailViewModel,
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onAddedGoCart: () -> Unit
) {
    val state by detailViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(itemId) { detailViewModel.load(itemId) }

    // Selection state hoisted here so content AND bottom bar share one source.
    var sizeIdx by rememberSaveable { mutableIntStateOf(0) }
    var milkIdx by rememberSaveable { mutableIntStateOf(0) }
    var sugarIdx by rememberSaveable { mutableIntStateOf(0) }
    var qty by rememberSaveable { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(label = "\u2039", onClick = onBack)
                Spacer(Modifier.weight(1f))
                Text("ITEM CUSTOMIZER", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                CircleIconButton(label = "\u2661", onClick = {})
            }
        },
        bottomBar = {
            val s = state
            if (s is DetailUiState.Success) {
                val item = s.item
                val sizes = item.customizations.sizes
                val milks = item.customizations.milkOptions
                val sugars = item.customizations.sugarLevels
                if (sizes.isNotEmpty() && milks.isNotEmpty() && sugars.isNotEmpty()) {
                    val size = sizes.getOrElse(sizeIdx) { sizes.first() }
                    val milk = milks.getOrElse(milkIdx) { milks.first() }
                    val price = (item.basePrice + size.extraPrice + milk.extraPrice) * qty
                    DetailBottomBar(
                        priceText = cartViewModel.money(price),
                        qty = qty,
                        onDec = { if (qty > 1) qty-- },
                        onInc = { qty++ },
                        onAdd = {
                            val sugar = sugars.getOrElse(sugarIdx) { sugars.first() }
                            cartViewModel.addToCart(
                                item, size.label, size.extraPrice,
                                milk.name, milk.extraPrice, sugar, qty
                            )
                            onAddedGoCart()
                        }
                    )
                }
            }
        }
    ) { padding ->
        when (val s = state) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = BrewOrange) }
            }
            is DetailUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Couldn't load this item.", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(s.message)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { detailViewModel.load(itemId) }) { Text("Retry") }
                }
            }
            is DetailUiState.Success -> {
                DetailContent(
                    item = s.item,
                    modifier = Modifier.padding(padding),
                    money = cartViewModel::money,
                    sizeIdx = sizeIdx,
                    milkIdx = milkIdx,
                    sugarIdx = sugarIdx,
                    onSize = { sizeIdx = it },
                    onMilk = { milkIdx = it },
                    onSugar = { sugarIdx = it }
                )
            }
        }
    }
}

@Composable
private fun CircleIconButton(label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PaleBorder)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) { Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    item: MenuItem,
    modifier: Modifier = Modifier,
    money: (Double) -> String,
    sizeIdx: Int,
    milkIdx: Int,
    sugarIdx: Int,
    onSize: (Int) -> Unit,
    onMilk: (Int) -> Unit,
    onSugar: (Int) -> Unit
) {
    val sizes = item.customizations.sizes
    val milks = item.customizations.milkOptions
    val sugars = item.customizations.sugarLevels

    // Defensive guard: malformed API data must never crash the screen.
    if (sizes.isEmpty() || milks.isEmpty() || sugars.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("Customizations unavailable for this item.") }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Hero image with badge overlay
        Box {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                item.badge,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                item.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                money(item.basePrice),
                color = BrewOrange,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Text(
            "\u2605 ${item.rating} (${item.reviewCount}) \u2022 ${item.prepTime} \u2022 ${item.calories} cal",
            color = StarAmber,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            item.description,
            style = MaterialTheme.typography.bodyMedium,
            color = InkGray
        )

        SectionLabel("KEY INGREDIENTS")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item.ingredients.forEach { ingredient ->
                AssistChip(onClick = {}, label = { Text(ingredient, fontSize = 12.sp) })
            }
        }

        SectionLabel("Size Selection")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            sizes.forEachIndexed { index, option ->
                SizeBox(
                    label = option.label,
                    price = if (option.extraPrice > 0)
                        "+${money(option.extraPrice)}" else "+${money(0.0)}",
                    selected = index == sizeIdx,
                    onSelect = { onSize(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SectionLabel("Milk Options / Spreads")
        milks.forEachIndexed { index, option ->
            OptionRow(
                label = option.name,
                price = "+${money(option.extraPrice)}",
                selected = index == milkIdx,
                onSelect = { onMilk(index) }
            )
        }

        SectionLabel("Sugar Levels / Serving")
        Row {
            sugars.forEachIndexed { index, level ->
                SugarTab(
                    label = level,
                    selected = index == sugarIdx,
                    onSelect = { onSugar(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkGray)
}

@Composable
private fun SizeBox(
    label: String,
    price: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelect,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) BrewOrange else PaleBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(price, fontSize = 11.sp, color = InkGray)
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    price: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) BrewOrange else PaleBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(price, fontSize = 12.sp, color = InkGray)
        }
    }
}

@Composable
private fun SugarTab(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onSelect),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (selected) Color.Black else Color.Transparent)
        )
    }
}

@Composable
private fun DetailBottomBar(
    priceText: String,
    qty: Int,
    onDec: () -> Unit,
    onInc: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            TextButton(onClick = onDec) { Text("-") }
            Text("$qty", fontWeight = FontWeight.Bold)
            TextButton(onClick = onInc) { Text("+") }
        }
        Spacer(Modifier.width(10.dp))
        Button(
            onClick = onAdd,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrewOrange)
        ) {
            Text("Add to Cart \u2022 $priceText", fontWeight = FontWeight.Bold)
        }
    }
}
