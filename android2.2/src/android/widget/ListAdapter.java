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

/**
 * ListAdapter从{@link Adapter}派生，作为{@link ListView}与以l数据集之间桥梁，通常情况下
 * 数据来自于cursor，但并不是说必须来自于cursor，ListView可以展示任何形式由ListAdapter包装的数据。 <br>
 * Extended {@link Adapter} that is the bridge between a {@link ListView} and
 * the data that backs the list. Frequently that data comes from a Cursor, but
 * that is not required. The ListView can display any data provided that it is
 * wrapped in a ListAdapter.
 */
public interface ListAdapter extends Adapter {

	/**
	 * Are all items in this ListAdapter enabled? If yes it means all items are
	 * selectable and clickable.
	 * <br>所有的数据项是否可用，如果返回yes，则数据项可以被选择和被点击。
	 * 
	 * @return True if all items are enabled
	 */
	public boolean areAllItemsEnabled();

	/**
	 * Returns true if the item at the specified position is not a separator. (A
	 * separator is a non-selectable, non-clickable item).
	 * <br>如果指定位置的项不是分隔符，则返回true。
	 * The result is unspecified if position is invalid. An
	 * {@link ArrayIndexOutOfBoundsException} should be thrown in that case for
	 * fast failure.
	 * 
	 * @param position
	 *            Index of the item
	 * @return True if the item is not a separator
	 */
	boolean isEnabled(int position);
}
