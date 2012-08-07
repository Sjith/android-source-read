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

package com.android.internal.util;

import java.lang.reflect.Array;
import java.util.Collection;

// XXX these should be changed to reflect the actual memory allocator we use.
// it looks like right now objects want to be powers of 2 minus 8
// and the array size eats another 4 bytes

/**
 * ArrayUtils contains some methods that you can call to find out
 * the most efficient increments by which to grow arrays.
 * <br>ArrayUtils包含了一些方法，这些方法通过增长数组长度来提高效率
 */
public class ArrayUtils
{
	/**
	 * 空数组
	 */
    private static Object[] EMPTY = new Object[0];
    //sCache的大小
    private static final int CACHE_SIZE = 73;
    //保存每一种类型的空数组对应的变量
    private static Object[] sCache = new Object[CACHE_SIZE];

    private ArrayUtils() { /* cannot be instantiated */ }

    /**
     * 较为理想的字节数组长度
     * @param need 可能需要的长度
     * @return
     */
    public static int idealByteArraySize(int need) {
        for (int i = 4; i < 32; i++)
            if (need <= (1 << i) - 12)
                return (1 << i) - 12;

        return need;
    }
    /**
     * 较为理想的字节数组长度
     * @param need 可能需要的长度
     * @return
     */
    public static int idealBooleanArraySize(int need) {
        return idealByteArraySize(need);
    }
    /**
     * 较为理想的short数组长度
     * @param need 可能需要的长度
     * @return
     */
    public static int idealShortArraySize(int need) {
        return idealByteArraySize(need * 2) / 2;
    }
    /**
     * 较为理想的字符数组长度
     * @param need 可能需要的长度
     * @return
     */
    public static int idealCharArraySize(int need) {
        return idealByteArraySize(need * 2) / 2;
    }
    /**
     * 较为理想的Int数组长度
     * @param need 可能需要的长度
     * @return
     */
    public static int idealIntArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }
    /**
     * 较为理想的Float数组长度
     * @param need 可能需要的长度
     * @return
     */
    public static int idealFloatArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }
    /**
     * 较为理想的Object数组长度
     * @param need 可能需要的长度
     * @return
     */
    public static int idealObjectArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }
    /**
     * 较为理想的long数组长度
     * @param need 可能需要的长度
     * @return
     */
    public static int idealLongArraySize(int need) {
        return idealByteArraySize(need * 8) / 8;
    }

    /**
     * Checks if the beginnings of two byte arrays are equal.
     * <br>判断两个字节数组是否相同,首先判断两个数组是否为同一个对象的引用，
     * 然后判断两个数组的长度是否相同小于比较的长度，最后比较每一个元素。
     * @param array1 the first byte array
     * @param array2 the second byte array
     * @param length the number of bytes to check
     * @return true if they're equal, false otherwise
     */
    public static boolean equals(byte[] array1, byte[] array2, int length) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length < length || array2.length < length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an empty array of the specified type.  The intent is that
     * it will return the same empty array every time to avoid reallocation,
     * although this is not guaranteed.
     * <br>返回一个空的指定类型的数组，本函数的目的是每一次都返回一个相同的数组来避免重新分配空间。
     * 虽然本方法并不能保证一定能够实现此目的。
     */
    public static <T> T[] emptyArray(Class<T> kind) {
        if (kind == Object.class) {
            return (T[]) EMPTY;
        }
        //索引值,根据类型来计算索引值
        int bucket = ((System.identityHashCode(kind) / 8) & 0x7FFFFFFF) % CACHE_SIZE;
        Object cache = sCache[bucket];
        //如果没有保存，则新建一个
        if (cache == null || cache.getClass().getComponentType() != kind) {
            cache = Array.newInstance(kind, 0);
            sCache[bucket] = cache;

            // Log.e("cache", "new empty " + kind.getName() + " at " + bucket);
        }

        return (T[]) cache;
    }

    /**
     * Checks that value is present as at least one of the elements of the array.
     * 判断一个数组中是否包含了指定数据
     * @param array the array to check in
     * @param value the value to check for
     * @return true if the value is present in the array
     */
    public static <T> boolean contains(T[] array, T value) {
        for (T element : array) {
            if (element == null) {
                if (value == null) return true;
            } else {
                if (value != null && element.equals(value)) return true;
            }
        }
        return false;
    }
}
