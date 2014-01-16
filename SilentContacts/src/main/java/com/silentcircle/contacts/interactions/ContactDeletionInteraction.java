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

package com.silentcircle.contacts.interactions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.silentcircle.contacts.ScContactSaveService;
import com.silentcircle.contacts.model.account.AccountType;
import com.silentcircle.contacts.R;
import com.silentcircle.silentcontacts.ScContactsContract.RawContacts;
import com.silentcircle.silentcontacts.ScContactsContract.RawContacts.Entity;
import com.silentcircle.contacts.model.AccountTypeManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

import java.util.HashSet;

/**
 * An interaction invoked to delete a contact.
 */
public class ContactDeletionInteraction extends SherlockFragment
        implements LoaderCallbacks<Cursor>, OnDismissListener {

    private static final String FRAGMENT_TAG = "deleteContact";

    private static final String KEY_ACTIVE = "active";
    private static final String KEY_CONTACT_URI = "contactUri";
    private static final String KEY_FINISH_WHEN_DONE = "finishWhenDone";
    public static final String ARG_CONTACT_URI = "contactUri";

    private static final String[] ENTITY_PROJECTION = new String[] {
        Entity._ID, //0
    };

    private static final int COLUMN_INDEX_RAW_CONTACT_ID = 0;

    private boolean mActive;
    private Uri mContactUri;
    private boolean mFinishActivityWhenDone;
    private Context mContext;
    private AlertDialog mDialog;

    /** This is a wrapper around the fragment's loader manager to be used only during testing. */
//    private TestLoaderManager mTestLoaderManager;

    @VisibleForTesting
    int mMessageId;

    /**
     * Starts the interaction.
     *
     * @param activity the activity within which to start the interaction
     * @param contactUri the URI of the contact to delete
     * @param finishActivityWhenDone whether to finish the activity upon completion of the
     *        interaction
     * @return the newly created interaction
     */
    public static ContactDeletionInteraction start(SherlockFragmentActivity activity, Uri contactUri, 
            boolean finishActivityWhenDone) {
        return startWithTestLoaderManager(activity, contactUri, finishActivityWhenDone, null);
    }

    /**
     * Starts the interaction and optionally set up a {@link TestLoaderManager}.
     *
     * @param activity the activity within which to start the interaction
     * @param contactUri the URI of the contact to delete
     * @param finishActivityWhenDone whether to finish the activity upon completion of the
     *        interaction
     * @param testLoaderManager the {@link TestLoaderManager} to use to load the data, may be null
     *        in which case the default {@link LoaderManager} is used
     * @return the newly created interaction
     */
//    @VisibleForTesting
    static ContactDeletionInteraction startWithTestLoaderManager(SherlockFragmentActivity activity, Uri contactUri, 
            boolean finishActivityWhenDone,
            /* TestLoaderManager*/ Object testLoaderManager) {
        if (contactUri == null) {
            return null;
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        ContactDeletionInteraction fragment = (ContactDeletionInteraction) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new ContactDeletionInteraction();
//            fragment.setTestLoaderManager(testLoaderManager);
            fragment.setContactUri(contactUri);
            fragment.setFinishActivityWhenDone(finishActivityWhenDone);
            fragmentManager.beginTransaction().add(fragment, FRAGMENT_TAG)
                .commitAllowingStateLoss();
        }
        else {
//            fragment.setTestLoaderManager(testLoaderManager);
            fragment.setContactUri(contactUri);
            fragment.setFinishActivityWhenDone(finishActivityWhenDone);
        }
        return fragment;
    }

    @Override
    public LoaderManager getLoaderManager() {
        // Return the TestLoaderManager if one is set up.
        LoaderManager loaderManager = super.getLoaderManager();
//        if (mTestLoaderManager != null) {
//            // Set the delegate: this operation is idempotent, so let's just do it every time.
//            mTestLoaderManager.setDelegate(loaderManager);
//            return mTestLoaderManager;
//        } 
//        else {
            return loaderManager;
//        }
    }

    /** Sets the TestLoaderManager that is used to wrap the actual LoaderManager in tests. */
//    private void setTestLoaderManager(TestLoaderManager mockLoaderManager) {
//        mTestLoaderManager = mockLoaderManager;
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.setOnDismissListener(null);
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void setContactUri(Uri contactUri) {
        mContactUri = contactUri;
        mActive = true;
        if (isStarted()) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_CONTACT_URI, mContactUri);
            getLoaderManager().restartLoader(R.id.dialog_delete_contact_loader_id, args, this);
        }
    }

    private void setFinishActivityWhenDone(boolean finishActivityWhenDone) {
        this.mFinishActivityWhenDone = finishActivityWhenDone;

    }

    /* Visible for testing */
    boolean isStarted() {
        return isAdded();
    }

    @Override
    public void onStart() {
        if (mActive) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_CONTACT_URI, mContactUri);
            getLoaderManager().initLoader(R.id.dialog_delete_contact_loader_id, args, this);
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.hide();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri contactUri = args.getParcelable(ARG_CONTACT_URI);
        return new CursorLoader(mContext,
                Uri.withAppendedPath(contactUri, Entity.CONTENT_DIRECTORY), ENTITY_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        if (!mActive) {
            return;
        }

        long contactId = 0;

        // This cursor may contain duplicate raw contacts, so we need to de-dupe them first
        HashSet<Long>  readOnlyRawContacts = Sets.newHashSet();
        HashSet<Long>  writableRawContacts = Sets.newHashSet();

        AccountTypeManager accountTypes = AccountTypeManager.getInstance(getActivity());
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            contactId = cursor.getLong(COLUMN_INDEX_RAW_CONTACT_ID);
            AccountType type = accountTypes.getAccountType();
            boolean writable = type == null || type.areContactsWritable();
            if (writable) {
                writableRawContacts.add(contactId);
            }
            else {
                readOnlyRawContacts.add(contactId);
            }
        }

        int readOnlyCount = readOnlyRawContacts.size();
        int writableCount = writableRawContacts.size();
        if (readOnlyCount > 0 && writableCount > 0) {
            mMessageId = R.string.readOnlyContactDeleteConfirmation;
        }
        else if (readOnlyCount > 0 && writableCount == 0) {
            mMessageId = R.string.readOnlyContactWarning;
        }
        else if (readOnlyCount == 0 && writableCount > 1) {
            mMessageId = R.string.multipleContactDeleteConfirmation;
        }
        else {
            mMessageId = R.string.deleteConfirmation;
        }

        final Uri contactUri = RawContacts.getLookupUri(contactId);
        showDialog(mMessageId, contactUri);

        // We don't want onLoadFinished() calls any more, which may come when the database is
        // updating.
        getLoaderManager().destroyLoader(R.id.dialog_delete_contact_loader_id);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showDialog(int messageId, final Uri contactUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            builder.setIconAttribute(android.R.attr.alertDialogIcon);

        mDialog = builder.setMessage(messageId)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            doDeleteContact(contactUri);
                        }
                    }
                )
                .create();

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mActive = false;
        mDialog = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ACTIVE, mActive);
        outState.putParcelable(KEY_CONTACT_URI, mContactUri);
        outState.putBoolean(KEY_FINISH_WHEN_DONE, mFinishActivityWhenDone);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mActive = savedInstanceState.getBoolean(KEY_ACTIVE);
            mContactUri = savedInstanceState.getParcelable(KEY_CONTACT_URI);
            mFinishActivityWhenDone = savedInstanceState.getBoolean(KEY_FINISH_WHEN_DONE);
        }
    }

    protected void doDeleteContact(Uri contactUri) {
        mContext.startService(ScContactSaveService.createDeleteContactIntent(mContext, contactUri));
        if (isAdded() && mFinishActivityWhenDone) {
            getActivity().finish();
        }
    }
}
