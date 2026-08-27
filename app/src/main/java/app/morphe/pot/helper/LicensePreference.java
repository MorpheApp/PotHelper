/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/PotHelper
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

package app.morphe.pot.helper;

public enum LicensePreference {
    PROJECT_NOTICE_WITH_LICENSE(
            "pref_project_license_pot_helper",
            "https://github.com/MorpheApp/PotHelper",
            true,
            false
    ),
    SAFEPARCEL(
            "pref_third_party_license_safe_parcel",
            "https://github.com/microg/SafeParcel",
            true,
            false
    ),
    NANOPB(
            "pref_third_party_license_nano_pb",
            "https://github.com/nanopb/nanopb",
            false,
            true
    );

    public final String key;
    public final String url;
    public final boolean resizeDialog;
    public final boolean useNumberFormat;

    LicensePreference(String key, String url, boolean resizeDialog, boolean useNumberFormat) {
        this.key = key;
        this.url = url;
        this.resizeDialog = resizeDialog;
        this.useNumberFormat = useNumberFormat;
    }
}
