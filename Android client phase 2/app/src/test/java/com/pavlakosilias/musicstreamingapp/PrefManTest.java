package com.pavlakosilias.musicstreamingapp;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class PrefManTest {
    @Test
    public void testKeysShowConfig() {
        for (String key : new String[]{"broker", "chunks"}) {
            Set<String> values = new HashSet<>();
            String value;
            while (true) {
                value = PrefMan.getInstance().get(key);
                if (!values.contains(value)) {
                    values.add(value);
                } else {
                    break;
                }
            }
            System.err.println(String.format("Found Key: %s\\tValues: %s", key, values));
        }
    }
}