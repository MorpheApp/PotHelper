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

package app.morphe.pot.helper.potokens;

import android.util.Log;

public class PoTokenServiceImpl {
    private static final String TAG = "morphe: PoTokenServiceImpl";

    static {
        System.loadLibrary("pot");
    }

    public static byte[] buildPoTokenResult(byte[] identifier) {
        Log.d(TAG, "buildPoTokenResult");

        try {
            byte[] result = mintPoTokenResult(identifier);
            if (result == null || result.length == 0) {
                throw new IllegalStateException("Native token generation returned no result");
            }
            return result;
        } catch (RuntimeException ex) {
            Log.e(TAG, "buildPoTokenResult failed", ex);
            throw ex;
        }
    }

    private static native byte[] mintPoTokenResult(byte[] identifier);
}
