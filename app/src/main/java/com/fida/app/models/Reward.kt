package com.fida.app.models

data class Reward(
    val id: String = "",
    val name: String,
    val description: String,
    val imageUrl: String,
    val cost: Int,
    val type: Type
) {
    enum class Type {
        COIN, GEM
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Reward {
            val typeStr = (map["type"] as? String) ?: "coin"
            return Reward(
                id = (map["id"] as? String) ?: "",
                name = (map["name"] as? String) ?: "",
                description = (map["description"] as? String) ?: "",
                imageUrl = (map["imageUrl"] as? String) ?: "",
                cost = ((map["cost"] as? Long)?.toInt() ?: (map["cost"] as? Int) ?: 0),
                type = if (typeStr.lowercase() == "gem") Type.GEM else Type.COIN
            )
        }
    }
}
