package com.kayitasistani;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.UUID;

public class KullaniciRepository {
    private final MongoCollection<Document> collection;

    public KullaniciRepository(MongoDBManager mongoDBManager) {
        this.collection = mongoDBManager.getDatabase().getCollection("kullanicilar");
    }

    public boolean kullaniciVarMi(UUID uuid) {
        return collection.find(Filters.eq("uuid", uuid.toString())).first() != null;
    }

    public void kullaniciEkle(Kullanici kullanici) {
        collection.insertOne(kullanici.toDocument());
    }

    public Kullanici kullaniciGetir(UUID uuid) {
        Document doc = collection.find(Filters.eq("uuid", uuid.toString())).first();
        if (doc == null) return null;
        return Kullanici.fromDocument(doc);
    }

    public Kullanici kullaniciGetirKullaniciAdi(String kullaniciAdi) {
        Document doc = collection.find(Filters.eq("kullaniciAdi", kullaniciAdi)).first();
        if (doc == null) return null;
        return Kullanici.fromDocument(doc);
    }

    public void kullaniciGuncelle(Kullanici kullanici) {
        collection.replaceOne(Filters.eq("uuid", kullanici.getUuid().toString()), kullanici.toDocument());
    }
} 