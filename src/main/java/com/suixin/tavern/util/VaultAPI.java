package com.suixin.tavern.util;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultAPI {

    private static Economy economy;

    public static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = (Economy) rsp.getProvider();
        return economy != null;
    }

    public static double getMoney(String name) {
        if (economy == null) {
            throw new UnsupportedOperationException("还没连接到Vault");
        }
        return economy.getBalance(name);
    }

    public static void giveMoney(String name, double money) {
        if (economy == null) {
            throw new UnsupportedOperationException("还没连接到Vault");
        }
        economy.depositPlayer(name, money);
    }

    public static void removeMoney(String name, double money) {
        if (economy == null) {
            throw new UnsupportedOperationException("还没连接到Vault");
        }
        economy.withdrawPlayer(name, money);
    }

}
