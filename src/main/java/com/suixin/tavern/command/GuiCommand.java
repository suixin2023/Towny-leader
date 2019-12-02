package com.suixin.tavern.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GuiCommand implements CommandExecutor {
	private final Inventory inv2;

	public GuiCommand() {
		inv2 = Bukkit.createInventory(null,54 , "§2 酒馆");
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String arg, final String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}else {
			sender.sendMessage("You must be a player!");
			return false;
		}
		if (cmd.getName().equalsIgnoreCase("tnopen")) {
			player.closeInventory();
			player.openInventory(inv2);
			sender.sendMessage("§2§l请输入下注金额");
		}
		if (args.length > 0) {
			player.sendMessage(ChatColor.RED + "过多的参数!");
		}
		return true;
	}
}
