package com.audiosnap.library.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

public class IOUtil {
    
    public static ByteBuffer readFileByteBuffer(File file, ByteOrder order) throws IOException{
    	FileChannel inChannel = new RandomAccessFile(file, "r").getChannel();
		ByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size()).order(order);
		// access the buffer as you wish.
		inChannel.close();
		return buffer;
    }
    
    public static short[] readFileShortArray(File file, ByteOrder order) throws IOException{
		return shorts(readFileByteBuffer(file, order));
    }
    
    public static byte[] readFile(File file, ByteOrder order) throws IOException{
    	return array(readFileByteBuffer(file, order));
    }
    
    public static byte[] readFile(String file, ByteOrder order) throws IOException {
        return readFile(new File(file), order);
    }
    
    private static short[] shorts(ByteBuffer buffer){
		ShortBuffer samples = buffer.asShortBuffer();
		short[] shorts = new short[samples.limit()];
		for(int i = 0; i < samples.limit(); i++) shorts[i] = samples.get(i);
		return shorts;
	}
    
    private static byte[] array(ByteBuffer buffer){
		byte[] array = new byte[buffer.limit()];
		for(int i = 0; i < buffer.limit(); i++) array[i] = buffer.get(i);
		return array;
	}
    
    public static void writeByteBuffer(ByteBuffer buffer, File file, ByteOrder order) throws IOException{
    	FileChannel channel = new FileOutputStream(file, false).getChannel();
    	buffer.rewind();
		// Writes a sequence of bytes to this channel from the given buffer.
		channel.write(buffer.order(order));
		// close the channel
		channel.close();
    }
    
    public static void writeShortArrayToFile(short[] array, File file) throws IOException{
    	
    	ByteBuffer myByteBuffer = ByteBuffer.allocate(array.length * 2);
    	myByteBuffer.order(ByteOrder.BIG_ENDIAN);

    	ShortBuffer myShortBuffer = myByteBuffer.asShortBuffer();
    	myShortBuffer.put(array);

    	FileChannel out = new FileOutputStream(file).getChannel();
    	out.write(myByteBuffer);
    	out.close();
    	
    }
}