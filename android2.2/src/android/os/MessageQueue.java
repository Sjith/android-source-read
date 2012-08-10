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

import java.util.ArrayList;

import android.util.AndroidRuntimeException;
import android.util.Config;
import android.util.Log;

import com.android.internal.os.RuntimeInit;

/**
 * 消息队列，用于保存消息<br>
 * 底层类，用于保持由{@link Looper}分发的消息队列。消息并没有直接发送给MessageQueue,
 * 而是通过{@link Handler}来与looper相关联
 * Low-level class holding the list of messages to be dispatched by a
 * {@link Looper}.  Messages are not added directly to a MessageQueue,
 * but rather through {@link Handler} objects associated with the Looper.
 * <p>You can retrieve the MessageQueue for the current thread with
 * {@link Looper#myQueue() Looper.myQueue()}.
 * <br>通过使用{@link Looper#myQueue() Looper.myQueue()}方法获取当前线程的MessageQueue
 */
public class MessageQueue {
	//消息
    Message mMessages;
    /**
     * 空闲Handler的队列
     */
    private final ArrayList mIdleHandlers = new ArrayList();
    /**
     * 是否已经退出
     */
    private boolean mQuiting = false;
    /**
     * 是否允许退出
     */
    boolean mQuitAllowed = true;
    
    /**
     * Callback interface for discovering when a thread is going to block
     * waiting for more messages.
     * <br>回调接口，当发现一个线程已经没有消息，正在等待其他的消息时进行回调使用的接口
     */
    public static interface IdleHandler {
        /**
         * Called when the message queue has run out of messages and will now
         * wait for more.  Return true to keep your idle handler active, false
         * to have it removed.  This may be called if there are still messages
         * pending in the queue, but they are all scheduled to be dispatched
         * after the current time.
         * <br>当消息队列已经没有消息并且将要等待更多的消息时被调用，如果返回true则会保持空闲的handler
         * 处于活跃状态，否则将其移除。该函数也可能被调用当消息队列中仍然存在消息，但是这些消息仍然会被分发出去。
         */
        boolean queueIdle();
    }

    /**
     * Add a new {@link IdleHandler} to this message queue.  This may be
     * removed automatically for you by returning false from
     * {@link IdleHandler#queueIdle IdleHandler.queueIdle()} when it is
     * invoked, or explicitly removing it with {@link #removeIdleHandler}.
     * <br>添加一个{@link IdleHandler}到本消息队列，如果{@link IdleHandler#queueIdle IdleHandler.queueIdle()} 
     * 返回false且该函数被调用，或者明确调用{@link #removeIdleHandler}将会删除此IdleHandler
     * <p>This method is safe to call from any thread.
     * 
     * @param handler The IdleHandler to be added.
     */
    public final void addIdleHandler(IdleHandler handler) {
        if (handler == null) {
            throw new NullPointerException("Can't add a null IdleHandler");
        }
        synchronized (this) {
            mIdleHandlers.add(handler);
        }
    }

    /**
     * Remove an {@link IdleHandler} from the queue that was previously added
     * with {@link #addIdleHandler}.  If the given object is not currently
     * in the idle list, nothing is done.
     * <br>删除指定的IdleHandler
     * @param handler The IdleHandler to be removed.
     */
    public final void removeIdleHandler(IdleHandler handler) {
        synchronized (this) {
            mIdleHandlers.remove(handler);
        }
    }

    MessageQueue() {
    }
    
