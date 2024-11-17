package il.ac.tau.cs.hanukcoin;

import junit.framework.TestCase;

import java.util.ArrayList;

public class ValidationTest extends TestCase {

	@org.junit.Test
	public void test_validation() throws Exception {
		ArrayList<Block> blocks = new ArrayList();
		blocks.add(HanukCoinUtils.createBlock0forTestStage());
		for (int i = 0; i < 3; i++) {
			Block previous = blocks.get(i);
			blocks.add(HanukCoinUtils.mineCoinAttempt(
				// alternate between 2 wallets
				(i + 1) % 2,
				previous,
				10000000
			));
		}
		System.err.println(blocks);
		assertTrue(HanukCoinUtils.checkBlockChainValidation(blocks));
	}

}
