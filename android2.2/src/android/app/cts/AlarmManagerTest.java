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

package android.app.cts;

import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.app.cts.ISecondary;
import android.test.AndroidTestCase;

import dalvik.annotation.BrokenTest;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestStatus;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.ToBeFixed;

@TestTargetClass(AlarmManager.class)
public class AlarmManagerTest extends AndroidTestCase {
    private AlarmManager mAlarmManager;
    private Intent mIntent;
    private PendingIntent mSender;
    private Intent mServiceIntent;

    /*
     *  The default snooze delay: 5 seconds
     */
    private final long SNOOZE_DELAY = 5 * 1000L;
    private long mWakeupTime;
    private MockAlarmReceiver mMockAlarmReceiver;

    private Sync mSync;

    private final int TIME_DELTA = 200;
    private final int TIME_DELAY = 2000;
    private ISecondary mSecondaryService = null;

    class Sync {
        public boolean mIsConnected;
        public boolean mIsDisConnected;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mIntent = new Intent(MockAlarmReceiver.MOCKACTION);
        mSender = PendingIntent.getBroadcast(mContext, 0, mIntent, 0);
        mMockAlarmReceiver = new MockAlarmReceiver();
        IntentFilter filter = new IntentFilter(MockAlarmReceiver.MOCKACTION);
        mContext.registerReceiver(mMockAlarmReceiver, filter);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mServiceIntent != null) {
            mContext.stopService(mServiceIntent);
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setTimeZone",
        args = {java.lang.String.class}
    )
    @BrokenTest("Broken by CL148448. Default timezone of the test and the service differ.")
    public void testSetTimeZone() throws Exception {
        mSync = new Sync();
        final String ACTION = "android.app.REMOTESERVICE";
        mServiceIntent = new Intent(ACTION);
        mContext.startService(mServiceIntent);
        mContext.bindService(new Intent(ISecondary.class.getName()), mSecondaryConnection,
                Context.BIND_AUTO_CREATE);

        synchronized (mSync) {
            if (!mSync.mIsConnected) {
                mSync.wait();
            }
        }
        final TimeZone currentZone = TimeZone.getDefault();

        // test timeZone is null, timeZone won't be set
        String timeZone = null;
        mAlarmManager.setTimeZone(timeZone);
        TimeZone values = TimeZone.getDefault();
        assertEquals(currentZone.getID(), values.getID());
        // test another process's timezone

        assertEquals(currentZone.getID(), mSecondaryService.getTimeZoneID());

        // nothing in timZone, timeZone won't be set
        timeZone = "";
        mAlarmManager.setTimeZone(timeZone);
        values = TimeZone.getDefault();
        assertEquals(currentZone.getID(), values.getID());
        // test timeZone as different time zone
        String[] timeZones = TimeZone.getAvailableIDs();
        // set different time zone
        timeZone = currentZone.getID().equals(timeZones[0]) ? timeZones[1] : timeZones[0];
        mAlarmManager.setTimeZone(timeZone);
        Thread.sleep(TIME_DELAY);
        values = TimeZone.getDefault();
        TimeZone zone = TimeZone.getTimeZone(timeZone);
        assertEquals(zone.getID(), values.getID());

        // test another process's timezone
        assertEquals(zone.getID(), mSecondaryService.getTimeZoneID());

        // set time zone as origin time zone
        TimeZone.setDefault(currentZone);

        mContext.stopService(mServiceIntent);
        mServiceIntent = null;
    }

    private ServiceConnection mSecondaryConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSecondaryService = ISecondary.Stub.asInterface(service);
            synchronized (mSync) {
                mSync.mIsConnected = true;
                mSync.notify();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mSecondaryService = null;
            synchronized (mSync) {
                mSync.mIsDisConnected = true;
                mSync.notify();
            }
        }
    };

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "set",
        args = {int.class, long.class, android.app.PendingIntent.class}
    )
    @ToBeFixed(bug = "1475410", explanation = "currently if make a device sleep android"
            + "framework will throw out exception")
    public void testSetTypes() throws Exception {
        // TODO: try to find a way to make device sleep then test whether
        // AlarmManager perform the expected way

        // test parameter type is RTC_WAKEUP
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = System.currentTimeMillis() + SNOOZE_DELAY;
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, mWakeupTime, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        assertEquals(mMockAlarmReceiver.rtcTime, mWakeupTime, TIME_DELTA);

        // test parameter type is RTC
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = System.currentTimeMillis() + SNOOZE_DELAY;
        mAlarmManager.set(AlarmManager.RTC, mWakeupTime, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        assertEquals(mMockAlarmReceiver.rtcTime, mWakeupTime, TIME_DELTA);

        // test parameter type is ELAPSED_REALTIME
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = SystemClock.elapsedRealtime() + SNOOZE_DELAY;
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME, mWakeupTime, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        assertEquals(mMockAlarmReceiver.elapsedTime, mWakeupTime, TIME_DELTA);

        // test parameter type is ELAPSED_REALTIME_WAKEUP
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = SystemClock.elapsedRealtime() + SNOOZE_DELAY;
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, mWakeupTime, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        assertEquals(mMockAlarmReceiver.elapsedTime, mWakeupTime, TIME_DELTA);
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "set",
        args = {int.class, long.class, android.app.PendingIntent.class}
    )
    @ToBeFixed(bug = "1475410", explanation = "currently if make a device sleep android"
            + "framework will throw out exception")
    public void testAlarmTriggersImmediatelyIfSetTimeIsNegative() throws Exception {
        // An alarm with a negative wakeup time should be triggered immediately.
        // This exercises a workaround for a limitation of the /dev/alarm driver
        // that would instead cause such alarms to never be triggered.
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = -1000;
        mAlarmManager.set(AlarmManager.RTC, mWakeupTime, mSender);
        Thread.sleep(TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
    }
    
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "setRepeating",
        args = {int.class, long.class, long.class, android.app.PendingIntent.class}
    )
    @ToBeFixed(bug = "1475410", explanation = "currently if make a device sleep android"
            + "framework will throw out exception")
    public void testSetRepeating() throws Exception {
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = System.currentTimeMillis() + SNOOZE_DELAY;
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mWakeupTime, TIME_DELAY / 2, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        mMockAlarmReceiver.setAlarmedFalse();
        Thread.sleep(TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        mAlarmManager.cancel(mSender);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "cancel",
        args = {android.app.PendingIntent.class}
    )
    public void testCancel() throws Exception {
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = System.currentTimeMillis() + SNOOZE_DELAY;
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mWakeupTime, 1000, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        mMockAlarmReceiver.setAlarmedFalse();
        Thread.sleep(TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        mAlarmManager.cancel(mSender);
        Thread.sleep(TIME_DELAY);
        mMockAlarmReceiver.setAlarmedFalse();
        Thread.sleep(TIME_DELAY * 5);
        assertFalse(mMockAlarmReceiver.alarmed);
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "setInexactRepeating",
        args = {int.class, long.class, long.class, android.app.PendingIntent.class}
    )
    @ToBeFixed(bug="1556930", explanation="no way to set the system clock")
    public void testSetInexactRepeating() throws Exception {

        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, mSender);
        SystemClock.setCurrentTimeMillis(System.currentTimeMillis()
                + AlarmManager.INTERVAL_FIFTEEN_MINUTES);
        // currently there is no way to write Android system clock. When try to
        // write the system time, there will be log as
        // " Unable to open alarm driver: Permission denied". But still fail
        // after tried many permission.
    }
}
