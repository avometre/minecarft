package com.kayitasistani;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class ProfilKomutu implements CommandExecutor {
    private final KullaniciRepository kullaniciRepo;
    private final KayitAsistani plugin;

    public ProfilKomutu(KayitAsistani plugin, KullaniciRepository kullaniciRepo) {
        this.plugin = plugin;
        this.kullaniciRepo = kullaniciRepo;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Bu komut sadece oyuncular tarafından kullanılabilir.").color(NamedTextColor.RED));
            return true;
        }
        Player oyuncu = (Player) sender;
        UUID uuid = oyuncu.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Kullanici kullanici = kullaniciRepo.kullaniciGetir(uuid);
                if (kullanici == null) {
                    oyuncu.sendMessage(Component.text("Hesabınız bulunamadı.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                    return;
                }
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                String kayitTarihi = kullanici.getKayitTarihi() != null ? kullanici.getKayitTarihi().toInstant().atZone(ZoneId.systemDefault()).format(dtf) : "-";
                String sonGiris = kullanici.getSonGirisTarihi() != null ? kullanici.getSonGirisTarihi().toInstant().atZone(ZoneId.systemDefault()).format(dtf) : "-";
                String sifreDegistirme = kullanici.getSifreDegistirmeTarihi() != null ? kullanici.getSifreDegistirmeTarihi().toInstant().atZone(ZoneId.systemDefault()).format(dtf) : "-";
                String toplamSure = formatSure(kullanici.getToplamOynamaSuresi());
                oyuncu.sendMessage(Component.text("--- Profil Bilgileriniz ---").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                oyuncu.sendMessage(Component.text("Kullanıcı Adı: ").color(NamedTextColor.YELLOW).append(Component.text(kullanici.getKullaniciAdi()).color(NamedTextColor.WHITE)));
                oyuncu.sendMessage(Component.text("Kayıt Tarihi: ").color(NamedTextColor.YELLOW).append(Component.text(kayitTarihi).color(NamedTextColor.WHITE)));
                oyuncu.sendMessage(Component.text("Son Giriş: ").color(NamedTextColor.YELLOW).append(Component.text(sonGiris).color(NamedTextColor.WHITE)));
                oyuncu.sendMessage(Component.text("Son Giriş IP: ").color(NamedTextColor.YELLOW).append(Component.text(kullanici.getSonGirisIp() != null ? kullanici.getSonGirisIp() : "-").color(NamedTextColor.WHITE)));
                oyuncu.sendMessage(Component.text("Hesap Durumu: ").color(NamedTextColor.YELLOW).append(Component.text(kullanici.getHesapDurumu() != null ? kullanici.getHesapDurumu() : "-").color(NamedTextColor.WHITE)));
                oyuncu.sendMessage(Component.text("Yetki Seviyesi: ").color(NamedTextColor.YELLOW).append(Component.text(kullanici.getYetkiSeviyesi() != null ? kullanici.getYetkiSeviyesi() : "-").color(NamedTextColor.WHITE)));
                oyuncu.sendMessage(Component.text("Toplam Oynama Süresi: ").color(NamedTextColor.YELLOW).append(Component.text(toplamSure).color(NamedTextColor.WHITE)));
                oyuncu.sendMessage(Component.text("Şifre Değiştirme Tarihi: ").color(NamedTextColor.YELLOW).append(Component.text(sifreDegistirme).color(NamedTextColor.WHITE)));
            } catch (Exception e) {
                plugin.getLogger().severe("/profil komutunda hata: " + e.getMessage());
                oyuncu.sendMessage(Component.text("Profil bilgileri alınırken hata oluştu.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
            }
        });
        return true;
    }

    private String formatSure(long ms) {
        long saniye = ms / 1000;
        long saat = saniye / 3600;
        long dakika = (saniye % 3600) / 60;
        long sn = saniye % 60;
        return String.format("%02d saat %02d dk %02d sn", saat, dakika, sn);
    }
} 