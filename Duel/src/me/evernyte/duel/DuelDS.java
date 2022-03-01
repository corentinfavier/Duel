package me.evernyte.duel;

import java.util.UUID;

import org.bukkit.entity.Player;

public class DuelDS {
	
	private Player challenger;
	private Player challenged;
	private UUID challengerID;
	private UUID challengedID;
	private int bet = 0;
	private String currency = null;
	
	public DuelDS(Player challenger, Player challenged, int bet, String currency) {
		this.challenger = challenger;
		this.challenged = challenged;
		this.challengerID = challenger.getUniqueId();
		this.challengedID = challenged.getUniqueId();
		this.bet = bet;
		this.currency = currency;
	}
	
	public DuelDS(Player challenger, Player challenged) {
		this.challenger = challenger;
		this.challenged = challenged;
		this.challengerID = challenger.getUniqueId();
		this.challengedID = challenged.getUniqueId();
	}
	
	public String getDuelData() {
		return getOpponents() + "\n" + getBets();
	}
	
	public String getOpponents() {
		return "Challenger: " + getChallenger().getName() + "\nChallenged: " + getChallenged().getName();
	}
	
	public Player getChallenger() {
		return challenger;
	}
	
	public Player getChallenged() {
		return challenged;
	}
	
	public String getBets() {
		if(currency == null)
			return "Bet: None";
		else
			return "Bet: " + bet + "\nCurrency: " + currency;
	}

}
