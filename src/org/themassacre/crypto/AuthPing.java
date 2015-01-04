package org.themassacre.crypto;

import java.util.Random;

public class AuthPing {
	HashFunction func;
	public AuthPing(HashFunction func) {
		this.func = func;
	}
	
	public byte[] answer(byte[] data, byte[] key) {
		byte[] result = new byte[data.length];
		byte[] hash = func.computeHash(key);
		
		int pos = 0;
		for(int i = 0; i < data.length; i++) {
			result[i] = (byte)(data[i] ^ hash[pos]);
			if(pos >= hash.length-1) {
				pos = 0;
				hash = func.computeHash(hash);
			}
			
			pos++;
		}
		
		return func.computeHash(result);
	}
	
	public byte[] genQuestion() {
		byte[] q = new byte[func.getDigestLength()*2];
		Random r = new Random(System.currentTimeMillis());
		
		for(int i = 0; i < q.length; i++)
			q[i] = (byte)r.nextInt();
		
		return q;
	}
	
}
