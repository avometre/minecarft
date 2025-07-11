package com.kayitasistani;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class KayitAsistani extends JavaPlugin implements Listener {
    private static KayitAsistani instance;
    private MongoDBManager mongoDBManager;
    private KullaniciRepository kullaniciRepository;
    private final Map<UUID, Long> girisZamanlari = new ConcurrentHashMap<>();
    private int girisEngellemeSuresi = 60; // saniye
    private int girisMaksDeneme = 5;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        // Config'ten ayarları oku
        if (getConfig().isConfigurationSection("giris")) {
            girisEngellemeSuresi = getConfig().getInt("giris.engellemeSuresi", 60);
            girisMaksDeneme = getConfig().getInt("giris.maksimumDeneme", 5);
        }
        getLogger().info("KayitAsistani başlatılıyor...");
        mongoDBManager = new MongoDBManager(this);
        mongoDBManager.connect();
        kullaniciRepository = new KullaniciRepository(mongoDBManager);
        this.getCommand("kayıt").setExecutor(new KayitKomutu(this, kullaniciRepository));
        this.getCommand("giriş").setExecutor(new GirisKomutu(this, kullaniciRepository, girisEngellemeSuresi, girisMaksDeneme));
        this.getCommand("şifremideğiştir").setExecutor(new SifremiDegistirKomutu(this, kullaniciRepository));
        this.getCommand("profil").setExecutor(new ProfilKomutu(this, kullaniciRepository));
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("KayitAsistani aktif!");
    }

    @Override
    public void onDisable() {
        // İleride başka kaynaklar eklenirse burada kapatılabilir
        girisZamanlari.clear();
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

    public KullaniciRepository getKullaniciRepository() {
        return kullaniciRepository;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player oyuncu = event.getPlayer();
        girisZamanlari.put(oyuncu.getUniqueId(), System.currentTimeMillis());
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            boolean kayitli = false;
            try {
                kayitli = kullaniciRepository.kullaniciVarMi(oyuncu.getUniqueId());
            } catch (Exception e) {
                getLogger().severe("Kullanıcı sorgusunda hata: " + e.getMessage());
            }
            final boolean kayitliFinal = kayitli;
            Bukkit.getScheduler().runTask(this, () -> {
                try {
                    if (kayitliFinal) {
                        oyuncu.showTitle(Title.title(
                            Component.text("Giriş Yapınız!").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                            Component.text("/giriş <şifre>").color(NamedTextColor.GRAY),
                            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                        ));
                    } else {
                        oyuncu.showTitle(Title.title(
                            Component.text("Kayıt Olunuz!").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD),
                            Component.text("/kayıt <şifre>").color(NamedTextColor.GRAY),
                            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                        ));
                    }
                } catch (Exception e) {
                    getLogger().severe("Title gösteriminde hata: " + e.getMessage());
                }
            });
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Long giris = girisZamanlari.remove(uuid);
        if (giris == null) return;
        long cikis = System.currentTimeMillis();
        long oynamaSuresi = cikis - giris;
        // Minimum 5 saniye filtre
        if (oynamaSuresi < 5000) return;
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                Kullanici kullanici = kullaniciRepository.kullaniciGetir(uuid);
                if (kullanici != null) {
                    long mevcutSure = kullanici.getToplamOynamaSuresi();
                    kullanici.setToplamOynamaSuresi(mevcutSure + oynamaSuresi);
                    kullaniciRepository.kullaniciGuncelle(kullanici);
                }
            } catch (Exception e) {
                getLogger().severe("Oynama süresi güncellenirken hata: " + e.getMessage());
            }
        });
        GirisKomutu.removeOturum(uuid);
    }
} 