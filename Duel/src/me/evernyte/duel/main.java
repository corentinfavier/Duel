package me.evernyte.duel;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public class main extends JavaPlugin implements Listener{
	Duel myDuels;

	@Override
	public void onEnable() {
		myDuels = new Duel(this);
		Bukkit.getPluginManager().registerEvents(this, this);
	}

    Map<String, Long> lastDuel = new HashMap<String, Long>();

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		OfflinePlayer playeroff = e.getPlayer();
		Player playerOff = playeroff.getPlayer();
		String playerOffName = playeroff.getPlayer().getName();
		if(myDuels.activeDuels.containsKey(playerOffName)) {
			Bukkit.broadcastMessage("in activeDuels");
			String playerOnName = myDuels.activeDuels.get(playerOffName);
			Player playerOn = Bukkit.getServer().getPlayer(playerOnName);
			myDuels.activeDuels.remove(playerOffName);
			myDuels.activeDuels.remove(playerOnName);
			playerOn.sendMessage(ChatColor.YELLOW + "You won the duel against " + ChatColor.GOLD + playerOffName);
			myDuels.teleportPlayersSpawn(playerOn, playerOn);
		}
		if(myDuels.activeBets.containsKey(playerOffName)) {
			Bukkit.broadcastMessage("in activeBets");
			String playerOnName = myDuels.activeDuels.get(playerOffName);
			Player playerOn = Bukkit.getServer().getPlayer(playerOnName);
			int duelBet;
			try {
				myDuels.checkBets(playerOff.getName());
				duelBet = myDuels.checkBets(playerOff.getName());
			}catch(NullPointerException ex) {
				duelBet = 0;
			}
			
			String wotsBet = myDuels.getWhatIsBet(playerOff.getName());
			if(wotsBet.equalsIgnoreCase("money")) {
				myDuels.removeMoney(playerOff.getName(),duelBet);
				myDuels.addMoney(playerOnName, duelBet);
			}
			else if(wotsBet.equalsIgnoreCase("tokens")) {
				myDuels.removeTokens(playerOff.getName(), duelBet);
    			myDuels.addTokens(playerOnName, duelBet);
			}
			
			playerOn.sendMessage(ChatColor.YELLOW + "You won the duel against " + ChatColor.GOLD + playerOff.getName());
        	playerOff.sendMessage(ChatColor.YELLOW + "You lost the duel against " + ChatColor.GOLD + playerOn);
        	if(wotsBet.equalsIgnoreCase("money")) {
    			playerOn.sendMessage(ChatColor.YELLOW + "You received " + ChatColor.GOLD + "$" + duelBet);
    			playerOff.sendMessage(ChatColor.YELLOW + "You lost " + ChatColor.GOLD + "$" + duelBet);
        	}
    		else {
    			playerOn.sendMessage(ChatColor.YELLOW + "You received " + ChatColor.GOLD + duelBet + " " + wotsBet);
    			playerOff.sendMessage(ChatColor.YELLOW + "You lost " + ChatColor.GOLD + duelBet + " " + wotsBet);
    		}
        	
			myDuels.activeBets.remove(playerOffName);
			myDuels.activeBets.remove(playerOnName);
			myDuels.teleportPlayersSpawn(playerOn, playerOn);
			myDuels.clearDuelInventory(playerOff, playerOn, playerOffName);
        	myDuels.teleportPlayersSpawn(playerOff, playerOn);
        	myDuels.healPlayers(playerOff,  playerOn);
		}
		//Bukkit.broadcastMessage("PlayerQuitEvent active");
		//Bukkit.broadcastMessage(playerOffName + " has logged off.");
		
	}

	@EventHandler
	public void killPlayer(EntityDeathEvent e) {
		
	    Entity deadEntity = e.getEntity();
	    Entity killer = e.getEntity().getKiller();
	    try {
		    		Player loser = (Player) deadEntity;
			        if(myDuels.activeDuels.get(loser.getName()) != null) {
			        	String opponent = myDuels.getOpponents(loser.getName());
				        Player winner = Bukkit.getServer().getPlayer(opponent);
			        		//Loser is always key
			        	int duelBet;	
				        try {
				        	myDuels.checkBets(loser.getName());
			        		duelBet = myDuels.checkBets(loser.getName());
				        }catch(NullPointerException ex) {
			        		duelBet = 0;
				        }

			        		String wotsBet = myDuels.getWhatIsBet(loser.getName());

				        		if(wotsBet.equalsIgnoreCase("money")) {
				        			myDuels.removeMoney(loser.getName(), duelBet);
				        			myDuels.addMoney(opponent, duelBet);

				        		}
				        		else if(wotsBet.equalsIgnoreCase("tokens")){
				        			myDuels.removeTokens(loser.getName(), duelBet);
				        			myDuels.addTokens(opponent, duelBet);
				        		}
				        	
				        	winner.sendMessage(ChatColor.YELLOW + "You won the duel against " + ChatColor.GOLD + loser.getName());
				        	loser.sendMessage(ChatColor.YELLOW + "You lost the duel against " + ChatColor.GOLD + opponent);
				        	if(duelBet>0) {
				        		if(wotsBet.equalsIgnoreCase("money")) {
				        			winner.sendMessage(ChatColor.YELLOW + "You received " + ChatColor.GOLD + "$" + duelBet);
				        			loser.sendMessage(ChatColor.YELLOW + "You lost " + ChatColor.GOLD + "$" + duelBet);
				        	}
				        		else {
				        			winner.sendMessage(ChatColor.YELLOW + "You received " + ChatColor.GOLD + duelBet + " " + wotsBet);
				        			loser.sendMessage(ChatColor.YELLOW + "You lost " + ChatColor.GOLD + duelBet + " " + wotsBet);
				        		}
				        	}
				        	myDuels.clearDuelInventory(loser, winner, opponent);
				        	myDuels.teleportPlayersSpawn(loser, winner);
				        	myDuels.healPlayers(loser,  winner);
				        	myDuels.teleportPlayersSpawn(winner, loser);
			        }
	    }catch(ClassCastException ex){
	    	
	    }

	}
	
	
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String command = event.getMessage(); 
		String[] array = command.split(" ");
		Player player = event.getPlayer();
		String playerIGN = player.getName();
		if(array.length == 2) {
			if(array[0].equalsIgnoreCase("/duel") && array[1].equalsIgnoreCase(playerIGN)) {
				event.setCancelled(true);
		        player.sendMessage(ChatColor.RED + "You cannot duel yourself.");
		    }
		}
		if(array[0].contains("/")) {
			if(myDuels.getOpponents(playerIGN) != null) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.DARK_RED + "You cannot type commands while in a duel.");
			}
		}
		if(command.contains("/duel")) {
			try {
				Player player1 = Bukkit.getServer().getPlayer(array[1]);
				if(myDuels.mistake.contains(playerIGN) || array[1].equalsIgnoreCase(playerIGN) || myDuels.disabled.contains(playerIGN) || myDuels.disabled.contains(array[1]) ||array[1].equalsIgnoreCase("accept") || array[1].equalsIgnoreCase("deny") || array[1].equalsIgnoreCase("enable") || array[1].equalsIgnoreCase("disable") || array[1].equalsIgnoreCase("help")) {
						myDuels.mistake.remove(playerIGN);
				}
				else {
		            if(this.lastDuel.containsKey(playerIGN)) {
		                long lastMessage = this.lastDuel.get(playerIGN);
		                long currentMessage = System.currentTimeMillis();
		                long difference = Math.round((currentMessage - lastMessage)/1000);    
		                long thirty = 30 - difference;
		                if(difference < 30) {
		                    player.sendMessage(ChatColor.RED + "Please wait " + thirty + " more seconds before dueling someone again.");
		                    event.setCancelled(true);
		                }
		                else {
		                    lastDuel.put(playerIGN, System.currentTimeMillis());
		                }
		            
					}
		            else {
		                lastDuel.put(playerIGN, System.currentTimeMillis());
		            }
				}
			}catch(NullPointerException ex) {
				
			}
			
        }
	}
}

