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
 * Copyright (C) 2009 The Android Open Source Project
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

package com.silentcircle.contacts.editor;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.silentcircle.contacts.GroupMetaDataLoader;
import com.silentcircle.contacts.model.RawContactDelta;
import com.silentcircle.contacts.model.RawContactModifier;
import com.silentcircle.contacts.model.account.AccountType;
import com.silentcircle.contacts.model.dataitem.DataKind;
import com.silentcircle.contacts.R;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.GroupMembership;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Organization;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.Photo;
import com.silentcircle.silentcontacts.ScContactsContract.CommonDataKinds.StructuredName;
// import com.android.internal.util.Objects;

import java.util.ArrayList;

/**
 * Custom view that provides all the editor interaction for a specific
 * {link ScRawContact} represented through an {@link com.silentcircle.contacts.model.RawContactDelta}. Callers can
 * reuse this view and quickly rebuild its contents through
 * {link #setState(com.silentcircle.contacts.model.RawContactDelta, com.silentcircle.contacts.model.account.AccountType, ViewIdGenerator)}.
 * <p>
 * Internal updates are performed against {@link com.silentcircle.contacts.model.RawContactDelta.ValuesDelta} so that the
 * source {link RawContact} can be swapped out. Any state-based changes, such as
 * adding {link Data} rows or changing {@link com.silentcircle.contacts.model.account.AccountType.EditType}, are performed through
 * {@link com.silentcircle.contacts.model.RawContactModifier} to ensure that {@link com.silentcircle.contacts.model.account.AccountType} are enforced.
 */
public class RawContactEditorView extends BaseRawContactEditorView {
    
    private static final String TAG = "RawContactEditorView";

    private LayoutInflater mInflater;

    private StructuredNameEditorView mName;
    private PhoneticNameEditorView mPhoneticName;
    private GroupMembershipView mGroupMembershipView;

    private ViewGroup mFields;
    private Button mAddFieldButton;

    private long mRawContactId = -1;
    private boolean mAutoAddToDefaultGroup = true;
    private Cursor mGroupMetaData;
    private DataKind mGroupMembershipKind;
    private RawContactDelta mState;

    private ContactEditorFragment parent;
    
    private boolean mPhoneticNameAdded;

    public RawContactEditorView(Context context) {
        super(context);
    }

    public RawContactEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        View view = getPhotoEditor();
        if (view != null) {
            view.setEnabled(enabled);
        }

        if (mName != null) {
            mName.setEnabled(enabled);
        }

        if (mPhoneticName != null) {
            mPhoneticName.setEnabled(enabled);
        }

        if (mFields != null) {
            int count = mFields.getChildCount();
            for (int i = 0; i < count; i++) {
                mFields.getChildAt(i).setEnabled(enabled);
            }
        }

        if (mGroupMembershipView != null) {
            mGroupMembershipView.setEnabled(enabled);
        }

        mAddFieldButton.setEnabled(enabled);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mName = (StructuredNameEditorView)findViewById(R.id.edit_name);
        mName.setDeletable(false);

        mPhoneticName = (PhoneticNameEditorView)findViewById(R.id.edit_phonetic_name);
        mPhoneticName.setDeletable(false);

        mFields = (ViewGroup)findViewById(R.id.sect_fields);

