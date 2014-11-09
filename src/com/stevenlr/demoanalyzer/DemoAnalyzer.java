package com.stevenlr.demoanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.stevenlr.demoanalyzer.util.Pair;

public class DemoAnalyzer {
	
	private Match match;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	public static String installDir = "C:\\Program Files (x86)\\Steam\\SteamApps\\common\\Counter-Strike Global Offensive";
	public static String replaysPath;
	public static String binPath;
	
	public static void main(String args[]) {
		replaysPath = installDir + File.separator + "csgo" + File.separator + "replays";
		binPath = installDir + File.separator + "bin";
		
		if (!(new File("matchs").isDirectory())) {
			new File("matchs").mkdir();
		}
		
		File directory = new File(replaysPath);
		
		List<File> files = Arrays.asList(directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".dem");
			}
		}));
		
		Collections.sort(files, new Comparator<File>() {
			@Override
			public int compare(File arg0, File arg1) {
				return new Long(arg0.lastModified()).compareTo(arg1.lastModified());
			}
		});
		
		for (int i = 0; i < files.size(); ++i) {
			File f = files.get(i);
			new DemoAnalyzer(files.get(i));
			System.out.println((i + 1) + "/" + files.size());
		}
	}
	
	public DemoAnalyzer(File f) {
		String matchId = f.getName().substring(0, f.getName().length() - 4);
		String date = makeDate(f);
		
		Runtime r = Runtime.getRuntime();
		BufferedReader br = null;
		Process p = null;
		
		try {
			p = r.exec("\"" + binPath + File.separator + "demoinfogo\" -gameevents -nofootsteps \"" + replaysPath + File.separator + f.getName() + "\"");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (br == null) {
			System.err.println("Error reading demo file " + f.getName());
			System.exit(1);
		}
		
		BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		match = new Match(date, matchId);
		
		parsePlayersInfos(br);
		skipToNextRound(br);
		match.nextRound();
		parseMatch(br);
		
		try {
			String line;
			while ((line = bre.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (p.exitValue() == 1) {
			OutputJSON out = new OutputJSON(match);
			out.write("matchs" + File.separator + match.getId() + ".js");
			System.out.print("[ OK ] ");
		} else {
			System.out.print("[FAIL] ");
		}
		
		p.destroy();
	}
	
	private String makeDate(File f) {
		return dateFormat.format(new Date(f.lastModified()));
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
