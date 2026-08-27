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

import java.io.IOException;

public record Key(KeyData data, Integer keyId) implements ProtoMessage {

    public static Key parseFrom(ProtoReader reader) throws IOException {
        KeyData data = null;
        Integer keyId = null;
        while (reader.hasNext()) {
            int tag = reader.readTag();
            int fieldNumber = ProtoReader.getFieldNumber(tag);
            int wireType = ProtoReader.getWireType(tag);
            switch (fieldNumber) {
                case 1 -> data = reader.readOptionalMessage(wireType, KeyData::parseFrom);
                case 3 -> keyId = reader.readOptionalInt32(wireType);
                default -> reader.skipField(wireType);
            }
        }
        return new Key(data, keyId);
    }

    @Override
    public void writeTo(ProtoWriter writer) throws IOException {
        if (data != null) {
            writer.writeMessageField(1, data);
        }
        if (keyId != null) {
            writer.writeInt32Field(3, keyId);
        }
    }
}
