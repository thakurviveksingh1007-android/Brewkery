package com.brewkery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.brewkery.ui.CartViewModel
import com.brewkery.ui.NavGraph
import com.brewkery.ui.theme.BrewkeryTheme

class MainActivity : ComponentActivity() {

    // Activity-scoped: same cart instance shared by all 4 screens.
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrewkeryTheme {
                NavGraph(cartViewModel = cartViewModel)
            }
        }
    }
}
