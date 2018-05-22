/**
 * 
 */
package Bot;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author ttran209
 *
 */
public class BattleDungeonAction extends BaseAction implements Action {

    public static final int MINIMUM_ENERGY = 8;
    
    // Collection point to click when we want to do Sell Rune And Continue
    private final static List<Point> doSellRuneAndContinue = Arrays.asList(
        new Point(1700, 180),
        new Point(1700, 180),
        new Point(900, 800), // Sell button
        new Point(880, 600), // Confirm button
        new Point(1255, 280),
        new Point(870, 530),
        new Point(1700, 700)
   );

    private final static List<Point> doKeepRuneAndContinue = Arrays.asList(
        new Point(1700, 180),
        new Point(1700, 180),
        new Point(1250, 800), // Keep button
        new Point(880, 600), // Confirm button
        new Point(1255, 280),
        new Point(870, 530),
        new Point(1700, 700)
    );

    private final static List<Point> doBuyEnergy = Arrays.asList(
        new Point(1700, 180),
        new Point(1700, 180),
        new Point(1350,650),
        new Point(900, 800), // Sell Item button
        new Point(880, 600), // Confirm button
        new Point(1255, 280),
        new Point(870, 530),
        new Point(870, 600), // Buy Energy Now
        new Point(700, 400), // Buy Energy Item
        new Point(900, 620), // Confirm Buy Energy Item
        new Point(1030, 610), // ok Button
        new Point(1540, 170), // ok Button
        new Point(870, 530),
        new Point(1700, 700)
    );

    private final static List<Point> doReborn = Arrays.asList(
        new Point(1700, 180),
        new Point(1700, 180),
        // new Point(1350,650),//No reborn button
        new Point(1700, 180), // Confirm button
        new Point(1255, 280),
        new Point(870, 530),
        new Point(1700, 700)
    );

    private final static List<Point> doRestartMap = Arrays.asList(new Point(100, 200), new Point(100, 200));

    public BattleDungeonAction() throws AWTException {
        super();
        super.robot = new Robot();
    }

    @Override
    public void doSellRuneAndContinue() throws InterruptedException {
        System.out.println("Do Sell Rune And Continue");
        doClick(doSellRuneAndContinue);
    }

    @Override
    public void doKeepRuneAndContinue() throws InterruptedException {
        System.out.println("Do Keep Rune And Continue");
        doClick(doKeepRuneAndContinue);
    }

    @Override
    public void doBuyEnergy() throws InterruptedException {
        System.out.println("Do Buy Energy");
        doClick(doBuyEnergy);
    }

    @Override
    public void doReborn() throws InterruptedException {
        System.out.println("Do Reborn");
        doClick(doReborn);
    }

    @Override
    public void doRestartMap() throws InterruptedException {
        System.out.println("Do Restart Map");
        doClick(doRestartMap);
    }

    @Override
    public int getMinimumEnergyToContinue() {
        return MINIMUM_ENERGY;
    }

    @Override
    public int shouldWeDo() throws IOException {
        return super.shouldWeDo();
    }
}
