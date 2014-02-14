package org.themassacre.minegame;

public class Tworojok64HashFunction implements HashFunction {
	public byte[] computeHash(byte[] data) {
		return org.themassacre.crypto.Tworojok64.computeDigest(data);
	}
	
	public String toString() {
		return "Tworojok64";
	}
	
}
