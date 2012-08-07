/*
 * Copyright (C) 2009 The Android Open Source Project
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
 * SparseArrays map longs to Objects. Unlike a normal array of Objects, there
 * can be gaps in the indices. It is intended to be more efficient than using a
 * HashMap to map Longs to Objects. <br>
 * long型到object映射的稀疏矩阵。与普通数组不同的是，索引之间可能有间隔。该类从long向 object映射的速度比使用普通的HashMap更有效。
 * 
 * @hide
 */
public class LongSparseArray<E> {
	/**
	 * 待删除对象
	 */
	private static final Object DELETED = new Object();
	/**
	 * 是否有需要清理的数据
	 */
	private boolean mGarbage = false;

	/**
	 * Creates a new SparseArray containing no mappings.
	 */
	public LongSparseArray() {
		this(10);
	}

	/**
	 * Creates a new SparseArray containing no mappings that will not require
	 * any additional memory allocation to store the specified number of
	 * mappings. <br>
	 * 构建一个新的稀疏数组
	 */
	public LongSparseArray(int initialCapacity) {
		// 计算数组的大小
		initialCapacity = ArrayUtils.idealIntArraySize(initialCapacity);

		mKeys = new long[initialCapacity];
		mValues = new Object[initialCapacity];
		mSize = 0;
	}

	/**
	 * 返回所有key数组的拷贝
	 * 
	 * @return A copy of all keys contained in the sparse array.
	 */
	public long[] getKeys() {
		int length = mKeys.length;
		long[] result = new long[length];
		System.arraycopy(mKeys, 0, result, 0, length);
		return result;
	}

	/**
	 * Sets all supplied keys to the given unique value.
	 * 
	 * @param keys
	 *            Keys to set
	 * @param uniqueValue
	 *            Value to set all supplied keys to
	 */
	public void setValues(long[] keys, E uniqueValue) {
		int length = keys.length;
		for (int i = 0; i < length; i++) {
			put(keys[i], uniqueValue);
		}
	}

	/**
	 * Gets the Object mapped from the specified key, or <code>null</code> if no
	 * such mapping has been made.
	 */
	public E get(long key) {
		return get(key, null);
	}

	/**
	 * Gets the Object mapped from the specified key, or the specified Object if
	 * no such mapping has been made.
	 */
	public E get(long key, E valueIfKeyNotFound) {
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i < 0 || mValues[i] == DELETED) {
			return valueIfKeyNotFound;
		} else {
			return (E) mValues[i];
		}
	}

	/**
	 * Removes the mapping from the specified key, if there was any.
	 */
	public void delete(long key) {
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i >= 0) {
			if (mValues[i] != DELETED) {
				mValues[i] = DELETED;
				mGarbage = true;
			}
		}
	}

	/**
	 * Alias for {@link #delete(long)}.
	 */
	public void remove(long key) {
		delete(key);
	}

	/**
	 * 清理数据
	 */
	private void gc() {
		// Log.e("SparseArray", "gc start with " + mSize);

		int n = mSize;
		// 新数组的索引
		int o = 0;
		long[] keys = mKeys;
		Object[] values = mValues;

		for (int i = 0; i < n; i++) {
			Object val = values[i];

			if (val != DELETED) {
				if (i != o) {
					keys[o] = keys[i];
					values[o] = val;
				}

				o++;
			}
		}

		mGarbage = false;
		mSize = o;

		// Log.e("SparseArray", "gc end with " + mSize);
	}

	/**
	 * Adds a mapping from the specified key to the specified value, replacing
	 * the previous mapping from the specified key if there was one.
	 * <br>插入新值，如果已经在值数组中，则将值替换，否则插入
	 */
	public void put(long key, E value) {
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i >= 0) {
			mValues[i] = value;
		} else {
			i = ~i;

			if (i < mSize && mValues[i] == DELETED) {
				mKeys[i] = key;
				mValues[i] = value;
				return;
			}
			
			//先清理，然后获取新的索引
			if (mGarbage && mSize >= mKeys.length) {
				gc();

				// Search again because indices may have changed.
				i = ~binarySearch(mKeys, 0, mSize, key);
			}

			if (mSize >= mKeys.length) {
				int n = ArrayUtils.idealIntArraySize(mSize + 1);

				long[] nkeys = new long[n];
				Object[] nvalues = new Object[n];

				// Log.e("SparseArray", "grow " + mKeys.length + " to " + n);
				System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
				System.arraycopy(mValues, 0, nvalues, 0, mValues.length);

				mKeys = nkeys;
				mValues = nvalues;
			}

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
	public long keyAt(int index) {
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
	public int indexOfKey(long key) {
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
	 * <br>返回值在值数组中的位置，而不是对应的key
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
	 * 清空值数组
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
	 * key is greater than all existing keys in the array.
	 * <br>增加一个值，并保证增加后key数组按照从小到大的顺序排列
	 */
	public void append(long key, E value) {
		//如果值在最大值和最小值之间则使用put方法插入
		if (mSize != 0 && key <= mKeys[mSize - 1]) {
			put(key, value);
			return;
		}
		
		//如果有需要删除的数据，并且key数组已满，则进行清理
		if (mGarbage && mSize >= mKeys.length) {
			gc();
		}

		int pos = mSize;
		//如果key数组已满，则需要重新分配空间
		if (pos >= mKeys.length) {
			int n = ArrayUtils.idealIntArraySize(pos + 1);

			long[] nkeys = new long[n];
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
	 * 二进制查找
	 * 
	 * @param a
	 * @param start
	 * @param len
	 * @param key
	 * @return 如果找到则返回索引值，如果比最后一个值还大，则返回最后位置的非，否则返回最接近key但比key大的值索引的非
	 */
	private static int binarySearch(long[] a, int start, int len, long key) {
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

	/**
	 * 索引数组
	 */
	private long[] mKeys;
	/**
	 * 值数组
	 */
	private Object[] mValues;
	/**
	 * 值个数
	 */
	private int mSize;
}