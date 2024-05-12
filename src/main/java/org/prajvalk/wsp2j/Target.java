package org.prajvalk.wsp2j;

import org.prajvalk.wsp2j.plugins.ReportX;

import java.io.Serializable;

public class Target implements Serializable {
    public static ReportX reporter;

    private final String CLASS;
    private final String ID;
    private final String URL;
    private String hash;

    public Target(String CLASS, String ID, String URL) {
        this.CLASS = CLASS;
        this.ID = ID;
        this.URL = URL;
    }

    public void refresh() {
        String data = Utility.getData(URL);
        reporter.postState.put(ID, data);
        hash = Utility.getHash(data);
    }

    public String getID() {
        return ID;
    }

    public String getHash() {
        return hash;
    }

    public String getCLASS() {
        return CLASS;
    }
}
