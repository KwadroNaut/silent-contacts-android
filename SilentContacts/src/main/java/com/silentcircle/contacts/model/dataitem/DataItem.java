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
 * Copyright (C) 2012 The Android Open Source Project
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

package com.silentcircle.contacts.model.dataitem;

import android.content.ContentValues;

import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Email;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Event;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.GroupMembership;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Identity;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Im;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Nickname;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Note;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Organization;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Phone;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Photo;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.SipAddress;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.StructuredName;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.StructuredPostal;
import com.silentcircle.silentcontacts.ScContactsContract.Data;
import com.silentcircle.contacts.model.AccountTypeManager;
import com.silentcircle.contacts.model.RawContact;

/**
 * This is the base class for data items, which represents a row from the Data table.
 */
public class DataItem {

    private final ContentValues mContentValues;

    /**
     * The raw contact that this data item is associated with.  This can be null.
     */
    private final RawContact mRawContact;
    private DataKind mDataKind;

    protected DataItem(RawContact rawContact, ContentValues values) {
        mContentValues = values;
        mRawContact = rawContact;
    }

    /**
     * Factory for creating subclasses of DataItem objects based on the mimetype in the
     * content values.  Raw contact is the raw contact that this data item is associated with.
     */
    public static DataItem createFrom(RawContact rawContact, ContentValues values) {
        final String mimeType = values.getAsString(Data.MIMETYPE);

        if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new GroupMembershipDataItem(rawContact, values);
        }
        else if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new StructuredNameDataItem(rawContact, values);
        }
        else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new PhoneDataItem(rawContact, values);
        }
        else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new EmailDataItem(rawContact, values);
        }
        else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new StructuredPostalDataItem(rawContact, values);
        }
        else if (Im.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new ImDataItem(rawContact, values);
        }
        else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new OrganizationDataItem(rawContact, values);
        }
        else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new NicknameDataItem(rawContact, values);
        }
        else if (Note.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new NoteDataItem(rawContact, values);
//        } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType)) {
//            return new WebsiteDataItem(rawContact, values);
        }
        else if (SipAddress.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new SipAddressDataItem(rawContact, values);
        }
        else if (Event.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new EventDataItem(rawContact, values);
//        } else if (Relation.CONTENT_ITEM_TYPE.equals(mimeType)) {
//            return new RelationDataItem(rawContact, values);
        }
        else if (Identity.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new IdentityDataItem(rawContact, values);
        }
        else if (Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
            return new PhotoDataItem(rawContact, values);
        }

        // generic
        return new DataItem(rawContact, values);
    }

    public ContentValues getContentValues() {
        return mContentValues;
    }

    protected RawContact getRawContact() {
        return mRawContact;
    }

    public void setRawContactId(long rawContactId) {
        mContentValues.put(Data.RAW_CONTACT_ID, rawContactId);
    }

    /**
     * Returns the data id.
     */
    public long getId() {
        return mContentValues.getAsLong(Data._ID);
    }

    public long getRawContactId() {
        return mContentValues.getAsLong(Data.RAW_CONTACT_ID);
    }
    /**
     * Returns the mimetype of the data.
     */
    public String getMimeType() {
        return mContentValues.getAsString(Data.MIMETYPE);
    }

    public void setMimeType(String mimeType) {
        mContentValues.put(Data.MIMETYPE, mimeType);
    }

    public boolean isPrimary() {
        Integer primary = mContentValues.getAsInteger(Data.IS_PRIMARY);
        return primary != null && primary != 0;
    }

    public boolean isSuperPrimary() {
        Integer superPrimary = mContentValues.getAsInteger(Data.IS_SUPER_PRIMARY);
        return superPrimary != null && superPrimary != 0;
    }

    public int getDataVersion() {
        return mContentValues.getAsInteger(Data.DATA_VERSION);
    }

    public AccountTypeManager getAccountTypeManager() {
        if (mRawContact == null) {
            return null;
        } else {
            return mRawContact.getAccountTypeManager();
        }
    }

    /**
     * This method can only be invoked if the raw contact is non-null.
     */
    public DataKind getDataKind() {
        if (mRawContact == null) {
            throw new IllegalStateException("mRawContact must be non-null to call getDataKind()");
        }

        if (mDataKind == null) {
            mDataKind = getAccountTypeManager().getKindOrFallback(getMimeType());
        }

        return mDataKind;
    }

    public boolean hasKindTypeColumn() {
        final String key = getDataKind().typeColumn;
        return key != null && mContentValues.containsKey(key);
    }

    public int getKindTypeColumn() {
        final String key = getDataKind().typeColumn;
        return mContentValues.getAsInteger(key);
    }

    /**
     * This builds the data string depending on the type of data item by using the generic
     * DataKind object underneath.  This DataItem object must be associated with a raw contact
     * for this function to work.
     */
    public String buildDataString() {
        if (mRawContact == null) {
            throw new IllegalStateException("mRawContact must be non-null to call getDataKind()");
        }
        final DataKind kind = getDataKind();

        if (kind.actionBody == null) {
            return null;
        }
        CharSequence actionBody = kind.actionBody.inflateUsing(mRawContact.getContext(),  mContentValues);
        return actionBody == null ? null : actionBody.toString();
    }

    /**
     * This builds the data string(intended for display) depending on the type of data item. It
     * returns the same value as {@link #buildDataString} by default, but certain data items can
     * override it to provide their version of formatted data strings.
     *
     * @return Data string representing the data item, possibly formatted for display
     */
    public String buildDataStringForDisplay() {
        return buildDataString();
    }

    public String getKindString() {
        final DataKind kind = getDataKind();
        return (kind.titleRes == -1 || kind.titleRes == 0) ? ""
                : mRawContact.getContext().getString(kind.titleRes);
    }
}
