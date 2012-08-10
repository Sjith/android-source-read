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

import android.util.Config;
import android.util.Printer;

/**
 * 消息泵，用于从MessageQueue中取出消息，并将消息分发给目标。
 * 用于thread执行一个消息循环的类，Thread默认的没有消息循环。创建Looper，通过在线程中调用{@link #prepare}
 * 方法，该线程将执行循环操作，然后调用{@link #loop}使线程处理消息，直到循环结束。
 * Class used to run a message loop for a thread. Threads by default do not have
 * a message loop associated with them; to create one, call {@link #prepare} in
 * the thread that is to run the loop, and then {@link #loop} to have it process
 * messages until the loop is stopped.
 * 
 * <p>
 * Most interaction with a message loop is through the {@link Handler} class.
 * 大部分的消息循环处理通过{@link Handler}类进行。
 * <p>
 * This is a typical example of the implementation of a Looper thread, using the
 * separation of {@link #prepare} and {@link #loop} to create an initial Handler
 * to communicate with the Looper.
 * 
 * <pre>
 * class LooperThread extends Thread {
 * 	public Handler mHandler;
 * 
 * 	public void run() {
 * 		Looper.prepare();
 * 
 * 		mHandler = new Handler() {
 * 			public void handleMessage(Message msg) {
 * 				// process incoming messages here
 * 			}
 * 		};
 * 
 * 		Looper.loop();
 * 	}
 * }
 * </pre>
 */
public class Looper {
	private static final boolean DEBUG = false;
	private static final boolean localLOGV = DEBUG ? Config.LOGD : Config.LOGV;

	// sThreadLocal.get() will return null unless you've called prepare().
	//如果没有执行过prepare()方法，则返回null
	private static final ThreadLocal sThreadLocal = new ThreadLocal();
	
	/**
	 * 匹配的消息队列
	 */
	final MessageQueue mQueue;
	volatile boolean mRun;
	//线程
	Thread mThread;
	private Printer mLogging = null;
	/**
	 * 主线程的looper
	 */
	private static Looper mMainLooper = null;

	/**
	 * Initialize the current thread as a looper. This gives you a chance to
	 * create handlers that then reference this looper, before actually starting
	 * the loop. Be sure to call {@link #loop()} after calling this method, and
	 * end it by calling {@link #quit()}.
	 */
	public static final void prepare() {
		if (sThreadLocal.get() != null) {
			throw new RuntimeException(
					"Only one Looper may be created per thread");
		}
		sThreadLocal.set(new Looper());
	}

	/**
	 * 设置MainLooper。该方法由android系统调用，请不要调用此方法。
	 * Initialize the current thread as a looper, marking it as an application's
	 * main looper. The main looper for your application is created by the
	 * Android environment, so you should never need to call this function
	 * yourself. {@link #prepare()}
	 */

	public static final void prepareMainLooper() {
		prepare();
		setMainLooper(myLooper());
		if (Process.supportsProcesses()) {
			myLooper().mQueue.mQuitAllowed = false;
		}
	}

	private synchronized static void setMainLooper(Looper looper) {
		mMainLooper = looper;
	}

	/**
	 * Returns the application's main looper, which lives in the main thread of
	 * the application.
	 * <br>获取应用程序的主Looper，与UI线程相关。
	 */
	public synchronized static final Looper getMainLooper() {
		return mMainLooper;
	}

	/**
	 * Run the message queue in this thread. Be sure to call {@link #quit()} to
	 * end the loop.
	 * <br>运行当前线程的消息队列，记得调用{@link #quit()}结束循环
	 */
	public static final void loop() {
		//获取当前线程的looper
		Looper me = myLooper();
		MessageQueue queue = me.mQueue;
		while (true) {
			//获取下一个消息
			Message msg = queue.next(); // might block
			// if (!me.mRun) {
			// break;
			// }
			if (msg != null) {
				if (msg.target == null) {
					// No target is a magic identifier for the quit message.
					return;
				}
				if (me.mLogging != null)
					me.mLogging.println(">>>>> Dispatching to " + msg.target
							+ " " + msg.callback + ": " + msg.what);
				//分发消息
				msg.target.dispatchMessage(msg);
				if (me.mLogging != null)
					me.mLogging.println("<<<<< Finished to    " + msg.target
							+ " " + msg.callback);
				//清空消息
				msg.recycle();
			}
		}
	}

	/**
	 * Return the Looper object associated with the current thread. Returns null
	 * if the calling thread is not associated with a Looper.
	 * <br>获取与当前线程相关的Looper。
	 */
	public static final Looper myLooper() {
		return (Looper) sThreadLocal.get();
	}

	/**
	 * Control logging of messages as they are processed by this Looper. If
	 * enabled, a log message will be written to <var>printer</var> at the
	 * beginning and ending of each message dispatch, identifying the target
	 * Handler and message contents.
	 * 
	 * @param printer
	 *            A Printer object that will receive log messages, or null to
	 *            disable message logging.
	 */
	public void setMessageLogging(Printer printer) {
		mLogging = printer;
	}

	/**
	 * Return the {@link MessageQueue} object associated with the current
	 * thread. This must be called from a thread running a Looper, or a
	 * NullPointerException will be thrown.
	 */
	public static final MessageQueue myQueue() {
		return myLooper().mQueue;
	}

	private Looper() {
		mQueue = new MessageQueue();
		mRun = true;
		mThread = Thread.currentThread();
	}

	public void quit() {
		Message msg = Message.obtain();
		// NOTE: By enqueueing directly into the message queue, the
		// message is left with a null target. This is how we know it is
		// a quit message.
		//将消息插入队列
		mQueue.enqueueMessage(msg, 0);
	}

	/**
	 * Return the Thread associated with this Looper.
	 */
	public Thread getThread() {
		return mThread;
	}

	public void dump(Printer pw, String prefix) {
		pw.println(prefix + this);
		pw.println(prefix + "mRun=" + mRun);
		pw.println(prefix + "mThread=" + mThread);
		pw.println(prefix + "mQueue=" + ((mQueue != null) ? mQueue : "(null"));
		if (mQueue != null) {
			synchronized (mQueue) {
				Message msg = mQueue.mMessages;
				int n = 0;
				while (msg != null) {
					pw.println(prefix + "  Message " + n + ": " + msg);
					n++;
					msg = msg.next;
				}
				pw.println(prefix + "(Total messages: " + n + ")");
			}
		}
	}

	public String toString() {
		return "Looper{" + Integer.toHexString(System.identityHashCode(this))
				+ "}";
	}

	static class HandlerException extends Exception {

		HandlerException(Message message, Throwable cause) {
			super(createMessage(cause), cause);
		}

		static String createMessage(Throwable cause) {
			String causeMsg = cause.getMessage();
			if (causeMsg == null) {
				causeMsg = cause.toString();
			}
			return causeMsg;
		}
	}
}
