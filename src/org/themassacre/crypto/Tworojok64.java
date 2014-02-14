/* medvedx64 2014 02 13 */

package org.themassacre.crypto;
import org.themassacre.minegame.Utils;

public class Tworojok64 {
	public static byte[] computeDigest(byte[] bytes) {
		byte[] src;
		if((bytes.length % 8) == 0) {
			src = new byte[bytes.length + 8];
			System.arraycopy(bytes, 0, src, 0, bytes.length);
		}
		else {
			src = new byte[bytes.length + 8 - (bytes.length % 8) + 8];
			System.arraycopy(bytes, 0, src, 0, bytes.length);
		}
		
		long[] digest = new long[8];
		
		digest[0] = Long.parseLong("CFB1DF3178E75B7", 16);
		digest[1] = Long.parseLong("44EAFFD72D3AC0EC", 16);
		digest[2] = Long.parseLong("FD6983DCB9162AA", 16);
		digest[3] = Long.parseLong("E4ECBFB86AF98CD", 16);
		digest[4] = Long.parseLong("91EE2C65FE4FCD6", 16);
		digest[5] = Long.parseLong("7E40F1C98ACF42B6", 16);
		digest[6] = Long.parseLong("D0C939D340227F4", 16);
		digest[7] = Long.parseLong("992D9117ED55990", 16);
		
		for(int i = 0; i < bytes.length; i++) {
			byte[] tmp = new byte[8];
			System.arraycopy(src, i, tmp, 0, 8);
			long current = Utils.bytesToLong(tmp);
			
			for(int z = 0; z < 1024; z++) {
				
				for(int x = 0;  x < 8; x++) {
					digest[x] ^= (digest[(x == 0)? 7: x-1] + current);
					digest[x] += Long.rotateRight(digest[x], i + x + 15);
				}

			}

		}
		
		return Utils.createBytes(digest);
		
	}
}
