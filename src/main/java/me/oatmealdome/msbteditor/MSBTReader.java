// Licensed under WTFPL (Version 2)
// Refer to the license.txt attached.

package me.oatmealdome.msbteditor;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import me.oatmealdome.util.AssortedUtil;

/**
 * This class contains a method which allows you to read an MSBT
 * file and have it returned in the form of an MSBTFile instance.
 * 
 * @author OatmealDome
 *
 */
public class MSBTReader {

	/**
	 * Reads an MBST file and returns a representative MSBTFile instance.
	 * Based off the documentation at <a href="http://smg2-5.wikidot.com/msbt">
	 * Super Mario Galaxy 2.5's wiki.</a>
	 * 
	 * @param ras The RandomAccessFile stream. Don't ask me why it's "ras"
	 *            and not "raf".
	 * @return An MSBTFile instance.
	 * @throws Exception If any error occurs, this method will throw an Exception.
	 */
	public static MSBTFile readMSBT(RandomAccessFile ras) throws Exception {
		// Check for MSBT magic number
		byte[] magicNumbers = new byte[8];
		for (int i = 0; i < 8; i++) {
			magicNumbers[i] = ras.readByte();
		}

		String fileId = new String(magicNumbers, "UTF-8");
		if (!fileId.equals("MsgStdBn")) {
			throw new Exception("Could not find MSBT magic number, is the file really an MSBT file?");
		}

		// We skip over some bytes here:
		// short: byte order, 0xFEFF (big endian)
		// short: unknown, 0x0000
		// short: unknown, 0x0103
		// short: unknown (# of sections?), 0x0003
		// short: unknown, 0x0000
		ras.skipBytes(10);

		int fileSize = ras.readInt();
		System.out.println("MSBT file size: " + fileSize);

		// We skip over more bytes here:
		// short[5]: unknown, 0x0000
		ras.skipBytes(10);

		String[] labels = null;
		byte[][] atr1Entries = null;
		String[] text = null;

		while (true) {
			// Get section header
			byte[] headerBytes = new byte[4];
			try {
				for (int i = 0; i < 4; i++) {
					headerBytes[i] = ras.readByte();
				}
			} catch (EOFException eof) {
				System.out.println("Reached end of file.");
				break;
			}

			String sectionHeader = new String(headerBytes, "UTF-8");
			long beginningPos = ras.getFilePointer();
			int sectionSize = ras.readInt();
			System.out.println(sectionHeader + " section size: " + sectionSize);

			switch (sectionHeader) {
			case "LBL1":
				labels = parseLBL1(ras, beginningPos);
				break;
			case "ATR1":
				atr1Entries = parseATR1(ras);
				break;
			case "TXT2":
				text = parseTXT2(ras, beginningPos);
				break;
			default:
				System.out.println("Skipping unknown section " + sectionHeader + ".");
				break;
			}

			seekToNextSection(ras, beginningPos, sectionSize);
		}

		return new MSBTFile(labels, atr1Entries, text);
	}

	private static String[] parseLBL1(RandomAccessFile ras, long beginningPos) throws IOException {
		// We skip even more bytes here:
		// long[2]: unknown, all 0x00000000
		ras.skipBytes(8);

		// Always 101 sections for some reason, but we'll read it anyway
		int numOfEntries = ras.readInt();
		System.out.println("Found " + numOfEntries + " LBL1 entries");

		ArrayList<String> labels = new ArrayList<String>();

		/*
		 * TODO: This breaks on more complex MSBT files, where for some
		 * inexplicable reason numOfLabels is sometimes bigger than how
		 * many offsets there are.
		 * 
		 * TODO: In bigger/more complex MSBT files, the labels and text
		 * data will be out of order (e.g. "WinMessage" will display
		 * "Charged!" and "SuperGaugeReady" will display "You're winner!").
		 * This may be related to the above...?
		 * 
		 * Further reverse engineering and documentation is needed.
		 */
		for (int i = 0; i < numOfEntries; i++) {
			int numOfLabels = ras.readInt();
			dbg("entry " + i + " has " + numOfLabels + " (file pointer at " + Integer.toHexString((int)ras.getFilePointer()).toUpperCase() + ")");
			for (int j = 0; j < numOfLabels; j++) {
				int offset = ras.readInt();
				long lastPos = ras.getFilePointer();
				ras.seek(beginningPos + offset + 13);
				StringBuilder builder = new StringBuilder();
				while (true) {
					byte b = ras.readByte();
					if (b == 0x00) // terminator byte
						break;
					builder.append((char) b);
				}
				labels.add(builder.toString());
				ras.seek(lastPos);
			}
			if (numOfLabels == 0) {
				ras.skipBytes(4);
			}
		}

		return labels.toArray(new String[labels.size()]);
	}

	private static byte[][] parseATR1(RandomAccessFile ras) throws IOException {
		// We skip even more bytes here:
		// long[2]: unknown, all 0x00000000
		ras.skipBytes(8);

		int numOfEntries = ras.readInt();
		System.out.println("Found " + numOfEntries + " ATR1 entries.");

		int entrySize = ras.readInt();
		System.out.println("ATR1 entries are " + entrySize + " bytes long.");

		byte[][] entries = new byte[numOfEntries][];

		for (int i = 0; i < numOfEntries; i++) {
			entries[i] = new byte[entrySize];
			ras.read(entries[i]);
		}

		return entries;
	}

	private static String[] parseTXT2(RandomAccessFile ras, long beginningPos) throws IOException {
		// We skip even more bytes here:
		// long[2]: unknown, all 0x00000000
		ras.skipBytes(8);

		int numOfEntries = ras.readInt();
		System.out.println("Found " + numOfEntries + " TXT2 entries.");

		String[] entries = new String[numOfEntries];
		for (int i = 0; i < numOfEntries; i++) {
			int offset = ras.readInt();
			long lastPos = ras.getFilePointer();
			ras.seek(beginningPos + offset + 13);

			// TODO: Seems like a really hacky and messy way to do this.
			// See if there's a better method later.
			ArrayList<Byte> byteRep = new ArrayList<Byte>();
			byte b = 0x00;
			byte prevByte = 0x00;
			byteRep.add((byte) 0x00);
			while (true) {
				prevByte = b;
				b = ras.readByte();
				byteRep.add(b);
				if (prevByte == 0x00 && b == 0x00) // terminator bytes
					break;
			}
			Byte[] byteRepArray = byteRep.toArray(new Byte[byteRep.size()]);
			entries[i] = new String(AssortedUtil.toBytePrimitive(byteRepArray), "UTF-16");

			ras.seek(lastPos);
		}

		return entries;
	}

	private static void seekToNextSection(RandomAccessFile ras, long beginningPos, int sectionSize) throws IOException {
		long alignment = (sectionSize / 16 + 1) * 16 - sectionSize;
		if (alignment == 0)
			alignment = 16;
		ras.seek(beginningPos + alignment + sectionSize + 12);
	}
	
	@SuppressWarnings("unused")
	/**
	 * Internal debugging function. This exists only to shorten
	 * the amount of typing needed to print to the console.
	 * 
	 * @param obj Anything.
	 */
	private static void dbg(Object obj) {
		if (true) // disable for now
			return;
		System.out.println(obj);
	}

}
