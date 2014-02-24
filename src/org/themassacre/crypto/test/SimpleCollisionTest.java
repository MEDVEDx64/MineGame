package org.themassacre.crypto.test;
import org.themassacre.crypto.Tworojok64;
import org.themassacre.generic.Utils;

import java.util.ArrayList;

public class SimpleCollisionTest {
	static int test(ArrayList<byte[]> hashes, byte[] cur_hash)
	{
		for (byte[] b: hashes) {
			int i; for (i = 0; i<b.length /*must be equal && i<cur_hash.length*/; i++) if(b[i] != cur_hash[i]) break;
			if (i == b.length) return i;
		}
		return -1;
	}
	
	public static void main(String[] args) {/*
		byte[] i = {0,0,0,0,0,0,0,0};
		ArrayList<byte[]> hashes = new ArrayList<byte[]>();
		for (int k = 0; k<1000; k++) {
		//while (true) {
			if(i[7] == 0) System.out.printf("0x%02x%02x%02x%02x%02x%02x%02x%02x\n", i[0], i[1], i[2], i[3], i[4], i[5], i[6], i[7]);
						
			byte[] chash = Tworojok64.computeDigest(i);
			
			 
			if (test(hashes, chash))
					System.out.printf("!!! Collision detected @ 0x%02x%02x%02x%02x%02x%02x%02x%02x\n", i[0], i[1], i[2], i[3], i[4], i[5], i[6], i[7]);
			hashes.add(chash);
				
			if (++i[7] == 0 && ++i[6] == 0 && ++i[5] == 0 && ++i[4] == 0 && ++i[3] == 0 && ++i[2] == 0 && ++i[1] == 0) i[0]++;
		}
*/			
		int found = 0;
		ArrayList<byte[]> hashes = new ArrayList<byte[]>();
		long[] current = {0};
		//for (int k = 0; k<1000; k++) {
		while (true) {
			if(current[0]%100 == 0)
				System.out.println("(" + found + ") " + current[0]);
			
			byte[] c = Utils.createBytes(current);
			int idx;
			for(idx = 0; idx < 8; idx++)
				if(c[idx] != 0) break;
			byte[] c1 = new byte[8-idx];
			System.arraycopy(c, idx, c1, 0, 8-idx);
			
			byte[] chash = Tworojok64.computeDigest(c1);
			if(test(hashes, chash) >= 0) {
				System.out.println("!!! Collision detected @ " + current[0] + " == " + test(hashes, chash));
				found++;
			}
			hashes.add(chash);
			
			current[0]++;
		}

	}

}
