package com.suixin.tavern.handler;

import com.suixin.tavern.Tavern;
import com.suixin.tavern.entity.PlayerBetDate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BetDataHandler {
    private Tavern tavern;
    List<PlayerBetDate> currentBetList =new ArrayList<>();

    public BetDataHandler(final Tavern tavern) {
        this.tavern = tavern;
    }
    public BetDataHandler() {

    }

    public List<PlayerBetDate> getCurrentBetList() {
        return currentBetList;
    }

    public void setCurrentBetList(List<PlayerBetDate> currentBetList) {
        this.currentBetList = currentBetList;
    }
}
