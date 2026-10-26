package com.familiasantana.client;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config {
    public static final String[] NAMES = {
            "Marca", "FPS", "CPS", "Ping", "Coordenadas", "Direcao", "Relogio",
            "Keystrokes", "Armor HUD", "Efeitos", "Toggle Sprint", "Zoom", "Fullbright"
    };

    private static final Map<String, Boolean> MAP = new HashMap<String, Boolean>();
    private static File file;

    public static void load() {
        file = new File(Minecraft.getMinecraft().mcDataDir, "config/familiasantana.properties");
        Properties p = new Properties();
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                p.load(in);
            } catch (IOException ignored) {
            }
        }
        for (String n : NAMES) {
            boolean def = !n.equals("Fullbright");
            MAP.put(n, Boolean.parseBoolean(p.getProperty(n, String.valueOf(def))));
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (String n : NAMES) p.setProperty(n, String.valueOf(get(n)));
        try {
            file.getParentFile().mkdirs();
            try (OutputStream out = new FileOutputStream(file)) {
                p.store(out, "Familia Santana Client");
            }
        } catch (IOException ignored) {
        }
    }

    public static boolean get(String name) {
        Boolean b = MAP.get(name);
        return b != null && b;
    }

    public static void toggle(String name) {
        MAP.put(name, !get(name));
    }
}
