package Bot;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class SWAssistant {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;

    private static final String DUNG = "BattleDungeonResult";
    private static final String DUNG_HERO = "BattleEventInstanceResult";
    private static final String SCENARIO = "BattleScenarioResult";
    private static final String RAID = "BattleRiftOfWorldsRaidResult";
    private static final String RAID_DUNG = "BattleRiftDungeonResult";
    private static int screen_width_1 = 0;

    private static Action action;
    private static String mapType;

    /**
     * Creates a WatchService and registers the given directory
     */
    SWAssistant(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();

        walkAndRegisterDirectories(dir);
    }

    /**
     * Register the given directory with the WatchService; This function will be
     * called by FileVisitor
     */
    private void registerDirectory(Path dir) throws IOException {
        // WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
        // ENTRY_MODIFY);
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void walkAndRegisterDirectories(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Process all events for keys queued to the watcher
     * 
     * @throws InterruptedException
     * @throws IOException
     * @throws AWTException
     */
    void processEvents(String mapType) throws InterruptedException, IOException, AWTException {
        action = getRobot(mapType);
        if (action == null)
            return;
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                @SuppressWarnings("rawtypes")
                WatchEvent.Kind kind = event.kind();

                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked")
                Path name = ((WatchEvent<Path>) event).context();
                Path child = dir.resolve(name);

                // if directory is created, and watching recursively, then register it and its
                // sub-directories
                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child)) {
                            walkAndRegisterDirectories(child);
                        }
                    } catch (IOException x) {
                        // do something useful
                    }
                }

                // Listen the rune file changed
                if (kind == ENTRY_MODIFY) {
                    // Looking in full log first
                    if (child.toString().endsWith("txt")) {
                        //Stop Auto Click
                        //Do Auto Click
                        Robot r = new Robot();
                        r.keyPress(KeyEvent.VK_F8);
                        r.keyRelease(KeyEvent.VK_F8);
                        // Sleep 3 second to complete writing
                        Thread.sleep(3000);
                        // Reading csv file
                        int current_energy = readLogFile(child.toString());
                        if (current_energy < 0) {
                            System.out.println("Error when reading current energy. Restart map");
                            action.doRestartMap();
                        } else if (current_energy < action.getMinimumEnergyToContinue()) {
                            // Ran out of energy
                            System.out.println("Current Energy:" + current_energy);
                            System.out.println("Buy Energy...");
                            action.doBuyEnergy();
                        } else {
                            System.out.println("Current Energy:" + current_energy);
                            int nextAction = action.shouldWeDo();
                            if (nextAction > 0) {
                                System.out.println("Keep the rune");
                                action.doKeepRuneAndContinue();
                            } else {
                                System.out.println("Sale the rune");
                                Thread.sleep(3000);
                                action.doSellRuneAndContinue();
                            }
                        }
                        Thread.sleep(3000);
                        //Do Auto Click
                        r.mouseMove(screen_width_1 + 1350,650);
                        r.keyPress(KeyEvent.VK_F8);
                        r.keyRelease(KeyEvent.VK_F8);
                    }
                
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * @param file
     * @return current energy, -1 if we have any error
     * @throws IOException
     * @throws AWTException
     * @throws InterruptedException
     */
    private static int readLogFile(String file) throws IOException, AWTException, InterruptedException {
        int res = -1;
        FileInputStream in = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String strLine = "", tmp;
        while ((tmp = br.readLine()) != null) {
            if (tmp.contains(mapType)) {
                // set last response
                strLine = tmp;
            }
        }

        String lastLine = strLine;
        try {
            JSONObject responseJSON = new JSONObject(lastLine);
            JSONObject currentWizard = (JSONObject) responseJSON.get("wizard_info");
            int currentEnergy = (Integer) currentWizard.get("wizard_energy");
            if(DUNG.equals(mapType) || SCENARIO.equals(mapType) || RAID.equals(mapType)) {
                int win_lose = (Integer) responseJSON.get("win_lose");
                if (win_lose == 2 && currentEnergy < action.getMinimumEnergyToContinue()) {
                    System.out.println("Dead....Do Reborn...");
                    action.doBuyEnergy();
                } else if (win_lose == 2) {
                    System.out.println("Dead....Do Reborn...");
                    action.doReborn();
                }
            }
            res = currentEnergy;
        } catch (JSONException e) {
            // eat exception
            System.out.println("ERROR............." + lastLine);
        }
        in.close();
        return res;
    }

    private static Action getRobot(String mapType) throws AWTException {
        if (DUNG.equals(mapType)) {
            return new BattleDungeonAction();
        } else if (RAID.equals(mapType)) {
            return new BattleRiftOfWorldsRaidAction();
        } else if (SCENARIO.equals(mapType)) {
            return new BattleScenarioAction();
        } else if (RAID_DUNG.equals(mapType)) {
            return new BattleRiftDungeonAction();
        } else if (DUNG_HERO.equals(mapType)) {
            return new BattleDungeonHeroAction();
        }
        return null;
    }

    public static void main(String[] args) throws IOException, InterruptedException, AWTException {
        Path dir = Paths.get("C:/Users/ttran209/Desktop/Summoners War Exporter Files/");
        System.out.println("Please enter map Type");
        System.out.println("1. Battle Dungeon");
        System.out.println("2. Battle Rift Of Worlds Raid");
        System.out.println("3. Battle Battle Scenario");
        System.out.println("4. Battle Rift Dungeon Action");
        System.out.println("5. Battle Rift Hero Action");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            int map = Integer.parseInt(br.readLine());
            switch (map) {
            case 1:
                System.out.println("Start Battle Dungeon Action....");
                mapType = DUNG;
                break;
            case 2:
                System.out.println("Start Battle Rift Of Worlds Raid Action....");
                mapType = RAID;
                break;
            case 3:
                System.out.println("Start Battle Scenario Action....");
                mapType = SCENARIO;
                break;
            case 4:
                System.out.println("Start Battle Rift Dungeon Action....");
                mapType = RAID_DUNG;
                break;
            case 5:
                System.out.println("Start Battle Dungeon Hero Action....");
                mapType = DUNG_HERO;
                break;
            default:
                System.out.println("Invalid Action....");
                break;
            }
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid map Type!");
        }

        Robot r = new Robot();
        r.mouseMove(screen_width_1 + 1350,650);
        r.keyPress(KeyEvent.VK_F8);
        r.keyRelease(KeyEvent.VK_F8);
        new SWAssistant(dir).processEvents(mapType);
    }
}
