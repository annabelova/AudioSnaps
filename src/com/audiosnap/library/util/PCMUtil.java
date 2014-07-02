package com.audiosnap.library.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class PCMUtil {

	public static void fadeIn(File file, long per){
		short[] data = null;
		try {
			data = IOUtil.readFileShortArray(file, ByteOrder.BIG_ENDIAN);
			int length = (int)(data.length * per/100);
			for(int i = 0; i < length; i++){
				 data[i] *= 1/(length - i);
			}
			IOUtil.writeShortArrayToFile(data, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	 }
	 
	 public static void fadeOut(File file, long per){
		 short[] data = null;
			try {
				data = IOUtil.readFileShortArray(file, ByteOrder.BIG_ENDIAN);
				int length = (int)(data.length * per/100);
				for(int i = data.length - 1; i > data.length - length; i--){
					 data[i] *= 1/(length - data.length + i + 1);
				}
				IOUtil.writeShortArrayToFile(data, file);
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }
	 
	 public static void removeFirstBytes(File file, long num){
		 try {
			
			// temporary file
			File tmp = new File(file.getAbsolutePath() + ".tmp"); 
			 
			// cut-off and copy to temporary file
			FileOutputStream fout = new FileOutputStream(tmp.getAbsolutePath());
			FileInputStream fin = new FileInputStream(file.getAbsolutePath());
			FileChannel chanOut = fout.getChannel();
			FileChannel chanIn = fin.getChannel();
			chanIn.transferTo(num, chanIn.size()  - num, chanOut);
			fin.close();
			fout.close();
			
			// copy the temporary file to the original
			fout = new FileOutputStream(file.getAbsolutePath());
			fin = new FileInputStream(tmp.getAbsolutePath());
			chanOut = fout.getChannel();
			chanIn = fin.getChannel();
			chanIn.transferTo(0, chanIn.size(), chanOut);
			fin.close();
			fout.close();
			
			// delete temporary file
			tmp.delete();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	 }
	 
	 public static ByteBuffer removeFirstBytes(ByteBuffer buffer, int num){

			// set the marker at the initial position
			buffer.position(buffer.limit() - num);
			
			// temporal array for storing result
			byte[] tmp = new byte[buffer.remaining()];
			
			// copy last bytes to temporary
			buffer.get(tmp);
			
			ByteBuffer res = ByteBuffer.wrap(tmp);
			
			res.rewind();
		 
			return res;
	 }
	 
//	 public static short[] fadeInfadeOut(ByteBuffer buffer, long per){
//			ShortBuffer samples = buffer.asShortBuffer();
//			int length = (int) (samples.limit() * per / 100);
//			short[] shorts = new short[samples.limit()];
//			for(int i = 0; i < samples.limit(); i++){
//				if(i < length){
//					shorts[i] = 
//							Double.valueOf((Double.valueOf(samples.get(i)) / (length - i))).shortValue();
//				}else if(i > samples.limit() - length){
//					shorts[i] = 
//							Double.valueOf((Double.valueOf(samples.get(i)) / (length - (samples.limit() - i) + 1))).shortValue();
//				}else{
//					shorts[i] = samples.get(i);
//				}
//			}
//			return shorts;
//		}
	 
	 public static short[] fadeInfadeOut(ByteBuffer buffer, long per){
		 
		// returned value is the actual array, not a copy, so modifications 
		// to the array write through to the buffer
		 
		 	short[] samples = shorts(buffer);
			
			int length = (int) (samples.length * per / 100);
			
			double ctr = 1.0;
			
			for(int i = 0; i < length; i++){
				
				ctr = Double.valueOf(i)/Double.valueOf(length);
				
				samples[i] = 
						Double.valueOf((samples[i] * ctr)).shortValue();
				
				samples[samples.length - i - 1] = 
						Double.valueOf(samples[samples.length - i - 1] * ctr).shortValue();
			}
			
			return samples;
		 
	 }
	 
	 public static short[] shorts(ByteBuffer buffer){
		ShortBuffer samples = buffer.asShortBuffer();
		short[] shorts = new short[samples.limit()];
		for(int i = 0; i < samples.limit(); i++) shorts[i] = samples.get(i);
		return shorts;
	}
	 
}
