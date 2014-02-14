package org.themassacre.minegame;

public interface HashFunction {
	public byte[] computeHash(byte[] data);
	public String toString();
	
}
