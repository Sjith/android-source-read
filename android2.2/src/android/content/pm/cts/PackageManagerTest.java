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

package android.content.pm.cts;

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.content.pm.IPackageInstallObserver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.provider.Contacts.People;
import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This test is based on the declarations in AndroidManifest.xml. We create mock declarations
 * in AndroidManifest.xml just for test of PackageManager, and there are no corresponding parts
 * of these declarations in test project.
 */
@TestTargetClass(PackageManager.class)
public class PackageManagerTest extends AndroidTestCase {
    private PackageManager mPackageManager;
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    private static final String CONTENT_PKG_NAME = "com.android.cts.content";
    private static final String ACTIVITY_ACTION_NAME = "android.intent.action.PMTEST";
    private static final String MAIN_ACTION_NAME = "android.intent.action.MAIN";
    private static final String SERVICE_ACTION_NAME =
                                "android.content.pm.cts.activity.PMTEST_SERVICE";
    private static final String PERMISSION_NAME = "android.permission.INTERNET";
    private static final String ACTIVITY_NAME = "android.content.pm.cts.TestPmActivity";
    private static final String SERVICE_NAME = "android.content.pm.cts.TestPmService";
    private static final String RECEIVER_NAME = "android.content.pm.cts.PmTestReceiver";
    private static final String INSTRUMENT_NAME = "android.content.pm.cts.TestPmInstrumentation";
    private static final String PROVIDER_NAME = "android.content.cts.MockContentProvider";
    private static final String PERMISSIONGROUP_NAME = "android.permission-group.COST_MONEY";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPackageManager = getContext().getPackageManager();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryIntentActivityOptions",
            method = "queryIntentActivityOptions",
            args = {android.content.ComponentName.class, android.content.Intent[].class,
                    android.content.Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryIntentActivities",
            method = "queryIntentActivities",
            args = {android.content.Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryInstrumentation",
            method = "queryInstrumentation",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryBroadcastReceivers",
            method = "queryBroadcastReceivers",
            args = {android.content.Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryPermissionsByGroup",
            method = "queryPermissionsByGroup",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryContentProviders",
            method = "queryContentProviders",
            args = {java.lang.String.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryIntentServices",
            method = "queryIntentServices",
            args = {android.content.Intent.class, int.class}
        )
    })
    public void testQuery() throws NameNotFoundException {
        // Test query Intent Activity related methods

        Intent activityIntent = new Intent(ACTIVITY_ACTION_NAME);
        String cmpActivityName = "android.content.pm.cts.TestPmCompare";
        // List with different activities and the filter doesn't work,
        List<ResolveInfo> listWithDiff = mPackageManager.queryIntentActivityOptions(
                new ComponentName(PACKAGE_NAME, cmpActivityName), null, activityIntent, 0);
        checkActivityInfoName(ACTIVITY_NAME, listWithDiff);

        // List with the same activities to make filter work
        List<ResolveInfo> listInSame = mPackageManager.queryIntentActivityOptions(
                new ComponentName(PACKAGE_NAME, ACTIVITY_NAME), null, activityIntent, 0);
        assertEquals(0, listInSame.size());

        // Test queryIntentActivities
        List<ResolveInfo> intentActivities =
                mPackageManager.queryIntentActivities(activityIntent, 0);
        assertTrue(intentActivities.size() > 0);
        checkActivityInfoName(ACTIVITY_NAME, intentActivities);

        // End of Test query Intent Activity related methods

        // Test queryInstrumentation
        String targetPackage = "android";
        List<InstrumentationInfo> instrumentations = mPackageManager.queryInstrumentation(
                targetPackage, 0);
        checkInstrumentationInfoName(INSTRUMENT_NAME, instrumentations);

        // Test queryIntentServices
        Intent serviceIntent = new Intent(SERVICE_ACTION_NAME);
        List<ResolveInfo> services = mPackageManager.queryIntentServices(serviceIntent,
                PackageManager.GET_INTENT_FILTERS);
        checkServiceInfoName(SERVICE_NAME, services);

        // Test queryBroadcastReceivers
        String receiverActionName = "android.content.pm.cts.PackageManagerTest.PMTEST_RECEIVER";
        Intent broadcastIntent = new Intent(receiverActionName);
        List<ResolveInfo> broadcastReceivers = new ArrayList<ResolveInfo>();
        broadcastReceivers = mPackageManager.queryBroadcastReceivers(broadcastIntent, 0);
        checkActivityInfoName(RECEIVER_NAME, broadcastReceivers);

        // Test queryPermissionsByGroup, queryContentProviders
        String testPermissionsGroup = "android.permission-group.NETWORK";
        List<PermissionInfo> permissions = mPackageManager.queryPermissionsByGroup(
                testPermissionsGroup, PackageManager.GET_META_DATA);
        checkPermissionInfoName(PERMISSION_NAME, permissions);

        ApplicationInfo appInfo = mPackageManager.getApplicationInfo(PACKAGE_NAME, 0);
        List<ProviderInfo> providers = mPackageManager.queryContentProviders(PACKAGE_NAME,
                appInfo.uid, 0);
        checkProviderInfoName(PROVIDER_NAME, providers);
    }

    private void checkActivityInfoName(String expectedName, List<ResolveInfo> resolves) {
        // Flag for checking if the name is contained in list array.
        boolean isContained = false;
        Iterator<ResolveInfo> infoIterator = resolves.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().activityInfo.name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }

    private void checkServiceInfoName(String expectedName, List<ResolveInfo> resolves) {
        boolean isContained = false;
        Iterator<ResolveInfo> infoIterator = resolves.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().serviceInfo.name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }

    private void checkPermissionInfoName(String expectedName, List<PermissionInfo> permissions) {
        boolean isContained = false;
        Iterator<PermissionInfo> infoIterator = permissions.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }

    private void checkProviderInfoName(String expectedName, List<ProviderInfo> providers) {
        boolean isContained = false;
        Iterator<ProviderInfo> infoIterator = providers.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }

    private void checkInstrumentationInfoName(String expectedName,
            List<InstrumentationInfo> instrumentations) {
        boolean isContained = false;
        Iterator<InstrumentationInfo> infoIterator = instrumentations.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPackageInfo",
            method = "getPackageInfo",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getApplicationInfo",
            method = "getApplicationInfo",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getApplicationInfo",
            method = "getApplicationLabel",
            args = {android.content.pm.ApplicationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getServiceInfo",
            method = "getServiceInfo",
            args = {android.content.ComponentName.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPackageArchiveInfo",
            method = "getPackageArchiveInfo",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPackagesForUid",
            method = "getPackagesForUid",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getNameForUid",
            method = "getNameForUid",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getActivityInfo",
            method = "getActivityInfo",
            args = {android.content.ComponentName.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPackageGids",
            method = "getPackageGids",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPermissionInfo",
            method = "getPermissionInfo",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPermissionGroupInfo",
            method = "getPermissionGroupInfo",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getAllPermissionGroups",
            method = "getAllPermissionGroups",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getInstalledApplications",
            method = "getInstalledApplications",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getInstalledPackages",
            method = "getInstalledPackages",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getInstrumentationInfo",
            method = "getInstrumentationInfo",
            args = {android.content.ComponentName.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getReceiverInfo",
            method = "getReceiverInfo",
            args = {android.content.ComponentName.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getText",
            method = "getText",
            args = {java.lang.String.class, int.class, android.content.pm.ApplicationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Test getSystemSharedLibraryNames",
            method = "getSystemSharedLibraryNames",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getLaunchIntentForPackage",
            method = "getLaunchIntentForPackage",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test isSafeMode",
            method = "isSafeMode",
            args = {}
        )
    })
    @ToBeFixed(bug="1686810", explanation="From the doc of function getSystemSharedLibraryNames()."
        + " It will return an String array or null(both are Ok).")
    public void testGetInfo() throws NameNotFoundException {
        // Test getApplicationInfo, getText
        ApplicationInfo appInfo = mPackageManager.getApplicationInfo(PACKAGE_NAME, 0);
        int discriptionRes = R.string.hello_android;
        String expectedDisciptionRes = "Hello, Android!";
        CharSequence appText = mPackageManager.getText(PACKAGE_NAME, discriptionRes, appInfo);
        assertEquals(expectedDisciptionRes, appText);
        ComponentName activityName = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
        ComponentName serviceName = new ComponentName(PACKAGE_NAME, SERVICE_NAME);
        ComponentName receiverName = new ComponentName(PACKAGE_NAME, RECEIVER_NAME);
        ComponentName instrName = new ComponentName(PACKAGE_NAME, INSTRUMENT_NAME);

        // Test getPackageInfo
        PackageInfo packageInfo = mPackageManager.getPackageInfo(PACKAGE_NAME,
                PackageManager.GET_INSTRUMENTATION);
        assertEquals(PACKAGE_NAME, packageInfo.packageName);

        // Test getApplicationInfo, getApplicationLabel
        String appLabel = "Android TestCase";
        assertEquals(appLabel, mPackageManager.getApplicationLabel(appInfo));
        assertEquals(PACKAGE_NAME, appInfo.processName);

        // Test getServiceInfo
        assertEquals(SERVICE_NAME, mPackageManager.getServiceInfo(serviceName,
                PackageManager.GET_META_DATA).name);

        // Test getReceiverInfo
        assertEquals(RECEIVER_NAME, mPackageManager.getReceiverInfo(receiverName, 0).name);

        // Test getPackageArchiveInfo
        final String apkRoute = getContext().getPackageCodePath();
        final String apkName = getContext().getPackageName();
        assertEquals(apkName, mPackageManager.getPackageArchiveInfo(apkRoute, 0).packageName);

        // Test getPackagesForUid, getNameForUid
        checkPackagesNameForUid(PACKAGE_NAME, mPackageManager.getPackagesForUid(appInfo.uid));
        assertEquals(PACKAGE_NAME, mPackageManager.getNameForUid(appInfo.uid));

        // Test getActivityInfo
        assertEquals(ACTIVITY_NAME, mPackageManager.getActivityInfo(activityName, 0).name);

        // Test getPackageGids
        assertTrue(mPackageManager.getPackageGids(PACKAGE_NAME).length > 0);

        // Test getPermissionInfo
        assertEquals(PERMISSION_NAME, mPackageManager.getPermissionInfo(PERMISSION_NAME, 0).name);

        // Test getPermissionGroupInfo
        assertEquals(PERMISSIONGROUP_NAME, mPackageManager.getPermissionGroupInfo(
                PERMISSIONGROUP_NAME, 0).name);

        // Test getAllPermissionGroups
        List<PermissionGroupInfo> permissionGroups = mPackageManager.getAllPermissionGroups(0);
        checkPermissionGroupInfoName(PERMISSIONGROUP_NAME, permissionGroups);

        // Test getInstalledApplications
        assertTrue(mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA).size() > 0);

        // Test getInstalledPacakge
        assertTrue(mPackageManager.getInstalledPackages(0).size() > 0);

        // Test getInstrumentationInfo
        assertEquals(INSTRUMENT_NAME, mPackageManager.getInstrumentationInfo(instrName, 0).name);

        // Test getSystemSharedLibraryNames, in javadoc, String array and null
        // are all OK as return value.
        mPackageManager.getSystemSharedLibraryNames();

        // Test getLaunchIntentForPackage, Intent of activity
        // android.content.pm.cts.TestPmCompare is set to match the condition
        // to make sure the return of this method is not null.
        assertEquals(MAIN_ACTION_NAME, mPackageManager.getLaunchIntentForPackage(PACKAGE_NAME)
                .getAction());

        // Test isSafeMode. Because the test case will not run in safe mode, so
        // the return will be false.
        assertFalse(mPackageManager.isSafeMode());
    }

    private void checkPackagesNameForUid(String expectedName, String[] uid) {
        boolean isContained = false;
        for (int i = 0; i < uid.length; i++) {
            if (uid[i].equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }

    private void checkPermissionGroupInfoName(String expectedName,
            List<PermissionGroupInfo> permissionGroups) {
        boolean isContained = false;
        Iterator<PermissionGroupInfo> infoIterator = permissionGroups.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }


    /**
     * Simple test for {@link PackageManager#getPreferredActivities(List, List, String)} that tests
     * calling it has no effect. The method is essentially a no-op because no preferred activities
     * can be added.
     * @see PackageManager#addPreferredActivity(IntentFilter, int, ComponentName[], ComponentName)
     */
    public void testGetPreferredActivities() {
        assertNoPreferredActivities();
    }

    /**
     * Helper method to test that {@link PackageManager#getPreferredActivities(List, List, String)}
     * returns empty lists.
     */
    private void assertNoPreferredActivities() {
        List<ComponentName> outActivities = new ArrayList<ComponentName>();
        List<IntentFilter> outFilters = new ArrayList<IntentFilter>();
        mPackageManager.getPreferredActivities(outFilters, outActivities, PACKAGE_NAME);
        assertEquals(0, outActivities.size());
        assertEquals(0, outFilters.size());
    }

    /**
     * Test that calling {@link PackageManager#addPreferredActivity(IntentFilter, int,
     * ComponentName[], ComponentName)} throws a {@link SecurityException}.
     * <p/>
     * The method is protected by the {@link android.permission.SET_PREFERRED_APPLICATIONS}
     * signature permission. Even though this app declares that permission, it still should not be
     * able to call this method because it is not signed with the platform certificate.
     */
    public void testAddPreferredActivity() {
        IntentFilter intentFilter = new IntentFilter(ACTIVITY_ACTION_NAME);
        ComponentName[] componentName = {new ComponentName(PACKAGE_NAME, ACTIVITY_NAME)};
        try {
            mPackageManager.addPreferredActivity(intentFilter, IntentFilter.MATCH_CATEGORY_HOST,
                    componentName, componentName[0]);
            fail("addPreferredActivity unexpectedly succeeded");
        } catch (SecurityException e) {
            // expected
        }
        assertNoPreferredActivities();
    }

    /**
     * Test that calling {@link PackageManager#clearPackagePreferredActivities(String)} has no
     * effect.
     */
    public void testClearPackagePreferredActivities() {
        // just ensure no unexpected exceptions are thrown, nothing else to do
        mPackageManager.clearPackagePreferredActivities(PACKAGE_NAME);
    }

    private void checkComponentName(String expectedName, List<ComponentName> componentNames) {
        boolean isContained = false;
        Iterator<ComponentName> nameIterator = componentNames.iterator();
        String current;
        while (nameIterator.hasNext()) {
            current = nameIterator.next().getClassName();
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }

    private void checkIntentFilterAction(String expectedName, List<IntentFilter> intentFilters) {
        boolean isContained = false;
        Iterator<IntentFilter> filterIterator = intentFilters.iterator();
        IntentFilter currentFilter;
        String currentAction;
        while (filterIterator.hasNext()) {
            currentFilter = filterIterator.next();
            for (int i = 0; i < currentFilter.countActions(); i++) {
                currentAction = currentFilter.getAction(i);
                if (currentAction.equals(expectedName)) {
                    isContained = true;
                    break;
                }
            }
        }
        assertTrue(isContained);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setApplicationEnabledSetting",
            method = "setApplicationEnabledSetting",
            args = {java.lang.String.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getApplicationEnabledSetting",
            method = "getApplicationEnabledSetting",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setComponentEnabledSetting",
            method = "setComponentEnabledSetting",
            args = {android.content.ComponentName.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getComponentEnabledSetting",
            method = "getComponentEnabledSetting",
            args = {android.content.ComponentName.class}
        )
    })
    public void testAccessEnabledSetting() {
        mPackageManager.setApplicationEnabledSetting(PACKAGE_NAME,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                mPackageManager.getApplicationEnabledSetting(PACKAGE_NAME));

        ComponentName componentName = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
        mPackageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                mPackageManager.getComponentEnabledSetting(componentName));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test addPermission",
            method = "addPermission",
            args = {android.content.pm.PermissionInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test removePermission",
            method = "removePermission",
            args = {java.lang.String.class}
        )
    })
    @ToBeFixed(bug="1561181", explanation="According to javadoc of these two methods," +
            " we created a permission-tree and permission under this tree in our" +
            " AndroidManifest.xml, then invoked addPermission method, but there was" +
            " 'java.lang.SecurityException: Not allowed to modify non-dynamic permission'" +
            " exception. We found that" +
            " {@link com.android.server# PackageManagerService.addPermission" +
            " (also removePermission) is the very method with function same to our target" +
            " method, and in PackageManagerService.addPermission, there is a if-branch to check" +
            " whether 'type' equals to BasePermission.TYPE_DYNAMIC(L1004), and the 'type' is" +
            " parsed from /data/system/packages.xml in emulator, but we found no 'type'" +
            " tag in packages.xml, as well as no other explicit ways to add this 'type' tag," +
            " so we can't add permission in dynamic way.")
    public void testOpPermission() {
        PermissionInfo permissionInfo = new PermissionInfo();
        String permissionName = "com.android.cts.stub.permission.TEST_DYNAMIC.ADD";
        permissionInfo.name = permissionName;
        permissionInfo.labelRes = R.string.permlab_testDynamic;
        permissionInfo.nonLocalizedLabel = "Test Tree";

        // TODO: Bug ID 1561181.
        // Can't add permission in dynamic way
    }

    /**
     * Test that calling {@link PackageManager#addPackageToPreferred(String) throws a
     * {@link SecurityException}.
     * <p/>
     * The method is protected by the {@link android.permission.SET_PREFERRED_APPLICATIONS}
     * signature permission. Even though this app declares that permission, it still should not be
     * able to call this method.
     */
    public void testAddPackageToPreferred() {
        // Test addPackageToPreferred, getPreferredPackages
        List<PackageInfo> pkgInfo = null;
        pkgInfo = mPackageManager.getPreferredPackages(0);
        int pkgInfoSize = pkgInfo.size();
        try {
            // addPackageToPreferred is protected by the
            // android.permission.SET_PREFERRED_APPLICATIONS signature permission.
            // Even though this app declares that permission, it still should not be able to call
            // this
            mPackageManager.addPackageToPreferred(CONTENT_PKG_NAME);
            fail("addPackageToPreferred unexpectedly succeeded");
        } catch (SecurityException e) {
            // expected
        }
        pkgInfo = mPackageManager.getPreferredPackages(0);
        // addPackageToPreferred should have no effect
        assertEquals(pkgInfo.size(), pkgInfoSize);
    }

    /**
     * Test that calling {@link PackageManager#removePackageFromPreferred(String)} throws a
     * {@link SecurityException}.
     * <p/>
     * The method is protected by the {@link android.permission.SET_PREFERRED_APPLICATIONS}
     * signature permission. Even though this app declares that permission, it still should not be
     * able to call this method.
     */
    public void testRemovePackageFromPreferred() {
        try {
            mPackageManager.removePackageFromPreferred(CONTENT_PKG_NAME);
            fail("removePackageFromPreferred unexpectedly succeeded");
        } catch (SecurityException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getActivityIcon",
            method = "getActivityIcon",
            args = {android.content.ComponentName.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getActivityIcon",
            method = "getActivityIcon",
            args = {android.content.Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getDrawable",
            method = "getDrawable",
            args = {java.lang.String.class, int.class, android.content.pm.ApplicationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getDefaultActivityIcon",
            method = "getDefaultActivityIcon",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getApplicationIcon",
            method = "getApplicationIcon",
            args = {android.content.pm.ApplicationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getApplicationIcon",
            method = "getApplicationIcon",
            args = {java.lang.String.class}
        )
    })
    public void testGetIcon() throws NameNotFoundException {
        assertNotNull(mPackageManager.getApplicationIcon(PACKAGE_NAME));
        assertNotNull(mPackageManager.getApplicationIcon(mPackageManager.getApplicationInfo(
                PACKAGE_NAME, 0)));
        assertNotNull(mPackageManager
                .getActivityIcon(new ComponentName(PACKAGE_NAME, ACTIVITY_NAME)));
        assertNotNull(mPackageManager.getActivityIcon(new Intent(MAIN_ACTION_NAME)));
        assertNotNull(mPackageManager.getDefaultActivityIcon());

        // getDrawable is called by ComponentInfo.loadIcon() which called by getActivityIcon()
        // method of PackageMaganer. Here is just assurance for its functionality.
        int iconRes = R.drawable.start;
        ApplicationInfo appInfo = mPackageManager.getApplicationInfo(PACKAGE_NAME, 0);
        assertNotNull(mPackageManager.getDrawable(PACKAGE_NAME, iconRes, appInfo));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test checkSignatures",
            method = "checkSignatures",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test checkPermission",
            method = "checkPermission",
            args = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void testCheckMethods() {
        assertEquals(PackageManager.SIGNATURE_MATCH, mPackageManager.checkSignatures(PACKAGE_NAME,
                CONTENT_PKG_NAME));
        assertEquals(PackageManager.PERMISSION_GRANTED,
                mPackageManager.checkPermission(PERMISSION_NAME, PACKAGE_NAME));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test resolveActivity",
            method = "resolveActivity",
            args = {android.content.Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test resolveContentProvider",
            method = "resolveContentProvider",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test resolveService",
            method = "resolveService",
            args = {android.content.Intent.class, int.class}
        )
    })
    public void testResolveMethods() {
        // Test resolveActivity
        Intent intent = new Intent(ACTIVITY_ACTION_NAME);
        intent.setComponent(new ComponentName(PACKAGE_NAME, ACTIVITY_NAME));
        assertEquals(ACTIVITY_NAME, mPackageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo.name);

        // Test resolveService
        intent = new Intent(SERVICE_ACTION_NAME);
        intent.setComponent(new ComponentName(PACKAGE_NAME, SERVICE_NAME));
        ResolveInfo resolveInfo = mPackageManager.resolveService(intent,
                PackageManager.GET_INTENT_FILTERS);
        assertEquals(SERVICE_NAME, resolveInfo.serviceInfo.name);

        // Test resolveContentProvider
        String providerAuthorities = "ctstest";
        assertEquals(PROVIDER_NAME,
                mPackageManager.resolveContentProvider(providerAuthorities, 0).name);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getResourcesForApplication",
            method = "getResourcesForApplication",
            args = {android.content.pm.ApplicationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getResourcesForApplication",
            method = "getResourcesForApplication",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getResourcesForActivity",
            method = "getResourcesForActivity",
            args = {android.content.ComponentName.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getXml",
            method = "getXml",
            args = {java.lang.String.class, int.class, android.content.pm.ApplicationInfo.class}
        )
    })
    public void testGetResources() throws NameNotFoundException {
        ComponentName componentName = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
        int resourceId = R.xml.pm_test;
        String xmlName = "com.android.cts.stub:xml/pm_test";
        ApplicationInfo appInfo = mPackageManager.getApplicationInfo(PACKAGE_NAME, 0);
        assertNotNull(mPackageManager.getXml(PACKAGE_NAME, resourceId, appInfo));
        assertEquals(xmlName, mPackageManager.getResourcesForActivity(componentName)
                .getResourceName(resourceId));
        assertEquals(xmlName, mPackageManager.getResourcesForApplication(appInfo).getResourceName(
                resourceId));
        assertEquals(xmlName, mPackageManager.getResourcesForApplication(PACKAGE_NAME)
                .getResourceName(resourceId));
    }
}
