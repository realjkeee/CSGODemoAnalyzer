package com.stevenlr.demoanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.stevenlr.demoanalyzer.util.Pair;

public class DemoAnalyzer {
	
	private Match match;
	
	public static void main(String args[]) {
		new DemoAnalyzer("info.txt");
	}
	
	public DemoAnalyzer(String filename) {
		BufferedReader br;
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		match = new Match();
		
		parsePlayersInfos(br);
		skipToNextRound(br);
		match.nextRound();
		parseMatch(br);
	}

	private void parsePlayersInfos(BufferedReader br) {
		String line;
		
		try {
			while((line = br.readLine()) != null) {
				if(line.equalsIgnoreCase("begin_new_match"))
					return;
				
				if(line.equalsIgnoreCase("player_team"))
					parsePlayerTeam(br);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parsePlayerTeam(BufferedReader br) {
		String line;
		String name = "";
		int id = -1;
		int team = -1;
		boolean isBot = true;
		
		try {
			while((line = br.readLine()) != null) {
				if(line.equalsIgnoreCase("}"))
					break;
				
				line = line.trim();
				
				if(line.startsWith("isbot")) {
					isBot = line.endsWith("1");
					continue;
				}
				if(line.startsWith("team")) {
					team = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
				}
				else if(line.startsWith("userid")) {
					String infos = line.substring(7).trim();
					Pair<Integer, String> user = parsePlayerIdName(infos);
					
					if(user == null)
						continue;
					
					id = user.getFirst();
					name = user.getSecond();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!isBot) {
			match.setPlayer(id, name, team);
		}
	}
	
	private void skipToNextRound(BufferedReader br) {
		String line;
		
		try {
			while((line = br.readLine()) != null) {
				if(line.equalsIgnoreCase("round_start"))
					return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Pair<Integer, String> parsePlayerIdName(String line) {
		int parenthesisStart = line.length() - 1;
		int id;
		String name;
		
		while(line.charAt(parenthesisStart) != '(' && parenthesisStart > 0)
			parenthesisStart--;
		
		if(parenthesisStart == 0)
			return null;
		
		String idString = line.substring(parenthesisStart + 1, line.length() - 1);
		
		id = Integer.parseInt(idString.substring(idString.indexOf(':') + 1));
		name = line.substring(0, parenthesisStart).trim();
		
		return new Pair<Integer, String>(id, name);
	}
	
	private void parseMatch(BufferedReader br) {
		String line;
		
		try {
			while((line = br.readLine()) != null) {
				if(line.equalsIgnoreCase("round_start")) {
					match.nextRound();
				}
				else if(line.equalsIgnoreCase("round_end")) {
					parseRoundEnd(br);
				}
				else if(line.equalsIgnoreCase("player_death")) {
					parsePlayerDeath(br);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parseRoundEnd(BufferedReader br) {
		String line;
		int winTeam = -1;
		
		try {
			while((line = br.readLine()) != null) {
				if(line.equalsIgnoreCase("}"))
					break;
				
				line = line.trim();
				
				if(line.startsWith("winner")) {
					winTeam = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(winTeam != -1) {
			match.setWinTeam(winTeam);
		}
	}

	private void parsePlayerDeath(BufferedReader br) {
		String line;
		Pair<Integer, String> killer = null;
		Pair<Integer, String> victim = null;
		boolean abnormalWeapon = false;
		
		try {
			while((line = br.readLine()) != null) {
				if(line.equalsIgnoreCase("}"))
					break;
				
				line = line.trim();
				
				if(line.startsWith("userid")) {
					victim = parsePlayerIdName(line.substring(7).trim());
				}
				else if(line.startsWith("attacker")) {
					killer = parsePlayerIdName(line.substring(9).trim());
				}
				else if(line.startsWith("weapon:")) {
					String weapon = line.substring(7).trim();
					
					if(weapon.equalsIgnoreCase("hegrenade")
							|| weapon.equalsIgnoreCase("knife")) {
						abnormalWeapon = true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(killer != null && victim != null) {
			match.registerKill(killer.getFirst(), victim.getFirst(), abnormalWeapon);
		}
	}
}
