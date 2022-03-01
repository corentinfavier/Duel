package me.evernyte.duel;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.*;

public class Duel implements CommandExecutor {
	
	@SuppressWarnings("unused")
	private main plugin;

	public Duel(main plugin) {
		this.plugin = plugin;
		plugin.getCommand("duel").setExecutor(this);
		plugin.getCommand("money").setExecutor(this);
		plugin.getCommand("token").setExecutor(this);
	}
	
	
	public HashMap<String, String> activeDuels = new HashMap<String, String>(); //Stores duels currently happening
	public HashMap<String, Integer> activeBets = new HashMap<String, Integer>(); //Stores bets of duels currently happening
	public HashMap<String, Integer> possibleBets = new HashMap<String, Integer>(); //Stores temporary bets waiting for approval by player2
	public HashMap<String, String> whatsBet = new HashMap<String, String>(); //Stores if the bet was money or tokens
	ArrayList<String> challenger = new ArrayList<String>(); //Stores temporary challenger waiting for approval by player2
	public HashMap<String, Long> challengerTimer = new HashMap<String, Long>();
	ArrayList<String> challenged = new ArrayList<String>(); //Stores temporary challenged waiting for approval to become the challenged by player2
	public HashMap<String, Integer> money = new HashMap<String, Integer>(); //Edit for pramsing : this is probably going to need to be changed
	public HashMap<String, Integer> tokens = new HashMap<String, Integer>(); //Edit for pramsing : this is probably going to need to be changed
	ArrayList<String> disabled = new ArrayList<String>(); //For people who disabled dueling
	ArrayList<String> mistake = new ArrayList<String>(); //Prevents people from having 30 seconds CD if they mess their cmd up
	
	//For setting up the place for the winner to be teleported on win
	Location spawn = Bukkit.getWorld("world").getSpawnLocation();
	//For reference, the place where the players will be teleported needs to be put in by pramsing, as I do not know where to teleport them on vexed 
	//The place where the players will fight needs to be setup and put in after player2 accept in order to actually teleport them
	Location fight = Bukkit.getWorld("world").getSpawnLocation();
	Location arena = fight.add(-5, 5, -5);
	//Used in main to teleport players back to spawn
	
	
	public void healPlayers(Player player1, Player player2) {
		player1.setHealth(20.0);
		player1.setFoodLevel(20);
		player1.setFoodLevel(20);
		player2.setHealth(20.0);
	}
	
