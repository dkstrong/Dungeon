package asf.dungeon.utility;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Danny on 11/13/2014.
 */
public class OutputAndroidFix extends Output {
        public OutputAndroidFix() {
        }

        public OutputAndroidFix(int bufferSize) {
                super(bufferSize);
        }

        public OutputAndroidFix(int bufferSize, int maxBufferSize) {
                super(bufferSize, maxBufferSize);
        }

        public OutputAndroidFix(byte[] buffer) {
                super(buffer);
        }

        public OutputAndroidFix(byte[] buffer, int maxBufferSize) {
                super(buffer, maxBufferSize);
        }

        public OutputAndroidFix(OutputStream outputStream) {
                super(outputStream);
        }

        public OutputAndroidFix(OutputStream outputStream, int bufferSize) {
                super(outputStream, bufferSize);
        }
        // string

        /** Writes the length and string, or null. Short strings are checked and if ASCII they are written more efficiently, else they
         * are written as UTF8. If a string is known to be ASCII, {@link #writeAscii(String)} may be used. The string can be read using
         * {@link com.esotericsoftware.kryo.io.Input#readString()} or {@link com.esotericsoftware.kryo.io.Input#readStringBuilder()}.
         * @param value May be null. */
        public void writeString (String value) throws KryoException {
                if (value == null) {
                        writeByte(0x80); // 0 means null, bit 8 means UTF8.
                        return;
                }
                int charCount = value.length();
                if (charCount == 0) {
                        writeByte(1 | 0x80); // 1 means empty string, bit 8 means UTF8.
                        return;
                }
                // Detect ASCII.
                boolean ascii = false;
                if (charCount > 1 && charCount < 64) {
                        ascii = true;
                        for (int i = 0; i < charCount; i++) {
                                int c = value.charAt(i);
                                if (c > 127) {
                                        ascii = false;
                                        break;
                                }
                        }
                }
                if (ascii) {
                        if (capacity - position < charCount)
                                writeAscii_slow(value, charCount);
                        else {
                                // http://stackoverflow.com/questions/12004180/exception-only-on-android-3-0
                                //value.getBytes(0, charCount, buffer, position);
                                //position += charCount;
                                byte[] valueB = value.getBytes();
                                for (int j = 0; j < valueB.length; j++) {
                                        this.writeByte(valueB[j]);
                                }
                        }
                        buffer[position - 1] |= 0x80;
                } else {
                        writeUtf8Length(charCount + 1);
                        int charIndex = 0;
                        if (capacity - position >= charCount) {
                                // Try to write 8 bit chars.
                                byte[] buffer = this.buffer;
                                int position = this.position;
                                for (; charIndex < charCount; charIndex++) {
                                        int c = value.charAt(charIndex);
                                        if (c > 127) break;
                                        buffer[position++] = (byte)c;
                                }
                                this.position = position;
                        }
                        if (charIndex < charCount) writeString_slow(value, charCount, charIndex);
                }
        }

        /** Writes the length and CharSequence as UTF8, or null. The string can be read using {@link com.esotericsoftware.kryo.io.Input#readString()} or
         * {@link com.esotericsoftware.kryo.io.Input#readStringBuilder()}.
         * @param value May be null. */
        public void writeString (CharSequence value) throws KryoException {
                if (value == null) {
                        writeByte(0x80); // 0 means null, bit 8 means UTF8.
                        return;
                }
                int charCount = value.length();
                if (charCount == 0) {
                        writeByte(1 | 0x80); // 1 means empty string, bit 8 means UTF8.
                        return;
                }
                writeUtf8Length(charCount + 1);
                int charIndex = 0;
                if (capacity - position >= charCount) {
                        // Try to write 8 bit chars.
                        byte[] buffer = this.buffer;
                        int position = this.position;
                        for (; charIndex < charCount; charIndex++) {
                                int c = value.charAt(charIndex);
                                if (c > 127) break;
                                buffer[position++] = (byte)c;
                        }
                        this.position = position;
                }
                if (charIndex < charCount) writeString_slow(value, charCount, charIndex);
        }