        mAddFieldButton = (Button) findViewById(R.id.button_add_field);
        mAddFieldButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddInformationPopupWindow();
            }
        });
    }

    /**
     * Set the internal state for this view, given a current
     * {@link RawContactDelta} state and the {@link com.silentcircle.contacts.model.account.AccountType} that
     * apply to that state.
     */
    @Override
    public void setState(RawContactDelta state, AccountType type, ViewIdGenerator vig, boolean isProfile) {

        mState = state;

        // Remove any existing sections
        mFields.removeAllViews();

        // Bail if invalid state or account type
        if (state == null || type == null) return;

        setId(vig.getId(state, null, null, ViewIdGenerator.NO_VIEW_INDEX));

        // Make sure we have a StructuredName and Organization
        RawContactModifier.ensureKindExists(state, type, StructuredName.CONTENT_ITEM_TYPE);
        RawContactModifier.ensureKindExists(state, type, Organization.CONTENT_ITEM_TYPE);

        mRawContactId = state.getRawContactId();

        // Show photo editor when supported
        RawContactModifier.ensureKindExists(state, type, Photo.CONTENT_ITEM_TYPE);
        setHasPhotoEditor((type.getKindForMimetype(Photo.CONTENT_ITEM_TYPE) != null));
        getPhotoEditor().setEnabled(isEnabled());
        mName.setEnabled(isEnabled());

        mPhoneticName.setEnabled(isEnabled());

        // Show and hide the appropriate views
        mFields.setVisibility(VISIBLE);
        mName.setVisibility(VISIBLE);
        mPhoneticName.setVisibility(VISIBLE);

        mGroupMembershipKind = type.getKindForMimetype(GroupMembership.CONTENT_ITEM_TYPE);
        if (mGroupMembershipKind != null) {
            mGroupMembershipView = (GroupMembershipView)mInflater.inflate(R.layout.item_group_membership, mFields, false);
            mGroupMembershipView.setParent(parent);
            mGroupMembershipView.setKind(mGroupMembershipKind);
            mGroupMembershipView.setEnabled(isEnabled());
        }

        // Create editor sections for each possible data kind
        for (DataKind kind : type.getSortedDataKinds()) {
            // Skip kind of not editable
            if (!kind.editable) 
                continue;

            final String mimeType = kind.mimeType;
            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Handle special case editor for structured name
                final RawContactDelta.ValuesDelta primary = state.getPrimaryEntry(mimeType);

                mName.setValues(type.getKindForMimetype(DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME), primary, state, false, vig);
                mPhoneticName.setValues(type.getKindForMimetype(DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME), primary, state,
                        false, vig);
            } 
            else if (Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Handle special case editor for photos
                final RawContactDelta.ValuesDelta primary = state.getPrimaryEntry(mimeType);
                getPhotoEditor().setValues(kind, primary, state, false, vig);
            } 
            else if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType)) {
                if (mGroupMembershipView != null) {
                    mGroupMembershipView.setState(state);
                }
            } 
            else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Create the organization section
                final KindSectionView section = (KindSectionView) mInflater.inflate(R.layout.item_kind_section, mFields, false);
                section.setTitleVisible(false);
                section.setEnabled(isEnabled());
                section.setState(kind, state, false, vig);

                // If there is organization info for the contact already, display it
                if (!section.isEmpty()) {
                    mFields.addView(section);
                } 
                else {
                    // Otherwise provide the user with an "add organization" button that shows the
                    // EditText fields only when clicked
                    final View organizationView = mInflater.inflate(R.layout.organization_editor_view_switcher, mFields, false);
                    final View addOrganizationButton = organizationView.findViewById(R.id.add_organization_button);
                    final ViewGroup organizationSectionViewContainer = (ViewGroup) organizationView.findViewById(R.id.container);

                    organizationSectionViewContainer.addView(section);

                    // Setup the click listener for the "add organization" button
                    addOrganizationButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Once the user expands the organization field, the user cannot
                            // collapse them again.
                            organizationSectionViewContainer.setVisibility(VISIBLE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                EditorAnimator.getInstance().expandOrganization(addOrganizationButton, organizationSectionViewContainer);
                            }
                            else {
                                addOrganizationButton.setVisibility(INVISIBLE);
                            }
                        }
                    });
                    mFields.addView(organizationView);
                }
            } 
            else {
                // Otherwise use generic section-based editors
                if (kind.fieldList == null)
                    continue;
                final KindSectionView section = (KindSectionView)mInflater.inflate(R.layout.item_kind_section, mFields, false);
                section.setEnabled(isEnabled());
                section.setState(kind, state, false, vig);
                mFields.addView(section);
            }
        }

        if (mGroupMembershipView != null) {
            mFields.addView(mGroupMembershipView);
        }

        updatePhoneticNameVisibility();

        addToDefaultGroupIfNeeded();

        final int sectionCount = getSectionViewsWithoutFields().size();
        mAddFieldButton.setVisibility(sectionCount > 0 ? VISIBLE : GONE);
        mAddFieldButton.setEnabled(isEnabled());
    }

    @Override
    public void setGroupMetaData(Cursor groupMetaData) {
        mGroupMetaData = groupMetaData;
        addToDefaultGroupIfNeeded();
        if (mGroupMembershipView != null) {
            mGroupMembershipView.setGroupMetaData(groupMetaData);
        }
    }

    /**
     * The parent of the RawContactEditorView
     * @param p the parent
     */
    public void setParent(ContactEditorFragment p) {
        parent = p;
    }

    public void setAutoAddToDefaultGroup(boolean flag) {
        this.mAutoAddToDefaultGroup = flag;
    }

    /**
     * If automatic addition to the default group was requested (see
     * {@link #setAutoAddToDefaultGroup}, checks if the raw contact is in any
     * group and if it is not adds it to the default group (in case of Google
     * contacts that's "My Contacts").
     */
    private void addToDefaultGroupIfNeeded() {
        if (!mAutoAddToDefaultGroup || mGroupMetaData == null || mGroupMetaData.isClosed() || mState == null) {
            return;
        }

        boolean hasGroupMembership = false;
        ArrayList<RawContactDelta.ValuesDelta> entries = mState.getMimeEntries(GroupMembership.CONTENT_ITEM_TYPE);
        if (entries != null) {
            for (RawContactDelta.ValuesDelta values : entries) {
                Long id = values.getGroupRowId();
                if (id != null && id.longValue() != 0) {
                    hasGroupMembership = true;
                    break;
                }
            }
        }

        if (!hasGroupMembership) {
            long defaultGroupId = getDefaultGroupId();
            if (defaultGroupId != -1) {
                RawContactDelta.ValuesDelta entry = RawContactModifier.insertChild(mState, mGroupMembershipKind);
                entry.setGroupRowId(defaultGroupId);
            }
        }
    }

    /**
     * Returns the default group (e.g. "My Contacts") for the current raw contact's
     * account.  Returns -1 if there is no such group.
     */
    private long getDefaultGroupId() {
        mGroupMetaData.moveToPosition(-1);
        while (mGroupMetaData.moveToNext()) {
            long groupId = mGroupMetaData.getLong(GroupMetaDataLoader.GROUP_ID);
            if (!mGroupMetaData.isNull(GroupMetaDataLoader.AUTO_ADD) && mGroupMetaData.getInt(GroupMetaDataLoader.AUTO_ADD) != 0) {
                return groupId;
            }
        }
        return -1;
    }

    public TextFieldsEditorView getNameEditor() {
        return mName;
    }

    public TextFieldsEditorView getPhoneticNameEditor() {
        return mPhoneticName;
    }

    private void updatePhoneticNameVisibility() {
        boolean showByDefault =
                getContext().getResources().getBoolean(R.bool.config_editor_include_phonetic_name);

        if (showByDefault || mPhoneticName.hasData() || mPhoneticNameAdded) {
            mPhoneticName.setVisibility(VISIBLE);
        } else {
            mPhoneticName.setVisibility(GONE);
        }
    }

    @Override
    public long getRawContactId() {
        return mRawContactId;
    }

    /**
     * Return a list of KindSectionViews that have no fields yet...
     * these are candidates to have fields added in
     * {@link #showAddInformationPopupWindow()}
     */
    private ArrayList<KindSectionView> getSectionViewsWithoutFields() {
        final ArrayList<KindSectionView> fields =  new ArrayList<KindSectionView>(mFields.getChildCount());
        for (int i = 0; i < mFields.getChildCount(); i++) {
            View child = mFields.getChildAt(i);
            if (child instanceof KindSectionView) {
                final KindSectionView sectionView = (KindSectionView) child;
                // If the section is already visible (has 1 or more editors), then don't offer the
                // option to add this type of field in the popup menu
                if (sectionView.getEditorCount() > 0) {
                    continue;
                }
                DataKind kind = sectionView.getKind();
                // not a list and already exists? ignore
                if ((kind.typeOverallMax == 1) && sectionView.getEditorCount() != 0) {
                    continue;
                }
                if (DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME.equals(kind.mimeType)) {
                    continue;
                }
                if (DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(kind.mimeType)
                        && mPhoneticName.getVisibility() == VISIBLE) {
                    continue;
                }

                fields.add(sectionView);
            }
        }
        return fields;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showAddInformationPopupWindow() {
        final ArrayList<KindSectionView> fields = getSectionViewsWithoutFields();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            showAddInformationPopupWindow(fields);
        }
        else {
            final PopupMenu popupMenu = new PopupMenu(getContext(), mAddFieldButton);
            final Menu menu = popupMenu.getMenu();
            for (int i = 0; i < fields.size(); i++) {
                menu.add(Menu.NONE, i, Menu.NONE, fields.get(i).getTitle());
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final KindSectionView view = fields.get(item.getItemId());
                    if (DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(view.getKind().mimeType)) {
                        mPhoneticNameAdded = true;
                        updatePhoneticNameVisibility();
                    }
                    else {
                        view.addItem();
                    }

                    // If this was the last section without an entry, we just added one, and therefore
                    // there's no reason to show the button.
                    if (fields.size() == 1) {
                        mAddFieldButton.setVisibility(GONE);
                    }

                    return true;
                }
            });
            popupMenu.show();
        }
    }
    
    private void showAddInformationPopupWindow(ArrayList<KindSectionView> fields) {
        if (parent == null)
            return;

        AddInfoDialog infoDialog = AddInfoDialog.newInstance(fields, this);
        FragmentManager fragmentManager = parent.getFragmentManager();
        infoDialog.show(fragmentManager, "SilentContactsAddInfo");        
    }

    /*
     * The add info pop-up selection dialog
     */
    public static class AddInfoDialog extends SherlockDialogFragment {

        // Use static here - no simple way to hand over the fields array or RawContactEditorView to the dialog object
        // handle with care, not thread safe
        private static ArrayList<KindSectionView> fieldsStatic;
        private static RawContactEditorView editorView;
        
        private static String SELECTION = "selection";

        /**
         * Create a AddInfoDialog instance and set the arguments.
         * 
         * @param fields
         *            Array list that contains the selectable new fields
         * @return
         */
        public static AddInfoDialog newInstance(ArrayList<KindSectionView> fields, RawContactEditorView editView) {
            fieldsStatic = fields;
            editorView = editView;
            AddInfoDialog f = new AddInfoDialog();

            String texts[] = new String[fields.size()];
            for (int i = 0; i < fields.size(); i++)
                texts[i] = fields.get(i).getTitle();        

            Bundle args = new Bundle();
            args.putStringArray(SELECTION, texts);
            f.setArguments(args);
            return f;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(getArguments().getStringArray(SELECTION), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (fieldsStatic == null || editorView == null)
                        return;
                    // The 'which' argument contains the index position
                    // of the selected item
                  final KindSectionView view = fieldsStatic.get(which);
                  if (DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(view.getKind().mimeType)) {
                      editorView.mPhoneticNameAdded = true;
                      editorView.updatePhoneticNameVisibility();
                  } 
                  else {
                      view.addItem();
                  }
  
                  // If this was the last section without an entry, we just added one, and therefore
                  // there's no reason to show the button.
                  if (fieldsStatic.size() == 1) {
                      editorView.mAddFieldButton.setVisibility(GONE);
                  }
                  editorView = null;
                  fieldsStatic = null;
                }
            });
            return builder.create();
        }
    }
}
