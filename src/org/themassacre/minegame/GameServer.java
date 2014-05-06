package org.themassacre.minegame;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.*;

import javax.xml.bind.DatatypeConverter;

import org.themassacre.crypto.HashFunction;
import org.themassacre.crypto.SHA256HashFunction;
import org.themassacre.crypto.Tworojok64HashFunction;
import org.themassacre.generic.Utils;

public class GameServer extends Thread {

	public static List<User> users = new ArrayList<User>();
	public static String diff = "4/4@4";
	public static short port = 9750;
	final static int DEFAULT_TICKS = 60;
	public static HashFunction func = new SHA256HashFunction();
	
	public static String state = "Waiting";
	static int stateTicks = 0;
	public static String roundID;
	
	public void run() {
		int[] rules = parseRules(diff);
		while(true) {
			try {
				Thread.sleep(1000);
				if(state.equals("Open") || state.equals("Closed")) {
					if(stateTicks >= 0) stateTicks--;
					if(stateTicks == 10) {
						boolean isSomeoneInRound = false;
						for(User u: users)
							if(u.nickname.length() != 0 && u.ready) {
								isSomeoneInRound = true;
								break;
							}
						
						if(isSomeoneInRound) {
							for(User u: users)
								if(u.nickname.length() != 0) {
									if(u.ready) {
										String[][] cmd = {
												{"NEAR"},
												{"RULES", diff},
												{"FUNC", func.toString()}
										};
										
										ArrayList<String[]> msg = new ArrayList<String[]>();
										for(String[] c: cmd)
											msg.add(c);
										
										u.sendln(Utils.createMessage(msg));
									}
									else {
										u.sendln(Utils.formatSimpleMessage("DROP", "You are NOT ready!"));
										u.nickname = "";
									}
								}
							state = "Closed";
						} else
							stateTicks = DEFAULT_TICKS;
						
					}
					
					if(stateTicks == 0) {
					    System.out.println("Sending START signal");
						String pb = DatatypeConverter.printBase64Binary(Utils.createBytes(generateBlock(rules[0])));
						Thread.sleep(3);
						
						for(User u: users) {
							if(u.nickname.length() == 0) continue;
							u.pBlock = pb;
							u.sBlock = DatatypeConverter.printBase64Binary(Utils.createBytes(generateBlock(rules[1])));
							
							List<String[]> msg = new ArrayList<String[]>();
							String[] cmd0 = { "ID", GameServer.roundID };
							String[] cmd1 = { "PBLOCK", u.pBlock };
							String[] cmd2 = { "SBLOCK", u.sBlock };
							String[] cmd3 = { "START" };
							msg.add(cmd0);
							msg.add(cmd1);
							msg.add(cmd2);
							msg.add(cmd3);
							
							u.sendln(Utils.createMessage(msg));
							Thread.sleep(3);
						}
						
						state = "Running";
					}
				}
				
				else if(state.equals("Running"))
					if(getRegisteredUsersCount() < 1)
						startNewRound();

			} catch(InterruptedException | IOException e) {
			}
		}
	}
	