    /**
     * 获取下一个消息
     * @return
     */
    final Message next() {
        boolean tryIdle = true;

        while (true) {
            long now;
            Object[] idlers = null;
    
            // Try to retrieve the next message, returning if found.
            synchronized (this) {
            	//当前的已经开机毫秒数
                now = SystemClock.uptimeMillis();
                //获取下一个需要执行的消息
                Message msg = pullNextLocked(now);
                if (msg != null) return msg;
                if (tryIdle && mIdleHandlers.size() > 0) {
                    idlers = mIdleHandlers.toArray();
                }
            }
    
            // There was no message so we are going to wait...  but first,
            // if there are any idle handlers let them know.
            //看是否有空闲的handler，如果存在则通知他们
            boolean didIdle = false;
            if (idlers != null) {
                for (Object idler : idlers) {
                    boolean keep = false;
                    try {
                        didIdle = true;
                        keep = ((IdleHandler)idler).queueIdle();
                    } catch (Throwable t) {
                        Log.wtf("MessageQueue", "IdleHandler threw exception", t);
                    }

                    if (!keep) {
                        synchronized (this) {
                            mIdleHandlers.remove(idler);
                        }
                    }
                }
            }
            
            // While calling an idle handler, a new message could have been
            // delivered...  so go back and look again for a pending message.
            if (didIdle) {
                tryIdle = false;
                continue;
            }

            synchronized (this) {
                // No messages, nobody to tell about it...  time to wait!
                try {
                    if (mMessages != null) {
                        if (mMessages.when-now > 0) {
                            Binder.flushPendingCommands();
                            this.wait(mMessages.when-now);
                        }
                    } else {
                        Binder.flushPendingCommands();
                        this.wait();
                    }
                }
                catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * 获取下一个需要执行的且在第一个需要执行的message执行时间之后的第一个message.
     * @param now
     * @return
     */
    final Message pullNextLocked(long now) {
        Message msg = mMessages;
        if (msg != null) {
            if (now >= msg.when) {
                mMessages = msg.next;
                if (Config.LOGV) Log.v(
                    "MessageQueue", "Returning message: " + msg);
                return msg;
            }
        }

        return null;
    }

    /**
     * 插入一个消息
     * @param msg 消息
     * @param when 执行message的时间，基于{@link android.os.SystemClock#uptimeMillis}。
     * @return
     */
    final boolean enqueueMessage(Message msg, long when) {
    	//判断是否message的when是否为0，如果不为0则抛出异常
        if (msg.when != 0) {
            throw new AndroidRuntimeException(msg
                    + " This message is already in use.");
        }
        //如果执行message没有执行目标并且不允许退出则抛出异常。
        if (msg.target == null && !mQuitAllowed) {
            throw new RuntimeException("Main thread not allowed to quit");
        }
        synchronized (this) {
        	//如果已经退出则抛出异常
            if (mQuiting) {
                RuntimeException e = new RuntimeException(
                    msg.target + " sending message to a Handler on a dead thread");
                Log.w("MessageQueue", e.getMessage(), e);
                return false;
            } else if (msg.target == null) {
                mQuiting = true;
            }
            //给message设置执行时间
            msg.when = when;
            //Log.d("MessageQueue", "Enqueing: " + msg);
            Message p = mMessages;
            //如果为需要立即执行的消息，则将message插入到第一个.p==null表示没有其他需要等待消息。when ==0 || when < p.when表示需要立即执行
            if (p == null || when == 0 || when < p.when) {
                msg.next = p;
                mMessages = msg;
                this.notify();
            } else {
            	//插入到合适的位置，链表插入操作
                Message prev = null;
                while (p != null && p.when <= when) {
                    prev = p;
                    p = p.next;
                }
                msg.next = prev.next;
                prev.next = msg;
                this.notify();
            }
        }
        return true;
    }

    final boolean removeMessages(Handler h, int what, Object object,
            boolean doRemove) {
        synchronized (this) {
            Message p = mMessages;
            boolean found = false;
            //删除操作分成两部分，第一部分删除前端，这一部分message的target，what，object都与参数相匹配。
            //后一部分至少有一个与参数不匹配，需要删除此部分中的所有与参数匹配的message.            
            //这是由于需要保证mMessage指向参数不匹配的第一个message。从此message开始分成前端和后端两部分。
            // Remove all messages at front.
            while (p != null && p.target == h && p.what == what
                   && (object == null || p.obj == object)) {
                if (!doRemove) return true;
                found = true;
                Message n = p.next;
                mMessages = n;
                p.recycle();
                p = n;
            }

            // Remove all messages after front.
            while (p != null) {
                Message n = p.next;
                if (n != null) {
                    if (n.target == h && n.what == what
                        && (object == null || n.obj == object)) {
                    	//如果找到相匹配的则删除
                        if (!doRemove) return true;
                        found = true;
                        Message nn = n.next;
                        n.recycle();
                        p.next = nn;
                        continue;
                    }
                }
                //向后移动
                p = n;
            }
            
            return found;
        }
    }

    final void removeMessages(Handler h, Runnable r, Object object) {
        if (r == null) {
            return;
        }

        synchronized (this) {
            Message p = mMessages;

            // Remove all messages at front.
            while (p != null && p.target == h && p.callback == r
                   && (object == null || p.obj == object)) {
                Message n = p.next;
                mMessages = n;
                p.recycle();
                p = n;
            }

            // Remove all messages after front.
            while (p != null) {
                Message n = p.next;
                if (n != null) {
                    if (n.target == h && n.callback == r
                        && (object == null || n.obj == object)) {
                        Message nn = n.next;
                        n.recycle();
                        p.next = nn;
                        continue;
                    }
                }
                p = n;
            }
        }
    }

    final void removeCallbacksAndMessages(Handler h, Object object) {
        synchronized (this) {
            Message p = mMessages;

            // Remove all messages at front.
            while (p != null && p.target == h
                    && (object == null || p.obj == object)) {
                Message n = p.next;
                mMessages = n;
                p.recycle();
                p = n;
            }

            // Remove all messages after front.
            while (p != null) {
                Message n = p.next;
                if (n != null) {
                    if (n.target == h && (object == null || n.obj == object)) {
                        Message nn = n.next;
                        n.recycle();
                        p.next = nn;
                        continue;
                    }
                }
                p = n;
            }
        }
    }

    /*
    private void dumpQueue_l()
    {
        Message p = mMessages;
        System.out.println(this + "  queue is:");
        while (p != null) {
            System.out.println("            " + p);
            p = p.next;
        }
    }
    */

    void poke()
    {
        synchronized (this) {
            this.notify();
        }
    }
}
