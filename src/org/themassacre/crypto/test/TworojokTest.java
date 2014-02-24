package org.themassacre.crypto.test;
import org.themassacre.crypto.Tworojok64;
import org.themassacre.generic.Utils;

import java.io.*;

public class TworojokTest {
	public static void main(String[] args) throws Exception {
		ByteArrayOutputStream src = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		while(true) {
		    int len = System.in.read(buf, 0, 1024);
		    if(len <= 0) break;
		    src.write(buf, 0, len);
		}
		
		byte[] hash = Tworojok64.computeDigest(src.toByteArray());
		System.out.println();
		System.out.println(Utils.hexView(hash));
	}

}
