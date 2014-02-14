package org.themassacre.minegame;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

public class Utils {
    public static final String CMD_TRAIL = ";";
    public static final String ARG_TRAIL = ":";
	
	public static byte[] createBytes(long[] data) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 8);        
		LongBuffer lBuffer = byteBuffer.asLongBuffer();
		lBuffer.put(data);

		return byteBuffer.array();
	}
	
	public static long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(8);
	    buffer.put(bytes);
	    buffer.flip(); 
	    return buffer.getLong();
	}
	
	public static long[] packBytes(byte[] data) {
		if((data.length % 8) != 0) return null;
		long[] result = new long[data.length/8];
		byte[] buffer = new byte[8];
		for(int i = 0; i < data.length/8; i++) {
			System.arraycopy(data, i * 8, buffer, 0, 8);
			result[i] = bytesToLong(buffer);
		}
		return result;
	}
	
	public static boolean isHashAccepted(byte[] hash, int diff) {
		try {
			for(int i = 0; i < diff; i++) {
				if(hash[i] != 0)
					return false;
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			return false;
		}
		
		return true;
	}
	
	public static List<String[]> parseMessage(String msg) {
		List<String[]> result = new ArrayList<String[]>();
		try {
			if(!msg.startsWith(CMD_TRAIL)) return result;
			String[] commands = msg.substring(1).split(CMD_TRAIL);
			for(String c: commands)
				result.add(c.split(ARG_TRAIL));
		} catch(Exception e) {
		}
		
		return result;
	}
	
	public static String createMessage(List<String[]> src) {
		String result = "";
		for(int i = 0; i < src.size(); i++) {
			result += CMD_TRAIL;
			String[] current = src.get(i);
			for(int z = 0; z < current.length; z++) {
				result += current[z];
				if(z != current.length-1)
					result += ARG_TRAIL;
			}
		}
		
		return result;
	}
	
	public static String formatSimpleMessage(String cmd, String arg) {
		List<String[]> l = new ArrayList<String[]>();
		String[] s;
		if(arg == null)
			s = new String[1];
		else {
			s = new String[2];
			s[1] = arg;
		}
		s[0] = cmd;
		l.add(s);
		return Utils.createMessage(l);
	}
	
    public static long nextLong(Random rng, long n) {
        long bits, val;
        do {
            bits = (rng.nextLong() << 1) >>> 1;
            val = bits % n;
        } while (bits-val+(n-1) < 0L);
        return val;
    }
}
