package com.brewkery.data.repository

import com.brewkery.data.model.BrewkeryMenuResponse
import com.brewkery.data.model.MenuItem
import com.brewkery.data.network.BrewkeryApi
import com.brewkery.data.network.RetrofitClient

class MenuRepository(
    private val api: BrewkeryApi = RetrofitClient.api
) {
    suspend fun loadMenu(): BrewkeryMenuResponse = api.getMenu()

    suspend fun loadItem(id: Int): MenuItem = api.getItemDetail(id)
}
