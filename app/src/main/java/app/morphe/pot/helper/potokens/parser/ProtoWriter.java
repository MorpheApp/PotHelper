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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class ProtoWriter {

    private static final int WIRETYPE_VARINT = 0;
    private static final int WIRETYPE_LENGTH_DELIMITED = 2;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public void writeInt32Field(int fieldNumber, int value) {
        writeTag(fieldNumber, WIRETYPE_VARINT);
        writeVarint64(value);
    }

    public void writeBytesField(int fieldNumber, byte[] value) throws IOException {
        writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        writeVarint64(value.length);
        buffer.write(value);
    }

    public void writeMessageField(int fieldNumber, ProtoMessage message) throws IOException {
        ProtoWriter nested = new ProtoWriter();
        message.writeTo(nested);
        byte[] payload = nested.toByteArray();
        writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        writeVarint64(payload.length);
        buffer.write(payload);
    }

    private void writeTag(int fieldNumber, int wireType) {
        writeVarint64(((long) fieldNumber << 3) | wireType);
    }

    private void writeVarint64(long value) {
        while (true) {
            if ((value & ~0x7FL) == 0) {
                buffer.write((int) value);
                return;
            }
            buffer.write((int) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
    }

    public byte[] toByteArray() {
        return buffer.toByteArray();
    }
}
