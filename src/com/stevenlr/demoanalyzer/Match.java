package com.stevenlr.demoanalyzer;

import java.util.HashMap;
import java.util.Map;

public class Match {
	
	private Map<Integer, Player> players;
	private int currentRound;
	
	public Match() {
		currentRound = -1;
		players = new HashMap<Integer, Player>();
	}
	
	public void setPlayer(int id, String name, int team) {
		players.put(id, new Player(this, id, name, team));
	}
	
	public void nextRound() {
		currentRound++;
		
		if(currentRound >= 30) {
			System.err.println("Number of rounds exceeded 30");
			System.exit(1);
		}
	}

	public void registerKill(int killer, int victim, boolean abnormalWeapon) {
		Player pkiller = players.get(killer);
		Player pvictim = players.get(victim);
		
		if(pkiller == null || pvictim == null)
			return;
		
		pkiller.registerKill(currentRound, abnormalWeapon);
		pvictim.registerDeath(currentRound);
	}

	public int getAlivePlayers(int team) {
		int alivePlayers = 0;
		Object[] plist = players.values().toArray();
		
		for(int i = 0; i < plist.length; i++) {
			if(((Player) plist[i]).getTeam() == team)
				alivePlayers++;
		}
		
		return alivePlayers;
	}
}
