package org.themassacre.crypto.app;

import java.io.*;
import org.themassacre.crypto.*;

import com.sun.corba.se.impl.ior.ByteBuffer;

class DotThread extends Thread {
	public void run() {
		while(true) {
			System.err.print(".");
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException eInt) {
				break;
			}
		}
	}
}

public class Baercoder {

	static HashFunction func = new Tworojok64HashFunction();
	static byte[] hash = new byte[func.getDigestLength()];
	
	public static void main(String[] args) throws IOException {
		if(args.length < 1) {
			System.err.println("No key file specified.");
			System.exit(-1);
		}
		
		boolean beVerbose = (args.length > 1 && args[1].equals("-v"))? true: false;
		ByteBuffer buffer = new ByteBuffer();
		
		System.err.print("Loading key file");
		DotThread dt = new DotThread();
		dt.start();
		
		try(FileInputStream in = new FileInputStream(new File(args[0]))) {
			while(true) {
				int c = in.read();
				if(c < 0) break;
				buffer.append(c);
			}
		} catch(Exception e) {
			System.err.println("--- Can't read key file ---");
			e.printStackTrace();
			System.exit(-1);
		}
		
		System.err.print("\nComputing genesis");
		byte[] hash = func.computeHash(buffer.toArray());
		
		dt.interrupt();
		System.err.print("\nEncoding.\n");
		
		int lenTotal = 0;
		int prevTotal = 0;
		byte[] streamBuf = new byte[func.getDigestLength()];
		while(true) {
			int len = System.in.read(streamBuf, 0, func.getDigestLength());
			if(len <= 0) break;
			for(int i = 0; i < func.getDigestLength(); i++)
				streamBuf[i] ^= hash[i];
			System.out.write(streamBuf, 0, len);
			
			if(beVerbose) {
				lenTotal += len;
				if(lenTotal != prevTotal)
					System.err.print("\r" + ((lenTotal > 1023)? (lenTotal/1024 + "K"): lenTotal) + " ");
				prevTotal = lenTotal;
			}
			
			byte[] hashNew = func.computeHash(hash);
			hash = hashNew;
		}
		
		System.err.println();
	}

}


/*		dropped code

		while(true) {
			b = System.in.read();
			if(b < 0) break;
			System.out.write(b^hash[cPos]);
			
			cPos++;
			if(cPos >= func.getDigestLength()) {
				cPos = 0;
				byte[] hashNew = func.computeHash(hash);
				hash = hashNew;
			}
		}

*/