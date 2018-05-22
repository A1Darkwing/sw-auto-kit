/**
 * 
 */
package Bot;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * @author ttran209
 *
 */
public class BattleRiftDungeonAction extends BaseAction implements Action {

    private static int current_line_rune_number = 0;
    public static final int MINIMUM_ENERGY = 8;

    // Collection point to click when we want to do Sell Rune And Continue
    private final static List<Point> doSellRuneAndContinue = Arrays.asList(
            new Point(1700, 180),
            new Point(1700, 180),
            new Point(900, 800), // Sell button
            new Point(880, 600), // Confirm button
            new Point(1250, 280), // Close popup
            new Point(870, 530), new Point(1700, 700)
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
            new Point(1350,650),//No reborn
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


    public BattleRiftDungeonAction() throws AWTException {
        super();
        robot = new Robot();
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
    public int shouldWeDo() throws IOException {
        int res = 1;
        FileInputStream in = new FileInputStream(resultFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String strLine = null, tmp;
        int count = 0;
        boolean isNewRune = false;
        while ((tmp = br.readLine()) != null) {
            count = count + 1;
            if (count > current_line_rune_number) {
                current_line_rune_number = current_line_rune_number + 1;
                strLine = tmp;
                isNewRune = true;
            }
        }

        String lastLine = strLine;
        if (lastLine != null && isNewRune) {
            String[] runeList = lastLine.split(",");
            System.out.println("New Item Found:" + runeList[7]);
            if (!runeList[7].isEmpty()) {
                if ("Grindstone".equals(runeList[7]) || "Enchanted Gem".equals(runeList[7])) {
                    // String runeStart = runeList[8].replace("*", "");
                    // Integer grade = Integer.parseInt(runeStart);
                    // String rate = String.valueOf(runeList[13]);
                    System.out.println("Found new Grindstone: " + lastLine);
                    String mainStat = runeList[14];
                    if ((mainStat.contains("DEF") || mainStat.contains("ATK")) && mainStat.contains("+")) {
                        res = 0;
                    }
                } else if ("Rune".equals(runeList[7])) {
                    res = 0;
                	if (!runeList[8].isEmpty()) {
                        String runeStart = runeList[8].replace("*", "");
                        Integer grade = Integer.parseInt(runeStart);
                        String rate = String.valueOf(runeList[13]);
                        System.out.println("Found new Rune: " + lastLine);
                        boolean isLegendary = false;
                        if ("Legendary".equals(rate)) {
                            isLegendary = true;
                        }
                        if (grade != null) {
                            if (grade >= 5) {
                                double efficiency = Double.parseDouble(runeList[11]);
                                int runeSlot = Integer.parseInt(runeList[12]);
                                String mainStat = runeList[14];
                                if (runeSlot == 4 || runeSlot == 6) {
                                    if (mainStat.contains("%") && grade == 6 && efficiency > 35) {
                                        res = 1;
                                    }
                                    if (mainStat.contains("%") && grade == 5 && isLegendary && efficiency > 35) {
                                        res = 1;
                                    }
                                } else if (runeSlot == 2 && isLegendary) {
                                    res = 1;
                                } else {
                                    if (isLegendary || efficiency > 35) {
                                        res = 1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        in.close();
        return res;
    }

    @Override
    public int getMinimumEnergyToContinue() {
        return MINIMUM_ENERGY;
    }

}
