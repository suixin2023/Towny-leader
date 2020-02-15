package com.suixin.townyleader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.sql.*;
import java.util.logging.Logger;

public class TownyLeader extends JavaPlugin implements CommandExecutor {
	public Logger log;
	private BetDataHandler betDataHandler;

	@Override
	public void onEnable() {
		this.log = getLogger();
		getLogger().info("==================[TownyLeader]==================");
		this.betDataHandler = new BetDataHandler(this);
		getCommand("townyleader").setExecutor(this);
	}
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String arg, final String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}else {
			sender.sendMessage("错误，这是一个玩家指令!");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("townyleader")) {
//			set mayor 城镇 npcssss

			List<String> argsList = Arrays.asList(args);
			if (argsList.size() != 4) {
				return true;
			}
			String arg1 = argsList.get(0);
			String arg2 = argsList.get(1);
			String arg3 = argsList.get(2);
			String arg4 = argsList.get(3);
			if (arg1.equals("set") && arg2.equals("mayor")) {
				try {
					//设置玩家或者npc为城镇所有者
					setLeader(player,arg3, arg4);
				}catch (Exception e){

				}
			}

		}

		return true;
	}

	private void setLeader (Player player, String arg3, String arg4){
		//查询玩家所在国家
		MayorBetDate mayorBetDate = selectMayorBetDate(player.getName());
		//权限判断
		if(true){
			player.sendMessage("§c无权限，你不是国家领导人");
		}
		//判断输入的城镇名是否属于这个国家
		MayorBetDate mayorBetDate1 = selectMayorBetDateByName(arg3);
		if (!mayorBetDate1.getTownyName().equals(mayorBetDate.getTownyName())) {
			player.sendMessage("§c输入的城镇不属于你所在的国家");
		}
		//查询输入的玩家是否是该城镇的居民
		MayorBetDate mayorBetDate2 = selectMayorBetDateByResident(arg4);
		if (mayorBetDate2 == null){
			player.sendMessage("§c输入的玩家不属于你所在的国家");
		}
		//修改城镇所有者
		updateMayor(arg4,arg3);
	}

    //查询玩家所在国家
	public MayorBetDate selectMayorBetDate(String name){
		String sql = "select * from towny_towns where name ="+name;
		MayorBetDate mayorBetDate = new MayorBetDate();
		try {
			JdbcUtil db = new JdbcUtil();
			db.openConnection(this.getBetDataHandler());
			ResultSet rst = db.execQuery(sql);
			if (rst!=null && rst.next()) {
				mayorBetDate.setTownyName(rst.getString("name"));
				mayorBetDate.setNation(rst.getString("nation"));
				mayorBetDate.setMayor(rst.getString("mayor"));
				mayorBetDate.setResidents(rst.getString("residents"));
			}

			db.close(rst);
			db.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mayorBetDate;
	}

	//查询输入的城镇名是否属于这个国家
	public MayorBetDate selectMayorBetDateByName(String name){
		String sql = "select * from towny_towns where name ="+name;
		MayorBetDate mayorBetDate = new MayorBetDate();
		try {
			JdbcUtil db = new JdbcUtil();
			db.openConnection(this.getBetDataHandler());
			ResultSet rst = db.execQuery(sql);
			if (rst!=null && rst.next()) {
				mayorBetDate.setTownyName(rst.getString("name"));
				mayorBetDate.setNation(rst.getString("nation"));
				mayorBetDate.setMayor(rst.getString("mayor"));
				mayorBetDate.setResidents(rst.getString("residents"));
			}

			db.close(rst);
			db.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mayorBetDate;
	}

	//查询输入的玩家是否是该城镇的居民
	public MayorBetDate selectMayorBetDateByResident(String name){
		String sql = "select * from towny_towns where name ="+name;
		MayorBetDate mayorBetDate = new MayorBetDate();
		try {
			JdbcUtil db = new JdbcUtil();
			db.openConnection(this.getBetDataHandler());
			ResultSet rst = db.execQuery(sql);
			if (rst!=null && rst.next()) {
				mayorBetDate.setTownyName(rst.getString("name"));
				mayorBetDate.setNation(rst.getString("nation"));
				mayorBetDate.setMayor(rst.getString("mayor"));
				mayorBetDate.setResidents(rst.getString("residents"));
			}

			db.close(rst);
			db.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mayorBetDate;
	}

	//修改城镇所有者
	public void updateMayor(String mayor,String name){
		String sql;
		sql = "update towny_towns set mayor = "+mayor+"where name = "+name;
		try {
			JdbcUtil db = new JdbcUtil();
			db.openConnection(this.getBetDataHandler());
			db.execCommand(sql);
			db.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BetDataHandler getBetDataHandler() {
		return betDataHandler;
	}

	public void setBetDataHandler(BetDataHandler betDataHandler) {
		this.betDataHandler = betDataHandler;
	}
}