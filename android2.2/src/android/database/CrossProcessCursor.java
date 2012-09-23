/*
 * Copyright (C) 2008 The Android Open Source Project
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

package android.database;

public interface CrossProcessCursor extends Cursor {
	/**
	 * returns a pre-filled window, return NULL if no such window <br>
	 * 返回一个已经实现填充的window，如果没有这种window则返回NULL
	 */
	CursorWindow getWindow();

	/**
	 * copies cursor data into the window start at pos <br>
	 * 将cursor中的data拷贝到window中，从pos开始
	 */
	void fillWindow(int pos, CursorWindow winow);

	/**
	 * 当cursor移动到新的位置的时候本函数被调用，给子类提供了更改它需要更改的状态的机会，如果返回false，则cursor的move操作同样执行，
	 * 并且滚动到beforeFirst位置 <br>
	 * This function is called every time the cursor is successfully scrolled to
	 * a new position, giving the subclass a chance to update any state it may
	 * have. If it returns false the move function will also do so and the
	 * cursor will scroll to the beforeFirst position.
	 * 
	 * @param oldPosition
	 *            the position that we're moving from
	 * @param newPosition
	 *            the position that we're moving to
	 * @return true if the move is successful, false otherwise
	 */
	boolean onMove(int oldPosition, int newPosition);

}
