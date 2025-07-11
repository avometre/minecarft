package com.kayitasistani;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class GirisKomutu implements CommandExecutor {
    private final KullaniciRepository kullaniciRepo;
    private final KayitAsistani plugin;
    private static final Set<UUID> oturumAcanlar = Collections.synchronizedSet(new HashSet<>());
    // Başarısız giriş sayısı ve engel bitiş zamanı için mapler
    private static final ConcurrentHashMap<UUID, Integer> denemeSayilari = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> engelBitisZamanlari = new ConcurrentHashMap<>();
    private final int maksimumDeneme;
    private final long engellemeSuresiMs;

    public GirisKomutu(KayitAsistani plugin, KullaniciRepository kullaniciRepo, int engellemeSuresiSaniye, int maksimumDeneme) {
        this.plugin = plugin;
        this.kullaniciRepo = kullaniciRepo;
        this.engellemeSuresiMs = engellemeSuresiSaniye * 1000L;
        this.maksimumDeneme = maksimumDeneme;
    }

    public static void removeOturum(UUID uuid) {
        oturumAcanlar.remove(uuid);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Bu komut sadece oyuncular tarafından kullanılabilir.").color(NamedTextColor.RED));
            return true;
        }
        Player oyuncu = (Player) sender;
        if (!oyuncu.isOnline()) {
            return true;
        }
        if (args.length != 1) {
            sendKullanimMesaji(oyuncu);
            return true;
        }
        final UUID uuidFinal = oyuncu.getUniqueId();
        final String sifreFinal = args[0];

        // Engel kontrolü
        Long engelBitis = engelBitisZamanlari.get(uuidFinal);
        if (engelBitis != null && System.currentTimeMillis() < engelBitis) {
            long kalan = (engelBitis - System.currentTimeMillis()) / 1000;
            sendActionBar(oyuncu, "Çok fazla hatalı giriş! Lütfen " + kalan + " saniye sonra tekrar deneyin.", NamedTextColor.RED);
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!oyuncu.isOnline()) return;
            try {
                Kullanici kullanici = kullaniciRepo.kullaniciGetir(uuidFinal);
                if (kullanici == null || kullanici.getSifreHash() == null) {
                    sendActionBar(oyuncu, "Hesabınız bulunamadı. Lütfen önce /kayıt ile kayıt olun.", NamedTextColor.RED);
                    return;
                }
                if (BCrypt.checkpw(sifreFinal, kullanici.getSifreHash())) {
                    oturumAcanlar.add(uuidFinal);
                    // Son giriş tarihi ve IP güncelle
                    kullanici.setSonGirisTarihi(new java.util.Date());
                    String girisIp = null;
                    if (oyuncu.getAddress() != null && oyuncu.getAddress().getAddress() != null) {
                        girisIp = oyuncu.getAddress().getAddress().getHostAddress();
                        kullanici.setSonGirisIp(girisIp);
                    }
                    try {
                        kullaniciRepo.kullaniciGuncelle(kullanici);
                        plugin.getLogger().info(oyuncu.getName() + " (" + uuidFinal + ") kullanıcısı giriş yaptı. IP: " + (girisIp != null ? girisIp : "bilinmiyor"));
                    } catch (Exception ex) {
                        plugin.getLogger().severe("Kullanıcı güncellenirken hata: " + ex.getMessage());
                        sendActionBar(oyuncu, "Giriş kaydı güncellenemedi! Lütfen tekrar deneyin.", NamedTextColor.RED);
                        return;
                    }
                    // Başarılı girişte sayaç ve engel sıfırlanır
                    denemeSayilari.remove(uuidFinal);
                    engelBitisZamanlari.remove(uuidFinal);
                    sendActionBar(oyuncu, "Başarıyla giriş yaptınız!", NamedTextColor.GREEN);
                } else {
                    // Başarısız giriş
                    int deneme = denemeSayilari.getOrDefault(uuidFinal, 0) + 1;
                    if (deneme >= maksimumDeneme) {
                        engelBitisZamanlari.put(uuidFinal, System.currentTimeMillis() + engellemeSuresiMs);
                        denemeSayilari.remove(uuidFinal);
                        sendActionBar(oyuncu, "Çok fazla hatalı giriş! " + (engellemeSuresiMs/1000) + " saniye boyunca giriş yapamazsınız.", NamedTextColor.RED);
                    } else {
                        denemeSayilari.put(uuidFinal, deneme);
                        int kalanHak = maksimumDeneme - deneme;
                        sendActionBar(oyuncu, "Şifre yanlış! (" + deneme + "/" + maksimumDeneme + ") - Kalan deneme: " + kalanHak, NamedTextColor.RED);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("/giriş komutunda hata: " + e.getMessage());
                sendActionBar(oyuncu, "Bir hata oluştu, lütfen tekrar deneyin.", NamedTextColor.RED);
            }
        });
        return true;
    }

    public static boolean oturumAcik(UUID uuid) {
        return oturumAcanlar.contains(uuid);
    }

    private void sendActionBar(Player oyuncu, String mesaj, NamedTextColor renk) {
        if (oyuncu.isOnline()) {
            oyuncu.sendActionBar(Component.text(mesaj).color(renk).decorate(TextDecoration.BOLD));
        }
    }

    private void sendKullanimMesaji(Player oyuncu) {
        oyuncu.sendActionBar(Component.text("Kullanım: /giriş <şifre>").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }
} 