package com.kayitasistani;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bukkit.plugin.Plugin;

public class MongoDBManager {
    private static final String URI = "mongodb://minecraft:%40merR4141%40%40%21@94.154.34.158:27017/minecraftdb?authSource=minecraftdb";
    private static final String DB_NAME = "minecraftdb";
    private MongoClient client;
    private MongoDatabase database;
    private final Plugin plugin;

    public MongoDBManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void connect() {
        int retry = 0;
        while (retry < 3) {
            try {
                client = MongoClients.create(URI);
                database = client.getDatabase(DB_NAME);
                // Test bağlantı
                database.listCollectionNames().first();
                plugin.getLogger().info("MongoDB bağlantısı başarılı!");
                break;
            } catch (Exception e) {
                retry++;
                plugin.getLogger().warning("MongoDB bağlantısı başarısız! Tekrar deneniyor... (" + retry + ")");
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                if (retry == 3) {
                    plugin.getLogger().severe("MongoDB bağlantısı tamamen başarısız: " + e.getMessage());
                }
            }
        }
    }

    public synchronized MongoDatabase getDatabase() {
        return database;
    }

    public synchronized void close() {
        if (client != null) client.close();
    }
} 