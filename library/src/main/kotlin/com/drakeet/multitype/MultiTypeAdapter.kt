/*
 * Copyright (c) 2016-present. Drakeet Xu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drakeet.multitype

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * @author Drakeet Xu
 */
open class MultiTypeAdapter @JvmOverloads constructor(
  /**
   * Sets and updates the items atomically and safely. It is recommended to use this method
   * to update the items with a new wrapper list or consider using [CopyOnWriteArrayList].
   *
   * Note: If you want to refresh the list views after setting items, you should
   * call [RecyclerView.Adapter.notifyDataSetChanged] by yourself.
   *
   * @since v2.4.1
   */
  open var items: List<Any> = emptyList(),
  open val initialCapacity: Int = 0,
  override var types: Types = MutableTypes(initialCapacity),
) : RecyclerView.Adapter<ViewHolder>(), MultiTypeDelegate {

  override fun <T> register(type: Type<T>) {
    super.register(type)
    type.delegate._adapter = this
  }

  inline fun <reified T : Any> register(delegate: ItemViewDelegate<T, *>) {
    register(T::class.java, delegate)
  }

  inline fun <reified T : Any> register(
    // Keep this parameter to provide the explicit relationship
    @Suppress("UNUSED_PARAMETER") clazz: KClass<T>,
    delegate: ItemViewDelegate<T, *>,
  ) {
    // Always use the reified type to avoid javaPrimitiveType problem
    // See https://github.com/drakeet/MultiType/issues/302
    register(T::class.java, delegate)
  }

  fun <T> register(clazz: Class<T>, binder: ItemViewBinder<T, *>) {
    register(clazz, binder as ItemViewDelegate<T, *>)
  }

  inline fun <reified T : Any> register(binder: ItemViewBinder<T, *>) {
    register(binder as ItemViewDelegate<T, *>)
  }

  inline fun <reified T : Any> register(clazz: KClass<T>, binder: ItemViewBinder<T, *>) {
    register(clazz, binder as ItemViewDelegate<T, *>)
  }

  @CheckResult
  fun <T : Any> register(clazz: KClass<T>): OneToManyFlow<T> {
    return register(clazz.java)
  }

  override fun getItemViewType(position: Int): Int {
    return indexInTypesOf(position, items[position])
  }

  override fun onCreateViewHolder(parent: ViewGroup, indexViewType: Int): ViewHolder {
    return getOutDelegate(indexViewType).onCreateViewHolder(parent.context, parent)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    onBindViewHolder(holder, position, emptyList())
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
    val item = items[position]
    getOutDelegateByViewHolder(holder).onBindViewHolder(holder, item, payloads)
  }

  override fun getItemCount(): Int = items.size

  /**
   * Called to return the stable ID for the item, and passes the event to its associated delegate.
   *
   * @param position Adapter position to query
   * @return the stable ID of the item at position
   * @see ItemViewDelegate.getItemId
   * @see RecyclerView.Adapter.setHasStableIds
   * @since v3.2.0
   */
  override fun getItemId(position: Int): Long {
    val itemViewType = getItemViewType(position)
    return getOutDelegate(itemViewType).getItemId(position)
  }

  /**
   * Called when a view created by this adapter has been recycled, and passes the event to its
   * associated delegate.
   *
   * @param holder The ViewHolder for the view being recycled
   * @see RecyclerView.Adapter.onViewRecycled
   * @see ItemViewDelegate.onViewRecycled
   */
  override fun onViewRecycled(holder: ViewHolder) {
    getOutDelegateByViewHolder(holder).onViewRecycled(holder)
  }

  /**
   * Called by the RecyclerView if a ViewHolder created by this Adapter cannot be recycled
   * due to its transient state, and passes the event to its associated item view delegate.
   *
   * @param holder The ViewHolder containing the View that could not be recycled due to its
   * transient state.
   * @return True if the View should be recycled, false otherwise. Note that if this method
   * returns `true`, RecyclerView *will ignore* the transient state of
   * the View and recycle it regardless. If this method returns `false`,
   * RecyclerView will check the View's transient state again before giving a final decision.
   * Default implementation returns false.
   * @see RecyclerView.Adapter.onFailedToRecycleView
   * @see ItemViewDelegate.onFailedToRecycleView
   */
  override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
    return getOutDelegateByViewHolder(holder).onFailedToRecycleView(holder)
  }

  /**
   * Called when a view created by this adapter has been attached to a window, and passes the
   * event to its associated item view delegate.
   *
   * @param holder Holder of the view being attached
   * @see RecyclerView.Adapter.onViewAttachedToWindow
   * @see ItemViewDelegate.onViewAttachedToWindow
   */
  override fun onViewAttachedToWindow(holder: ViewHolder) {
    getOutDelegateByViewHolder(holder).onViewAttachedToWindow(holder)
  }

  /**
   * Called when a view created by this adapter has been detached from its window, and passes
   * the event to its associated item view delegate.
   *
   * @param holder Holder of the view being detached
   * @see RecyclerView.Adapter.onViewDetachedFromWindow
   * @see ItemViewDelegate.onViewDetachedFromWindow
   */
  override fun onViewDetachedFromWindow(holder: ViewHolder) {
    getOutDelegateByViewHolder(holder).onViewDetachedFromWindow(holder)
  }
}
