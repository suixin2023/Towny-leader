package com.suixin.tavern.listener;

import com.suixin.tavern.Tavern;
import com.suixin.tavern.handler.DatabaseHandler;
import com.suixin.tavern.handler.Ranks;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class HandlePlayerPrefix implements Listener {
    private final DatabaseHandler databaseHandler;
    private final Ranks ranks;
    @SuppressWarnings("unused")
    private final Tavern tavern;

    Map<String, Integer> map = new HashMap<String, Integer>();

    public HandlePlayerPrefix(final DatabaseHandler databaseHandler, final Ranks ranks, final Tavern tavern) {
        this.databaseHandler = databaseHandler;
        this.ranks = ranks;
        this.tavern = tavern;
    }

    @EventHandler
    public void onKill(final PlayerDeathEvent death) {
        int kills = 0;
        if (death.getEntity().getKiller() != null) {
            final String killed = death.getEntity().getName();
            final Player player = death.getEntity().getKiller();

            if (this.map.containsKey(player.getName())) {
                kills = this.map.get(player.getName());
            }
            if (this.map.containsKey(killed)) {
                this.map.put(killed, 0);
            }
            this.databaseHandler.LoadPlayerData(killed);
            final int killedpoint = databaseHandler.getPoints();
            this.databaseHandler.LoadPlayerData(player.getName());
            final int fame = this.databaseHandler.getFame();
            if (!player.getName().equalsIgnoreCase(killed)) {
                this.calculateFame(killed, player, fame, kills, killedpoint);
            }
            kills++;
            this.map.put(player.getName(), kills);
        }
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        String rank = null;
        this.databaseHandler.LoadPlayerData(event.getPlayer().getName());
        this.databaseHandler.LoadConfig();
        rank = this.ranks.GetRank(this.databaseHandler.getFame());
        if (rank != null && rank != "") {
            final String a = String.format(ChatColor.WHITE + "[" + this.databaseHandler.PrefixColor + rank + ChatColor.WHITE + "] ");
            final String format = event.getFormat();
            event.setFormat(a + format);
        }
    }

    @EventHandler
    public void onPlayerLogin(final PlayerLoginEvent event) {
        final Player player = event.getPlayer();

        try {
            this.databaseHandler.FirstRun(player.getName());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        this.map.put(player.getName(), 0);
    }

    private void calculateFame(final String killed, final Player player, int fame, final int kills, final int killedpoint) {
        final String currentRank = this.ranks.GetRank(fame);
        final String tag = this.databaseHandler.getTag();
        fame++;
        player.sendMessage(ChatColor.GREEN + "你击杀了 " + killed + " 获得一点 " + tag + ".");
        this.databaseHandler.LoadPlayerData(player.getName());
        if (killedpoint <= 0) {
            player.sendMessage(ChatColor.RED + killed + " 已经是穷光蛋了 无法获得任何点数.");
        } else {
            player.sendMessage(ChatColor.GREEN + "你击杀了 " + killed + " 获得了点数1点(可兑换物品).");
            this.databaseHandler.SavePlayerPoint(player.getName(), databaseHandler.getPoints() + 1);
            this.databaseHandler.SavePlayerPoint(killed, killedpoint - 1);
        }
        this.databaseHandler.SavePlayerFame(player.getName(), fame);
        final String newRank = this.ranks.GetRank(fame);

        if (!currentRank.equalsIgnoreCase(newRank)) {
            player.sendMessage(ChatColor.GREEN + "恭喜! 您已晋升为 " + newRank);
        }
    }
}