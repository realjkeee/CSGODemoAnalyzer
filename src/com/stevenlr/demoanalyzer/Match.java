package com.stevenlr.demoanalyzer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Match {
	
	private Map<Integer, Player> players;
	private int currentRound;
	private int[] winTeam;
	
	public Match() {
		currentRound = -1;
		players = new HashMap<Integer, Player>();
		winTeam = new int[30];
		
		for(int i = 0; i < 30; i++) {
			winTeam[i] = -1;
		}
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
			if(((Player) plist[i]).getTeam() == team
			   && !((Player) plist[i]).getRound(currentRound).getDeath())
				alivePlayers++;
		}
		
		return alivePlayers;
	}
	
	public Collection<Player> getPlayers() {
		return players.values();
	}

	public void setWinTeam(int winTeam) {
		this.winTeam[currentRound] = winTeam;
	}

	public int getRoundWinTeam(int i) {
		return winTeam[i];
	}
}
