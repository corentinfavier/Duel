package me.evernyte.duel;

import java.util.UUID;


public class PlayerDS {
	
	private UUID playerID;
	private float balance;
	
	public PlayerDS(UUID playerID) {
		this.playerID = playerID;
		balance = 0;
	}
	
	public void add(float extraAmount) {
		balance += extraAmount;
	}
	
}
