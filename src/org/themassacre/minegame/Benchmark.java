package org.themassacre.minegame;

public class Benchmark {

	public static void main(String[] args) {
	    
		if(args.length < 2) {
			System.err.println("usage: Benchmark numThreads pBlockLength/sBlockLength@diff");
			System.exit(-1);
		}
		
		Utils.loadNativeKernel();
		
		int[] rules = GameServer.parseRules(args[1]);
		if(rules[3] > 0) {
			System.err.println("Bad rules format.");
			System.exit(-1);
		}
		
		MiningCrew mc = new MiningCrew(rules[2], Integer.parseInt(args[0]),
				GameServer.generateBlock(rules[0]), GameServer.generateBlock(rules[1]));
		System.err.println("Resulting nonce is " + mc.getResult());

	}

}
