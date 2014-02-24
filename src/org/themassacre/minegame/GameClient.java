package org.themassacre.minegame;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.DatatypeConverter;

import org.themassacre.crypto.HashFunction;
import org.themassacre.crypto.SHA256HashFunction;
import org.themassacre.crypto.Tworojok64HashFunction;
import org.themassacre.generic.Utils;

class Watcher extends Thread {
    @Override public void run() {
        try {
            long[] nonceArr = new long[1];
			nonceArr[0] = GameClient.getCrew().getResult();
			if(GameClient.getCrew().isDone()) {
    			System.out.println("! Sending nonce !");
    			GameClient.sendln(Utils.formatSimpleMessage("NONCE", DatatypeConverter.printBase64Binary(Utils.createBytes(nonceArr))));
			}
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception eAnother) {
        }
    }
}

public class GameClient {
    public static MiningCrew getCrew() {
        return crew;
    }
    
	static OutputStream sOut = null;
	static MiningCrew crew = null;
	public static String nickname = "";
	private static int threads = Runtime.getRuntime().availableProcessors();
	
	public static int getNumThreads() {
	    return threads;
	}
	
	static int[] rules = null;
	static long[] pBlock = null;
	static long[] sBlock = null;
	
	public static void sendln(String s) throws IOException {
		sOut.write((s + "\n").getBytes("UTF-8"));
	}
	
	static ConsoleListener c = null;
	public static boolean autoReady = false;
	
	public static void reset() {
		rules = null;
		pBlock = null;
		sBlock = null;
	}
	
	static List<String[]> lastMessage = new ArrayList<String[]>();
	public static boolean isOKMessageReceived() {
	    for(String[] cmd: lastMessage) {
	        try {
	            if(cmd[0].equals("OK"))
	                return true;
	        } catch(ArrayIndexOutOfBoundsException e) {
	        }
	    }
	    return false;
	}
	
	static HashFunction func = new SHA256HashFunction();
	static String currentFunc = "SHA-256";
	static String prevFunc = "SHA-256";
	
	static void updateHashFunction() {
		if(!currentFunc.equals(prevFunc)) {
			if(currentFunc.equals("Tworojok64")) {
				func = new Tworojok64HashFunction();
				prevFunc = currentFunc;
			}
			else {
				func = new SHA256HashFunction();
				prevFunc = "SHA-256";
			}
		}
	}
	
	public static void startMining(int threads) throws IOException {
	    if(rules == null || sBlock == null || pBlock == null)
	        System.out.println("ERROR: Can't mine because server didn't sent minimal required data set (or data need to be updated)!");
	    else {
	    	updateHashFunction();
	        crew = new MiningCrew(rules[2], threads, pBlock, sBlock, func);
	        new Watcher().start();
	    }
	}
	
	public static void stopMining() {

	    if(crew != null) {
    	    crew.stop();
    	    crew = null;
	    }
    	    
	}
	
	public static void toggleMining() throws IOException {
	    if(crew == null)
	        startMining(threads);
	    else
	        stopMining();
	}
	
	public static void main(String[] args) {
		String addr = "localhost";
		short port = 9750;

		try {
		    if(args[0].contains("help")) {
    		    System.err.println("Usage: GameClient host port threads nickname");
    		    System.exit(-1);
    		}
		    
			addr = args[0];
			port = Short.parseShort(args[1]);
			threads = Integer.parseInt(args[2]);
			nickname = args[3];
		} catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
		}
		
		SHA256HashFunction.loadNativeKernel();
		
