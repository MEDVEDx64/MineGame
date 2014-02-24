package org.themassacre.minegame;

import org.themassacre.crypto.HashFunction;
import org.themassacre.crypto.SHA256HashFunction;
import org.themassacre.crypto.Tworojok64HashFunction;

public class Benchmark {

	public static void main(String[] args) {
	    
		if(args.length < 2) {
			System.err.println("usage: Benchmark numThreads pBlockLength/sBlockLength@diff");
			System.exit(-1);
		}
		
		HashFunction func = args.length < 3? new SHA256HashFunction(): new Tworojok64HashFunction();
		
		int[] rules = GameServer.parseRules(args[1]);
		if(rules[3] > 0) {
			System.err.println("Bad rules format.");
			System.exit(-1);
		}
		
		MiningCrew mc = new MiningCrew(rules[2], Integer.parseInt(args[0]),
				GameServer.generateBlock(rules[0]), GameServer.generateBlock(rules[1]), func);
		System.err.println("Resulting nonce is " + mc.getResult());

	}

}
