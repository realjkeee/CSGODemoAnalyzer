package com.stevenlr.demoanalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

public class OutputJSON {
	
	private Match match;
	private List<Player> players;
	
	public OutputJSON(Match match) {
		this.match = match;
		
		Collection<Player> playersCollection = match.getPlayers();
		players = new ArrayList<Player>(playersCollection);
		
		Collections.sort(players, new Comparator<Player>() {
			@Override
			public int compare(Player o1, Player o2) {
				return o1.getTeam() - o2.getTeam();
			}
		});
	}
	
	public void write(String outputFilename) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("register_match_data({\n");
		builder.append("'id': '" + match.getId() + "', ");
		builder.append("'date': '" + match.getDate() + "', ");
		makePlayersElement(builder);
		builder.append("});");
		
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(outputFilename)));
			osw.write(builder.toString());
			osw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void makePlayersElement(StringBuilder b) {
		b.append("[");
		
		for(int i = 0; i < players.size(); i++) {	
			b.append("{");
			
			Player p = players.get(i);
			
			try {
				byte[] name = p.getName().getBytes("UTF-8");
				b.append("'name': '" + DatatypeConverter.printBase64Binary(name) + "', ");
				b.append("'team': '" + p.getTeam() + "',\n");
				b.append("'rounds': ");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			makePlayerElement(p, b);
			
			b.append("}");
			
			if (i != players.size() - 1) {
				b.append(",\n");
			}
		}
		
		b.append("]\n");
	}

	private void makePlayerElement(Player p, StringBuilder b) {
		b.append("[");
		
		for(int i = 0; i < 30; i++) {
			PlayerRound round = p.getRound(i);
			int winTeam = match.getRoundWinTeam(i);
			
			makeRoundElement(round, b, winTeam == p.getTeam());
			
			if (i != 29) {
				b.append(",\n");
			}
		}
		
		b.append("]");
	}
	
	private void makeRoundElement(PlayerRound r, StringBuilder b, boolean won) {
		b.append("{");
		b.append("'kills': " + r.getKills() + ", ");
		b.append("'died': " + r.getDeath() + ", ");
		b.append("'won': " + won + ", ");
		b.append("'clutchKills': " + r.getClutchKills() + ", ");
		b.append("'specialKills': " + r.getAbnormalKills() + ", ");
		b.append("'isAce': " + (r.getKills() == 5) + ", ");
		b.append("'isClutch': " + (r.getClutchKills() > 0 && won));
		b.append("}");
	}
}
