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

package android.os;

/**
 * 一个比较方便的类，用于启动一个新的thread，该thread拥有一个looper，looper可以被用于创建一个handler类，start()
 * 方法仍然需要被调用。 <br>
 * Handy class for starting a new thread that has a looper. The looper can then
 * be used to create handler classes. Note that start() must still be called.
 */
public class HandlerThread extends Thread {
	/**
	 * 优先级
	 */
	private int mPriority;
	/**
	 * 线程id
	 */
	private int mTid = -1;
	/**
	 * 用于创建handler的looper
	 */
	private Looper mLooper;

	public HandlerThread(String name) {
		super(name);
		mPriority = Process.THREAD_PRIORITY_DEFAULT;
	}

	/**
	 * Constructs a HandlerThread.
	 * 
	 * @param name
	 * @param priority
	 *            The priority to run the thread at. The value supplied must be
	 *            from {@link android.os.Process} and not from java.lang.Thread.
	 */
	public HandlerThread(String name, int priority) {
		super(name);
		mPriority = priority;
	}

	/**
	 * Call back method that can be explicitly over ridden if needed to execute
	 * some setup before Looper loops.
	 */
	protected void onLooperPrepared() {
	}

	public void run() {
		//获取线程ID
		mTid = Process.myTid();
		//looper准备
		Looper.prepare();
		synchronized (this) {
			//获取与当前线程相关的looper
			mLooper = Looper.myLooper();
			notifyAll();
		}
		Process.setThreadPriority(mPriority);
		onLooperPrepared();
		Looper.loop();
		mTid = -1;
	}

	/**
	 * 返回与当前线程相关的looper。
	 * <br>
	 * This method returns the Looper associated with this thread. If this
	 * thread not been started or for any reason is isAlive() returns false,
	 * this method will return null. If this thread has been started, this
	 * method will block until the looper has been initialized.
	 * 
	 * @return The looper.
	 */
	public Looper getLooper() {
		if (!isAlive()) {
			return null;
		}

		// If the thread has been started, wait until the looper has been
		// created.
		synchronized (this) {
			while (isAlive() && mLooper == null) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		}
		return mLooper;
	}

	/**
	 * 请求当前正在运行的looper退出。
	 * <br>
	 * Ask the currently running looper to quit. If the thread has not been
	 * started or has finished (that is if {@link #getLooper} returns null),
	 * then false is returned. Otherwise the looper is asked to quit and true is
	 * returned.
	 */
	public boolean quit() {
		Looper looper = getLooper();
		if (looper != null) {
			looper.quit();
			return true;
		}
		return false;
	}

	/**
	 * Returns the identifier of this thread. See Process.myTid().
	 */
	public int getThreadId() {
		return mTid;
	}
}
