package org.themassacre.crypto;
import java.io.*;

import org.themassacre.generic.Utils;

public class Chain implements Closeable, AutoCloseable {

	public static int diff = 0;
	protected static HashFunction func = new Tworojok64HashFunction();
	String last = "";
	OutputStream out;
	
	public static void main(String[] args) {
		BufferedReader cIn = new BufferedReader(new InputStreamReader(System.in));
		
		if(args.length > 0) {
			if(args[0].equals("verify")) {
				try {
					int result = verify(System.in);
					if(result == 0) {
						System.err.println("Chain is valid.");
						System.exit(0);
					} else {
						System.err.println("Chain is NOT valid (bad hash at line " + result + ").");
						System.exit(1);
					}
				} catch(IOException e) {
					System.err.println("Verification failed: I/O error.");
					System.exit(-1);
				}
			}
		}
		
		try(Chain chain = new Chain(System.out)) {
			while(true) {
				String line = cIn.readLine();
				if(line == null) break;
				chain.println(line);
				
			}
		} catch(Exception e) {
			System.err.println(e);
		}

	}

	public Chain(File f) throws FileNotFoundException {
		out = new FileOutputStream(f);
	}
	
	public Chain(OutputStream stream) {
		out = stream;
	}
	
	void println(String s) throws IOException {
		byte[] hash = func.computeHash(last.getBytes("UTF-8"));
		last = Utils.hexView(hash) + (s.length() == 0? "": " " + s);
		out.write((last + "\n").getBytes("UTF-8"));
	}
	
	@Override
	public void close() throws IOException {
		byte[] hash;
		long nonce = 0;
		String candidate = Utils.hexView(func.computeHash(last.getBytes("UTF-8"))) + " ";
		
		if(diff == 0) 
			hash = func.computeHash(last.getBytes("UTF-8"));
		else {
			do {
				nonce++;
				hash = func.computeHash((candidate + nonce).getBytes("UTF-8"));
			} while(!Utils.isHashAccepted(hash, diff));
		}
		
		if(diff != 0)
			out.write((candidate + nonce + "\n").getBytes("UTF-8"));
		out.write((Utils.hexView(hash) + "\n").getBytes("UTF-8"));
		out.close();
		
		last = "";
	}
	
	/** Validation of a chain file/stream: returns 0 on success, otherwise number of chain-breaking line will be returned. */
	public static int verify(InputStream stream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		int lineNum = 0;
		String prev = "";
		
		while(true) {
			lineNum++;
			
			String line = in.readLine();
			if(line == null) break;
			if(line.length() == 0) continue;
			
			//try {
				String hashStr = (line + " ").split(" +")[0];
				if(!hashStr.equalsIgnoreCase(Utils.hexView(func.computeHash(prev.getBytes("UTF-8")))))
					return lineNum;
				
				prev = line;
			//} catch(ArrayIndexOutOfBoundsException e) {
			//	return lineNum;
			//}
		}
		
		return 0;
	}

}
