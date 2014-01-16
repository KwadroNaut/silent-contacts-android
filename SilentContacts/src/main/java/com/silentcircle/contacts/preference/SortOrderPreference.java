/*
Copyright © 2013-2014, Silent Circle, LLC.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Any redistribution, use, or modification is done solely for personal 
      benefit and not for any commercial purpose or for monetary gain
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name Silent Circle nor the names of its contributors may 
      be used to endorse or promote products derived from this software 
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL SILENT CIRCLE, LLC BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.silentcircle.contacts.preference;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.silentcircle.contacts.R;
import com.silentcircle.silentcontacts.ScContactsContract;

/**
 * Custom preference: sort-by.
 */
public final class SortOrderPreference extends ListPreference {

    private ContactsPreferences mPreferences;
    private Context mContext;

    public SortOrderPreference(Context context) {
        super(context);
        prepare();
    }

    public SortOrderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepare();
    }

    private void prepare() {
        mContext = getContext();
        mPreferences = new ContactsPreferences(mContext);
        setEntries(new String[]{
                mContext.getString(R.string.display_options_sort_by_given_name),
                mContext.getString(R.string.display_options_sort_by_family_name),
        });
        setEntryValues(new String[]{
                String.valueOf(ScContactsContract.Preferences.SORT_ORDER_PRIMARY),
                String.valueOf(ScContactsContract.Preferences.SORT_ORDER_ALTERNATIVE),
        });
        setValue(String.valueOf(mPreferences.getSortOrder()));
    }

    @Override
    protected boolean shouldPersist() {
        return false;   // This preference takes care of its own storage
    }

    @Override
    public CharSequence getSummary() {
        switch (mPreferences.getSortOrder()) {
            case ScContactsContract.Preferences.SORT_ORDER_PRIMARY:
                return mContext.getString(R.string.display_options_sort_by_given_name);
            case ScContactsContract.Preferences.SORT_ORDER_ALTERNATIVE:
                return mContext.getString(R.string.display_options_sort_by_family_name);
        }
        return null;
    }

    @Override
    protected boolean persistString(String value) {
        int newValue = Integer.parseInt(value);
        if (newValue != mPreferences.getSortOrder()) {
            mPreferences.setSortOrder(newValue);
            notifyChanged();
        }
        return true;
    }

    @Override
    // UX recommendation is not to show cancel button on such lists.
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNegativeButton(null, null);
    }
}
