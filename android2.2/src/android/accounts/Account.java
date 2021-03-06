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

package android.accounts;

import android.os.Parcelable;
import android.os.Parcel;
import android.text.TextUtils;

/**
 * Value type that represents an Account in the {@link AccountManager}. This object is
 * {@link Parcelable} and also overrides {@link #equals} and {@link #hashCode}, making it
 * suitable for use as the key of a {@link java.util.Map}
 * <br>值类型，代表{@link AccountManager}管理的一个账户，该对象是{@link Parcelable}同时重写了{@Link #equals}
 * 和{@lin #hashCode}方法，使之可以作为{@link java.util.Map}的key
 */
public class Account implements Parcelable {
	/**
	 * 名称
	 */
    public final String name;
    /**
     * 类型
     */
    public final String type;

    public boolean equals(Object o) {
    	//判断时候和this相等
        if (o == this) return true;
        //判断是否为Account类型
        if (!(o instanceof Account)) return false;
        //判断name和type是否相等
        final Account other = (Account)o;
        return name.equals(other.name) && type.equals(other.type);
    }

    public int hashCode() {
        int result = 17;
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    public Account(String name, String type) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("the name must not be empty: " + name);
        }
        if (TextUtils.isEmpty(type)) {
            throw new IllegalArgumentException("the type must not be empty: " + type);
        }
        this.name = name;
        this.type = type;
    }

    public Account(Parcel in) {
        this.name = in.readString();
        this.type = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(type);
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    public String toString() {
        return "Account {name=" + name + ", type=" + type + "}";
    }
}
