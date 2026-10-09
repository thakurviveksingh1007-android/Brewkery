package com.brewkery.data.model

import com.google.gson.annotations.SerializedName

data class BrewkeryMenuResponse(
    @SerializedName("meta") val meta: ShopMeta,
    @SerializedName("categories") val categories: List<Category>,
    @SerializedName("items") val items: List<MenuItem>
)

data class ShopMeta(
    @SerializedName("app") val app: String,
    @SerializedName("tagline") val tagline: String,
    @SerializedName("currency_symbol") val currencySymbol: String,
    @SerializedName("delivery_fee") val deliveryFee: Double,
    @SerializedName("tax_rate_percent") val taxRatePercent: Double,
    @SerializedName("estimated_delivery_time") val estimatedDeliveryTime: String
)

data class Category(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("item_count") val itemCount: Int
)

data class MenuItem(
    @SerializedName("id") val id: Int,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("name") val name: String,
    @SerializedName("tagline") val tagline: String,
    @SerializedName("description") val description: String,
    @SerializedName("base_price") val basePrice: Double,
    @SerializedName("rating") val rating: Double,
    @SerializedName("review_count") val reviewCount: Int,
    @SerializedName("prep_time") val prepTime: String,
    @SerializedName("calories") val calories: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("badge") val badge: String,
    @SerializedName("ingredients") val ingredients: List<String>,
    @SerializedName("customizations") val customizations: Customizations
)

data class Customizations(
    @SerializedName("sizes") val sizes: List<SizeOption>,
    @SerializedName("sugar_levels") val sugarLevels: List<String>,
    @SerializedName("milk_options") val milkOptions: List<MilkOption>
)

data class SizeOption(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String,
    @SerializedName("extra_price") val extraPrice: Double
)

data class MilkOption(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("extra_price") val extraPrice: Double
)
