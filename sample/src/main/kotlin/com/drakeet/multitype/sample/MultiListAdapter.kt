package com.drakeet.multitype.sample

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.drakeet.multitype.ItemViewDelegate
import com.drakeet.multitype.MultiTypeDelegate
import com.drakeet.multitype.MutableTypes
import com.drakeet.multitype.Types

class MultiListAdapter(
  initialCapacity: Int = 0,
  override val types: Types = MutableTypes(initialCapacity),
) : ListAdapter<Any, ViewHolder>(diffCallback), MultiTypeDelegate {

  companion object {
    val diffCallback = object : DiffUtil.ItemCallback<Any>() {
      override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
      }

      override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return true
      }
    }
  }

  inline fun <reified T : Any> register(delegate: ItemViewDelegate<T, *>) {
    register(T::class.java, delegate)
  }

  override fun getItemViewType(position: Int): Int {
    return indexInTypesOf(position, getItem(position)!!)
  }

  override fun onCreateViewHolder(parent: ViewGroup, indexViewType: Int): ViewHolder {
    return getOutDelegate(indexViewType).onCreateViewHolder(parent.context, parent)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    onBindViewHolder(holder, position, emptyList())
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
    getOutDelegateByViewHolder(holder).onBindViewHolder(holder, getItem(position)!!, payloads)
  }

  override fun onViewRecycled(holder: ViewHolder) {
    getOutDelegateByViewHolder(holder).onViewRecycled(holder)
  }

  override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
    return getOutDelegateByViewHolder(holder).onFailedToRecycleView(holder)
  }

  override fun onViewAttachedToWindow(holder: ViewHolder) {
    getOutDelegateByViewHolder(holder).onViewAttachedToWindow(holder)
  }

  override fun onViewDetachedFromWindow(holder: ViewHolder) {
    getOutDelegateByViewHolder(holder).onViewDetachedFromWindow(holder)
  }
}