package me.hitoyu.aschan;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import me.hitoyu.hitocore.hitocore;
import me.hitoyu.ant.Api;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class aschan extends JavaPlugin implements Listener {
	// HitoCore integration part one
	hitocore hc = null;
	static hitocore hcs = null;

	// Ant integration
	boolean antInt = false;

	static String prefix = null;
	static me.hitoyu.ant.Api.Language language = null;
	static List<String> announcements = null;
	static int total = 0;
	static int interval = -1;
	int number = 0;
	static boolean random = false;
	
	int debug = 0;
	
	String plugin = "aschan";
	static String plugins = "aschan";
	
	Logger log = null;
	static Logger logs = null;
	
	public void onEnable() {
		pluginLoad();
		log.info("Another scheduled announcer enabled!");
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	void pluginLoad() {
		// Set all values to default, needed for reload command.
		prefix = null;
		language = null;
		announcements = null;
		total = 0;
		interval = -1;
		number = 0;
		random = false;
		debug = 0;
		log = null;
		logs = null;
		
		hc = null;
		hcs = null;
		
		antInt = false;
		
		// The actual startup part
		log = this.getLogger();
		logs = log;
		
		// HitoCore integration part two
		hc = (hitocore) Bukkit.getPluginManager().getPlugin("HitoCore");
		if(!(hc == null)) {
			hcs = hc;
			logs.info("Hooked into " + hcs);
		}
		
		
		antInt = setAntEnabled();
		if(antInt) {
			log.info("Hooked into ANT v" + Api.getPluginVersion());
		}
		
		if(new File(this.getDataFolder() + "config.yml").exists()) {
			hc.debugCall("config.yml exists, reloading it", null, true);
			this.reloadConfig();
		}
		
		// Create config if none exists.
		this.saveDefaultConfig();
		
		log.info("Loading config.");
		if(antInt) {
			hc.debugCall("Reading plugin language", null, true);
			language = Api.getLanguage(getConfig().getString("language"));
			hc.debugCall("Aschan Language set", language.getName(), false);
		}
		
		announcements = getConfig().getStringList("announcements");
		if(announcements == null) {
			announcements.add("Test announcement one");
			announcements.add("Test announcement two");
			getConfig().set("announcements", announcements);
			saveConfig();
			reloadConfig();
		}
		
		total = announcements.size();
		log.info(total + " announcements loaded.");
		hc.debugCall("Announcement 1", announcements.get(0), false);
		
		random = getConfig().getBoolean("random");
		hc.debugCall("Random announcement order", random, false);
		
		interval = getConfig().getInt("interval");
		if(interval == -1) {
			interval = 5;
		}
		
		prefix = getConfig().getString("prefix").replaceAll("&([0-9]|[a-fA-F])", "\u00A7$1");
		hc.debugCall("Announcement prefix", prefix, false);
		if(prefix == null) {
			prefix = "" + ChatColor.LIGHT_PURPLE + "[Announcement] ";
		}
		
		readAndSetDebug();
		
		hc.debugCall("Registering commands", null, false);
		this.getCommand("aschan").setExecutor(new command());
	}
	
	void readAndSetDebug() {
		debug = this.getConfig().getInt("debug");
		if (debug > 2) {
			this.getConfig().set("debug", 2);
			debug = 2;
		} else if (debug < 0) {
			this.getConfig().set("debug", 0);
			debug = 0;
		}
	}
	
	private boolean setAntEnabled() {
		try {
			Api.canHearMe();
			//antInt = true;
			return true;
		} catch (Exception ignore) {
			//antInt = false;
			return false;
		}
	}
	
	void broadcast(int num) {
		broadcast(announcements.get(num));
	}
	
	void broadcast(final String announcement) {
		if(hcs.getNumberOnline() == 0) {
			return;
		}
		
		Bukkit.broadcastMessage(prefix + announcement.replaceAll("&([0-9]|[a-fA-F])", "\u00A7$1"));
		
		if(antInt) {
			new Thread( new Runnable() {
				public void run() {
					hc.debugCall("Translation language", language, true);
					Api.broadcastTranslation(prefix, announcement.replaceAll("&([0-9]|[a-fA-F])", ""), "Aschan", language);
				}
			}).start();
		}
	}
	
	void cancelLoop() {
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	void broadcastLoop(int interval) {
		cancelLoop();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,new Runnable() {
			public void run() {
				final String message = announcements.get(number);
				hc.debugCall("Announcement", message, true);
				broadcast(message);
				
				if(random == true) {
					Random randomN = new Random();
					number = Math.abs(randomN.nextInt() % announcements.size());
				} else {
					if(number >= (announcements.size() - 1)) {
						number = 0;
					} else {
						number++;
					}
				}
			}
		}, 0L, interval * (20L * 60));
	}
	
	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) {
		hc.debugCall("Player joined", event.getPlayer().getName(), true);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				
				if(hc.getNumberOnline() == 0) {
					return;
				}
				
				broadcastLoop(interval);
			}
		}, 200L);
	}
	
	void onPlayerQuit(PlayerQuitEvent event) {
		hc.debugCall("Player disconnected", event.getPlayer().getName(), true);
		Bukkit.getScheduler().cancelTasks(this);
		log.info("Stopped broadcasting as no players are online");
	}
	
	boolean isDebugEnabled() {
		if (debug > 0) {
			return true;
		}
		return false;
	}

	boolean isAdvancedDebugEnabled() {
		if (debug > 1) {
			return true;
		}
		return false;
	}
}

