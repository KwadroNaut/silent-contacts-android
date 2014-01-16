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
 * limitations under the License
 */

package com.silentcircle.contacts.editor;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.silentcircle.contacts.R;

import java.util.ArrayList;

/**
 * Shows a popup asking the user what to do for a photo. The result is passed back to the Listener
 */
public class PhotoActionPopup {
    public static final String TAG = "PhotoActionPopup";

    /**
     * Bitmask flags to specify which actions should be presented to the user.
     */
    public static final class Flags {
        /** If set, show choice to use as primary photo. */
        public static final int ALLOW_PRIMARY = 1;
        /** If set, show choice to remove photo. */
        public static final int REMOVE_PHOTO = 2;
        /** If set, show choices to take a picture with the camera, or pick one from the gallery. */
        public static final int TAKE_OR_PICK_PHOTO = 4;
        /**
         *  If set, modifies the wording in the choices for TAKE_OR_PICK_PHOTO
         *  to emphasize that the existing photo will be replaced.
         */
        public static final int TAKE_OR_PICK_PHOTO_REPLACE_WORDING = 8;
    }

    /**
     * Convenient combinations of commonly-used flags (see {@link Flags}).
     */
    public static final class Modes {
        public static final int NO_PHOTO = Flags.TAKE_OR_PICK_PHOTO;
        public static final int READ_ONLY_ALLOW_PRIMARY = Flags.ALLOW_PRIMARY;
        public static final int PHOTO_DISALLOW_PRIMARY = Flags.REMOVE_PHOTO | Flags.TAKE_OR_PICK_PHOTO |
                Flags.TAKE_OR_PICK_PHOTO_REPLACE_WORDING;
        public static final int PHOTO_ALLOW_PRIMARY = Flags.ALLOW_PRIMARY | Flags.REMOVE_PHOTO | Flags.TAKE_OR_PICK_PHOTO |
                Flags.TAKE_OR_PICK_PHOTO_REPLACE_WORDING;
    }

    private static ArrayList<ChoiceListItem> fillChoices(Context context, int mode) {
        // Build choices, depending on the current mode. We assume this Dialog is never called
        // if there are NO choices (e.g. a read-only picture is already super-primary)
        final ArrayList<ChoiceListItem> choices = new ArrayList<ChoiceListItem>(4);
        // Use as Primary
        if ((mode & Flags.ALLOW_PRIMARY) > 0) {
            choices.add(new ChoiceListItem(ChoiceListItem.ID_USE_AS_PRIMARY, context.getString(R.string.use_photo_as_primary)));
        }
        // Remove
        if ((mode & Flags.REMOVE_PHOTO) > 0) {
            choices.add(new ChoiceListItem(ChoiceListItem.ID_REMOVE, context.getString(R.string.removePhoto)));
        }
        // Take photo or pick one from the gallery.  Wording differs if there is already a photo.
        if ((mode & Flags.TAKE_OR_PICK_PHOTO) > 0) {
            boolean replace = (mode & Flags.TAKE_OR_PICK_PHOTO_REPLACE_WORDING) > 0;
            final int takePhotoResId = replace ? R.string.take_new_photo : R.string.take_photo;
            final String takePhotoString = context.getString(takePhotoResId);

            final int pickPhotoResId = replace ? R.string.pick_new_photo : R.string.pick_photo;
            final String pickPhotoString = context.getString(pickPhotoResId);

            choices.add(new ChoiceListItem(ChoiceListItem.ID_TAKE_PHOTO, takePhotoString));
            choices.add(new ChoiceListItem(ChoiceListItem.ID_PICK_PHOTO, pickPhotoString));
        }

        return choices;
    }

