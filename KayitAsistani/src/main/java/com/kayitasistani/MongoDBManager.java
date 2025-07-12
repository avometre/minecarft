package com.kayitasistani;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;

public class MongoDBManager {
    private MongoClient client;
    private MongoDatabase database;
    private final Plugin plugin;
    private final String uri;
    private final String dbName;

    public MongoDBManager(Plugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.uri = config.getString("mongodb.uri");
        this.dbName = config.getString("mongodb.database");
    }

    public synchronized void connect() {
        int retry = 0;
        while (retry < 3) {
            try {
                client = MongoClients.create(uri);
                database = client.getDatabase(dbName);
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