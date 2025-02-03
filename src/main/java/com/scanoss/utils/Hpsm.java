// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2024, SCANOSS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.scanoss.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * SCANOSS Hpsm Class
 * <p>
 * The Hpsm class provides all the necessary implementations to generate HPSM fingerprint for a given file or contents.
 * </p>
 */
public class Hpsm {

    // CRC8 table, Polynomial, initial CRC and post CRC XOR value.
    private static final int CRC8_MAXIM_DOW_TABLE_SIZE = 0x100;
    private static final int CRC8_MAXIM_DOW_POLYNOMIAL = 0x8C; // 0x31 reflected
    private static final int CRC8_MAXIM_DOW_INITIAL = 0x00; // 0x00 reflected
    private static final int CRC8_MAXIM_DOW_FINAL = 0x00; // 0x00 reflected
    private static int[] crc8MaximDowTable = new int[CRC8_MAXIM_DOW_TABLE_SIZE];

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    /**
     * Calculates the HPSM value for the given content, represented as an array of bytes.
     * This method performs normalization on the content, calculates CRC8 for each line,
     * and returns the hexadecimal representation of the CRC8 values.
     *
     * @param content the content as an array of bytes
     * @return the HPSM value in hexadecimal format
     */
    public static String calcHpsm(byte[] content) {
        List<Integer> listNormalized = new ArrayList<>();
        List<Integer> crcLines = new ArrayList<>();

        int lastLine = 0;
        crc8MaximDowGenerateTable();

        for (int i = 0; i < content.length ; i++) {
            char c = (char) content[i];
            if (c == '\n') {  // When there is a new line
                if (!listNormalized.isEmpty()) {
                    crcLines.add(crc8MaximDowBuffer(convertListToByteArray(listNormalized)));
                    listNormalized.clear();
                } else if (lastLine + 1 == i) {
                    crcLines.add(0xFF);
                } else if (i - lastLine > 1) {
                    crcLines.add(0x00);
                }
                lastLine = i;
            } else {
                int cNormalized = WinnowingUtils.normalize(c);
                if (cNormalized != 0) listNormalized.add(cNormalized);
            }
        }

        return convertToHex(convertListToByteArray(crcLines));
    }

    /**
     * Calculates CRC-8 using the Maxim/Dallas polynomial without using a lookup table.
     * This method is suitable for applications where memory constraints are critical
     * and a lookup table cannot be afforded.
     *
     * @param crc The current CRC value.
     * @param b The byte to be processed.
     * @return The updated CRC value after processing the byte.
     */
    private static int crc8MaximDowByteNoTable(int crc, int b) {
        crc ^= b;
        for (int count = 0; count < 8; count++) {
            boolean isSet = (crc & 0x01) != 0;
            crc >>= 1;
            if (isSet) crc ^= CRC8_MAXIM_DOW_POLYNOMIAL;
        }
        return crc;
    }

    /**
     * Generates a lookup table for CRC-8 using the Maxim/Dallas polynomial.
     * The generated table is used for faster CRC calculations.
     */
    private static void crc8MaximDowGenerateTable() {
        for (int i = 0; i < CRC8_MAXIM_DOW_TABLE_SIZE; i++) {
            crc8MaximDowTable[i] = crc8MaximDowByteNoTable(0, i);
        }
    }

    /**
     * Calculates CRC-8 using the Maxim/Dow polynomial with a lookup table.
     * This method utilizes a pre-generated lookup table for faster CRC calculations.
     *
     * @param crc The current CRC value.
     * @param b The byte to be processed.
     * @return The updated CRC value after processing the byte.
     */
    private static int crc8MaximDowByte(int crc, int b) {
        int index = b ^ crc;
        return crc8MaximDowTable[index] ^ (crc >> 8);
    }

    /**
     * Calculates CRC-8 for a buffer of bytes using the Maxim/Dallas polynomial.
     *
     * @param buffer The buffer containing bytes for CRC calculation.
     * @return The CRC-8 value for the given buffer.
     */
    private static int crc8MaximDowBuffer(byte[] buffer) {
        int crc = CRC8_MAXIM_DOW_INITIAL;
        for (byte b : buffer) {
            crc = crc8MaximDowByte(crc, b & 0xFF); // Convert byte to unsigned integer
        }
        crc ^= CRC8_MAXIM_DOW_FINAL;
        return crc;
    }

    /**
     * Converts a list of integers to a byte array.
     *
     * @param integerList The list of integers to be converted.
     * @return The byte array representing the converted integers.
     */
    private static byte[] convertListToByteArray(List<Integer> integerList) {
        byte[] byteArray = new byte[integerList.size()];
        for (int i = 0; i < integerList.size(); i++) {
            byteArray[i] = integerList.get(i).byteValue();
        }
        return byteArray;
    }

    /**
     * Converts an array of bytes to its hexadecimal representation.
     *
     * @param bytes the array of bytes to be converted
     * @return the hexadecimal representation of the input byte array
     */
    private static String convertToHex(byte [] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8).toLowerCase();
    }
}
