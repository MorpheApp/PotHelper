/*
 * Copyright (C) 2013-2017 microG Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.common.api;

import com.google.android.gms.common.PublicApi;
import org.microg.safeparcel.AutoSafeParcelable;

@PublicApi
public final class Status extends AutoSafeParcelable implements Result {
    public static final Status SUCCESS = new Status();

    public Status() {
    }

    @Override
    public Status getStatus() {
        return this;
    }

    public static final Creator<Status> CREATOR = new AutoCreator<>(Status.class);
}
