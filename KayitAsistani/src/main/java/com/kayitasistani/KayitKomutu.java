package com.kayitasistani;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.util.Date;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class KayitKomutu implements CommandExecutor {
    private final KullaniciRepository kullaniciRepo;
    private final KayitAsistani plugin;

    public KayitKomutu(KayitAsistani plugin, KullaniciRepository kullaniciRepo) {
        this.plugin = plugin;
        this.kullaniciRepo = kullaniciRepo;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return true;
        }
        Player oyuncu = (Player) sender;
        if (args.length != 1) {
            oyuncu.sendActionBar(Component.text("Kullanım: /kayıt <şifre>").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
            return true;
        }
        String sifre = args[0];
        UUID uuid = oyuncu.getUniqueId();
        String kullaniciAdi = oyuncu.getName();
        String ip = null;
        if (oyuncu.getAddress() != null && oyuncu.getAddress().getAddress() != null) {
            ip = oyuncu.getAddress().getAddress().getHostAddress();
        }

        final String sifreFinal = sifre;
        final UUID uuidFinal = uuid;
        final String kullaniciAdiFinal = kullaniciAdi;
        final String ipFinal = ip;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (kullaniciRepo.kullaniciVarMi(uuidFinal)) {
                    oyuncu.sendActionBar(Component.text("Zaten kayıtlısınız!").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                    return;
                }
                if (kullaniciRepo.kullaniciGetirKullaniciAdi(kullaniciAdiFinal) != null) {
                    oyuncu.sendActionBar(Component.text("Bu kullanıcı adı zaten alınmış!").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                    return;
                }
                String hash = BCrypt.hashpw(sifreFinal, BCrypt.gensalt());
                Kullanici yeniKullanici = new Kullanici(uuidFinal, kullaniciAdiFinal, hash, new Date(), ipFinal);
                kullaniciRepo.kullaniciEkle(yeniKullanici);
                oyuncu.sendActionBar(Component.text("Başarıyla kayıt oldunuz!").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
            } catch (Exception e) {
                plugin.getLogger().severe("/kayıt komutunda hata: " + e.getMessage());
                oyuncu.sendActionBar(Component.text("Bir hata oluştu, lütfen tekrar deneyin.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
            }
        });
        return true;
    }
} 