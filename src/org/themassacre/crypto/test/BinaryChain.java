package org.themassacre.crypto.test;

import org.themassacre.crypto.*;

public class BinaryChain {

	static HashFunction func = new Tworojok64HashFunction();
	
	public static void main(String[] args) {
		int speed = 0;
		long length = 0;
		long time_prev = 0;
		byte[] hash = func.computeHash(new byte[0]); // genesis
		try {
			while(true) {
				System.out.write(hash);
				hash = func.computeHash(hash);
				
				speed++;
				length += 64;
				long time_current = System.currentTimeMillis()/1000;
				if(time_current > time_prev) {
					System.err.println(length/1000 + " KB, " + speed + " hashes/s");
					time_prev = time_current;
					speed = 0;
				}
			}
		} catch(Exception e) {
			System.err.println(e);
		}

	}

}