	public void teleportPlayersSpawn(Player player1, Player player2) {
		player1.teleport(spawn);
		player2.teleport(spawn);
	}
	
	


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//When player enters a cmd
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		Player player = (Player) sender;
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
//Duel Command
		if (label.equalsIgnoreCase("duel")) {
			
			if(args.length==0) {
				sender.sendMessage(ChatColor.YELLOW + "The format for duels is " + ChatColor.BOLD + "" + ChatColor.GOLD + "/duel [ign] \n" + ChatColor.RESET + ChatColor.YELLOW + "Check /duel help for more information.");
				return true;
			}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
//Duel Help
			else if(args[0].equals("help")) {
				sender.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Duel Commands: \n" + ChatColor.RESET + ChatColor.YELLOW + "/duel [ign] \n/duel [ign] [amount] [type] \n/duel enable \n/duel disable \n/duel accept [ign] \n/duel deny [ign]");
				return true;
			}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
//Duel disable
			else if(args[0].equals("disable")) {
				if(disabled.contains(sender.getName())) {
					sender.sendMessage(ChatColor.YELLOW + "You already have duel requests disabled.");
				}
				else {
				disabled.add(sender.getName());
				sender.sendMessage(ChatColor.YELLOW + "You have disabled incoming duel requests");
				//Bukkit.broadcastMessage("" + disabled);
				}
			}	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////						
//Duel enable
			else if(args[0].equals("enable")) {
				if(disabled.contains(sender.getName())) {
					sender.sendMessage(ChatColor.YELLOW + "You have enabled incoming duel requests.");
					disabled.remove(sender.getName());
				}
				else {
					sender.sendMessage(ChatColor.YELLOW + "You already have duel requests enabled.");
				}
			}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
//The person you challenge has duels disabled
			else if(disabled.contains(args[0])) {
					sender.sendMessage(ChatColor.YELLOW + "The player you are trying to duel has duels disabled.");
					return true;
		}
//You have duels disabled
			else if(disabled.contains(sender.getName())) {
					sender.sendMessage(ChatColor.YELLOW + "You have duels disabled, so you cannot duel someone else.");
					return true;
		}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Duel Accept
			else {
			if(args[0].equals("accept")) {
				if(args.length==1) {
					sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "ERROR! " + ChatColor.RESET + ChatColor.DARK_RED + "Enter /duel accept [ign] to accept a duel!");
					return true;
				}
				if(args.length>2) {
					sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "ERROR! " + ChatColor.RESET + ChatColor.DARK_RED + "Only enter /duel accept [ign], and no extra words!");
					return true;
				}
				if(disabled.contains(args[1])) {
					sender.sendMessage(ChatColor.YELLOW + "The player you are trying to duel has duels disabled.");
					return true;
				}
				 if(disabled.contains(sender.getName())) {
						sender.sendMessage(ChatColor.YELLOW + "You have duels disabled, so you cannot duel someone else.");
						return true;
			}
				
				try{
					Player player1 = Bukkit.getPlayer(args[1]);
					if(player1.hasPlayedBefore()) {
						if(player1.isOnline()) {
							if(activeDuels.get(player1.getName()) != null || getOpponents(player1.getName()) != null) { //Prevents from accepting a duel while the other is already in another duel
								sender.sendMessage(ChatColor.GOLD + player1.getName() + ChatColor.YELLOW + " is currently inside a duel, so you cannot duel them at the moment.");
								sender.sendMessage(ChatColor.GOLD + player1.getName() + ChatColor.YELLOW + "'s duel request has been removed.");
								challenger.remove(player1.getName());
								challenged.remove(sender.getName());
								return true;
							}
							if(challenger.contains(player1.getName())) {
								for(int i = 0; i<challenger.size(); i++) {  //Checks if player is actually being challenged by the person he entered in the 2nd argument
									if(challenger.get(i).equalsIgnoreCase(player1.getName())) {  //Checks if player is actually being challenged by the person he entered in the 2nd argument
										if(challenged.get(i).equalsIgnoreCase(sender.getName())) {  //Checks if player is actually being challenged by the person he entered in the 2nd argument
											
											//Checks for empty inventories
											Inventory inv1 = player.getInventory();
											Inventory inv2 = player1.getInventory();
											int invSize2 = inv2.getSize();
											int testInv2 = 0;
											int invSize1 = inv1.getSize();
											int testInv1 = 0;
											for (int j = 0; j < invSize1; j++) {
												if (inv1.getItem(j) != null) {
													testInv1++;
												}
											}
											for(int k = 0; k < invSize2; k++) {
												if(inv2.getItem(k) != null) {
													testInv2++;
												}
											}
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
				//Checks for empty armor slots
											if(player.getInventory().getHelmet() != null) {
												testInv1 = testInv1 + 1;
											}
											if(player.getInventory().getChestplate() != null) {
												testInv1 = testInv1 + 1;
											}
											if(player.getInventory().getLeggings() != null) {
												testInv1 = testInv1 + 1;
											}
											if(player.getInventory().getBoots() != null) {
												testInv1 = testInv1 + 1;
											}
											if(player1.getInventory().getHelmet() != null) {
												testInv2 = testInv2 + 1;
											}
											if(player1.getInventory().getChestplate() != null) {
												testInv2 = testInv2 + 1;
											}
											if(player1.getInventory().getLeggings() != null) {
												testInv2 = testInv2 + 1;
											}
											if(player1.getInventory().getBoots() != null) {
												testInv2 = testInv2 + 1;
											}
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////								
				//If inventory is not empty, duel does not start
											if(testInv1>0) {
												player.sendMessage(ChatColor.RED + "You cannot have items in your inventory!");		
												mistake.add(player.getName());
												return true;
											}
											if(testInv2>0) {
												player1.sendMessage(ChatColor.RED + "You cannot have items in your inventory!");
												player.sendMessage(ChatColor.RED + player1.getName() + " has items in his inventory, so the duel cannot be accepted.");
												mistake.add(player.getName());
												return true;
											}
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//Message is sent to the duelers that the duel is about to begin
												if(getWhatIsBet(player1.getName()).equals("money") && possibleBets.get(player1.getName())>money.get(player.getName())){
													sender.sendMessage(ChatColor.YELLOW + "You don't have enough money! The duel has been voided."); //If the challenged is too poor (money)
													challengerTimer.remove(i);
													challenger.remove(i);
													challenged.remove(i);
													possibleBets.remove(player1.getName());
													return true;
												}
												else if(getWhatIsBet(player1.getName()).equals("tokens") && possibleBets.get(player1.getName())>tokens.get(player.getName())) {
													sender.sendMessage(ChatColor.YELLOW + "You don't have enough tokens! The duel has been voided."); //If the challenged is too poor (tokens)
													challengerTimer.remove(i);
													challenger.remove(i);
													challenged.remove(i);
													possibleBets.remove(player1.getName());
													return true;
												}
												sender.sendMessage(ChatColor.YELLOW + "You accepted "+ ChatColor.GOLD + player1.getName() + ChatColor.YELLOW + "'s duel."); //Tells the dueled that he has accepted the duel
												player1.sendMessage(ChatColor.GOLD + sender.getName() + ChatColor.YELLOW + " accepted your duel."); //Tells the dueler that his duel has been accepted
												activeDuels.put(player1.getName(), player.getName()); //Adds duelers to activeDuels HashMap for main to use (double used to make sure that the loser is findable (can explain if needed))
												activeDuels.put(player.getName(), player1.getName()); //Adds duelers to activeDuels HashMap for main to use (double used to make sure that the loser is findable (can explain if needed))
												if(tokens.get(player.getName()) == null) {
													tokens.put(player.getName(), 0); //Probably removable : in case the players do not have any tokens, prevents null response 
												}
												if(money.get(player.getName()) == null) {
													money.put(player.getName(), 0); //Probably removable : in case the players do not have any money, prevents null response 
												}
												if(tokens.get(player1.getName()) == null) {
													tokens.put(player1.getName(), 0); //Probably removable : in case the players do not have any tokens, prevents null response 
												}
												if(money.get(player1.getName()) == null) {
													money.put(player1.getName(), 0); //Probably removable : in case the players do not have any money, prevents null response 
												}
												activeBets.put(player1.getName(), possibleBets.get(player1.getName())); //Adds the bet they made to activeBets HashMaps (double used to make sure that the loser is findable (can explain if needed))
												activeBets.put(player.getName(), possibleBets.get(player1.getName())); //Adds the bet they made to activeBets HashMaps (double used to make sure that the loser is findable (can explain if needed))
												whatIsBet(player.getName(), getWhatIsBet(player1.getName())); //Syncs whether the bet is money or tokens to the player who accepted (was already added for the challenger)
												possibleBets.remove(player1.getName()); //Removes the possible bet as the bet has been agreed upon
									            challengerTimer.remove(i);
												challenger.remove(i); //removes them from the list to prevent overloading the list
												challenged.remove(i); //removes them from the list to prevent overloading the list
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////								
					//Teleports both players to fighting arena
												teleportPlayersArena(player, player1);
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////							
					//Give both players the kit
												setInventory(player);
												setInventory(player1);
												healPlayers(player, player1);
											return true;
									}
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//If the person has not been challenged by person entered
									}
								}
							}
							else {
								sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR! "+ ChatColor.DARK_RED + "This player has not challenged you yet!"); 	
								mistake.add(player.getName());
							}
						}
						
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//If original challenger is offline
						else {
						sender.sendMessage(ChatColor.DARK_RED + "Player is currently offline!");
						challenger.remove(player1.getName());
						challenged.remove(sender.getName());
						mistake.add(player.getName());
						}
					
					}
					else {
						sender.sendMessage(ChatColor.DARK_RED + "This player has never joined before!");
						mistake.add(player.getName());
					}
				}catch(NullPointerException ex) {
					player.sendMessage(ChatColor.DARK_RED + "This player is not online.");
					mistake.add(player.getName());
				}

				return true;
			}	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Duel Deny
				
