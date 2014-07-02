package com.audiosnap.library.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.Arrays;
import android.annotation.TargetApi;
import android.os.Build;
import com.apache.commons.codec.DecoderException;
import com.apache.commons.codec.binary.Base64;
import com.apache.commons.codec.binary.Hex;
import com.audiosnap.library.KMPMatch;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class ExifUtil {

	public static void mergeIntoAudioSnap(String jpegPath, File audio, int width, int height) throws DecoderException, IOException{
		
		 byte[] sequence = Hex.decodeHex("FFDB".toCharArray());
		    
		 byte[] jpeg = IOUtil.readFile(jpegPath, ByteOrder.LITTLE_ENDIAN);
		    
		 int pos = KMPMatch.indexOf(jpeg, sequence);
		 
		 if(pos != -1){
			 byte[] jpeg1 = copyOfRange(jpeg, 0, pos);
			 byte[] exif = Hex.decodeHex(createExifFromaudio(audio, width, height).toCharArray());
			 byte[] jpeg2 =  copyOfRange(jpeg, pos, jpeg.length);
			 RandomAccessFile f = new RandomAccessFile(new File(jpegPath), "rw");
			 f.write(merge(jpeg1, exif, jpeg2));
			 f.close();
		 }
		
	}
	
	private static byte[] merge(byte[] one, byte[] two, byte[] three){
		byte[] combined = new byte[one.length + two.length + three.length];
		System.arraycopy(one,0,combined,0 ,one.length);
		System.arraycopy(two,0,combined,one.length,two.length);
		System.arraycopy(three,0,combined,one.length + two.length, three.length);
		return combined;
	}
	
	private static String createExifFromaudio(File audio, int width, int height) throws UnsupportedEncodingException, IOException{
		
		String encodedAudio = 
				Hex.encodeHexString(
						Base64.encodeBase64(IOUtil.readFile(audio, ByteOrder.LITTLE_ENDIAN))
				);
		
		String exif = 
				"457869660000" +
				"4D4D" +
				"002A" +
				"00000008" +
				"0003" +
				"0112" +
				"0003" +
				"00000001" +
				"00010000" +
				"0131" +
				"0002" +
				"0000000A" +
				"00000032" +
				"8769" +
				"0004" +
				"00000001" +
				"0000003C" +
				"00000000" +
				"417564696F536E617073" +
				"0004" +
				"927C" +
				"0002" +
				intToHex(encodedAudio.length() / 2, 8) +
				"00000072" +
				"A001" +
				"0003"+
				"00000001" +
				"00010000" +
				"A002" +
				"0004" +
				"00000001" +
				intToHex(width, 8) +
				"A003" +
				"0004" +
				"00000001" +
				intToHex(height, 8) +
				"00000000";
		
		exif = 	"FFE1" +
				intToHex(2 + exif.length() / 2, 4) + exif + encodedAudio;

		return exif.toUpperCase();
	}
	
	private static String intToHex(int num, int digits){
		String n = Integer.toHexString(num);
		if(n.length() < digits) n = padZeroes(digits - n.length()) + n;
		return n; 
	}
	
	private static String padZeroes(int n){
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < n; i++) s.append('0');
		return s.toString();
	}
	
	public static byte[] copyOfRange(byte[] original, int from, int to) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
	    int newLength = to - from;
	    if (newLength < 0)
	        throw new IllegalArgumentException(from + " > " + to);
	    byte[] copy = new byte[newLength];
	    System.arraycopy(original, from, copy, 0,
	                     Math.min(original.length - from, newLength));
	    return copy;
		}
		else return  Arrays.copyOfRange(original,from,to);
			
	}
	
	
}