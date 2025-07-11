package com.kayitasistani;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.mindrot.jbcrypt.BCrypt;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import java.util.UUID;
import java.util.Date;

public class SifremiDegistirKomutu implements CommandExecutor {
    private final KullaniciRepository kullaniciRepo;
    private final KayitAsistani plugin;

    public SifremiDegistirKomutu(KayitAsistani plugin, KullaniciRepository kullaniciRepo) {
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
        if (!oyuncu.isOnline()) return true;
        if (args.length != 2) {
            oyuncu.sendActionBar(Component.text("Kullanım: /şifremideğiştir <eskişifre> <yenişifre>").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
            return true;
        }
        final UUID uuidFinal = oyuncu.getUniqueId();
        final String eskiSifre = args[0];
        final String yeniSifre = args[1];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Kullanici kullanici = kullaniciRepo.kullaniciGetir(uuidFinal);
                if (kullanici == null || kullanici.getSifreHash() == null) {
                    oyuncu.sendActionBar(Component.text("Hesabınız bulunamadı.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                    return;
                }
                if (!BCrypt.checkpw(eskiSifre, kullanici.getSifreHash())) {
                    oyuncu.sendActionBar(Component.text("Eski şifre yanlış!").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                    return;
                }
                String yeniHash = BCrypt.hashpw(yeniSifre, BCrypt.gensalt());
                kullanici.setSifreHash(yeniHash);
                kullanici.setSifreDegistirmeTarihi(new Date());
                kullaniciRepo.kullaniciGuncelle(kullanici);
                oyuncu.sendActionBar(Component.text("Şifreniz başarıyla değiştirildi!").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
            } catch (Exception e) {
                plugin.getLogger().severe("/şifremideğiştir komutunda hata: " + e.getMessage());
                oyuncu.sendActionBar(Component.text("Bir hata oluştu, lütfen tekrar deneyin.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
            }
        });
        return true;
    }
} 