			if(args[0].equals("deny")) {
				if(args.length == 1) {
					sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "ERROR! " + ChatColor.RESET + ChatColor.DARK_RED + "Enter /duel deny [ign] to deny a duel!");
					mistake.add(player.getName());
					return true;
				}
				if(args.length>2) {
					sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "ERROR! " + ChatColor.RESET + ChatColor.DARK_RED + "Only enter /duel deny [ign], and no extra words!");
					mistake.add(player.getName());
					return true;
				}
				try {
					Player player1 = Bukkit.getPlayer(args[1]);	
					if(challenger.contains(player1.getName())) {
						if(!player1.isOnline()) {
							sender.sendMessage(ChatColor.DARK_RED + "Player is not online. The duel has been denied.");
							challenger.remove(player1.getName());
							challenged.remove(sender.getName());
						return true;
					}
					else {
						sender.sendMessage(ChatColor.YELLOW + "You denied " + ChatColor.GOLD + player1.getName() + ChatColor.YELLOW + "'s duel.");
						player1.sendMessage(ChatColor.GOLD + sender.getName() + ChatColor.YELLOW + " denied your duel.");
						challenger.remove(player1.getName());
						challenged.remove(sender.getName());
						return true;
					}
					}
					else {
						sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR! "+ ChatColor.DARK_RED + "This player has not challenged you yet!"); 	
						mistake.add(player.getName());
						return true;
					}

				}catch(NullPointerException ex) {
					player.sendMessage(ChatColor.DARK_RED + "This player is not online.");
					mistake.add(player.getName());
				}

			}		
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Check to see if player challenged has played before
			
			try{
				Player player1 = Bukkit.getPlayer(args[0]);
				Inventory inv = player.getInventory();
				int invSize = inv.getSize();
				int testInv = 0;
				for (int i = 0; i < invSize; i++) {
					if (inv.getItem(i) != null) {
						testInv = testInv + 1;
					}
				}
				if(player.getInventory().getHelmet() != null) {
					testInv = testInv + 1;
				}
				if(player.getInventory().getChestplate() != null) {
					testInv = testInv + 1;
				}
				if(player.getInventory().getLeggings() != null) {
					testInv = testInv + 1;
				}
				if(player.getInventory().getBoots() != null) {
					testInv = testInv + 1;
				}
				if(testInv>0) {
					player.sendMessage(ChatColor.RED + "You cannot have items in your inventory!");
					mistake.add(player.getName());
					return true;
				}
				if(args.length==3) {
					try {
						String whatBet = args[2]; //To check if money or tokens are being bet
						int bet = Integer.parseInt(args[1]); //To check how much money/tokens are being bet
						if(whatBet.equalsIgnoreCase("tokens")) { 
							if(tokens.containsKey(player.getName())){
								int originalBal = tokens.get(player.getName());
								if(originalBal>=bet) { //Probably editable : checks to see if player has enough tokens 
									challenged.add(args[0]);
									challenger.add(sender.getName());
									sender.sendMessage(ChatColor.YELLOW + "You have requested a duel with " + ChatColor.GOLD + player1.getName()); //Tells dueler the request has been sent
									sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "The duel is for " + ChatColor.GOLD + "" + ChatColor.BOLD + bet + " Tokens" + ChatColor.YELLOW + ".");
									player1.sendMessage(ChatColor.GOLD + sender.getName() + ChatColor.YELLOW + " has requested a duel with you. Type " + ChatColor.GOLD + "/duel accept " + sender.getName() + ChatColor.YELLOW + " to " + ChatColor.GREEN + "" + ChatColor.BOLD + "ACCEPT" + ChatColor.RESET + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/duel deny " + sender.getName() + ChatColor.YELLOW + " to " + ChatColor.RED + "" + ChatColor.BOLD + "DENY");
									player1.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "The duel is for " + ChatColor.GOLD + "" + ChatColor.BOLD + bet + " Tokens" + ChatColor.YELLOW + ".");
									possibleBets(player.getName(), bet);
									whatIsBet(player.getName(), whatBet);
							}
								else {
									sender.sendMessage(ChatColor.RED + "You do not have enough tokens.");
									mistake.add(player.getName());
									return true;
								}
						}
							else {
								sender.sendMessage(ChatColor.RED + "You do not have enough tokens.");
								mistake.add(player.getName());
								return true;
							}
						}			
						else if(whatBet.equalsIgnoreCase("money")) { 
							if(money.containsKey(player.getName())){
								int originalBal = money.get(player.getName());
								if(originalBal>=bet) { //Probably editable : checks to see if player has enough money 
									challenged.add(args[0]);
									challenger.add(sender.getName());
									sender.sendMessage(ChatColor.YELLOW + "You have requested a duel with " + ChatColor.GOLD + player1.getName()); //Tells dueler the request has been sent
									sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "The duel is for " + ChatColor.GOLD + "" + ChatColor.BOLD + "$" + bet);
									player1.sendMessage(ChatColor.GOLD + sender.getName() + ChatColor.YELLOW + " has requested a duel with you. Type " + ChatColor.GOLD + "/duel accept " + sender.getName() + ChatColor.YELLOW + " to " + ChatColor.GREEN + "" + ChatColor.BOLD + "ACCEPT" + ChatColor.RESET + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/duel deny " + sender.getName() + ChatColor.YELLOW + " to " + ChatColor.RED + "" + ChatColor.BOLD + "DENY");
									player1.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "The duel is for " + ChatColor.GOLD + "" + ChatColor.BOLD + "$" + bet);
									possibleBets(player.getName(), bet);
									whatIsBet(player.getName(), whatBet);
							}
							else {
								sender.sendMessage(ChatColor.RED + "You do not have enough money.");
								mistake.add(player.getName());
							}
						}
							else {
								sender.sendMessage(ChatColor.RED + "You do not have enough money.");
								mistake.add(player.getName());
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR! " + ChatColor.RESET + ChatColor.DARK_RED + "Incorrect currency input. Input money or tokens");
							mistake.add(player.getName());
							return true;
						}
						
					}catch(NumberFormatException ex) {
						sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "ERROR! " + ChatColor.RESET + ChatColor.DARK_RED + "You entered an invalid amount to bet!");
						mistake.add(player.getName());
					}
					return true;
				}
				if(args.length > 3) {
					sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "ERROR! " + ChatColor.RESET + ChatColor.DARK_RED + "Wrong format! Write either /duel [ign] or /duel [ign] [amount] [type]!");
					mistake.add(player.getName());
					return true;
				}
				if(args.length == 2) {
					sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "ERROR! " + ChatColor.RESET + ChatColor.DARK_RED + "Wrong format! Write either /duel [ign] or /duel [ign] [amount] [type]!");
					mistake.add(player.getName());
					return true;
				}
				else if(args.length == 1 || args.length == 3){
					challenged.add(args[0]); //Temporarily adds the challenged to list
					challenger.add(sender.getName()); //Temporarily adds the challenger to list
					challengerTimer.put(sender.getName(), System.currentTimeMillis());
					sender.sendMessage(ChatColor.YELLOW + "You have requested a duel with " + ChatColor.GOLD + player1.getName()); //Tells dueler the request has been sent
					player1.sendMessage(ChatColor.GOLD + sender.getName() + ChatColor.YELLOW + " has requested a duel with you. Type " + ChatColor.GOLD + "/duel accept " + sender.getName() + ChatColor.YELLOW + " to " + ChatColor.GREEN + "" + ChatColor.BOLD + "ACCEPT" + ChatColor.RESET + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/duel deny " + sender.getName() + ChatColor.YELLOW + " to " + ChatColor.RED + "" + ChatColor.BOLD + "DENY");
					possibleBets.remove(player.getName());
					//possibleBets(player.getName(), 0);
					whatIsBet(player.getName(), "");
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		                @Override
		                public void run() {
		                	if(challenger.contains(sender.getName()) || challenged.contains(args[0])) {
			                	challenger.remove(sender.getName());
			                	challenged.remove(args[0]);
			                	sender.sendMessage(ChatColor.YELLOW + "Your duel request to " + ChatColor.GOLD + player1.getName() + ChatColor.YELLOW + " has run out.");
			                	player1.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s duel request to you has run out.");
			                	challengerTimer.remove(sender.getName());
		                	}
		                	else {
		                	}
		                }
		            }, 1200);
					}
			}catch(NullPointerException ex) {
				player.sendMessage(ChatColor.DARK_RED + "This player is not online.");
				mistake.add(player.getName());
			}				
			return true;
		}
		}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
			
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
//My coins and tokens 	


		
		if(label.equalsIgnoreCase("money")) {
		
			//Check money
			if(args[0].equals("bal")) {
			String name = player.getName();
			if(money.containsKey(player.getName())){
			sender.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.GOLD + money.get(name) + ChatColor.YELLOW + " coins.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "You do not have any money!");
			}
		}
		
		
		//Give money
		if(args[0].equals("give")) {	
			int amount = Integer.parseInt(args[1]);
			if(money.containsKey(player.getName())){
				int originalBal = money.get(player.getName());
				int bal = originalBal + amount;
				money.put(player.getName(), bal);
				player.sendMessage(ChatColor.YELLOW + "Your new balance is now: " + ChatColor.GOLD + bal + ChatColor.YELLOW + " coins.");
			}
			else {
				money.put(player.getName(), amount);
				player.sendMessage(ChatColor.YELLOW + "Your new balance is now: " + ChatColor.GOLD + amount + ChatColor.YELLOW + " coins.");
				}
		}
		}
		

		
		//Tokens
		if(label.equalsIgnoreCase("token")) {
			
		//Check tokens
			if(args[0].equals("bal")) {
			String name = player.getName();
			if(tokens.containsKey(player.getName())){
				sender.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.GOLD + tokens.get(name) + ChatColor.YELLOW + " tokens.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "You do not have any tokens!");
			}
		}
		
		
		//Give tokens
		if(args[0].equals("give")) {	
			int amount = Integer.parseInt(args[1]);
			if(tokens.containsKey(player.getName())){
				int originalBal = tokens.get(player.getName());
				int bal = originalBal + amount;
				tokens.put(player.getName(), bal);
				player.sendMessage(ChatColor.YELLOW + "Your new balance is now: " + ChatColor.GOLD + bal + ChatColor.YELLOW + " tokens.");
			}
			else {
				tokens.put(player.getName(), amount);
				player.sendMessage(ChatColor.YELLOW + "Your new balance is now: " + ChatColor.GOLD + amount + ChatColor.YELLOW + " tokens.");
				}
		}
		}
		

		
	return false;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
	//Used later to set players' inventories upon entering the duel
	public boolean setInventory(Player player) {
		ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET, 1);
		ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
		ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
        helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
		ItemStack bow = new ItemStack(Material.BOW);
		ItemStack apples = new ItemStack(Material.GOLDEN_APPLE, 2);
		ItemStack arrows = new ItemStack(Material.ARROW, 16);
		ItemStack food = new ItemStack(Material.COOKED_BEEF, 32);
		player.getInventory().addItem(sword, bow, apples, food, arrows);
		player.getInventory().setHelmet(helmet);
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
		player.getInventory().setBoots(boots);

		return true;
	}
	
	//Clear inventory when duel is lost
	public boolean clearDuelInventory(Player loser, Player winner, String opponent) {
		loser.getInventory().clear();
		loser.getInventory().setHelmet(null);
		loser.getInventory().setChestplate(null);
		loser.getInventory().setLeggings(null);
		loser.getInventory().setBoots(null);
		winner.getInventory().clear();
		winner.getInventory().setHelmet(null);
		winner.getInventory().setChestplate(null);
		winner.getInventory().setLeggings(null);
		winner.getInventory().setBoots(null);
		teleportPlayersSpawn(loser, winner);
    	clearBets(loser.getName());
		clearBets(opponent);
    	removeDuels(opponent, loser.getName());
		removeDuels(loser.getName(), opponent);
		return true;
	}
	
	//Used later to teleport players to fighting arena (I can't make it so you get teleported in different places if you have multiple duels personally)
		public void teleportPlayersArena(Player player1, Player player2) {
			player1.teleport(arena);
			player2.teleport(arena);
		}	
		
		//Edit for pramsing : this is probably going to need to be changed
		public Integer removeMoney(String playerName, int bet) {
			money.replace(playerName, money.get(playerName), money.get(playerName) - bet);
			return money.get(playerName);
		}
		
		//Edit for pramsing : this is probably going to need to be changed
		public Integer removeTokens(String playerName, int bet) {
			tokens.replace(playerName, tokens.get(playerName), tokens.get(playerName) - bet);
			return tokens.get(playerName);
		}
		
		//Edit for pramsing : this is probably going to need to be changed
		public Integer checkMoney(String playerName) {
			return money.get(playerName);
		}
		
		//Edit for pramsing : this is probably going to need to be changed
		public Integer checkTokens(String playerName) {
			return tokens.get(playerName);
		}
		
		//Edit for pramsing : this is probably going to need to be changed
		public Integer addMoney(String playerName, int bet) {
			money.replace(playerName, money.get(playerName), money.get(playerName) + bet);
			return money.get(playerName);
		}
		
		//Edit for pramsing : this is probably going to need to be changed
		public Integer addTokens(String playerName, int bet) {
			tokens.replace(playerName, tokens.get(playerName), tokens.get(playerName) + bet);
			return tokens.get(playerName);
		}
		
		
		//Used in main to get if the guy who just died was in a duel or not
		public void GetActiveDuels(Player name) {
			name.sendMessage(activeDuels + "");
		}
		
		//Used in main to get the active bet of the guy who just died
		public Integer checkBets(String playerName) {
			int bets = activeBets.get(playerName);
			return bets;
		}
		
		//Used later once player2 has agreed to the bet and the bet is definitive
		public void addBets(String playerName, Integer setBet) {
			activeBets.put(playerName, setBet);
		}
		
		//Used in main to remove the bet to clear out activeBets and whatsBet HashMaps
		public void clearBets(String playerName) {
			activeBets.remove(playerName);
			whatsBet.remove(playerName);
		}
		
		//Used near the end to setup a possible bet, which does not impact the activeBets in case the duel is cancelled or denied
		public void possibleBets(String player, Integer possibleBet) {
			possibleBets.put(player, possibleBet);
		}
		
		//Used in main to check how much is bet
		public void whatIsBet(String player, String whatBet) {
			whatsBet.put(player, whatBet);
		}
		
		//Used in main to check if money or tokens were bet
		public String getWhatIsBet(String player) {
			String wotsBet = whatsBet.get(player);
			return wotsBet;
		}
		
		
		//Used in main to check who the loser's opponent is
		public String getOpponents(String name) {
			String opponent = activeDuels.get(name);
			return opponent;
					
		}
		
		//Used in main to clear out activeDuels HashMap
		public void removeDuels(String name1, String name2) {
			activeDuels.remove(name1, name2);
		}

}