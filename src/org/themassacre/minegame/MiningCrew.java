package org.themassacre.minegame;
import java.util.ArrayList;
import java.util.Random;

import org.themassacre.crypto.HashFunction;
import org.themassacre.generic.Utils;

public class MiningCrew {
	public double MHash = 0.0f;
	public long hashSpeed = 0;
	private long randomOne = 0;
	
	public int threadTick = 0;
	private int diff = 1;
	private int threads = 1;
	
	public int getDiff() {
		return diff;
	}
	
	public int getThreads() {
		return threads;
	}
	
	private boolean done = false;
	private long nonce = 0;
	
	public void setResult(long nonce) {
		this.nonce = nonce;
		this.done = true;
	}
	
	public boolean isDone() {
		return done;
	}
	
	boolean isStopped = false;
	
	public long getResult() {
		while(!isStopped) {
			try {
				Thread.sleep(32);
			} catch(InterruptedException e) {
			    return 0;
			}
			if(done)
				return nonce;
		}
		
		return 0;
	}
	
	private ArrayList<Miner> miners = null;
	public HashFunction hashFunction;
	
	public MiningCrew(int diff, int threads, long[] pBlock, long[] sBlock, HashFunction func) {
		this.diff = diff;
		this.threads = threads;
		this.hashFunction = func;
		
		Random r = new Random(System.currentTimeMillis());
		randomOne = Utils.nextLong(r, Long.MAX_VALUE);
		
		System.out.println("Using " + hashFunction + " algorythm");
		
		miners = new ArrayList<Miner>();
		for(int i = 0; i < threads; i++)
			miners.add(new Miner(i, this, pBlock, sBlock));
	}
	
	public void stop() {
	    isStopped = true;
	    try {
	        for(Miner m: miners)
	            m.setStopped();
	        miners = null;
	    } catch(NullPointerException e) {
	    }
	}
	
	public long getRandomOne() {
	    return randomOne;
	}

}
