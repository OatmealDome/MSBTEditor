// Licensed under WTFPL (Version 2)
// Refer to the license.txt attached.

package me.oatmealdome.msbteditor;

/**
 * A Java representation of an MSBT file.
 * 
 * @author OatmealDome
 *
 */
public class MSBTFile {
	
	public final String[] labels;
	public final byte[][] atrData;
	public final String[] text;
	
	public MSBTFile(String[] labels, byte[][] atrData, String[] text) {
		this.labels = labels;
		this.atrData = atrData;
		this.text = text;
	}

}
