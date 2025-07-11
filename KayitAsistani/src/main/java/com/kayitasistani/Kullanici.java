package com.kayitasistani;

import org.bson.Document;
import java.util.Date;
import java.util.UUID;

public class Kullanici {
    private UUID uuid;
    private String kullaniciAdi;
    private String sifreHash;
    private Date kayitTarihi;
    private Date sonGirisTarihi;
    private String ipAdresi;
    private String sonGirisIp;
    private String yetkiSeviyesi;
    private Date sifreDegistirmeTarihi;
    private String hesapDurumu;
    private long toplamOynamaSuresi;

    public Kullanici(UUID uuid, String kullaniciAdi, String sifreHash, Date kayitTarihi, String ipAdresi) {
        this.uuid = uuid;
        this.kullaniciAdi = kullaniciAdi;
        this.sifreHash = sifreHash;
        this.kayitTarihi = kayitTarihi;
        this.sonGirisTarihi = kayitTarihi;
        this.ipAdresi = ipAdresi;
        this.sonGirisIp = ipAdresi;
        this.yetkiSeviyesi = "oyuncu";
        this.sifreDegistirmeTarihi = kayitTarihi;
        this.hesapDurumu = "aktif";
        this.toplamOynamaSuresi = 0;
    }

    public Document toDocument() {
        return new Document("uuid", uuid.toString())
                .append("kullaniciAdi", kullaniciAdi)
                .append("sifreHash", sifreHash)
                .append("kayitTarihi", kayitTarihi)
                .append("sonGirisTarihi", sonGirisTarihi)
                .append("ipAdresi", ipAdresi)
                .append("sonGirisIp", sonGirisIp)
                .append("yetkiSeviyesi", yetkiSeviyesi)
                .append("sifreDegistirmeTarihi", sifreDegistirmeTarihi)
                .append("hesapDurumu", hesapDurumu)
                .append("toplamOynamaSuresi", toplamOynamaSuresi);
    }

    public static Kullanici fromDocument(Document doc) {
        Kullanici k = new Kullanici(
                UUID.fromString(doc.getString("uuid")),
                doc.getString("kullaniciAdi"),
                doc.getString("sifreHash"),
                doc.getDate("kayitTarihi"),
                doc.getString("ipAdresi")
        );
        k.sonGirisTarihi = doc.getDate("sonGirisTarihi");
        k.sonGirisIp = doc.getString("sonGirisIp");
        k.yetkiSeviyesi = doc.getString("yetkiSeviyesi");
        k.sifreDegistirmeTarihi = doc.getDate("sifreDegistirmeTarihi");
        k.hesapDurumu = doc.getString("hesapDurumu");
        k.toplamOynamaSuresi = doc.getLong("toplamOynamaSuresi");
        return k;
    }

    // Getter ve setter metodları eklenebilir
    public String getSifreHash() {
        return sifreHash;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getToplamOynamaSuresi() {
        return toplamOynamaSuresi;
    }
    public void setToplamOynamaSuresi(long sure) {
        this.toplamOynamaSuresi = sure;
    }

    public void setSifreHash(String hash) {
        this.sifreHash = hash;
    }
    public void setSifreDegistirmeTarihi(Date tarih) {
        this.sifreDegistirmeTarihi = tarih;
    }

    public String getKullaniciAdi() {
        return kullaniciAdi;
    }
    public Date getKayitTarihi() {
        return kayitTarihi;
    }
    public Date getSonGirisTarihi() {
        return sonGirisTarihi;
    }
    public String getSonGirisIp() {
        return sonGirisIp;
    }
    public String getHesapDurumu() {
        return hesapDurumu;
    }
    public String getYetkiSeviyesi() {
        return yetkiSeviyesi;
    }
    public Date getSifreDegistirmeTarihi() {
        return sifreDegistirmeTarihi;
    }

    public void setSonGirisTarihi(Date tarih) {
        this.sonGirisTarihi = tarih;
    }
    public void setSonGirisIp(String ip) {
        this.sonGirisIp = ip;
    }
} 