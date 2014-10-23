package com.stevenlr.demoanalyzer;

public class PlayerRound {
	
	private int kills = 0;
	private boolean death = false;
	private int killsClutch = 0;
	private int killsAbnormal = 0;
	
	public PlayerRound() {
		kills = 0;
		death = false;
		killsClutch = 0;
		killsAbnormal = 0;
	}
	
	public void registerKill(boolean abnormalWeapon, boolean isClutch) {
		kills++;
		
		if(abnormalWeapon)
			killsAbnormal++;
		
		if(isClutch)
			killsClutch++;
	}

	public void registerDeath() {
		death = true;
	}
	
	public boolean isAlive() {
		return !death;
	}
	
	public int getKills() {
		return kills;
	}
	
	public int getClutchKills() {
		return killsClutch;
	}
	
	public int getAbnormalKills() {
		return killsAbnormal;
	}
	
	public boolean getDeath() {
		return death;
	}
}
