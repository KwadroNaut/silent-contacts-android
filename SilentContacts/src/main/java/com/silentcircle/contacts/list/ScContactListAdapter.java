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
 * This  implementation is edited version of original Android sources.
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
package com.silentcircle.contacts.list;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.silentcircle.contacts.R;
import com.silentcircle.silentcontacts.ScContactsContract;
import com.silentcircle.silentcontacts.ScContactsContract.Directory;
import com.silentcircle.silentcontacts.ScContactsContract.RawContacts;

/**
 * A cursor adapter for the {@link ContactsContract.Contacts#CONTENT_TYPE} content type.
 * Also includes support for including the {@link ContactsContract.Profile} record in the
 * list.
 */
public abstract class ScContactListAdapter extends ScContactEntryListAdapter {

    // Coumn numbers changed, see below
    protected static class ContactQuery {
        private static final String[] CONTACT_PROJECTION_PRIMARY = new String[] {
            RawContacts._ID,                           // 0
            RawContacts.DISPLAY_NAME_PRIMARY,          // 1
            RawContacts.PHOTO_ID,                      // 2
            RawContacts.PHOTO_THUMBNAIL_URI,           // 3
            RawContacts.CONTACT_TYPE,                  // 4
//          Contacts.CONTACT_PRESENCE,                 // ?
//          Contacts.CONTACT_STATUS,                   // ?
        };

        private static final String[] CONTACT_PROJECTION_ALTERNATIVE = new String[] {
            RawContacts._ID,                           // 0
            RawContacts.DISPLAY_NAME_ALTERNATIVE,      // 1
            RawContacts.PHOTO_ID,                      // 2
            RawContacts.PHOTO_THUMBNAIL_URI,           // 3
            RawContacts.CONTACT_TYPE,                  // 4
//          Contacts.CONTACT_PRESENCE,                 // ?
//          Contacts.CONTACT_STATUS,                   // ?
        };

        private static final String[] FILTER_PROJECTION_PRIMARY = new String[] {
            RawContacts._ID,                           // 0
            RawContacts.DISPLAY_NAME_PRIMARY,          // 1
            RawContacts.PHOTO_ID,                      // 2
            RawContacts.PHOTO_THUMBNAIL_URI,           // 3
            RawContacts.CONTACT_TYPE,                  // 4
//          Contacts.CONTACT_PRESENCE,                 // ?
//          Contacts.CONTACT_STATUS,                   // ?
//            SearchSnippetColumns.SNIPPET,            // ?
        };

        private static final String[] FILTER_PROJECTION_ALTERNATIVE = new String[] {
            RawContacts._ID,                           // 0
            RawContacts.DISPLAY_NAME_ALTERNATIVE,      // 1
            RawContacts.PHOTO_ID,                      // 2
            RawContacts.PHOTO_THUMBNAIL_URI,           // 3
            RawContacts.CONTACT_TYPE,                  // 4
//          Contacts.CONTACT_PRESENCE,              // 2
//          Contacts.CONTACT_STATUS,                // 3
//          SearchSnippetColumns.SNIPPET,           // 8
        };

        public static final int CONTACT_ID               = 0;
        public static final int CONTACT_DISPLAY_NAME     = 1;
        public static final int CONTACT_PHOTO_ID         = 2;
        public static final int CONTACT_PHOTO_URI        = 3;
        public static final int CONTACT_TYPE             = 4;
//      public static final int CONTACT_PRESENCE_STATUS  = ?;
//      public static final int CONTACT_CONTACT_STATUS   = ?;
//      public static final int CONTACT_SNIPPET          = ?;
    }

    private CharSequence mUnknownNameText;

    private long mSelectedContactDirectoryId;
    private String mSelectedContactLookupKey;
    private long mSelectedContactId;

    public ScContactListAdapter(Context context) {
        super(context);

        mUnknownNameText = context.getText(R.string.missing_name);
    }

    public CharSequence getUnknownNameText() {
        return mUnknownNameText;
    }

