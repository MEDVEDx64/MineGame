package org.themassacre.minegame;

import org.themassacre.generic.Utils;

public class Miner extends Thread {
	public static double getMHash(long speed) {
		return getMHashCooked(getMHashRaw(speed));
	}
	
	public static double getMHashRaw(long speed) {
		return ((double)speed/1000000);
	}
	
	public static double getMHashCooked(double raw) {
		 return Math.rint(100.0 * raw) / 100.0;
	}

	MiningCrew crewObj;
	int id = 0;
	long[] data;
	int diff;
	
	public static long[] initializeBlocks(long[] primaryBlock, long[] secondaryBlock) {
		long[] blocks = new long[primaryBlock.length + secondaryBlock.length + 1];
		System.arraycopy(primaryBlock, 0, blocks, 1, primaryBlock.length);
		System.arraycopy(secondaryBlock, 0, blocks, primaryBlock.length + 1, secondaryBlock.length);
		return blocks;
	}
	
	public static byte[] initializeBlocks(byte[] primaryBlock, byte[] secondaryBlock) {
		byte[] blocks = new byte[primaryBlock.length + secondaryBlock.length + 8];
		System.arraycopy(primaryBlock, 0, blocks, 8, primaryBlock.length);
		System.arraycopy(secondaryBlock, 0, blocks, primaryBlock.length + 8, secondaryBlock.length);
		return blocks;
	}
	
	public Miner(int minerID, MiningCrew crewObj, long[] primaryBlock, long[] secondaryBlock) {
		this.id = minerID;
		this.crewObj = crewObj;
		data = initializeBlocks(primaryBlock, secondaryBlock);
		
		this.start();
	}
	
	void mesgPrint(String mesg) {
		System.out.println(id + ": " + mesg);
	}
	
	private boolean isStopped = false;
	public void setStopped() {
	    isStopped = true;
	}
	
	public void run() {
		data[0] = (Long.MAX_VALUE/crewObj.getThreads()) * id - crewObj.getRandomOne();
		
		long speed = 0; int time;
		int time_prev = 0;
		while(!isStopped) {
			if(crewObj.isDone())
				break;
			
			if(Utils.isHashAccepted(crewObj.hashFunction.computeHash(Utils.createBytes(data)), crewObj.getDiff())) {
				mesgPrint("Accepted!");
				crewObj.setResult(data[0]);
				break;
			}
			
			data[0]++;
			speed++;
			
			time = (int)(System.currentTimeMillis()/1000);
			if(time > time_prev) {
				time_prev = time;
				mesgPrint("Current speed: " + (speed > 9999? (getMHash(speed) + " MHash/s"): (speed + " Hash/s")));
				crewObj.MHash += getMHashRaw(speed);
				crewObj.hashSpeed += speed;
				crewObj.threadTick++;
				if(crewObj.threadTick == crewObj.getThreads()) {
					System.out.println("> " + (crewObj.hashSpeed > 9999? (getMHashCooked(crewObj.MHash) + " MHash/s, ") : (crewObj.hashSpeed + " Hash/s, "))
					        + crewObj.getThreads() + (crewObj.getThreads() == 1? " thread": " threads"));
					crewObj.MHash = 0.0f;
					crewObj.hashSpeed = 0;
					crewObj.threadTick = 0;
				}
				speed = 0;
			}
			
		}
	}
}
