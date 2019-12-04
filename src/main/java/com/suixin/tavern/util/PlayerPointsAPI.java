package com.suixin.tavern.util;

import java.util.UUID;

import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PlayerPointsAPI {
    private static PlayerPoints plugin;

    public static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("PlayerPoints") == null) {
            return false;
        }
        RegisteredServiceProvider<PlayerPoints> rsp = Bukkit.getServer().getServicesManager().getRegistration(PlayerPoints.class);
        if (rsp == null) {
            return false;
        }
        plugin = (PlayerPoints) rsp.getProvider();
        return plugin != null;
    }

    public static void giveMoney(UUID playerId, int amount) {
        if (plugin == null) {
            throw new UnsupportedOperationException("还没连接到PlayerPoints");
        }
        plugin.getAPI().give(playerId, amount);
    }

}
