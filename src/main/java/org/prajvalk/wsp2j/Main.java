package org.prajvalk.wsp2j;

import java.io.File;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Vector<String> monitoring = new Vector<>(1,1);
    private static Vector<Target> targets = new Vector<>(1,1);

    private static String timestring;

    private static int hours = 0;
    private static int minutes = 0;
    private static int seconds = 10;


    public static void main(String[] args) {
        System.out.println("Web Scrapping Project for Java (wsp2j)");
        System.out.println("Copyright (C) 2024, Chlorine Pentoxide & Prajval K");
        System.out.println("By using this utility you hereby agree and abide by \n" +
                "The MIT License (full license: https://opensource.org/license/mit)");
        System.out.println();

        initialize();
        timestring = Utility.getSpecificTime();
        initializeTimings();
        System.out.println("wsp2j::core [init]: Initialization complete.");
        refreshAll();

        System.out.println("wsp2j::core [init]: Scheduler running every "+hours+"hrs "+minutes+"min "+seconds+"sec");

        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(Main::refreshAll, 0, 60 * (60 * hours + minutes) + seconds, TimeUnit.SECONDS);
    }

    public static void initialize() {
        File listFile = new File("targets.list");
        File stateFile = new File("targets.obj");

        if(listFile.exists() && stateFile.exists()) {
            monitoring.addElement("wsp2j::core [init]: Found: 1) targets.list and 2) targets.obj");
            monitoring.addElement("wsp2j::core [init]: Initializing update and resume procedure.");
            targets = (Vector<Target>) Utility.getObject("targets.obj");
            updateTargets(false);
        } else if(listFile.exists()) {
            monitoring.addElement("wsp2j::core [init]: Found: 1) targets.list only");
            monitoring.addElement("wsp2j::core [init]: Initializing load and start procedure.");
            updateTargets(true);
        } else if(stateFile.exists()) {
            monitoring.addElement("wsp2j::core [init]: Found: 1) targets.obj only");
            monitoring.addElement("wsp2j::core [init]: Initializing resume procedure.");
            targets = (Vector<Target>) Utility.getObject("targets.obj");
        } else {
            System.out.println("wsp2j::core [init] ERROR : Core operating files : 1) targets.list or 2) targets.obj not found");
            System.exit(90);
        }
    }

    public static void initializeTimings() {
        File timingsFile = new File("wsp2j.frequency");
        String timings = null;
        if (timingsFile.exists()) {
            timings = Utility.readData(timingsFile.getName()).elementAt(0);
        } else {
            System.out.println("wsp2j::core [init]: Timings file wsp2j.frequency not found.");
            System.out.print("Please enter your timing frequency manually (format HH mm SS): ");
            Scanner sc = new Scanner(System.in);
            timings = sc.nextLine();
        }
        String[] timingsArray = timings.split(" ");
        hours = Integer.parseInt(timingsArray[0]);
        minutes = Integer.parseInt(timingsArray[1]);
        seconds = Integer.parseInt(timingsArray[2]);
    }

    public static int search(String st) {
        for(int i = 0; i < targets.size(); i++)
            if(st.equals(targets.elementAt(i).getID()))
                return i;
        return -1;
    }

    public static void updateTargets(boolean skipChecking) {
        Vector<String> list = Utility.readData("targets.list");
        for(String t : list) {
            String[] fields = t.split(",");
            String CLASS = fields[0];
            if(CLASS.equals("CLASSIFICATION")) continue;
            String TARGET_ID = fields[1];
            String URL = fields[2];
            if(!skipChecking)
                if(search(TARGET_ID) != -1)
                    continue;
            Target target = new Target(CLASS, TARGET_ID, URL);
            targets.addElement(target);
        }
    }

    public static void refreshAll() {
        int hits = 0;
        String timedata = Utility.getTime();
        monitoring.addElement("wsp2j::monitoring [core]: Initiating refresh procedure at "+timedata);
        long startTime = System.nanoTime();
        for(Target target : targets) {
            String preHash = target.getHash();
            if(preHash == null) preHash = "";
            target.refresh();
            String postHash = target.getHash();
            if(!preHash.equals(postHash)) {
                monitoring.addElement("wsp2j::monitoring ["+target.getCLASS()+"/"+target.getID()+"] "+timedata+": Change Detected");
                hits++;
            }
        }
        long runtime = System.nanoTime() - startTime;
        double runtime_ms = runtime / 1000000d;
        monitoring.addElement("wsp2j::monitoring [core]: Refresh took "+runtime_ms+" ms");
        monitoring.addElement("wsp2j::monitoring [core]: Found "+hits+" changes in the target list.");
        monitoring.addElement("wsp2j::core [refreshAll]: Saving objects");
        Utility.saveObject("targets.obj", targets);
        Utility.writeData("wsp2j-monitoring-"+timestring+".txt", monitoring);
    }

}
