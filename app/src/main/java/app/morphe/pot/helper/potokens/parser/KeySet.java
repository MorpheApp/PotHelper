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

package app.morphe.pot.helper.potokens.parser;

import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

public record KeySet(List<Key> keyList) implements ProtoMessage {

    public static KeySet parseFrom(byte[] data) throws IOException {
        return parseFrom(new ProtoReader(data));
    }

    public static KeySet parseFrom(ProtoReader reader) throws IOException {
        List<Key> keyList = new ArrayList<>();
        while (reader.hasNext()) {
            int tag = reader.readTag();
            int fieldNumber = ProtoReader.getFieldNumber(tag);
            int wireType = ProtoReader.getWireType(tag);
            if (fieldNumber == 2) {
                Key key = reader.readOptionalMessage(wireType, Key::parseFrom);
                if (key != null) {
                    keyList.add(key);
                }
            } else {
                reader.skipField(wireType);
            }
        }
        return new KeySet(Collections.unmodifiableList(keyList));
    }

    public Pair<Integer, SecretKeySpec> getKeyPair() {
        Key key = keyList.get(0);
        Integer keyId = key.keyId();
        KeyData keyData = key.data();
        CipherKey cipherKey = keyData.value();
        byte[] keyBytes = cipherKey.value();

        return new Pair<>(keyId, new SecretKeySpec(keyBytes, "AES"));
    }

    @Override
    public void writeTo(ProtoWriter writer) throws IOException {
        if (keyList != null) {
            for (Key key : keyList) {
                writer.writeMessageField(2, key);
            }
        }
    }
}
