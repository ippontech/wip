/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Web Integration Portlet (WIP).
 *	Web Integration Portlet (WIP) is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Web Integration Portlet (WIP) is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Web Integration Portlet (WIP).  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.ltpa.token;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.codec.binary.Base64;

public class LtpaLibrary {

    private static final byte[] ltpaTokenVersion = {0, 1, 2, 3};
    private static final int dateStringLength = 8;
    private static final String dateStringFiller = "00000000";
    private static final int creationDatePosition = ltpaTokenVersion.length;
    private static final int expirationDatePosition = creationDatePosition + dateStringLength;
    private static final int preUserDataLength = ltpaTokenVersion.length + dateStringLength + dateStringLength;
    private static final int hashLength = 20;

    /**
     * This method parses the LtpaToken cookie received from the web browser and returns the information in the <tt>TokenData</tt>
     * class.
     *
     * @param ltpaToken     - the cookie (base64 encoded).
     * @param ltpaSecretStr - the contents of the <tt>LTPA_DominoSecret</tt> field from the SSO configuration document.
     * @return The contents of the cookie. If the cookie is invalid (the hash - or some other - test fails), this method returns
     *         <tt>null</tt>.
     * @throws NoSuchAlgorithmException
     */
    public static TokenData parseLtpaToken(String ltpaToken, String ltpaSecretStr) throws NoSuchAlgorithmException {
        byte[] data = Base64.decodeBase64(ltpaToken.getBytes());

        int variableLength = data.length - hashLength;
        /* Compare to 20 to since variableLength must be at least (preUserDataLength + 1) [21] character long:
           * Token version: 4 bytes
           * Token creation: 8 bytes
           * Token expiration: 8 bytes
           * User name: variable length > 0
           */
        if (variableLength <= preUserDataLength) return null;

        byte[] ltpaSecret = Base64.decodeBase64(ltpaSecretStr.getBytes());

        if (!validateSHA(data, variableLength, ltpaSecret)) return null;

        if (!compareBytes(data, 0, ltpaTokenVersion, 0, ltpaTokenVersion.length)) return null;

        TokenData ret = new TokenData();
        ret.tokenCreated.setTimeInMillis((long) Integer.parseInt(getString(data, creationDatePosition, dateStringLength), 16) * 1000);
        ret.tokenExpiration
                .setTimeInMillis((long) Integer.parseInt(getString(data, expirationDatePosition, dateStringLength), 16) * 1000);

        byte[] nameBuffer = new byte[data.length - (preUserDataLength + hashLength)];
        System.arraycopy(data, preUserDataLength, nameBuffer, 0, variableLength - preUserDataLength);
        ret.username = new String(nameBuffer);

        return ret;
    }

    private static boolean validateSHA(byte[] ltpaTokenData, int variableLength, byte[] ltpaSecret) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        byte[] digestData = new byte[variableLength + ltpaSecret.length];

        System.arraycopy(ltpaTokenData, 0, digestData, 0, variableLength);
        System.arraycopy(ltpaSecret, 0, digestData, variableLength, ltpaSecret.length);

        byte[] digest = sha1.digest(digestData);

        if (digest.length > ltpaTokenData.length - variableLength) return false;

        int bytesToCompare = (digest.length <= ltpaTokenData.length - variableLength) ? digest.length : ltpaTokenData.length
                - variableLength;