        /** Writes a string that is known to contain only ASCII characters. Non-ASCII strings passed to this method will be corrupted.
         * Each byte is a 7 bit character with the remaining byte denoting if another character is available. This is slightly more
         * efficient than {@link #writeString(String)}. The string can be read using {@link com.esotericsoftware.kryo.io.Input#readString()} or
         * {@link com.esotericsoftware.kryo.io.Input#readStringBuilder()}.
         * @param value May be null. */
        public void writeAscii (String value) throws KryoException {
                if (value == null) {
                        writeByte(0x80); // 0 means null, bit 8 means UTF8.
                        return;
                }
                int charCount = value.length();
                switch (charCount) {
                        case 0:
                                writeByte(1 | 0x80); // 1 is string length + 1, bit 8 means UTF8.
                                return;
                        case 1:
                                writeByte(2 | 0x80); // 2 is string length + 1, bit 8 means UTF8.
                                writeByte(value.charAt(0));
                                return;
                }
                if (capacity - position < charCount)
                        writeAscii_slow(value, charCount);
                else {
                        // http://stackoverflow.com/questions/12004180/exception-only-on-android-3-0
                        //value.getBytes(0, charCount, buffer, position);
                        //position += charCount;
                        byte[] valueB = value.getBytes();
                        for (int j = 0; j < valueB.length; j++) {
                                this.writeByte(valueB[j]);
                        }
                }
                buffer[position - 1] |= 0x80; // Bit 8 means end of ASCII.
        }


        /** Writes the length of a string, which is a variable length encoded int except the first byte uses bit 8 to denote UTF8 and
         * bit 7 to denote if another byte is present. */
        private void writeUtf8Length (int value) {
                if (value >>> 6 == 0) {
                        require(1);
                        buffer[position++] = (byte)(value | 0x80); // Set bit 8.
                } else if (value >>> 13 == 0) {
                        require(2);
                        byte[] buffer = this.buffer;
                        buffer[position++] = (byte)(value | 0x40 | 0x80); // Set bit 7 and 8.
                        buffer[position++] = (byte)(value >>> 6);
                } else if (value >>> 20 == 0) {
                        require(3);
                        byte[] buffer = this.buffer;
                        buffer[position++] = (byte)(value | 0x40 | 0x80); // Set bit 7 and 8.
                        buffer[position++] = (byte)((value >>> 6) | 0x80); // Set bit 8.
                        buffer[position++] = (byte)(value >>> 13);
                } else if (value >>> 27 == 0) {
                        require(4);
                        byte[] buffer = this.buffer;
                        buffer[position++] = (byte)(value | 0x40 | 0x80); // Set bit 7 and 8.
                        buffer[position++] = (byte)((value >>> 6) | 0x80); // Set bit 8.
                        buffer[position++] = (byte)((value >>> 13) | 0x80); // Set bit 8.
                        buffer[position++] = (byte)(value >>> 20);
                } else {
                        require(5);
                        byte[] buffer = this.buffer;
                        buffer[position++] = (byte)(value | 0x40 | 0x80); // Set bit 7 and 8.
                        buffer[position++] = (byte)((value >>> 6) | 0x80); // Set bit 8.
                        buffer[position++] = (byte)((value >>> 13) | 0x80); // Set bit 8.
                        buffer[position++] = (byte)((value >>> 20) | 0x80); // Set bit 8.
                        buffer[position++] = (byte)(value >>> 27);
                }
        }

        private void writeString_slow (CharSequence value, int charCount, int charIndex) {
                for (; charIndex < charCount; charIndex++) {
                        if (position == capacity) require(Math.min(capacity, charCount - charIndex));
                        int c = value.charAt(charIndex);
                        if (c <= 0x007F) {
                                buffer[position++] = (byte)c;
                        } else if (c > 0x07FF) {
                                buffer[position++] = (byte)(0xE0 | c >> 12 & 0x0F);
                                require(2);
                                buffer[position++] = (byte)(0x80 | c >> 6 & 0x3F);
                                buffer[position++] = (byte)(0x80 | c & 0x3F);
                        } else {
                                buffer[position++] = (byte)(0xC0 | c >> 6 & 0x1F);
                                require(1);
                                buffer[position++] = (byte)(0x80 | c & 0x3F);
                        }
                }
        }

        private void writeAscii_slow (String value, int charCount) throws KryoException {
                byte[] buffer = this.buffer;
                int charIndex = 0;
                int charsToWrite = Math.min(charCount, capacity - position);
                while (charIndex < charCount) {
                        value.getBytes(charIndex, charIndex + charsToWrite, buffer, position);
                        charIndex += charsToWrite;
                        position += charsToWrite;
                        charsToWrite = Math.min(charCount - charIndex, capacity);
                        if (require(charsToWrite)) buffer = this.buffer;
                }
        }
}
