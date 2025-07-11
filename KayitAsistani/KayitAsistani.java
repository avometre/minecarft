package com.kayitasistani;

import org.bukkit.plugin.java.JavaPlugin;

public class KayitAsistani extends JavaPlugin {
    private static KayitAsistani instance;
    private MongoDBManager mongoDBManager;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("KayitAsistani başlatılıyor...");
        mongoDBManager = new MongoDBManager(this);
        mongoDBManager.connect();
        getLogger().info("KayitAsistani aktif!");
    }

    @Override
    public void onDisable() {
        if (mongoDBManager != null) {
            mongoDBManager.close();
        }
        getLogger().info("KayitAsistani devre dışı bırakıldı.");
    }

    public static KayitAsistani getInstance() {
        return instance;
    }

    public MongoDBManager getMongoDBManager() {
        return mongoDBManager;
    }
} 