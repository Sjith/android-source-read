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

package android.util;

import com.android.internal.util.ArrayUtils;

/**
 * SparseArrays map integers to Objects. Unlike a normal array of Objects, there
 * can be gaps in the indices. It is intended to be more efficient than using a
 * HashMap to map Integers to Objects. <br>
 * 该类映射integer到object。不像一个普通的object数组，在索引之间会有间隙。该类的目标是实现
 * 相比于从Integers到Objects的映射操作时提高其效率
 */
public class SparseArray<E> {
	// 被删除的值
	private static final Object DELETED = new Object();
	// 是否有被删除的数据
	private boolean mGarbage = false;

	/**
	 * Creates a new SparseArray containing no mappings.
	 */
	public SparseArray() {
		this(10);
	}

	/**
	 * Creates a new SparseArray containing no mappings that will not require
	 * any additional memory allocation to store the specified number of
	 * mappings.
	 */
	public SparseArray(int initialCapacity) {
		// 计算理想的数组大小
		initialCapacity = ArrayUtils.idealIntArraySize(initialCapacity);

		mKeys = new int[initialCapacity];
		mValues = new Object[initialCapacity];
		mSize = 0;
	}

	/**
	 * Gets the Object mapped from the specified key, or <code>null</code> if no
	 * such mapping has been made.
	 */
	public E get(int key) {
		return get(key, null);
	}

