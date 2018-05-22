package Bot;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

public abstract class BaseAction {
    
    private static int screen_width = 0;
    private final static int delay = 2000;
    private static int current_line_rune_number = 0;
    
    protected static final String resultFile = "C:/Users/ttran209/Desktop/Summoners War Exporter Files/￦iro-56209-runs.csv";
    
    protected Robot robot;

    protected void doClick(List<Point> points) throws InterruptedException {
        Random r = new Random();
        int Low = 10;
        int High = 500;
        int Result = r.nextInt(High-Low) + Low;
        Thread.sleep(3000 + Result);
        for (Point p : points) {
            robot.mouseMove(screen_width + (int) p.getX(), (int) p.getY());
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            Thread.sleep(delay);
        }
        Thread.sleep(3000 + Result);
    }
    
    protected int shouldWeDo() throws IOException {
        int res = 0;
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
                        } else {//Not slot 2 4 6
                            if (isLegendary || efficiency > 35) {
                                res = 1;
                            }
                        }
                    }
                }
            }
        }

        in.close();
        return res;
    }
}