        return compareBytes(digest, 0, ltpaTokenData, variableLength, bytesToCompare);
    }

    private static boolean compareBytes(byte[] b1, int offset1, byte[] b2, int offset2, int len) {
        if (len < 0) return false;
        // offset must be positive, and the compare chunk must be contained the buffer
        if ((offset1 < 0) || (offset1 + len > b1.length)) return false;
        if ((offset2 < 0) || (offset2 + len > b2.length)) return false;

        for (int i = 0; i < len; i++)
            if (b1[offset1 + i] != b2[offset2 + i]) return false;

        return true;
    }

    /**
     * Convert bytes from the buffer into a String.
     *
     * @param buffer - the buffer to take the bytes from.
     * @param offset - the offset in the buffer to start at.
     * @param len    - the number of bytes to convert into a String.
     * @return - A String representation of specified bytes.
     */
    private static String getString(byte[] buffer, int offset, int len) {
        if (len < 0) return "";
        if ((offset + len) > buffer.length) return "";

        byte[] str = new byte[len];
        System.arraycopy(buffer, offset, str, 0, len);
        return new String(str);
    }

    /**
     * Create a valid LTPA token for the specified user. The creation time is <tt>now</tt>.
     *
     * @param username        - the user to create the LTPA token for.
     * @param durationMinutes - the duration of the token validity in minutes.
     * @param ltpaSecretStr   - the LTPA Domino Secret to use to create the token.
     * @return - base64 encoded LTPA token, ready for the cookie.
     * @throws NoSuchAlgorithmException
     */
    public static String createLtpaToken(String username, int durationMinutes, String ltpaSecretStr) throws NoSuchAlgorithmException {
        return createLtpaToken(username, new GregorianCalendar(), durationMinutes, ltpaSecretStr);
    }

    /**
     * Create a valid LTPA token for the specified user.
     *
     * @param username        - the user to create the LTPA token for.
     * @param creationTime    - the time the token becomes valid.
     * @param durationMinutes - the duration of the token validity in minutes.
     * @param ltpaSecretStr   - the LTPA Domino Secret to use to create the token.
     * @return - base64 encoded LTPA token, ready for the cookie.
     * @throws NoSuchAlgorithmException
     */
    public static String createLtpaToken(String username, GregorianCalendar creationTime, int durationMinutes, String ltpaSecretStr)
            throws NoSuchAlgorithmException {
        // create byte array buffers for both strings
    	byte[] ltpaSecret = Base64.decodeBase64(ltpaSecretStr.getBytes() );
        byte[] usernameArray = username.getBytes();

        byte[] workingBuffer = new byte[preUserDataLength + usernameArray.length + ltpaSecret.length];

        // copy version into workingBuffer
        System.arraycopy(ltpaTokenVersion, 0, workingBuffer, 0, ltpaTokenVersion.length);

        GregorianCalendar expirationDate = (GregorianCalendar) creationTime.clone();
        expirationDate.add(Calendar.MINUTE, durationMinutes);

        // copy creation date into workingBuffer
        String hex = dateStringFiller + Integer.toHexString((int) (creationTime.getTimeInMillis() / 1000)).toUpperCase();
        System
                .arraycopy(hex.getBytes(), hex.getBytes().length - dateStringLength, workingBuffer, creationDatePosition,
                        dateStringLength);

        // copy expiration date into workingBuffer
        hex = dateStringFiller + Integer.toHexString((int) (expirationDate.getTimeInMillis() / 1000)).toUpperCase();
        System.arraycopy(hex.getBytes(), hex.getBytes().length - dateStringLength, workingBuffer, expirationDatePosition,
                dateStringLength);

        // copy user name into workingBuffer
        System.arraycopy(usernameArray, 0, workingBuffer, preUserDataLength, usernameArray.length);

        // copy the ltpaSecret into the workingBuffer
        System.arraycopy(ltpaSecret, 0, workingBuffer, preUserDataLength + usernameArray.length, ltpaSecret.length);

        byte[] hash = createHash(workingBuffer);

        // put the public data and the hash into the outputBuffer
        byte[] outputBuffer = new byte[preUserDataLength + usernameArray.length + hashLength];
        System.arraycopy(workingBuffer, 0, outputBuffer, 0, preUserDataLength + usernameArray.length);
        System.arraycopy(hash, 0, outputBuffer, preUserDataLength + usernameArray.length, hashLength);

        return new String(Base64.encodeBase64(outputBuffer));
    }

    private static byte[] createHash(byte[] buffer) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return sha1.digest(buffer);
    }
}
