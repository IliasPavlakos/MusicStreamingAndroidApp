package com.pavlakosilias.musicstreamingapp;

import java.util.HashMap;
import java.util.Map;

public class PrefMan {

    private static PrefMan instance;

    private final Map<String, Pref> defaults;

    private PrefMan() {
        defaults = new HashMap<String, Pref>() {{
            put("broker", new Pref("192.168.1.2", "192.168.1.5", "192.168.1.17", "localhost"));
            put("chunks", new Pref("C:\\Users\\paula\\Desktop\\c1chunks", "C:\\Users\\pgl\\Desktop\\c1chunks", "."));
            put("port", new Pref("5001", "5002", "5003"));
        }};
    }

    synchronized public static PrefMan getInstance() {
        if (instance == null) {
            instance = new PrefMan();
        }
        return instance;
    }

    public String get(String key) {
        return defaults.get(key).getNext();
    }

    class Pref {
        private String[] values;
        private int idx;

        public Pref(String... values) {
            this.values = values;
            idx = -1;
        }

        public String getNext() {
            idx = (idx + 1) % values.length;
            return values[idx];
        }
    }
}
