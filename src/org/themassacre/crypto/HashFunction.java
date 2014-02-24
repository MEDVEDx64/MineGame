package org.themassacre.crypto;

public interface HashFunction {
	public byte[] computeHash(byte[] data);
	public String toString();
	
}
