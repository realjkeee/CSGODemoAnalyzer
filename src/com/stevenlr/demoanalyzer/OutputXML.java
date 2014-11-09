package com.stevenlr.demoanalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class OutputXML {
	
	private Match match;
	
	public OutputXML(Match match) {
		this.match = match;
	}
	
	public void write(String outputFilename) {
		Document doc = null;
		
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.newDocument();
		} catch (ParserConfigurationException e) {	
			e.printStackTrace();
		}
		
		Element matchElement = doc.createElement("match");
		doc.appendChild(matchElement);
		
		Collection<Player> playersCollection = match.getPlayers();
		List<Player> players = new ArrayList<Player>(playersCollection);
		
		Collections.sort(players, new Comparator<Player>() {
			@Override
			public int compare(Player o1, Player o2) {
				return o1.getTeam() - o2.getTeam();
			}
		});
		
		for(Player p : players) {
			matchElement.appendChild(makePlayerElement(p, doc));
		}
		
		Transformer tf = null;
		
		try {
			tf = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		
		tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		
		Writer out = new StringWriter();
		try {
			tf.transform(new DOMSource(doc), new StreamResult(out));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(outputFilename)));
			osw.write("register_match_data('");
			
			String data = out.toString();
			data.replaceAll("'", "\'");
			data.replace('\n', '\\');
			
			osw.write(data);
			osw.write("');");
			osw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Element makePlayerElement(Player p, Document doc) {
		Element playerElement = doc.createElement("player");
		
		playerElement.setAttribute("name", p.getName());
		playerElement.setAttribute("team", String.valueOf(p.getTeam()));
		
		for(int i = 0; i < 30; i++) {
			PlayerRound round = p.getRound(i);
			int winTeam = match.getRoundWinTeam(i);
			Element roundElement = makeRoundElement(round, doc, winTeam == p.getTeam());
			playerElement.appendChild(roundElement);
		}
		
		return playerElement;
	}
	
	private Element makeRoundElement(PlayerRound r, Document doc, boolean won) {
		Element roundElement = doc.createElement("round");
		
		roundElement.setAttribute("kills", String.valueOf(r.getKills()));
		roundElement.setAttribute("died", String.valueOf(r.getDeath()));
		roundElement.setAttribute("won", String.valueOf(won));
		roundElement.setAttribute("clutchKills", String.valueOf(r.getClutchKills()));
		roundElement.setAttribute("specialKills", String.valueOf(r.getAbnormalKills()));
		roundElement.setAttribute("isAce", String.valueOf(r.getKills() == 5));
		roundElement.setAttribute("isClutch", String.valueOf(r.getClutchKills() > 0 && won));
		
		return roundElement;
	}
}
