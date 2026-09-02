package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.entity.CategoryEntity

data class CategoryItem(
    val key: String,
    val label: String,
    val icon: ImageVector? = null,
    val emoji: String? = null,
    val color: Color,
    val isCustom: Boolean = false
)

val PRESET_CATEGORY_EMOJIS = listOf(
    "🍽️", "🍕", "🍔", "☕", "🚗", "🚕", "🚌", "✈️",
    "🛒", "🛍️", "👕", "💡", "⚡", "🏠", "🏥", "💊",
    "📚", "🎓", "🎮", "🎬", "🍿", "💻", "📱", "🏋️",
    "🎨", "🎁", "👶", "🐱", "💼", "🛠️", "🌿", "⛽", "🏖️", "💸"
)

val PRESET_CATEGORY_COLORS = listOf(
    Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1), Color(0xFF2EBD85),
    Color(0xFFFFB830), Color(0xFFBA68C8), Color(0xFFFF8A65), Color(0xFFFF9F43),
    Color(0xFF54A0FF), Color(0xFFFF7675), Color(0xFF5F27CD), Color(0xFF10AC84),
    Color(0xFFEE5253), Color(0xFF00D2D3), Color(0xFF8395A7), Color(0xFF341F97)
)

enum class Category(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
) {
    FOOD("food", "খাবার", Icons.Rounded.Restaurant, Color(0xFFFF6B6B)),
    TRANSPORT("transport", "যানবাহন", Icons.Rounded.DirectionsBus, Color(0xFF4ECDC4)),
    GROCERY("grocery", "বাজার", Icons.Rounded.ShoppingCart, Color(0xFF45B7D1)),
    HEALTH("health", "স্বাস্থ্য", Icons.Rounded.LocalHospital, Color(0xFF2EBD85)),
    BILL("bill", "বিল", Icons.Rounded.Receipt, Color(0xFFFFB830)),
    EDUCATION("education", "শিক্ষা", Icons.Rounded.School, Color(0xFFBA68C8)),
    ENTERTAINMENT("entertainment", "বিনোদন", Icons.Rounded.SportsEsports, Color(0xFFFF8A65)),
    SHOPPING("shopping", "কেনাকাটা", Icons.Rounded.Checkroom, Color(0xFFFF9F43)),
    PERSONAL("personal", "ব্যক্তিগত", Icons.Rounded.Face, Color(0xFF54A0FF)),
    SALARY("salary", "বেতন", Icons.Rounded.Paid, Color(0xFF2ED573)),
    TUITION("tuition", "টিউশন", Icons.Rounded.School, Color(0xFF10AC84)),
    GIFT("gift", "উপহার", Icons.Rounded.Redeem, Color(0xFFFF7675)),
    OTHER("other", "অন্যান্য", Icons.Rounded.Category, Color(0xFFA4B0BE));

    fun toCategoryItem(): CategoryItem {
        return CategoryItem(
            key = key,
            label = label,
            icon = icon,
            emoji = null,
            color = color,
            isCustom = false
        )
    }

    companion object {
        fun fromKey(key: String): Category {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: OTHER
        }

        fun parseHexColor(hex: String, defaultColor: Color = Color(0xFFFF6B6B)): Color {
            return try {
                val cleanHex = hex.removePrefix("#")
                val colorInt = if (cleanHex.length == 6) {
                    android.graphics.Color.parseColor("#FF$cleanHex")
                } else {
                    android.graphics.Color.parseColor("#$cleanHex")
                }
                Color(colorInt)
            } catch (_: Exception) {
                defaultColor
            }
        }

        fun colorToHex(color: Color): String {
            val argb = color.toArgb()
            return String.format("#%06X", 0xFFFFFF and argb)
        }

        fun getCategoryItem(key: String, customCategories: List<CategoryEntity> = emptyList()): CategoryItem {
            // First check custom categories
            val custom = customCategories.firstOrNull { it.id == key || it.name.equals(key, ignoreCase = true) }
            if (custom != null) {
                return custom.toCategoryItem()
            }
            // Check default categories
            val defaultCat = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
            if (defaultCat != null) {
                return defaultCat.toCategoryItem()
            }
            // Fallback
            return OTHER.toCategoryItem()
        }
    }
}

fun CategoryEntity.toCategoryItem(): CategoryItem {
    return CategoryItem(
        key = id,
        label = name,
        icon = null,
        emoji = emoji.ifBlank { "🏷️" },
        color = Category.parseHexColor(colorHex),
        isCustom = true
    )
}

