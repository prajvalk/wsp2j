package org.prajvalk.wsp2j.plugins;

import org.prajvalk.wsp2j.Target;
import org.prajvalk.wsp2j.Utility;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ReportX implements Serializable {

    public Map<String, String> preState;
    public Map<String, String> postState;
    public Map<String, Vector<String>> reports;

    public ReportX() {
        preState = new HashMap<>();
        postState = new HashMap<>();
        reports = new HashMap<>();
    }

    public void report(Target tg, String pre, String post, String timestamp) {
        Vector<String> report = reports.get(tg.getID());
        if(pre == null) {
            report.addElement("wsp2j::reportx ["+tg.getCLASS()+"/"+tg.getID()+"]: Initial read for target loaded.");
        } else {
            report.addElement("wsp2j::reportx ["+tg.getCLASS()+"/"+tg.getID()+"]: Reporting computed difference");
            String[] diff = diffHelper(pre, post, new HashMap<>());
            report.addElement("wsp2j::reportx ["+tg.getCLASS()+"/"+tg.getID()+"]: Starting report for change detected at "+timestamp);
            for(int i = 0; i < diff.length; i++) {
                report.addElement("wsp2j::reportx ["+(i+1)+"]: "+diff[i]);
            }
            report.addElement("wsp2j::reportx ["+tg.getCLASS()+"/"+tg.getID()+"]: Report ended");
        }
        Utility.writeData("wsp2j-reportx-"+tg.getID()+".txt", report);
        postState.put(tg.getID(), pre);
    }

    public void next() {
        preState = postState;
        postState.clear();
    }

    private String[] diffHelper(String a, String b, Map<Long, String[]> lookup) {
        return lookup.computeIfAbsent(((long) a.length()) << 32 | b.length(), k -> {
            if (a.isEmpty() || b.isEmpty()) {
                return new String[]{a, b};
            } else if (a.charAt(0) == b.charAt(0)) {
                return diffHelper(a.substring(1), b.substring(1), lookup);
            } else {
                String[] aa = diffHelper(a.substring(1), b, lookup);
                String[] bb = diffHelper(a, b.substring(1), lookup);
                if (aa[0].length() + aa[1].length() < bb[0].length() + bb[1].length()) {
                    return new String[]{a.charAt(0) + aa[0], aa[1]};
                } else {
                    return new String[]{bb[0], b.charAt(0) + bb[1]};
                }
            }
        });
    }

}