    public static PhotoActionDialog createPopupMenu_10(Context context, final Listener listener, int mode) {

        final ArrayList<ChoiceListItem> choices = fillChoices(context, mode);

        return PhotoActionDialog.newInstance(choices, listener);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static ListPopupWindow createPopupMenu(Context context, View anchorView, final Listener listener, int mode) {

        final ArrayList<ChoiceListItem> choices = fillChoices(context, mode);

        final ListAdapter adapter = new ArrayAdapter<ChoiceListItem>(context, R.layout.select_dialog_item, choices);

        final ListPopupWindow listPopupWindow = new ListPopupWindow(context);
        final OnItemClickListener clickListener = new OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ChoiceListItem choice = choices.get(position);
                switch (choice.getId()) {
                    case ChoiceListItem.ID_USE_AS_PRIMARY:
                        listener.onUseAsPrimaryChosen();
                        break;
                    case ChoiceListItem.ID_REMOVE:
                        listener.onRemovePictureChosen();
                        break;
                    case ChoiceListItem.ID_TAKE_PHOTO:
                        listener.onTakePhotoChosen();
                        break;
                    case ChoiceListItem.ID_PICK_PHOTO:
                        listener.onPickFromGalleryChosen();
                        break;
                }
                listPopupWindow.dismiss();
            }
        };

        listPopupWindow.setAnchorView(anchorView);
        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setOnItemClickListener(clickListener);
        listPopupWindow.setModal(true);
        listPopupWindow.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
        final int minWidth = context.getResources().getDimensionPixelSize(R.dimen.photo_action_popup_min_width);
        if (anchorView.getWidth() < minWidth) {
            listPopupWindow.setWidth(minWidth);
        }
        return listPopupWindow;
    }

    private static final class ChoiceListItem {
        private final int mId;
        private final String mCaption;

        public static final int ID_USE_AS_PRIMARY = 0;
        public static final int ID_TAKE_PHOTO = 1;
        public static final int ID_PICK_PHOTO = 2;
        public static final int ID_REMOVE = 3;

        public ChoiceListItem(int id, String caption) {
            mId = id;
            mCaption = caption;
        }

        @Override
        public String toString() {
            return mCaption;
        }

        public int getId() {
            return mId;
        }
    }

    public interface Listener {
        void onUseAsPrimaryChosen();
        void onRemovePictureChosen();
        void onTakePhotoChosen();
        void onPickFromGalleryChosen();
    }
    
    /*
     * The photo action selection dialog
     */
    public static class PhotoActionDialog extends SherlockDialogFragment {

        // Use static here - no simple way to hand over the fields array or RawContactEditorView to the dialog object
        // handle with care, not thread safe
        private static ArrayList<ChoiceListItem> choiceStatic;
        private static Listener listener;
        private static String SELECTION = "selection";

        public static PhotoActionDialog newInstance(ArrayList<ChoiceListItem> choices, final Listener l) {
            PhotoActionDialog f = new PhotoActionDialog();
            choiceStatic = choices;
            listener = l;
            
            String texts[] = new String[choices.size()];
            int i = 0;
            for (ChoiceListItem choice : choices)
                texts[i++] = choice.toString();

            Bundle args = new Bundle();
            args.putStringArray(SELECTION, texts);
            f.setArguments(args);
            return f;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(getArguments().getStringArray(SELECTION), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (choiceStatic == null || listener == null)
                        return;
                    // The 'which' argument contains the index position of the selected item
                    int id = choiceStatic.get(which).getId();

                    switch (id) {
                    case ChoiceListItem.ID_USE_AS_PRIMARY:
                        listener.onUseAsPrimaryChosen();
                        break;
                    case ChoiceListItem.ID_REMOVE:
                        listener.onRemovePictureChosen();
                        break;
                    case ChoiceListItem.ID_TAKE_PHOTO:
                        listener.onTakePhotoChosen();
                        break;
                    case ChoiceListItem.ID_PICK_PHOTO:
                        listener.onPickFromGalleryChosen();
                        break;
                }
                }
            });
            return builder.create();
        }

    }
}
