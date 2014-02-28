package org.themassacre.crypto;


public class Tworojok64HashFunction implements HashFunction {
	public byte[] computeHash(byte[] data) {
		return org.themassacre.crypto.Tworojok64.computeDigest(data);
	}
	
	public int getDigestLength() {
		return 64;
	}
	
	public String toString() {
		return "Tworojok64";
	}
	
}
