package com.afollestad.aimage.cache;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {

    private static final char[] toDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static final MessageDigest sha256MessageDigest = getDigest("SHA-256");

    /**
     * Calculates the SHA-256 digest and returns the value as a hex string.
     * <p>
     * Throws a <code>RuntimeException</code> on JRE versions prior to
     * 1.4.0.
     * </p>
     *
     * @param data
     *            Data to digest
     * @return SHA-256 digest as a hex string
     * @since 1.4
     */
    public static String sha256Hex(String data) {
        return new String(encodeHex(sha256MessageDigest.digest(getBytesUnchecked(data, "UTF-8"))));
    }

    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static char[] encodeHex(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    private static byte[] getBytesUnchecked(String string, String charsetName) {
        if (string == null) {
            return null;
        }
        try {
            return string.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }
}
