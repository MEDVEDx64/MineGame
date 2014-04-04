package org.themassacre.crypto.test;
import java.io.*;

public class EntropyTest {

	public static void main(String[] args) throws IOException {
		long[] counters = new long[256];
		
		while(true) {
			int b = System.in.read();
			if(b < 0) break;
			counters[b]++;
		}
		
		//System.out.println("----- Results table: -----");
		for(int i = 0; i < 256; i++)
			System.out.println(counters[i]);
		//	System.out.println(i + ":\t" + counters[i]);
		//System.out.println("--------------------------");

	}

}