    public long getSelectedContactDirectoryId() {
        return mSelectedContactDirectoryId;
    }

    public String getSelectedContactLookupKey() {
        return mSelectedContactLookupKey;
    }

    public long getSelectedContactId() {
        return mSelectedContactId;
    }

    public void setSelectedContact(long selectedDirectoryId, String lookupKey, long contactId) {
        mSelectedContactDirectoryId = selectedDirectoryId;
        mSelectedContactLookupKey = lookupKey;
        mSelectedContactId = contactId;
    }

    protected static Uri buildSectionIndexerUri(Uri uri) {
        return uri.buildUpon()
                .appendQueryParameter(ScContactsContract.ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
    }

    
    @Override
    public String getContactDisplayName(int position) {
        return ((Cursor) getItem(position)).getString(ContactQuery.CONTACT_DISPLAY_NAME);
    }

    /**
     * Builds the {@link Contacts#CONTENT_LOOKUP_URI} for the given
     * {@link ListView} position.
     */
    public Uri getContactUri(int position) {
        int partitionIndex = getPartitionForPosition(position);
        Cursor item = (Cursor)getItem(position);
        return item != null ? getContactUri(partitionIndex, item) : null;
    }

    public Uri getContactUri(int partitionIndex, Cursor cursor) {

        long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
        Uri uri = RawContacts.getLookupUri(contactId);
        long directoryId = ((DirectoryPartition)getPartition(partitionIndex)).getDirectoryId();
        if (directoryId != Directory.DEFAULT) {
            uri = uri.buildUpon().appendQueryParameter(
                    ScContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(directoryId)).build();
        }
        return uri;
    }

    /**
     * Returns true if the specified contact is selected in the list. For a
     * contact to be shown as selected, we need both the directory and and the
     * lookup key to be the same. We are paying no attention to the contactId,
     * because it is volatile, especially in the case of directories.
     */
    public boolean isSelectedContact(int partitionIndex, Cursor cursor) {
        long directoryId = ((DirectoryPartition)getPartition(partitionIndex)).getDirectoryId();
        if (getSelectedContactDirectoryId() != directoryId) {
            return false;
        }
        return directoryId != Directory.LOCAL_INVISIBLE && getSelectedContactId() == cursor.getLong(ContactQuery.CONTACT_ID);
    }

    @Override
    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(mUnknownNameText);
        view.setQuickContactEnabled(isQuickContactEnabled());
        view.setActivatedStateSupported(isSelectionVisible());
        return view;
    }

    protected void bindSectionHeaderAndDivider(ContactListItemView view, int position, Cursor cursor) {
        if (isSectionHeaderDisplayEnabled()) {
            Placement placement = getItemPlacementInSection(position);

            // First position, set the contacts number string
            if (position == 0 /*&& cursor.getInt(ContactQuery.CONTACT_IS_USER_PROFILE) == 1*/) {
                view.setCountView(getContactsCount());
            } else {
                view.setCountView(null);
            }
            view.setSectionHeader(placement.sectionHeader);
            view.setDividerVisible(!placement.lastInSection);
        } else {
            view.setSectionHeader(null);
            view.setDividerVisible(true);
            view.setCountView(null);
        }
    }

    protected void bindPhoto(final ContactListItemView view, int partitionIndex, Cursor cursor) {
        if (!isPhotoSupported(partitionIndex)) {
            view.removePhotoView();
            return;
        }

        // Set the photo, if available
        long photoId = 0;
        if (!cursor.isNull(ContactQuery.CONTACT_PHOTO_ID)) {
            photoId = cursor.getLong(ContactQuery.CONTACT_PHOTO_ID);
        }

        if (photoId != 0) {
            getPhotoLoader().loadThumbnail(view.getPhotoView(), photoId, mDarkTheme);
        } else {
            final String photoUriString = cursor.getString(ContactQuery.CONTACT_PHOTO_URI);
            final Uri photoUri = photoUriString == null ? null : Uri.parse(photoUriString);
            getPhotoLoader().loadDirectoryPhoto(view.getPhotoView(), photoUri, mDarkTheme);
        }
    }

