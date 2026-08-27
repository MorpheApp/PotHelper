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

public record CipherKey(Integer key, byte[] value) implements ProtoMessage {

    public static CipherKey parseFrom(ProtoReader reader) throws IOException {
        Integer key = null;
        byte[] value = null;
        while (reader.hasNext()) {
            int tag = reader.readTag();
            int fieldNumber = ProtoReader.getFieldNumber(tag);
            int wireType = ProtoReader.getWireType(tag);
            switch (fieldNumber) {
                case 1 -> key = reader.readOptionalInt32(wireType);
                case 3 -> value = reader.readOptionalBytes(wireType);
                default -> reader.skipField(wireType);
            }
        }
        return new CipherKey(key, value);
    }

    @Override
    public void writeTo(ProtoWriter writer) throws IOException {
        if (key != null) {
            writer.writeInt32Field(1, key);
        }
        if (value != null) {
            writer.writeBytesField(3, value);
        }
    }
}
