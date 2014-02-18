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
		
		long[] fuse1 = { 0x33180B0641EFF54L };
		byte[] fuse = Utils.createBytes(fuse1);
		System.arraycopy(fuse, 0, src, src.length-8, 8);
		long[] digest = new long[8];
		
		digest[0] = 0xCFB1DF3178E75B7L;
		digest[1] = 0x44EAFFD72D3AC0ECL;
		digest[2] = 0xFD6983DCB9162AAL;
		digest[3] = 0xE4ECBFB86AF98CDL;
		digest[4] = 0x91EE2C65FE4FCD6L;
		digest[5] = 0x7E40F1C98ACF42B6L;
		digest[6] = 0xD0C939D340227F4L;
		digest[7] = 0x992D9117ED55990L;
		
		int z;
		long current;
		byte[] tmp = new byte[8];
		for(int i = 0; i < bytes.length; i++) {
			System.arraycopy(src, i, tmp, 0, 8);
			current = Utils.bytesToLong(tmp) >> (4-(i&3));
		
			for(z = 0; z < 1024; z++) {
				
				digest[0] ^= (digest[7] + current);
				digest[0] += Long.rotateRight(digest[0], i + 15);

				for(int x = 1;  x < 8; x++) {
					digest[x] ^= (digest[x-1] + current);
					digest[x] += Long.rotateRight(digest[x], i + x + 15);
				}

			}

		}
		
		return Utils.createBytes(digest);
		
	}
}
