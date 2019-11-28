package com.suixin.tavern.handler;

import com.suixin.tavern.PvPTitles;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DatabaseHandler {
    private final PvPTitles pvpTitles;
    private int Fame;
    private int Points;
    private int defaultPoints;
    private final Map<Integer, String> rankList;
    private final Map<Integer, Integer> reqFame;
    private final Map<Integer, String> cashList;
    public ChatColor PrefixColor;

    private String tag;
    private String ptag;

    public DatabaseHandler(final PvPTitles pvpTitles) {
        this.rankList = new HashMap<>();
        this.reqFame = new HashMap<>();
        this.cashList = new HashMap<>();
        this.pvpTitles = pvpTitles;
        this.SaveConfig();
    }

    public void FirstRun(final String playername) {
        final File file = new File((new StringBuilder())
                .append(this.pvpTitles.getDataFolder())
                .append(File.separator)
                .append("players")
                .append(File.separator)
                .append(playername)
                .append(".yml")
                .toString());

        if (!file.exists()) {
            this.pvpTitles.getDataFolder().mkdirs();

            final FileConfiguration config = new YamlConfiguration();
            config.set("Fame", 0);
            config.set("Points", new Random().nextInt(defaultPoints));
            try {
                config.save(file);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return cashList
     */
    public Map<Integer, String> getCashList() {
        return cashList;
    }

    public int getFame() {
        return this.Fame;
    }

    public int getPoints() {
        return Points;
    }

    public String getPtag() {
        return ptag;
    }

    public String getTag() {
        return this.tag;
    }

    public void LoadConfig() {
        final File file = new File(this.pvpTitles.getDataFolder(), "config.yml");
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        final List<String> configList = config.getStringList("RankNames");
        defaultPoints = config.getInt("defaultPoints", 50);
        for (int i = 0; i < configList.size(); i++) {
            this.rankList.put(i, configList.get(i));
        }
        @SuppressWarnings("unchecked")
        final List<Integer> derp = (List<Integer>) config.getList("ReqFame");
        for (int i = 0; i < derp.size(); i++) {
            this.reqFame.put(i, derp.get(i));
        }
        this.GetPrefixColor(config.getString("PrefixColor"));
        final ConfigurationSection cash = config.getConfigurationSection("Cash");
        for (final String key : cash.getKeys(false)) {
            try {
                final Integer num = Integer.valueOf(key);
                cashList.put(num, cash.getString(key));
            } catch (final Exception e) {
                pvpTitles.getLogger().warning(key + "不是一个数字 已忽略!");
            }
        }
        tag = config.getString("Tag");
        if (configList.size() != derp.size()) {
            this.pvpTitles.log.info("WARNING - RankNames and ReqFame are not equal in their numbers.");
            this.pvpTitles.log.info("WARNING - RankNames and ReqFame are not equal in their numbers.");
            this.pvpTitles.log.info("WARNING - RankNames and ReqFame are not equal in their numbers.");
        }
    }

    public void LoadPlayerData(final String playername) {
        final File file = new File((new StringBuilder())
                .append(this.pvpTitles.getDataFolder())
                .append(File.separator)
                .append("players")
                .append(File.separator)
                .append(playername)
                .append(".yml")
                .toString());
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.Fame = config.getInt("Fame");
        this.Points = config.getInt("Points");
    }

    public Map<Integer, String> RankList() {
        return this.rankList;
    }

    public Map<Integer, Integer> reqFame() {
        return this.reqFame;
    }

    public void SaveConfig() {
        final File file = new File(this.pvpTitles.getDataFolder(), "config.yml");

        if (!file.exists()) {
            this.pvpTitles.getDataFolder().mkdirs();
            final FileConfiguration config = new YamlConfiguration();
            final String[] ranks = { "None",
                                     "Hero",
                                     "Fierce Hero",
                                     "Mighty Hero",
                                     "Deadly Hero",
                                     "Terrifying Hero",
                                     "Conquering Hero",
                                     "Subjugating Hero",
                                     "Vanquishing Hero",
                                     "Renowned Hero",
                                     "Illustrious Hero",
                                     "Eminent Hero",
                                     "King's Hero",
                                     "Emperor's Hero",
                                     "Balthazar's Hero",
                                     "Legendary Hero" };
            final Integer[] reqfame = { 0, 25, 75, 180, 360, 600, 1000, 1680, 2800, 4665, 7750, 12960, 21600, 36000, 60000, 100000 };
            config.set("Tag", "Fame");
            config.set("PrefixColor", "green");
            config.set("defaultPoints", 50);
            config.set("RankNames", Arrays.asList(ranks));
            config.set("ReqFame", Arrays.asList(reqfame));
            config.set("Cash.10", "give {player} 1 1");
            config.set("Cash.20", "give {player} 1 2");
            config.set("Cash.30", "give {player} 1 3");
            try {
                config.save(file);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void SavePlayerData(final String playername, final String path, final int fame) {
        final File file = new File((new StringBuilder())
                .append(this.pvpTitles.getDataFolder())
                .append(File.separator)
                .append("players")
                .append(File.separator)
                .append(playername)
                .append(".yml")
                .toString());
        if (!file.exists()) {
            this.pvpTitles.getDataFolder().mkdir();
            try {
                file.createNewFile();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set(path, fame);
        try {
            config.save(file);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void SavePlayerFame(final String playername, final int fame) {
        SavePlayerData(playername, "Fame", fame);
    }

    public void SavePlayerPoint(final String playername, final int fame) {
        SavePlayerData(playername, "Points", fame);
    }

    public void setPoints(final int points) {
        Points = points;
    }

    private void GetPrefixColor(final String color) {
        this.PrefixColor = ChatColor.valueOf(color.toUpperCase());
    }
}
