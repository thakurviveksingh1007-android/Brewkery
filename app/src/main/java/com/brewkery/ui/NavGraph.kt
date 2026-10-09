package com.brewkery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.brewkery.ui.screens.CartScreen
import com.brewkery.ui.screens.DetailScreen
import com.brewkery.ui.screens.MenuScreen
import com.brewkery.ui.screens.OrderStatusScreen

object Routes {
    const val MENU = "menu"
    const val DETAIL = "detail"
    const val CART = "cart"
    const val STATUS = "status"
}

@Composable
fun NavGraph(cartViewModel: CartViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.MENU) {

        composable(Routes.MENU) {
            MenuScreen(
                onItemClick = { id -> navController.navigate("${Routes.DETAIL}/$id") },
                onCartClick = { navController.navigate(Routes.CART) },
                cartViewModel = cartViewModel
            )
        }

        composable(
            route = "${Routes.DETAIL}/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            val detailViewModel: DetailViewModel = viewModel()
            DetailScreen(
                itemId = itemId,
                detailViewModel = detailViewModel,
                cartViewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onAddedGoCart = { navController.navigate(Routes.CART) }
            )
        }

        composable(Routes.CART) {
            CartScreen(
                cartViewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onOrderPlaced = {
                    navController.navigate(Routes.STATUS) {
                        popUpTo(Routes.MENU)
                    }
                }
            )
        }

        composable(Routes.STATUS) {
            val order by cartViewModel.lastOrder.collectAsStateWithLifecycle()
            OrderStatusScreen(
                order = order,
                estimatedTime = cartViewModel.estimatedTime,
                onBackToMenu = {
                    navController.popBackStack(Routes.MENU, inclusive = false)
                }
            )
        }
    }
}
