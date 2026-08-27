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
import android.util.Pair;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import app.morphe.pot.helper.potokens.parser.KeySet;
import app.morphe.pot.helper.potokens.parser.IntegrityToken;
import app.morphe.pot.helper.potokens.parser.PoTokenResult;

public class PoTokenServiceImpl {
    private static final String TAG = "morphe: PoTokenServiceImpl";

    static {
        System.loadLibrary("pot");
    }

    public static byte[] buildPoTokenResult(byte[] bytes) {
        Log.d(TAG, "buildPoTokenResult");

        try {
            byte[][][] integrityTokens = mintMorpheIntegrityTokens(bytes);
            IntegrityToken integrityToken = new IntegrityToken(
                    encrypt(integrityTokens[0][0], integrityTokens[1][0]),
                    integrityTokens[2][0]
            );
            PoTokenResult poTokenResult = new PoTokenResult(integrityToken);
            return poTokenResult.toByteArray();
        } catch (Exception ex) {
            Log.e(TAG, "buildPoTokenResult failed", ex);
        }

        return new byte[0];
    }

    private static byte[] encrypt(byte[] rawKey, byte[] data) {
        try {
            Pair<Integer, SecretKeySpec> keyPair = KeySet.parseFrom(rawKey).getKeyPair();
            Integer keyId = keyPair.first;
            SecretKeySpec key = keyPair.second;
            byte[] iv = new byte[12];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            byte[] result = new byte[data.length + 28];
            System.arraycopy(iv, 0, result, 0, 12);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            cipher.doFinal(data, 0, data.length, result, 12);
            if (keyId != null) {
                byte[] output = new byte[result.length + 5];
                output[0] = 1;
                byte[] identifier = ByteBuffer.allocate(5).put(output[0]).putInt(keyId).array();
                System.arraycopy(identifier, 0, output, 0, identifier.length);
                System.arraycopy(result, 0, output, identifier.length, result.length);
                result = output;
            }
            return result;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to encrypt data", ex);
        }

        return new byte[0];
    }

    private static native byte[][][] mintMorpheIntegrityTokens(byte[] data);
}