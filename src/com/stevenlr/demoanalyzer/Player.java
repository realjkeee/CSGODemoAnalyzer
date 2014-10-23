package com.stevenlr.demoanalyzer;

public class Player {
	
	@SuppressWarnings("unused")
	private int id;
	private String name;
	private PlayerRound[] rounds;
	private Match match;
	private int team;
	
	public Player(Match match, int id, String name, int team) {
		this.id = id;
		this.name = name;
		this.team = team;
		this.match = match;
		
		rounds = new PlayerRound[30];
		
		for(int i = 0; i < 30; i++) {
			rounds[i] = new PlayerRound();
		}
	}

	public void registerKill(int round, boolean abnormalWeapon) {
		boolean isClutch = (match.getAlivePlayers(team) == 1);
		
		rounds[round].registerKill(abnormalWeapon, isClutch);
	}

	public void registerDeath(int round) {
		rounds[round].registerDeath();
	}

	public int getTeam() {
		return team;
	}
	
	public String getName() {
		return name;
	}
	
	public PlayerRound getRound(int id) {
		return rounds[id];
	}
}
