package Bot;

import java.io.IOException;

public interface Action {

    void doSellRuneAndContinue() throws InterruptedException;
    
    void doKeepRuneAndContinue() throws InterruptedException;
    
    void doBuyEnergy() throws InterruptedException;
    
    void doReborn() throws InterruptedException;
    
    void doRestartMap() throws InterruptedException;
    
    int getMinimumEnergyToContinue();

	int shouldWeDo() throws IOException;
}
