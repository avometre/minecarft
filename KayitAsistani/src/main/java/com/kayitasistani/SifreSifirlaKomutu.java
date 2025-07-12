package com.kayitasistani;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.command.ConsoleCommandSender;
import java.util.Date;

public class SifreSifirlaKomutu implements CommandExecutor {
    private final KayitAsistani plugin;
    private final KullaniciRepository kullaniciRepository;

    public SifreSifirlaKomutu(KayitAsistani plugin, KullaniciRepository kullaniciRepository) {
        this.plugin = plugin;
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("kayitasistani.sifresifirla")) {
            sender.sendMessage(Component.text("Bu komutu kullanmak için yetkiniz yok!", NamedTextColor.RED));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(Component.text("Kullanım: /şifresıfırla <oyuncu> <yeniŞifre>", NamedTextColor.YELLOW));
            return true;
        }
        String oyuncuAdi = args[0];
        String yeniSifre = args[1];
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Kullanici kullanici = kullaniciRepository.kullaniciGetirKullaniciAdi(oyuncuAdi);
            if (kullanici == null) {
                sender.sendMessage(Component.text("Belirtilen oyuncu bulunamadı!", NamedTextColor.RED));
                return;
            }
            String yeniHash = GirisKomutu.hashSifre(yeniSifre);
            kullanici.setSifreHash(yeniHash);
            kullanici.setSifreDegistirmeTarihi(new Date());
            kullaniciRepository.kullaniciGuncelle(kullanici);
            sender.sendMessage(Component.text("Oyuncunun şifresi başarıyla sıfırlandı!", NamedTextColor.GREEN));
            Player hedef = Bukkit.getPlayerExact(oyuncuAdi);
            if (hedef != null && hedef.isOnline()) {
                hedef.sendActionBar(Component.text("Şifreniz bir admin tarafından sıfırlandı.", NamedTextColor.YELLOW));
            }
        });
        return true;
    }
} 