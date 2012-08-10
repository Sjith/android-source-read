/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.widget;

import android.database.DataSetObserver;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Adapter用来作为{@link AdapterView}和展示数据的View之间的桥梁。Adapter提供对数据项的访问，
 * 同时负责为数据集的每一项创建一个view. An Adapter object acts as a bridge between an
 * {@link AdapterView} and the underlying data for that view. The Adapter
 * provides access to the data items. The Adapter is also responsible for making
 * a {@link android.view.View} for each item in the data set.
 * 
 * @see android.widget.ArrayAdapter
 * @see android.widget.CursorAdapter
 * @see android.widget.SimpleCursorAdapter
 */
public interface Adapter {
	/**
	 * Register an observer that is called when changes happen to the data used
	 * by this adapter. <br>
	 * 注册一个数据集观察者，用于当数据发生变化时调用
	 * 
	 * @param observer
	 *            the object that gets notified when the data set changes.
	 */
	void registerDataSetObserver(DataSetObserver observer);

	/**
	 * Unregister an observer that has previously been registered with this
	 * adapter via {@link #registerDataSetObserver}. <br>
	 * 注销数据集观察者
	 * 
	 * @param observer
	 *            the object to unregister.
	 */
	void unregisterDataSetObserver(DataSetObserver observer);

	/**
	 * How many items are in the data set represented by this Adapter. <br>
	 * 返回数据集的子项目的个数
	 * 
	 * @return Count of items.
	 */
	int getCount();

	/**
	 * Get the data item associated with the specified position in the data set. <br>
	 * 从数据集中获取指定位置的数据
	 * 
	 * @param position
	 *            Position of the item whose data we want within the adapter's
	 *            data set.
	 * @return The data at the specified position.
	 */
	Object getItem(int position);

	/**
	 * Get the row id associated with the specified position in the list. <br>
	 * 获取指定位置的数据的行号
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set whose
	 *            row id we want.
	 * @return The id of the item at the specified position.
	 */
	long getItemId(int position);

	/**
	 * 子项的id号是否为固定 <br>
	 * Indicated whether the item ids are stable across changes to the
	 * underlying data.
	 * 
	 * @return True if the same id always refers to the same object.
	 */
	boolean hasStableIds();

	/**
	 * 创建一个View用来显示指定位置的数据，你可以手动创建一个view或者从一个xml布局文件中创建 <br>
	 * Get a View that displays the data at the specified position in the data
	 * set. You can either create a View manually or inflate it from an XML
	 * layout file. When the View is inflated, the parent View (GridView,
	 * ListView...) will apply default layout parameters unless you use
	 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param convertView
	 *            The old view to reuse, if possible. Note: You should check
	 *            that this view is non-null and of an appropriate type before
	 *            using. If it is not possible to convert this view to display
	 *            the correct data, this method can create a new view.
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	View getView(int position, View convertView, ViewGroup parent);

	/**
	 * An item view type that causes the {@link AdapterView} to ignore the item
	 * view. For example, this can be used if the client does not want a
	 * particular view to be given for conversion in
	 * {@link #getView(int, View, ViewGroup)}.
	 * 
	 * @see #getItemViewType(int)
	 * @see #getViewTypeCount()
	 */
	static final int IGNORE_ITEM_VIEW_TYPE = AdapterView.ITEM_VIEW_TYPE_IGNORE;

	/**
	 * Get the type of View that will be created by {@link #getView} for the
	 * specified item. <br>
	 * 返回指定为值的数据项的类型，用于在不同的位置显示不同的样式。
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set whose
	 *            view type we want.
	 * @return An integer representing the type of View. Two views should share
	 *         the same type if one can be converted to the other in
	 *         {@link #getView}. Note: Integers must be in the range 0 to
	 *         {@link #getViewTypeCount} - 1. {@link #IGNORE_ITEM_VIEW_TYPE} can
	 *         also be returned.
	 * @see #IGNORE_ITEM_VIEW_TYPE
	 */
	int getItemViewType(int position);

	/**
	 * 返回view的类型个数 <br>
	 * <p>
	 * Returns the number of types of Views that will be created by
	 * {@link #getView}. Each type represents a set of views that can be
	 * converted in {@link #getView}. If the adapter always returns the same
	 * type of View for all items, this method should return 1.
	 * </p>
	 * <p>
	 * This method will only be called when when the adapter is set on the the
	 * {@link AdapterView}.
	 * </p>
	 * 
	 * @return The number of types of Views that will be created by this adapter
	 */
	int getViewTypeCount();

	static final int NO_SELECTION = Integer.MIN_VALUE;

	/**
	 * 如通过网数据集中没有包含任何数据则，空view显示。
	 * @return true if this adapter doesn't contain any data. This is used to
	 *         determine whether the empty view should be displayed. A typical
	 *         implementation will return getCount() == 0 but since getCount()
	 *         includes the headers and footers, specialized adapters might want
	 *         a different behavior.
	 */
	boolean isEmpty();
}
