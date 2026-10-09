package com.brewkery.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.brewkery.data.cart.CartCalculator
import com.brewkery.data.model.MenuItem
import com.brewkery.ui.CartViewModel
import com.brewkery.ui.MenuUiState
import com.brewkery.ui.MenuViewModel
import com.brewkery.ui.theme.BrewOrange
import com.brewkery.ui.theme.CreamChip
import com.brewkery.ui.theme.DarkBar
import com.brewkery.ui.theme.InkGray
import com.brewkery.ui.theme.LeafGreen
import com.brewkery.ui.theme.PaleBorder
import com.brewkery.ui.theme.StarAmber

@Composable
fun MenuScreen(
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    cartViewModel: CartViewModel,
    menuViewModel: MenuViewModel = viewModel()
) {
    val state by menuViewModel.state.collectAsStateWithLifecycle()
    val cartCount by cartViewModel.itemCount.collectAsStateWithLifecycle()
    val cartSubtotal by cartViewModel.subtotal.collectAsStateWithLifecycle()
    val lastOrder by cartViewModel.lastOrder.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            MenuHeader(cartCount = cartCount, onCartClick = onCartClick)
        },
        bottomBar = {
            if (cartCount > 0) {
                DarkCartBar(
                    total = cartViewModel.money(cartSubtotal),
                    onClick = onCartClick
                )
            }
        }
    ) { padding ->
        when (val s = state) {
            is MenuUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = BrewOrange) }
            }
            is MenuUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Couldn't load the menu.", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(s.message)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { menuViewModel.load() }) { Text("Retry") }
                }
            }
            is MenuUiState.Success -> {
                LaunchedEffect(s.data) {
                    val m = s.data.meta
                    cartViewModel.setShopConfig(
                        m.deliveryFee, m.taxRatePercent,
                        m.estimatedDeliveryTime, m.currencySymbol
                    )
                }
                // Filtered list computed from the SAME state object that drives
                // chip highlight: the two can never disagree with each other.
                // Search query narrows by item name/tagline on top of category.
                val byCategory = if (s.selectedCategoryId == "ALL") {
                    s.data.items
                } else {
                    s.data.items.filter { it.categoryId == s.selectedCategoryId }
                }
                val visible = if (query.isBlank()) {
                    byCategory
                } else {
                    byCategory.filter {
                        it.name.contains(query, ignoreCase = true) ||
                            it.tagline.contains(query, ignoreCase = true)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    StoreInfoCard(
                        tagline = s.data.meta.tagline,
                        eta = s.data.meta.estimatedDeliveryTime,
                        fee = cartViewModel.money(s.data.meta.deliveryFee)
                    )

                    if (lastOrder != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "\uD83D\uDECD Order ${lastOrder!!.ticketId} \u2022 PREPARING",
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    SearchBar(query = query, onQuery = { query = it })

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(Modifier.width(4.dp))
                            CategoryChip(
                                label = "All Items",
                                selected = s.selectedCategoryId == "ALL",
                                onClick = { menuViewModel.selectCategory("ALL") }
                            )
                        }
                        items(s.data.categories, key = { it.id }) { category ->
                            CategoryChip(
                                label = "${category.icon} ${category.name}",
                                selected = s.selectedCategoryId == category.id,
                                onClick = { menuViewModel.selectCategory(category.id) }
                            )
                        }
                        item { Spacer(Modifier.width(4.dp)) }
                    }

                    // Keyed by filter: forces a fresh list layout the moment a
                    // chip is tapped, so rows can never go stale.
                    key(s.selectedCategoryId) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item { Spacer(Modifier.height(2.dp)) }
                            items(visible, key = { it.id }) { item ->
                                MenuItemCard(item = item, onClick = { onItemClick(item.id) })
                            }
                            item { Spacer(Modifier.height(6.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuHeader(cartCount: Int, onCartClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) { Text("BK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Fresh Roast & Bakes", fontSize = 11.sp, color = InkGray)
            Text("Brewkery Artisans", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.weight(1f))
        // Fixed-size icon slot so the count badge always sits centered
        // on the corner, regardless of button padding.
        Box(
            modifier = Modifier.size(46.dp),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = onCartClick) { Text("\uD83D\uDECD", fontSize = 22.sp) }
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(PriceRedBadge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$cartCount",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private val PriceRedBadge = Color(0xFFE5484D)

@Composable
private fun StoreInfoCard(tagline: String, eta: String, fee: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CreamChip),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("\uD83C\uDFEA", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("STORE INFO \u2022", fontSize = 10.sp, color = InkGray, fontWeight = FontWeight.Bold)
                Text("Delivery in $eta", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("$fee flat fee", fontSize = 12.sp, color = InkGray)
            }
            Text(
                "Open",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(query: String, onQuery: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        placeholder = {
            Text("Search roast, cold brew, pastry...", color = InkGray, fontSize = 13.sp)
        },
        leadingIcon = { Text("\u2315", color = InkGray, fontSize = 15.sp) },
        singleLine = true,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = DarkBar,
            selectedLabelColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PaleBorder)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.badge,
                    color = BrewOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "\u2605 ${item.rating} (${item.reviewCount})",
                    color = StarAmber,
                    fontSize = 12.sp
                )
                Text(
                    CartCalculator.format(item.basePrice),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrewOrange),
                shape = RoundedCornerShape(20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp, vertical = 6.dp
                )
            ) {
                Text("+ Customize", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DarkCartBar(total: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkBar),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("View Your Cart $total", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("Proceed to Checkout \u203A", color = Color.White, fontSize = 13.sp)
        }
    }
}
