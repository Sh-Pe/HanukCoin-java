package il.ac.tau.cs.hanukcoin;

import java.util.ArrayList;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
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
        System.err.println(HanukCoinUtils.checkBlockChainValidation(blocks));
    }
}
