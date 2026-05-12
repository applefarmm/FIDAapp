package com.fida.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.databinding.ItemShopBinding
import com.fida.app.models.ShopItem

class ShopAdapter(
    private val items: List<ShopItem>,
    private val onItemClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    inner class ShopViewHolder(private val binding: ItemShopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShopItem) {
            binding.ivItemImage.setImageResource(item.imageRes)
            binding.tvItemName.text = item.name
            binding.tvItemDescription.text = item.description
            binding.tvItemEffect.text = item.effectDescription
            binding.tvItemPrice.text = item.price.toString()

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ItemShopBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}