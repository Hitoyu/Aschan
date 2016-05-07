package me.hitoyu.aschan;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

class command implements CommandExecutor {

	
	@SuppressWarnings("static-access")
	@Override
	public boolean onCommand(CommandSender player, Command cmd, String label, String[] args) {
		aschan.hcs.debugCall("player " + player.getName() + " ran command " + label + " " + Arrays.toString(args).replace(",", ""), null, false);
		
		aschan aschan = (aschan)Bukkit.getPluginManager().getPlugin("Aschan");
		if(args.length == 0) {
			String message = "/aschan <";
			String[] temp = {""};
			int c = 0;
			if(player.hasPermission("aschan.setdebug")) {
				temp[c] = "debug";
				c++;
			}
			
			if(player.hasPermission("aschan.broadcast")) {
				temp[c] = "broadcast";
				c++;
			}
			
			if(player.hasPermission("aschan.reload")) {
				temp[c] = "broadcast";
				c++;
			}
			
			if(c == 0) {
				player.sendMessage("You have no permissions for this command.");
			} else {
				for(int x = 0; x < c; x++) {
					if(x > 0) {
						message = message + "/";
					}
					message = message + temp[x];
				}
				
				player.sendMessage(message + ">");
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("debug")) {
			if (player.hasPermission("aschan.setdebug")) {
				if (args.length < 2) {
					player.sendMessage(ChatColor.RED + "You need to provide a debug level. 0, 1, or 2.");
					return false;
				}

				String num = args[1];

				try {
					int level = Integer.parseInt(num);
					changeDebugLevel(level);
					String prefix = ChatColor.GREEN + "Debug level changed to ";
					String mode = "";
					if (level == 0) {
						mode = "disabled\u00A7a.";
					} else if (level == 1) {
						mode = "normal\u00A7a.";
					} else {
						mode = "advanced\u00A7a.";
					}
					player.sendMessage(prefix + ChatColor.YELLOW + mode);
					return true;
				} catch (Exception ignore) {
					player.sendMessage(ChatColor.RED + "You need to enter a number.");
					return false;
				}

			}
		}
		
		if(args[0].equalsIgnoreCase("broadcast")) {
			if(args.length < 2) {
				player.sendMessage("You need to enter a number");
			}
			int num = -1;
			try {
				num = Integer.parseInt(args[1]);
				if(num >= aschan.announcements.size()) {
					num = aschan.announcements.size();
				} else if (num < 1) {
					num = 1;
				}
				aschan.broadcast(num - 1);
			} catch (Exception ex) {
				player.sendMessage("Did you enter a number or a word?");
				return true;
			}
		}
		
		if(args[0].equalsIgnoreCase("reload")) {
			aschan.reloadConfig();
			aschan.pluginLoad();
		}
		return false;
	
	}
	
	private void changeDebugLevel(int level) {
		if (level > 2) {
			level = 2;
		}
		if (level < 0) {
			level = 0;
		}
		aschan aschan = (aschan)Bukkit.getPluginManager().getPlugin("Aschan");
		aschan.getConfig().set("debug", level);
		aschan.saveConfig();
		aschan.reloadConfig();
		aschan.readAndSetDebug();

		if (aschan.isDebugEnabled() == true) {
			if (aschan.isAdvancedDebugEnabled() == true) {
				aschan.log.warning("You have advanced debug mode enabled. This may produce a lot of console spam.");
			} else {
				aschan.log.warning("You have debug mode enabled. This may produce some minor console spam.");
			}
			aschan.log.warning("Debug mode is used to help find and fix bugs, if you aren't doing either of those I recommend turning debug off. Disable it by setting 'debug' in the config to 0.");
		}
	}
}