		try(Socket s = new Socket(InetAddress.getByName(addr), port)) {
			System.out.println("Connected to " + s.getInetAddress());
			BufferedReader sIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
			sOut = s.getOutputStream();
			
			System.out.println("Sending Magic to server");
			sendln(Utils.formatSimpleMessage("CBA AFR KTL", null));
			c = new ConsoleListener();
			
			while(true) {
				String line = sIn.readLine();
				if(line.trim().length() == 0)
					break;
					
				System.out.println("--- Server says: " + line);
				List<String[]> message = Utils.parseMessage(line);
				if(message.size() == 0) {
					System.out.println("Warning: server response contains no valid messages");
					continue;
				}
				
				lastMessage = message;
				
				try {
					for(String[] cmd: message) {
					    
					    if(cmd[0].equals("ERR"))
							System.out.println("Error: " + (cmd.length > 1? cmd[1]: "<no message>"));
					    
						else if(cmd[0].equals("CRIT")) {
						    System.out.println("Critical error: " + (cmd.length > 1? cmd[1]: "<no message>"));
							reset();
							stopMining();
						}
						
						else if(cmd[0].equals("DROP")) {
						    System.out.println("Dropped from round" + (cmd.length > 1? ": " + cmd[1]: "."));
							reset();
							stopMining();
						}
						
						else if(cmd[0].equals("ROUND")) {
						    System.out.println("Server initiated a new round.");
						    
						    if(autoReady) {
						        System.out.println("Automatically sending READY signal");
						        sendln(Utils.formatSimpleMessage("READY", null));
						    }
						    
						    stopMining();
							reset();
						}
						
						else if(cmd[0].equals("RULES")) {
							rules = GameServer.parseRules(cmd[1]);
							if(rules[3] != 0)
								System.out.println("Warning: RULES string has failed to parse, DEFAULTS USED.");
						}
						
						else if(cmd[0].equals("PBLOCK"))
							pBlock = Utils.packBytes(DatatypeConverter.parseBase64Binary(cmd[1]));
						
						else if(cmd[0].equals("SBLOCK"))
							sBlock = Utils.packBytes(DatatypeConverter.parseBase64Binary(cmd[1]));
						
						else if(cmd[0].equals("NEAR"))
							System.out.println("Regisration closed.");
						
						else if(cmd[0].equals("START")) {
						    System.out.println("Got STRAT signal!");
						    startMining(threads);
						}
						
						else if(cmd[0].equals("WINNER")) {
						    System.out.println("WINNER: " + cmd[1]);
						    if(cmd[1].equals(nickname.split("\\x2e")[0])) {
						        long[] nonceArr = new long[1];
						        nonceArr[0] = crew.getResult();
						        System.out.println("You have won the round with nonce " + DatatypeConverter.printBase64Binary(Utils.createBytes(nonceArr)) +
						                " pBlock " + DatatypeConverter.printBase64Binary(Utils.createBytes(pBlock)) + " sBlock " +
						                DatatypeConverter.printBase64Binary(Utils.createBytes(sBlock)));
						    }
						    
						    stopMining();

						}
					    
						else if(cmd[0].equals("FUNC")) {
							currentFunc = cmd[1];
						}
						
						else if(cmd[0].equals("DOWN")) {
						    stopMining();
						    System.out.println("Got DOWN command from server, mining stopped.");
						}
						
						else if(cmd[0].equals("SESSION"))
						    System.out.println("! Your session key is " + cmd[1]);
					    
						else if(cmd[0].equals("REJECTED")) {
						    if(GameServer.state.equals("Running")) {
						    	Thread.sleep(64);
						    	System.err.println("WARNING: get REJECTED signal, rebooting miners.");
						    	stopMining();
						    	startMining(threads);
						    }
						}

					}
				} catch(ArrayIndexOutOfBoundsException eArr) {
					System.out.println("Warning: malformed message from server");
					continue;
				}
			}
		} catch(SocketTimeoutException eTimeout) {
			System.err.println(addr + ":" + port + " - connection timed out.");
		} catch(Exception e) {
			if(e.getMessage().startsWith("Okay")) {
				System.out.println("Connection closed.");
				System.exit(0);
			} else {
			    System.err.println("Fatal exception >_<");
				System.err.println(e);
				System.exit(-1);
			}
		}

	}

}

class ConsoleListener extends Thread {
	
	BufferedReader in;
	
	public ConsoleListener() {
		in = new BufferedReader(new InputStreamReader(System.in));
		this.start();
	}
	
	@Override public void run() {
		try {
			while(true) {
				String line = in.readLine();
				if(line.startsWith(Utils.CMD_TRAIL))
				    GameClient.sendln(line);
				    
				else if(line.startsWith("-")) {
				    try {
				        String[] splitted = line.substring(1).split("-");
				        for(String cmdRaw: splitted) {
    				        String[] cmd = cmdRaw.split(" +");
    				        if(cmd[0].equals("mine"))
    				            GameClient.toggleMining();
    				            
    				        else if(cmd[0].equals("reg")) {
    				            if(GameClient.nickname.length() == 0)
    				                System.err.println("^Nickname is not set!");
    				            else
    				                GameClient.sendln(Utils.formatSimpleMessage("REGISTER", GameClient.nickname));
    				        }
    				        
    				        else if(cmd[0].equals("rdy"))
    				            GameClient.sendln(Utils.formatSimpleMessage("READY", null));
    				            
    				        else if(cmd[0].equals("quit"))
    				            GameClient.sendln(Utils.formatSimpleMessage("DROP", null));
    				            
    				        else if(cmd[0].equals("back")) {
    				            System.out.println("^Getting back into session in automated way.");
    				            
    				            String[] cmd1 = { "SESSION", cmd[1] };
    				            String[] cmd2 = { "?", "Rules" };
    				            String[] cmd3 = { "?", "PBlock" };
    				            String[] cmd4 = { "?", "SBlock" };
    				            
    				            ArrayList<String[]> lst = new ArrayList<String[]>();
    				            lst.add(cmd1);
    				            lst.add(cmd2);
    				            lst.add(cmd3);
    				            lst.add(cmd4);
    				            
    				            GameClient.sendln(Utils.createMessage(lst));
    				            
    				            System.out.println("^Waiting for responses.");
    				            try {
    				                Thread.sleep(500);
    				            } catch(InterruptedException eInt) {}
    				        }
    				        
    				        else if(cmd[0].equals("auto")) {
    				            GameClient.autoReady = !GameClient.autoReady;
    				            System.out.println("^Toggled autoReady.");
    				        }
    				            
    				        else System.err.println("^what?");
				        }
				    } catch(ArrayIndexOutOfBoundsException | NullPointerException e1) {
				        System.err.println("^Bad command format.");
				    }
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}