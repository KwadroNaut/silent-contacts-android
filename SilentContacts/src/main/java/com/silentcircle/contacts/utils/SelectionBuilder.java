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
 * Copyright (C) 2011 The Android Open Source Project
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

package com.silentcircle.contacts.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a selection clause by concatenating several clauses with AND.
 */
public class SelectionBuilder {
    private static final String[] EMPTY_STRING_ARRAY =  new String[0];
    private final List<String> mWhereClauses;

    /**
     * @param baseSelection The base selection to start with. This is typically
     *      the user supplied selection arg. Pass null if no base selection is
     *      required.
     */
    public SelectionBuilder(String baseSelection) {
        mWhereClauses = new ArrayList<String>();
        addClause(baseSelection);
    }

    /**
     * Adds a new clause to the selection. Nothing is added if the supplied clause
     * is null or empty.
     */
    public SelectionBuilder addClause(String clause) {
        if (!TextUtils.isEmpty(clause)) {
            mWhereClauses.add(clause);
        }
        return this;
    }

    /**
     * Returns a combined selection clause with AND of all clauses added using
     * {@link #addClause(String)}. Returns null if no clause has been added or
     * only null/empty clauses have been added till now.
     */
    public String build() {
        if (mWhereClauses.size() == 0) {
            return null;
        }
        return DbQueryUtils.concatenateClauses(mWhereClauses.toArray(EMPTY_STRING_ARRAY));
    }
}
