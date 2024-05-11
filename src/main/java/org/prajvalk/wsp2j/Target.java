package org.prajvalk.wsp2j;

import java.io.Serializable;

public class Target implements Serializable {
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
        hash = Utility.getHash(Utility.getData(URL));
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
