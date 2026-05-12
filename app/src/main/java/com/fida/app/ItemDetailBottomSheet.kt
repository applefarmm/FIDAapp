package com.fida.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.fida.app.models.ItemType
import com.fida.app.models.ShopItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class ItemDetailBottomSheet : BottomSheetDialogFragment() {

    private var item: ShopItem? = null
    private var userCoins: Int = 0
    private var userHealth: Int = 0
    private var userXp: Int = 0
    private var userStreakShields: Int = 0

    private var onPurchaseListener: ((ShopItem) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_item_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shopItem = item ?: return

        val ivItemImage: ImageView = view.findViewById(R.id.ivItemImage)
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvItemDescription: TextView = view.findViewById(R.id.tvItemDescription)
        val tvItemEffect: TextView = view.findViewById(R.id.tvItemEffect)
        val tvItemPrice: TextView = view.findViewById(R.id.tvItemPrice)
        val tvYourCoins: TextView = view.findViewById(R.id.tvYourCoins)
        val tvYourStatus: TextView = view.findViewById(R.id.tvYourStatus)
        val btnCancel: MaterialButton = view.findViewById(R.id.btnCancel)
        val btnPurchase: MaterialButton = view.findViewById(R.id.btnPurchase)

        ivItemImage.setImageResource(shopItem.imageRes)
        tvItemName.text = shopItem.name
        tvItemDescription.text = shopItem.description
        tvItemEffect.text = shopItem.effectDescription
        tvItemPrice.text = shopItem.price.toString()
        tvYourCoins.text = getString(R.string.your_coins, userCoins)

        tvYourStatus.visibility = View.VISIBLE
        tvYourStatus.text = getStatDisplay(shopItem)

        if (userCoins < shopItem.price) {
            btnPurchase.isEnabled = false
            btnPurchase.text = getString(R.string.not_enough_coins)
        }

        btnCancel.setOnClickListener { dismiss() }

        btnPurchase.setOnClickListener {
            onPurchaseListener?.invoke(shopItem)
            Toast.makeText(requireContext(), getString(R.string.purchase_success), Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun getStatDisplay(item: ShopItem): String {
        return when (item.type) {
            ItemType.HEALTH -> "Current Health: $userHealth/100"
            ItemType.XP_BOOSTER -> "Current XP: $userXp"
            ItemType.STREAK_SHIELD -> "Current Shields: $userStreakShields"
            ItemType.POWER_UP -> "Power-ups are temporary boosts"
            ItemType.COSMETIC -> "Cosmetics are permanent unlocks"
        }
    }

    companion object {
        const val TAG = "ItemDetailBottomSheet"

        fun newInstance(
            item: ShopItem,
            userCoins: Int,
            userHealth: Int,
            userXp: Int,
            userStreakShields: Int,
            onPurchase: (ShopItem) -> Unit
        ): ItemDetailBottomSheet {
            return ItemDetailBottomSheet().apply {
                this.item = item
                this.userCoins = userCoins
                this.userHealth = userHealth
                this.userXp = userXp
                this.userStreakShields = userStreakShields
                this.onPurchaseListener = onPurchase
            }
        }
    }
}