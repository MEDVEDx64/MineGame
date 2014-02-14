package org.themassacre.crypto.test;
import org.themassacre.crypto.Tworojok64;
import java.io.*;

public class TworojokTest {
	public static void main(String[] args) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		byte[] hash = Tworojok64.computeDigest(in.readLine().getBytes("UTF-8"));
		for(int i = 0; i < hash.length; i++)
			System.out.print(String.format("%02x", hash[i]));
		System.out.println();
	}

}
