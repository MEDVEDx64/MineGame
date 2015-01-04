package org.themassacre.crypto.app;
import javax.xml.bind.DatatypeConverter;
import org.themassacre.crypto.*;
import java.io.*;
import java.net.Socket;

public class AuthTool {

	static void produceAnswer(BufferedReader in, PrintWriter out, String keyFileName) throws IOException {
		int len;
		byte[] tmp = new byte[4096];
		try(FileInputStream f = new FileInputStream(new File(keyFileName))) {
			len = f.read(tmp, 0, 4096);
		}
		byte[] key = new byte[len];
		System.arraycopy(tmp, 0, key, 0, len);
		
		String src = in.readLine();
		if(src == null || src.trim().length() == 0) {
			System.err.println("I got nothin'!");
			return;
		}
		
		out.println(DatatypeConverter.printBase64Binary((new AuthPing(new SHA256HashFunction())
				.answer(DatatypeConverter.parseBase64Binary(src), key))));
		out.flush();
	}
	
	public static void main(String[] args) throws IOException {
		String keyFileName;
		try {
			keyFileName = args[args.length-1];
			if(keyFileName.startsWith("-"))
				throw new RuntimeException();
		} catch(RuntimeException e) {
			System.err.println("No key file specified.");
			return;
		}

		String addr = null;
		short port = 0;
		for(String s: args) {
			if(s.startsWith("--host")) {
				try {
					String[] spl = s.split(":");
					addr = spl[1];
					port = Short.parseShort(spl[2]);
				} catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
					System.err.println("Bad host format (example: --host:localhost:19090).");
					return;
				}
			}
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter out = new PrintWriter(System.out);
		
		if(addr != null && port > 0) {
			try(Socket s = new Socket(addr, port)) {
				BufferedReader sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintWriter sout = new PrintWriter(s.getOutputStream());
				produceAnswer(sin, sout, keyFileName);
				(new RecvThread(sin, out)).start();
				
				while(true) {
					String l = in.readLine();
					if(l == null)
						break;
					sout.println(l);
					sout.flush();
				}
			}
		} else {
			produceAnswer(in, out, keyFileName);
		}

	}

}

class RecvThread extends Thread {
	BufferedReader in;
	PrintWriter out;
	
	public RecvThread(BufferedReader in, PrintWriter out) {
		this.in = in;
		this.out = out;
	}
	
	@Override public void run() {
		try {
			while(true) {
				String l = in.readLine();
				if(l == null)
					break;
				out.println(l);
				out.flush();
			}
		} catch(IOException e) {
		}
	}
}
