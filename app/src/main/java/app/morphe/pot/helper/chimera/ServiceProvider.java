/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package app.morphe.pot.helper.chimera;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

@SuppressWarnings("NullableProblems")
public class ServiceProvider extends ContentProvider {
    private static final String TAG = "morphe: ServiceProvider";
    private static final String[] COLUMNS = { "version", "apkPath", "loaderPath", "apkDescStr" };

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        return false;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (extras != null && TextUtils.equals("serviceIntentCall", method)) {
            Context context = getContext();
            String serviceAction = extras.getString("serviceActionBundleKey");
            if (context != null) {
                Intent intent = new Intent(serviceAction);
                intent.setPackage(context.getPackageName());
                ResolveInfo resolveInfo = context.getPackageManager().resolveService(intent, 0);
                if (resolveInfo != null) {
                    intent.setClassName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
                    Log.d(TAG, method + ": " + serviceAction + " -> " + intent);
                    Bundle bundle = new Bundle(1);
                    bundle.putParcelable("serviceResponseIntentKey", intent);
                    return bundle;
                }
            }
        }
        return super.call(method, arg, extras);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = new MatrixCursor(COLUMNS);
        Log.d(TAG, "query: " + uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType: " + uri);
        return "vnd.android.cursor.item/app.morphe.pot.helper.chimera";
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.d(TAG, "insert: " + uri + ", " + contentValues);
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete: " + uri + ", " + selection + ", " + Arrays.toString(selectionArgs));
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        Log.d(TAG, "update: " + uri + ", " + contentValues + ", " + selection + ", " + Arrays.toString(selectionArgs));
        return 0;
    }
}