	/**
	 * Gets the Object mapped from the specified key, or the specified Object if
	 * no such mapping has been made. 返回对应于key的值，如果没有找到则返回参数指定的默认值
	 * 
	 * @param key
	 *            键值
	 * @param valueIfKeyNotFound
	 *            如果没有找到的值
	 */
	public E get(int key, E valueIfKeyNotFound) {
		// 使用折半查找，加快速度
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i < 0 || mValues[i] == DELETED) {
			return valueIfKeyNotFound;
		} else {
			return (E) mValues[i];
		}
	}

	/**
	 * Removes the mapping from the specified key, if there was any. <br>
	 * 删除指定key对应的值
	 */
	public void delete(int key) {
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i >= 0) {
			if (mValues[i] != DELETED) {
				mValues[i] = DELETED;
				mGarbage = true;
			}
		}
	}

	/**
	 * Alias for {@link #delete(int)}.
	 */
	public void remove(int key) {
		delete(key);
	}

	/**
	 * 清理内存,删除是DELETED的数据
	 */
	private void gc() {
		// Log.e("SparseArray", "gc start with " + mSize);

		int n = mSize;
		int zero = 0;
		int[] keys = mKeys;
		Object[] values = mValues;

		for (int i = 0; i < n; i++) {
			Object val = values[i];

			if (val != DELETED) {
				if (i != zero) {
					keys[zero] = keys[i];
					values[zero] = val;
				}

				zero++;
			}
		}

		mGarbage = false;
		mSize = zero;

		// Log.e("SparseArray", "gc end with " + mSize);
	}

	/**
	 * Adds a mapping from the specified key to the specified value, replacing
	 * the previous mapping from the specified key if there was one. <br>
	 * 增加一个映射，如果已经存在映射则替换新的值
	 */
	public void put(int key, E value) {
		// 判断是否已经存在映射
		int i = binarySearch(mKeys, 0, mSize, key);
		// 查找到值，則替換
		if (i >= 0) {
			mValues[i] = value;
		} else {
			// 沒有找到，则需要将折半查找返回的值进行非操作，因为在折半查找的时候返回的是位置的非
			i = ~i;
			// 如果刚好此位置的数据为删除状态，则直接替换key和value
			if (i < mSize && mValues[i] == DELETED) {
				mKeys[i] = key;
				mValues[i] = value;
				return;
			}

			// 如果有需要删除的数据，则进行垃圾清理，同时进行重新查找
			if (mGarbage && mSize >= mKeys.length) {
				gc();

				// Search again because indices may have changed.
				// 重新查找，获取合适的插入位置
				i = ~binarySearch(mKeys, 0, mSize, key);
			}
			// key和value数组已经无法容纳更多的数据，则需要新分配空间
			if (mSize >= mKeys.length) {
				int n = ArrayUtils.idealIntArraySize(mSize + 1);

				int[] nkeys = new int[n];
				Object[] nvalues = new Object[n];

				// Log.e("SparseArray", "grow " + mKeys.length + " to " + n);
				System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
				System.arraycopy(mValues, 0, nvalues, 0, mValues.length);

				mKeys = nkeys;
				mValues = nvalues;
			}
			// 将合适位置处及以后的key和value都向后移动
			if (mSize - i != 0) {
				// Log.e("SparseArray", "move " + (mSize - i));
				System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i);
				System.arraycopy(mValues, i, mValues, i + 1, mSize - i);
			}

			mKeys[i] = key;
			mValues[i] = value;
			mSize++;
		}
	}

	/**
	 * Returns the number of key-value mappings that this SparseArray currently
	 * stores.
	 */
	public int size() {
		if (mGarbage) {
			gc();
		}

		return mSize;
	}

	/**
	 * Given an index in the range <code>0...size()-1</code>, returns the key
	 * from the <code>index</code>th key-value mapping that this SparseArray
	 * stores.
	 */
	public int keyAt(int index) {
		if (mGarbage) {
			gc();
		}

		return mKeys[index];
	}

	/**
	 * Given an index in the range <code>0...size()-1</code>, returns the value
	 * from the <code>index</code>th key-value mapping that this SparseArray
	 * stores.
	 */
	public E valueAt(int index) {
		if (mGarbage) {
			gc();
		}

		return (E) mValues[index];
	}

	/**
	 * Given an index in the range <code>0...size()-1</code>, sets a new value
	 * for the <code>index</code>th key-value mapping that this SparseArray
	 * stores.
	 */
	public void setValueAt(int index, E value) {
		if (mGarbage) {
			gc();
		}

		mValues[index] = value;
	}

	/**
	 * Returns the index for which {@link #keyAt} would return the specified
	 * key, or a negative number if the specified key is not mapped.
	 */
	public int indexOfKey(int key) {
		if (mGarbage) {
			gc();
		}

		return binarySearch(mKeys, 0, mSize, key);
	}

	/**
	 * Returns an index for which {@link #valueAt} would return the specified
	 * key, or a negative number if no keys map to the specified value. Beware
	 * that this is a linear search, unlike lookups by key, and that multiple
	 * keys can map to the same value and this will find only one of them.
	 */
	public int indexOfValue(E value) {
		if (mGarbage) {
			gc();
		}

		for (int i = 0; i < mSize; i++)
			if (mValues[i] == value)
				return i;

		return -1;
	}

	/**
	 * Removes all key-value mappings from this SparseArray.
	 */
	public void clear() {
		int n = mSize;
		Object[] values = mValues;

		for (int i = 0; i < n; i++) {
			values[i] = null;
		}

		mSize = 0;
		mGarbage = false;
	}

	/**
	 * Puts a key/value pair into the array, optimizing for the case where the
	 * key is greater than all existing keys in the array. <br>
	 * 附加一个新的数据,当新添加的key大于所有已经存在的key
	 */
	public void append(int key, E value) {
		if (mSize != 0 && key <= mKeys[mSize - 1]) {
			put(key, value);
			return;
		}

		if (mGarbage && mSize >= mKeys.length) {
			gc();
		}

		int pos = mSize;
		if (pos >= mKeys.length) {
			// 新建一个更大容量的数组
			int n = ArrayUtils.idealIntArraySize(pos + 1);

			int[] nkeys = new int[n];
			Object[] nvalues = new Object[n];

			// Log.e("SparseArray", "grow " + mKeys.length + " to " + n);
			System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
			System.arraycopy(mValues, 0, nvalues, 0, mValues.length);

			mKeys = nkeys;
			mValues = nvalues;
		}

		mKeys[pos] = key;
		mValues[pos] = value;
		mSize = pos + 1;
	}

	/**
	 * 折半查找，如果存在值则返回所在位置，如果key值大于所有的key则返回最后一个值的非，否则返回最接近于参数key且大于参数key的key的位置的非
	 * 
	 * @param a
	 * @param start
	 * @param len
	 * @param key
	 * @return
	 */
	private static int binarySearch(int[] a, int start, int len, int key) {
		int high = start + len, low = start - 1, guess;

		while (high - low > 1) {
			guess = (high + low) / 2;

			if (a[guess] < key)
				low = guess;
			else
				high = guess;
		}
		if (high == start + len)
			return ~(start + len);
		else if (a[high] == key)
			return high;
		else
			return ~high;
	}

	private void checkIntegrity() {
		for (int i = 1; i < mSize; i++) {
			if (mKeys[i] <= mKeys[i - 1]) {
				for (int j = 0; j < mSize; j++) {
					Log.e("FAIL", j + ": " + mKeys[j] + " -> " + mValues[j]);
				}

				throw new RuntimeException();
			}
		}
	}

	// key数组
	private int[] mKeys;
	// 值数组
	private Object[] mValues;
	// 数据
	private int mSize;
}
