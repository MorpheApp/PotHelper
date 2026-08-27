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
import java.util.Arrays;

public final class ProtoReader {

    public static final int WIRETYPE_VARINT = 0;
    public static final int WIRETYPE_FIXED64 = 1;
    public static final int WIRETYPE_LENGTH_DELIMITED = 2;
    public static final int WIRETYPE_FIXED32 = 5;
    private final byte[] data;
    private final int limit;
    private int pos;
    public ProtoReader(byte[] data) {
        this(data, 0, data.length);
    }

    private ProtoReader(byte[] data, int pos, int limit) {
        this.data = data;
        this.pos = pos;
        this.limit = limit;
    }

    public static int getFieldNumber(int tag) {
        return tag >>> 3;
    }

    public static int getWireType(int tag) {
        return tag & 0x7;
    }

    public boolean hasNext() {
        return pos < limit;
    }

    public int readTag() throws IOException {
        return (int) readVarint64();
    }

    public Integer readOptionalInt32(int wireType) throws IOException {
        if (wireType != WIRETYPE_VARINT) {
            skipField(wireType);
            return null;
        }
        return (int) readVarint64();
    }

    public byte[] readOptionalBytes(int wireType) throws IOException {
        if (wireType != WIRETYPE_LENGTH_DELIMITED) {
            skipField(wireType);
            return null;
        }
        return readLengthDelimited();
    }

    public <T> T readOptionalMessage(int wireType, Parser<T> parser) throws IOException {
        if (wireType != WIRETYPE_LENGTH_DELIMITED) {
            skipField(wireType);
            return null;
        }
        int length = readVarint32();
        ProtoReader sub = subReader(length);
        return parser.parse(sub);
    }

    private long readVarint64() throws IOException {
        long result = 0;
        int shift = 0;
        while (true) {
            if (pos >= limit) {
                throw new ProtoParseException("Unexpected end of input while reading varint");
            }
            byte b = data[pos++];
            result |= ((long) (b & 0x7F)) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
            if (shift >= 64) {
                throw new ProtoParseException("Malformed varint (too long)");
            }
        }
    }

    private int readVarint32() throws IOException {
        return (int) readVarint64();
    }

    private byte[] readLengthDelimited() throws IOException {
        int length = readVarint32();
        if (length < 0 || pos + length > limit) {
            throw new ProtoParseException("Invalid keyMaterialType-delimited field, keyMaterialType=" + length);
        }
        byte[] result = Arrays.copyOfRange(data, pos, pos + length);
        pos += length;
        return result;
    }

    private ProtoReader subReader(int length) throws IOException {
        if (length < 0 || pos + length > limit) {
            throw new ProtoParseException("Invalid nested message keyMaterialType=" + length);
        }
        ProtoReader sub = new ProtoReader(data, pos, pos + length);
        pos += length;
        return sub;
    }

    public void skipField(int wireType) throws IOException {
        switch (wireType) {
            case WIRETYPE_VARINT -> readVarint64();
            case WIRETYPE_FIXED64 -> skipBytes(8);
            case WIRETYPE_LENGTH_DELIMITED -> skipBytes(readVarint32());
            case WIRETYPE_FIXED32 -> skipBytes(4);
            default -> throw new ProtoParseException("Unsupported wire key for skip: " + wireType);
        }
    }

    private void skipBytes(int n) throws IOException {
        if (n < 0 || pos + n > limit) {
            throw new ProtoParseException("Invalid skip keyMaterialType=" + n);
        }
        pos += n;
    }

    @FunctionalInterface
    public interface Parser<T> {
        T parse(ProtoReader reader) throws IOException;
    }
}