    protected void bindName(final ContactListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, ContactQuery.CONTACT_DISPLAY_NAME, getContactNameDisplayOrder());
        // Note: we don't show phonetic any more (See issue 5265330)
    }

    protected void bindPresenceAndStatusMessage(final ContactListItemView view, Cursor cursor) {
        view.showPresenceAndStatusMessage(cursor, 0, 0);   // disabled in adapter
    }

    protected void bindSearchSnippet(final ContactListItemView view, Cursor cursor) {
        view.showSnippet(cursor, 0); // disabled in adapter
    }

    public int getSelectedContactPosition() {
        if (/*mSelectedContactLookupKey == null &&*/ mSelectedContactId == 0) {
            return -1;
        }

        Cursor cursor = null;
        int partitionIndex = -1;
        int partitionCount = getPartitionCount();
        for (int i = 0; i < partitionCount; i++) {
            DirectoryPartition partition = (DirectoryPartition) getPartition(i);
            if (partition.getDirectoryId() == mSelectedContactDirectoryId) {
                partitionIndex = i;
                break;
            }
        }
        if (partitionIndex == -1) {
            return -1;
        }

        cursor = getCursor(partitionIndex);
        if (cursor == null) {
            return -1;
        }

        cursor.moveToPosition(-1);      // Reset cursor
        int offset = -1;
        while (cursor.moveToNext()) {
//            if (mSelectedContactLookupKey != null) {
//                String lookupKey = cursor.getString(ContactQuery.CONTACT_LOOKUP_KEY);
//                if (mSelectedContactLookupKey.equals(lookupKey)) {
//                    offset = cursor.getPosition();
//                    break;
//                }
//            }
            if (mSelectedContactId != 0 && (mSelectedContactDirectoryId == Directory.DEFAULT
                    || mSelectedContactDirectoryId == Directory.LOCAL_INVISIBLE)) {
                long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
                if (contactId == mSelectedContactId) {
                    offset = cursor.getPosition();
                    break;
                }
            }
        }
        if (offset == -1) {
            return -1;
        }

        int position = getPositionForPartition(partitionIndex) + offset;
        if (hasHeader(partitionIndex)) {
            position++;
        }
        return position;
    }

    public boolean hasValidSelection() {
        return getSelectedContactPosition() != -1;
    }

    public Uri getFirstContactUri() {
        int partitionCount = getPartitionCount();
        for (int i = 0; i < partitionCount; i++) {
            DirectoryPartition partition = (DirectoryPartition) getPartition(i);
            if (partition.isLoading()) {
                continue;
            }
            Cursor cursor = getCursor(i);
            if (cursor == null) {
                continue;
            }
            if (!cursor.moveToFirst()) {
                continue;
            }
            return getContactUri(i, cursor);
        }
        return null;
    }

    @Override
    public void changeCursor(int partitionIndex, Cursor cursor) {
        super.changeCursor(partitionIndex, cursor);

        // Check if a profile exists
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            setProfileExists(cursor.getInt(ContactQuery.CONTACT_TYPE) == RawContacts.CONTACT_TYPE_OWN);
        }
    }

    /**
     * @return Projection useful for children.
     */
    protected final String[] getProjection(boolean forSearch) {
        final int sortOrder = getContactNameDisplayOrder();
        if (forSearch) {
            if (sortOrder == ScContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
                return ContactQuery.FILTER_PROJECTION_PRIMARY;
            } else {
                return ContactQuery.FILTER_PROJECTION_ALTERNATIVE;
            }
        } 
        else {
            if (sortOrder == ScContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
                return ContactQuery.CONTACT_PROJECTION_PRIMARY;
            } else {
                return ContactQuery.CONTACT_PROJECTION_ALTERNATIVE;
            }
        }
    }
}
