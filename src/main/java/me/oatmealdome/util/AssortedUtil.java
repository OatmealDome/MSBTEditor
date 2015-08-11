// Licensed under WTFPL (Version 2)
// Refer to the license.txt attached.

package me.oatmealdome.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Assorted utility functions that I like to use. Some of these 
 * are probably already available in Apache Commons, but oh well.
 * 
 * @author OatmealDome
 *
 */
public class AssortedUtil {
	
	/**
	 * Converts a Byte[] array to its primitive, byte[].
	 * 
	 * @param source Byte[] array
	 * @return byte[] array
	 */
	public static byte[] toBytePrimitive(Byte[] source) {
		byte[] dest = new byte[source.length];
		for (int i = 0; i < source.length; i++) {
			dest[i] = source[i];
		}
		return dest;
	}
	
	/**
	 * Converts a byte[] array to its wrapper, Byte[].
	 * 
	 * @param source byte[] array
	 * @return Byte[] array
	 */
	public static Byte[] toByteWrapper(byte[] source) {
		Byte[] dest = new Byte[source.length];
		for (int i = 0; i < source.length; i++) {
			dest[i] = source[i];
		}
		return dest;
	}
	
	/**
	 * Returns the Throwable's stack trace as a String.
	 * 
	 * @param throwable The Throwable.
	 * @return The stack trace as a String.
	 */
	public static String stackTraceAsString(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);
		return stringWriter.toString();
	}
	
}
