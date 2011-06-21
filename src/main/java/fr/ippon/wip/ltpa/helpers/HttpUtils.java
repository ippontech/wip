package fr.ippon.wip.ltpa.helpers;

import java.util.Vector;

import fr.ippon.wip.ltpa.exception.Base64DecodeException;

public class HttpUtils {

	private static final String	base64Chars	= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	public static final String base64Encode( byte[] bytes ) {
		if( bytes == null ) return null;

		StringBuffer ret = new StringBuffer();

		for( int sidx = 0, didx = 0; sidx < bytes.length; sidx += 3, didx += 4 ) {
			ret.append( base64Chars.charAt( ( bytes[ sidx ] >>> 2 ) & 077 ) );
			if( sidx + 1 < bytes.length ) {
				ret.append( base64Chars.charAt( ( bytes[ sidx + 1 ] >>> 4 ) & 017 | ( bytes[ sidx ] << 4 ) & 077 ) );
				if( sidx + 2 < bytes.length )
					ret.append( base64Chars.charAt( ( bytes[ sidx + 2 ] >>> 6 ) & 003 | ( bytes[ sidx + 1 ] << 2 ) & 077 ) );
				else
					ret.append( base64Chars.charAt( ( bytes[ sidx + 1 ] << 2 ) & 077 ) );
				if( sidx + 2 < bytes.length ) ret.append( base64Chars.charAt( bytes[ sidx + 2 ] & 077 ) );
			} else
				ret.append( base64Chars.charAt( ( bytes[ sidx ] << 4 ) & 077 ) );
		}

		int mod = ret.length() % 4;
		for( int i = 0; ( mod > 0 ) && ( i < 4 - mod ); i++ )
			ret.append( '=' );

		return ret.toString();
	} // public static final String base64Encode( byte[] bytes )

	public static final byte[] base64Decode( String data ) throws Base64DecodeException {
		if( data.length() == 0 ) return new byte[ 0 ];
		Vector dest = new Vector( data.length() );

		// ASCII printable to 0-63 conversion
		int prevBits = 0; // stores the bits left over from the previous step
		int modAdjust = 0; // stores the start of the current line.
		for( int i = 0; i < data.length(); i++ ) {
			char ch = data.charAt( i ); // get the character
			if( ch == '=' ) break; // is it the padding character, no check for correct position
			int mod = ( i - modAdjust ) % 4; // what is the index modulo 4 in the current line 
			if( mod == 0 ) {
				// the line can only be broken on modulo 0 (e.g. 72, 76 character per line. MIME specifies 76 as max).
				if( ( ch == '\r' ) || ( ch == '\n' ) ) { // we handle the encoders that use '\n' only as well
					modAdjust = i + 1; // skip the [CR/]LF sequence. The new line probably starts at i + 1;
					continue;
				}
			}
			// if we came to here, there was no special character
			int x = base64Chars.indexOf( ch ); // search for the character in the table
			if( x < 0 ) throw new Base64DecodeException(); // if the character was not found raise an exception
			switch( mod ) {
				case 0:
					prevBits = x << 2; // just store the bits and continue
					break;
				case 1:
					dest.add( new Byte( (byte)( prevBits | x >>> 4 ) ) ); // previous 6 bits OR 2 new ones
					prevBits = ( x & 017 ) << 4; // store 4 bits
					break;
				case 2:
					dest.add( new Byte( (byte)( prevBits | x >>> 2 ) ) ); // previous 4 bits OR 4 new ones
					prevBits = ( x & 003 ) << 6; // store 2 bits
					break;
				case 3:
					dest.add( new Byte( (byte)( prevBits | x ) ) ); // previous 2 bits OR 6 new ones
					break;
			}
		}

		byte[] ret = new byte[ dest.size() ]; // convert the Vector into an array
		for( int i = 0; i < ret.length; i++ )
			ret[ i ] = ( (Byte)dest.get( i ) ).byteValue();

		return ret;
	}

	public static final boolean isBase64Encoded( String sBase64 ) {
		int len = sBase64.length();
		if( len % 4 != 0 ) return false;
		for( int i = 0; i < len; i++ ) {
			char c = sBase64.charAt( i );
			if( ( c >= 'a' ) && ( c <= 'z' ) ) continue;
			if( ( c >= 'A' ) && ( c <= 'Z' ) ) continue;
			if( ( c >= '0' ) && ( c <= '9' ) ) continue;
			if( ( c == '+' ) || ( c == '/' ) || ( c == '=' ) ) continue;
			return false;
		}
		return true;
	}

}
