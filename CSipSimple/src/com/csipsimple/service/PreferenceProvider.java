/**
 * Copyright (C) 2010 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.csipsimple.service;

import com.csipsimple.utils.Log;
import com.csipsimple.utils.PreferencesWrapper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;

/**
 * This provider allow to retrieve preference from different process than the UI
 * process Should be used by service For the future could be usefull for third
 * party apps.
 * 
 * @author r3gis3r
 * 
 */
public class PreferenceProvider extends ContentProvider {

	private PreferencesWrapper prefs;
	private UriMatcher mUriMatcher;

	private final static String PREFS_TABLE_NAME = "preferences";
	private final static String RESET_TABLE_NAME = "raz";

	// For Provider
	public final static String AUTHORITY = "com.csipsimple.prefs";
	private final static String BASE_DIR_TYPE = "vnd.android.cursor.dir/vnd.csipsimple";
	private final static String BASE_ITEM_TYPE = "vnd.android.cursor.item/vnd.csipsimple";
	private final static String CONTENT_SCHEME = "content://";
	// Preference
	public final static String PREF_CONTENT_TYPE = BASE_DIR_TYPE + ".pref";
	public final static String PREF_CONTENT_ITEM_TYPE = BASE_ITEM_TYPE + ".pref";
	public final static Uri PREF_URI = Uri.parse(CONTENT_SCHEME + AUTHORITY + "/" + PREFS_TABLE_NAME);
	public final static Uri PREF_ID_URI_BASE = Uri.parse(CONTENT_SCHEME + AUTHORITY + "/" + PREFS_TABLE_NAME + "/");

	// Raz
	public final static Uri RAZ_URI = Uri.parse(CONTENT_SCHEME + AUTHORITY + "/" + RESET_TABLE_NAME);

	private static final int PREFS = 1;
	private static final int PREF_ID = 2;
	private static final int RAZ = 3;

	public final static String FIELD_NAME = "name";
	public final static String FIELD_VALUE = "value";
	public static final int COL_INDEX_NAME = 0;
	public static final int COL_INDEX_VALUE = 1;
	private static final String THIS_FILE = "PrefsProvider";

	public PreferenceProvider() {
		// Create and initialize URI matcher.
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AUTHORITY, PREFS_TABLE_NAME, PREFS);
		mUriMatcher.addURI(AUTHORITY, PREFS_TABLE_NAME + "/*", PREF_ID);
		mUriMatcher.addURI(AUTHORITY, RESET_TABLE_NAME, RAZ);
	}

	@Override
	public boolean onCreate() {
		prefs = new PreferencesWrapper(getContext());
		return true;
	}

	/**
	 * Return the MIME type for an known URI in the provider.
	 */
	@Override
	public String getType(Uri uri) {
		android.util.Log.w(THIS_FILE, "Get type --- " + uri.toString());
		switch (mUriMatcher.match(uri)) {
		case PREFS:
		case RAZ:
			return PREF_CONTENT_TYPE;
		case PREF_ID:
			return PREF_CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String order) {
		MatrixCursor resCursor = new MatrixCursor(new String[] { FIELD_NAME, FIELD_VALUE });
		switch (mUriMatcher.match(uri)) {
		case PREFS:
			// Ingore for now
			break;
		case PREF_ID:
			String name = uri.getLastPathSegment();
			Class<?> aClass = null;
			if (TextUtils.isEmpty(selection)) {
				aClass = PreferencesWrapper.gPrefClass(name);
			} else {
				try {
					aClass = Class.forName(selection);
				} catch (ClassNotFoundException e) {
					Log.e(THIS_FILE, "Impossible to retrieve class from selection");
				}
			}
			Object value = null;
			if (aClass == String.class) {
				value = prefs.getPreferenceStringValue(name);
			} else if (aClass == Float.class) {
				value = prefs.getPreferenceFloatValue(name);
			} else if (aClass == Boolean.class) {
				value = prefs.getPreferenceBooleanValue(name) ? 1 : 0;
			}
			if (value != null) {
				resCursor.addRow(new Object[] { name, value });
			} else {
				resCursor = null;
			}
			break;
		}
		return resCursor;
	}

	@Override
	public int update(Uri uri, ContentValues cv, String selection, String[] selectionArgs) {
		int count = 0;
		switch (mUriMatcher.match(uri)) {
		case PREFS:
			// Ignore for now
			break;
		case PREF_ID:
			String name = uri.getLastPathSegment();
			Class<?> aClass = null;
			if (TextUtils.isEmpty(selection)) {
				aClass = PreferencesWrapper.gPrefClass(name);
			} else {
				try {
					aClass = Class.forName(selection);
				} catch (ClassNotFoundException e) {
					Log.e(THIS_FILE, "Impossible to retrieve class from selection");
				}
			}
			if (aClass == String.class) {
				prefs.setPreferenceStringValue(name, cv.getAsString(FIELD_VALUE));
			} else if (aClass == Float.class) {
				prefs.setPreferenceFloatValue(name, cv.getAsFloat(FIELD_VALUE));
			} else if (aClass == Boolean.class) {
				prefs.setPreferenceBooleanValue(name, cv.getAsBoolean(FIELD_VALUE));
			}
			count++;
			break;
		case RAZ:
			prefs.resetAllDefaultValues();
			break;
		}
		return count;
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// Not implemented
		return 0;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// Not implemented
		return null;
	}

}