	public static void main(String[] args) {
		for(String s: args) {
			if(s.startsWith("-d")) diff = s.substring(2);
			if(s.startsWith("-tw64")) func = new Tworojok64HashFunction();
			try {
				if(s.startsWith("-p")) port = Short.parseShort(s.substring(2));
			} catch(NumberFormatException e) {
			}
		}
		
		System.err.println("Starting MineGame server");
		state = "Open"; stateTicks = DEFAULT_TICKS;
		roundID = generateID();
		new GameServer().start();
		
		try(ServerSocket ss = new ServerSocket(port)) {
			while(true) {
				try {
				    Socket usrSock = ss.accept();
					users.add(new User(usrSock));
					System.err.println(usrSock.getInetAddress() + " - connetcion established");
				} catch(Exception e) {
					System.err.println("Can't accept user socket");
					e.printStackTrace();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void startNewRound() throws IOException {
	    System.out.println("Initiating a new round");
		roundID = generateID();
		for(User u: users)
			u.ready = false;
		stateTicks = DEFAULT_TICKS;
		state = "Open";
		broadcast(Utils.formatSimpleMessage("ROUND", null));
	}
	
	public static boolean isUserExist(String nick) {
		for(User u: users)
			if(u.nickname.equals(nick))
				return true;
		return false;
	}
	
	public static int getRegisteredUsersCount() {
		int result = 0;
		for(User u: users)
			if(u.nickname.length() != 0)
				result++;
		return result;
	}
	
	public static void broadcast(String s) throws IOException {
		for(User u: users)
			if(u.nickname.length() != 0)
				u.sendln(s);
	}
	
	public static void broadcastReady(String s) throws IOException {
		for(User u: users)
			if(u.nickname.length() != 0 && u.ready)
				u.sendln(s);
	}
	
	public static long[] generateBlock(int size) {
		Random rnd = new Random(System.currentTimeMillis());
		long[] block = new long[size];
		for(int i = 0; i < block.length; i++)
			block[i] = rnd.nextLong();
		return block;
	}
	
	public static int[] parseRules(String rules) {
		int[] dest = new int[4];
		
		try {
			String[] spl1 = rules.split("/");
			String[] spl2 = spl1[1].split("@");
			dest[0] = Integer.parseInt(spl1[0]);
			dest[1] = Integer.parseInt(spl2[0]);
			dest[2] = Integer.parseInt(spl2[1]);
		} catch(Exception e) {
			dest[3] = 1;
		} finally {
			if(dest[0] == 0) dest[0] = 4;
			if(dest[1] == 0) dest[1] = 4;
			if(dest[2] == 0) dest[2] = 1;
		}
		
		return dest;
	}
	
	public static String generateID() {
		Random r = new Random(System.currentTimeMillis());
		long[] l = { r.nextLong() };
		return DatatypeConverter.printBase64Binary(Utils.createBytes(l));
	}

}

class User extends Thread {
	Socket socket;
	OutputStream out;
	BufferedReader in;
	
	public String session = "";
	public String sBlock = "";
	public String pBlock = "";
	public String nickname = "";
	public boolean ready = false;
	boolean spellCheck = false;
	
	int nonceAttempts = 3;
	
	@Override public void run() {
		try {
			while(true) {
				String recieved = in.readLine();
				if(recieved == null) break;
				if(recieved.length() == 0) break;
				List<String[]> message = Utils.parseMessage(recieved);
				if(message.size() == 0) {
					if(spellCheck)
						sendln(Utils.formatSimpleMessage("CRIT", "Bad message format"));
					else
						sendln(Utils.formatSimpleMessage("CRIT", "You didn't say the magic word!"));
					throw new Exception("Okay.");
				}
				
				for(String[] s: message) {
					if(spellCheck) {
						
						try {
							if(s[0].equals("?")) {
								if(s[1].equals("Kernel"))
									sendln(Utils.formatSimpleMessage("KERNEL", "WALL1.1.3"));
								else if(s[1].equals("Function"))
									sendln(Utils.formatSimpleMessage("FUNC", GameServer.func.toString()));
								else if(s[1].equals("State"))
									sendln(Utils.formatSimpleMessage("STATE", GameServer.state));
								else if(s[1].equals("RoundID"))
									sendln(Utils.formatSimpleMessage("ID", GameServer.roundID));
								else if(s[1].equals("Rules")) {
									if(GameServer.diff.length() == 0)
										sendln(Utils.formatSimpleMessage("ERR", "Rules is not set"));
									else
										sendln(Utils.formatSimpleMessage("RULES", GameServer.diff));
								}
								
								else if(s[1].equals("Features")) {
									List<String[]> msg = new ArrayList<String[]>();
									String[] cmd = {"FEATURES", "Session", "Workers"};
									msg.add(cmd);
									sendln(Utils.createMessage(msg));
								}
								
								else if(s[1].equals("PBlock")) {
									if(pBlock.length() == 0)
										sendln(Utils.formatSimpleMessage("ERR", "Block is not available"));
									else
										sendln(Utils.formatSimpleMessage("PBLOCK", pBlock));
								}
								
								else if(s[1].equals("SBlock")) {
									if(sBlock.length() == 0)
										sendln(Utils.formatSimpleMessage("ERR", "Block is not available"));
									else
										sendln(Utils.formatSimpleMessage("SBLOCK", sBlock));
								}
								
								else
									sendln(Utils.formatSimpleMessage("ERR", "Bad request"));
							}
							
							else if(s[0].equals("DROP")) {
								if(nickname.length() == 0)
									sendln(Utils.formatSimpleMessage("ERR", "Not registered"));
								else {
									nickname = "";
									sendln(Utils.formatSimpleMessage("OK", null));
								}
							}
							
							else if(s[0].equals("REGISTER")) {
								if(GameServer.state.equals("Open")) {
									String[] n = s[1].split("\\x2e");
									String nick = n[0];
									nick += (n.length > 1? "." + n[1]: ".x");
									
									if(GameServer.isUserExist(nick))
										sendln(Utils.formatSimpleMessage("ERR", "Nickname is already in use"));
									else {
										nickname = nick;
										sendln(Utils.formatSimpleMessage("OK", null));
										System.out.println(socket.getInetAddress() + " has registered as " + nick);
									}
								} else
									sendln(Utils.formatSimpleMessage("ERR", "Registration closed"));
							}
							
							else if(s[0].equals("READY")) {
								if(GameServer.state.equals("Open") && nickname.length() != 0) {
									ready = !ready;
									sendln(Utils.formatSimpleMessage("OK", null));
								} else
									sendln(Utils.formatSimpleMessage("ERR", "No way!"));
							}
							
							else if(s[0].equals("NONCE")) {
								if(GameServer.state.equals("Running") && nickname.length() != 0) {
									if(nonceAttempts < 1) {
										sendln(Utils.formatSimpleMessage("DROP", "Disqualified (too many mistakes)"));
										nickname = "";
									} else {
										int[] rules = GameServer.parseRules(GameServer.diff);
										byte[] userBlocks = Miner.initializeBlocks(DatatypeConverter.parseBase64Binary(this.pBlock),
												DatatypeConverter.parseBase64Binary(this.sBlock));
										System.arraycopy(DatatypeConverter.parseBase64Binary(s[1]), 0, userBlocks, 0, 8);
										if(Utils.isHashAccepted(GameServer.func.computeHash(userBlocks), rules[2])) {
											sendln(Utils.formatSimpleMessage("ACCEPTED", null));
											GameServer.broadcast(Utils.formatSimpleMessage("WINNER", nickname.split("\\x2e")[0]));
											System.out.println("*** Winner: " + nickname.split("\\x2e")[0]);
											GameServer.startNewRound();
										}
										else {
											sendln(Utils.formatSimpleMessage("REJECTED", null));
											nonceAttempts--;
										}
									}
								} else
									sendln(Utils.formatSimpleMessage("ERR", "No way!"));
							}
							
							else if(s[0].equals("SESSION")) {
								User target = null;
								for(User u: GameServer.users) {
									if(u.session.equals(s[1])) {
										target = u;
										break;
									}
								}
								
								if(target == null)
									sendln(Utils.formatSimpleMessage("ERR", "Invalid session key"));
								else {
									this.session = new String(target.session);
									this.nickname = new String(target.nickname);
									this.pBlock = new String(target.pBlock);
									this.sBlock = new String(target.sBlock);
									
									this.ready = target.ready;
									this.nonceAttempts = target.nonceAttempts;
									
									try {
										target.sendln(Utils.formatSimpleMessage("DROP", "Session hooked"));
									} catch(SocketException e) {
									}
									
									target.nickname = "";
									sendln(Utils.formatSimpleMessage("OK", null));
								}
							}
							
							else
								sendln(Utils.formatSimpleMessage("ERR", "Unknown command"));
						} catch(ArrayIndexOutOfBoundsException e) {
							sendln(Utils.formatSimpleMessage("ERR", "Internal server error."));
							e.printStackTrace();
						}
						
					} else {
						if(s[0].equals("CBA AFR KTL")) {
							spellCheck = true;
							String[] cmd1 = { "OK" };
							String[] cmd2 = { "SESSION", session };
							List<String[]> msg = new ArrayList<String[]>();
							msg.add(cmd1); msg.add(cmd2);
							sendln(Utils.createMessage(msg));
						}
						else {
							sendln(Utils.formatSimpleMessage("CRIT", "You didn't say the magic word!"));
							throw new Exception("Okay.");
						}
					}
				}
			}
		} catch(Exception e) {
			if(!e.getMessage().startsWith("Okay")) {
				try {
					sendln(Utils.formatSimpleMessage("CRIT", "Internal server error."));
				} catch(IOException eIO) {
				}
				System.err.println(socket.getInetAddress() + " caused an exception: " + e);
			}
		} finally {
			try {
				socket.close();
			} catch(IOException e) {
			}
			
			System.err.println(socket.getInetAddress() + " has disconnected");
		}
	}
	
	public void sendln(String s) throws IOException {
		out.write((s + "\n").getBytes("UTF-8"));
	}
	
	public User(Socket s) throws IOException {
		socket = s;
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = s.getOutputStream();
		
		session = GameServer.generateID();
		this.start();
	}
}