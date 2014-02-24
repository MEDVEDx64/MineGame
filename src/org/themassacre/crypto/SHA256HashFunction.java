package org.themassacre.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SHA256HashFunction implements HashFunction {
	public static boolean isNativeAvailable = false;
	
	public static void loadNativeKernel() {
	    try {
	        System.loadLibrary("computehash");
	        isNativeAvailable = true;
	        System.err.println("Using native kernel");
	    } catch(Throwable e) {
	    }
	}
    
    public static native byte[] computeHashNative(byte[] data);
    
    public static byte[] computeHashJava(byte[] data) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
		}
		
		md.update(data);
		return md.digest();
	}
	
	public byte[] computeHash(byte[] data) {
		if(isNativeAvailable)
		    return computeHashNative(data);
		return computeHashJava(data);
	}
	
	public String toString() {
		return "SHA-256";
	}
}
