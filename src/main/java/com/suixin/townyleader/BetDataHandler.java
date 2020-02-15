package com.suixin.townyleader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class BetDataHandler {
    private final TownyLeader townyLeader;

    public BetDataHandler(final TownyLeader townyLeader) {
        this.townyLeader = townyLeader;
        this.SaveDbConfig();
    }

    //读取数据库配置数据
    public FileConfiguration LoadDbData( ) {
        final File file = new File(this.townyLeader.getDataFolder(), "db.yml");
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config;
    }

    public void SaveDbConfig() {
        final File file = new File(this.townyLeader.getDataFolder(), "db.yml");

        if (!file.exists()) {
            this.townyLeader.getDataFolder().mkdirs();
            final FileConfiguration config = new YamlConfiguration();
            config.set("Mysql.dbDriver", "com.mysql.cj.jdbc.Driver");
            config.set("Mysql.dbURL", "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone = GMT");
            config.set("Mysql.userName", "root");
            config.set("Mysql.password", "1234");
            try {
                config.save(file);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

}
