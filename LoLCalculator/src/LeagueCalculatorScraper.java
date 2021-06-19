import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import javafx.application.Platform;

public class LeagueCalculatorScraper {
	private static final String BASE_URL = "https://na.op.gg/summoner/champions/userName=";
	private static final int SEASON_NUM = 17;
	private static LeagueCalculatorView view;
	
	//Below are the weightings of each position's WR in final win chance calculations
	// Last updated 3/25/20
	private static final double TOP_WR_WEIGHT = 0.0201;
	private static final double JG_WR_WEIGHT = 0.0784;
	private static final double MID_WR_WEIGHT = 0.0301;
	private static final double BOT_WR_WEIGHT = 0.0745;
	private static final double SUP_WR_WEIGHT = 0.0612;
	private static final double OFFSET = -14.4614;
	
	
	public static void setView(LeagueCalculatorView linkedView) {
		view = linkedView;
	}
	
	private static Player[] makeTeam(List<String> champsAndUsernames) {
		Player[] team = new Player[5];//top,jg,mid,adc,sup
		
		for (int i = 0; i < champsAndUsernames.size(); i+=2) 
			team[i/2] = new Player(champsAndUsernames.get(i), champsAndUsernames.get(i+1));
		
		return team;
	}
	
	public static void scrapeAndCalculateWinChance(List<String> champsAndUsernames) {
		Player[] team = makeTeam(champsAndUsernames);
		
		for (Player player : team) 
			scrapeRankedStats(player);
		
		for(Player player : team) {
			Platform.runLater( () -> {
				if(player.getChampGamesPlayed() == 0) {
					view.update(player.username + " has not played " + player.champ + " in ranked this " +
							"season. \n\n");
				}
				else {
				view.update(player.username + "'s win rate on " + player.champ + 
						" is " + String.format("%.1f%%", player.champWR) +
						" with " + player.getChampGamesPlayed() + " games played.\n\n");
				}
			});
		}
		
		calculateWinChance(team);
	}
	
	public static void runJavaScriptCalculator(String[] args) {
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(buildJSCommand(args));
			
			BufferedReader stdInput = new BufferedReader(new 
				     InputStreamReader(proc.getInputStream()));
			
			//System.out.println("Here is the standard output of the command:\n");
			String s,log = "";
			while ((s = stdInput.readLine()) != null)
			    log += s + "\n";
			
			final String infoLog = log; //need this so that Platform.runLater() can use the variable
			
			Platform.runLater(()->{
				view.update(infoLog);
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String buildJSCommand(String[] args) {
		String cmd = "node FiveFactorCalculator.js";
		
		for(int i = 0; i < 10; ++i)
			cmd += " " + args[i];
		
		return cmd;
	}
	
	
	/**
	 * Updates the na.op.gg page for every player in a set of usernames. Executes the updating
	 * operation in parallel so they all finish at approximately the same time. See updatePlayer()
	 * method for more details.
	 * @param usernames - Set of usernames whose pages we want to update
	 */
	public static void updatePlayers(List<String> usernames) {
		Thread sideThread = null;
		for(String name : usernames) {
			sideThread = new Thread( ()->{
				updatePlayer(name);
				if(name.equals(usernames.get(usernames.size()-1))) {
					Platform.runLater(()->{
						view.update("All players updated!\n");
					});
				}
			});
			sideThread.start();
		}
//		try {
//			sideThread.join();//BLOCKS UNTIL THE 5TH THREAD TERMINATES
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * This method updates the player page for the player with the given username on
	 * na.op.gg. This process takes ~10 seconds because it creates a webclient, navigates
	 * to the page, then clicks the update button and waits 3 seconds.
	 * @param username - username of a player. Should be just their username w/ no other punctuation
	 */
	public static void updatePlayer(String username) {
		try (WebClient webClient = new WebClient(BrowserVersion.CHROME)){

			disableWebClientListeners(webClient);

			HtmlPage page = webClient.getPage("https://na.op.gg/summoner/userName=" + username);
			
			/*
			 * Currently this method will click update regardless of when the page was last updated.
			 * To take that information into account, we have to do some scraping to isolate the
			 * text for when it was last updated.
			 */
			
			DomElement btn = page.getElementById("SummonerRefreshButton");

			btn.click();
			webClient.waitForBackgroundJavaScript(3000);
			//System.out.println("Updated " + username + "!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Disables the listeners for a webclient and disables logging for htmlUnit. Basically
	 * disables all exception throwing for htmlUnit.
	 * @param webClient - Webclient whose listeners we want to disable
	 */
	private static void disableWebClientListeners(WebClient webClient) {
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setCssEnabled(false);
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
	}
	
	public static double calculateWinChance(Player[] team) {
		double topWRComponent = (TOP_WR_WEIGHT * team[0].getCompositeWR());
		double jgWRComponent = (JG_WR_WEIGHT * team[1].getCompositeWR());
		double midWRComponent = (MID_WR_WEIGHT * team[2].getCompositeWR());
		double adcWRComponent = (BOT_WR_WEIGHT * team[3].getCompositeWR());
		double supWRComponent = (SUP_WR_WEIGHT* team[4].getCompositeWR());
		
	    double winChance = 1 - ( 1 / (1 + (Math.pow(Math.E,(OFFSET + 
	            topWRComponent + jgWRComponent + midWRComponent + adcWRComponent + supWRComponent)))));

	    Platform.runLater( () -> {
	    	view.update("\nYour team's estimated chance of winning this game is " + 
	    			String.format("%.2f", winChance*100) + "%. Good luck! <3\n\n");
        });
	    
	    return winChance;
	}
	
	public static void scrapeRankedStats(Player player) {
		try {
			Document doc = Jsoup.connect(BASE_URL + player.username).get();
			int season = SEASON_NUM;
			
			if (doc.select(getChampNameSelector(season,1)).text() == null)
                season = 15;
			if (doc.select(getChampNameSelector(season,1)).text() == null) {
				player.overallWR = 50; 
				Platform.runLater( () -> {
                	view.update("Default overall WR set for " + player.username + "\n\n");
                });
			}
			else if (doc.select(getTableSelector(season)).text().contains("no results") == true) {
                player.overallWR = 50; 
                Platform.runLater( () -> {
                	view.update("Default overall WR set for " + player.username + "\n\n");
                });
            }
			else {
				String champName;
				int champWins, champLosses;
				
				for(int i = 1; doc.select(getChampNameSelector(season,i)).first() != null ; ++i) {
					champName = doc.select(getChampNameSelector(season,i)).first().text().trim().toUpperCase();
					//System.out.println("Champ name: " + champName);
					champWins = getChampWins(doc,season,i);
					champLosses = getChampLosses(doc,season,i);
					
					player.addChamp(champName, champWins, champLosses);
				}
				Platform.runLater( () -> {
					if(player.wins + player.losses == 0) {
						player.overallWR = 50;
						view.update(player.username + " has not played any ranked games this season.\n\n");
					}
					view.update(String.format("%s's overallWR is %.1f%%\n\n",
								player.username,player.overallWR));
				});
			}
		
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static String getTableSelector(int season) {
		return "#SummonerLayoutContent > "
				+ "div.tabItem.Content.SummonerLayoutContent.summonerLayout-champions > div > "
				+ "div > div.Content.tabItems > div.tabItem.season-" + season + " > div";
	}
	
	private static String getChampNameSelector(int season, int i) {
		return "#SummonerLayoutContent > "
				+ "div.tabItem.Content.SummonerLayoutContent.summonerLayout-champions > div > "
				+ "div > div.Content.tabItems > div.tabItem.season-" + season + " > table > tbody > "
						+ "tr:nth-child(" + i + ") > td.ChampionName.Cell";
	}
	
	private static int getChampWins(Document doc, int season, int i) {
		String rawChampWins;
		Element elem;
		int champWins;
		try {
			elem = doc.select(getChampWinsSelector(season,i)).first();
			if (elem == null)
				champWins = 0;
			else {
				rawChampWins = elem.text().replace("W", "");
				//System.out.println("Raw champ wins: " + rawChampWins);
				champWins = Integer.parseInt(rawChampWins);
			}
		} catch(NumberFormatException e) {
			champWins = 0;
		}
		
		return champWins;
		
	}
	
	private static int getChampLosses(Document doc, int season, int i) {
		String rawChampLosses;
		int champLosses;
		Element elem;
		try {
			elem = doc.select(getChampLossesSelector(season,i)).first();
			if (elem == null)
				champLosses = 0;
			else {
				rawChampLosses = doc.select(getChampLossesSelector(season,i)).first().text().replace("L", "");
				champLosses = Integer.parseInt(rawChampLosses);
			}
		} catch(NumberFormatException e) {
			champLosses = 0;
		}
		
		return champLosses;
		
	}
	
	
	private static String getChampWinsSelector(int season, int i) {
		return "#SummonerLayoutContent > "
				+ "div.tabItem.Content.SummonerLayoutContent.summonerLayout-champions > div > "
				+ "div > div.Content.tabItems > div.tabItem.season-" + season + " > table > tbody > "
						+ "tr:nth-child(" + i + ") > td.RatioGraph.Cell > div > div > div.Text.Left";
	}
	
	private static String getChampLossesSelector(int season, int i) {
		return "#SummonerLayoutContent > "
				+ "div.tabItem.Content.SummonerLayoutContent.summonerLayout-champions > div > "
				+ "div > div.Content.tabItems > div.tabItem.season-" + season + " > table > tbody > "
						+ "tr:nth-child(" + i + ") > td.RatioGraph.Cell > div > div > div.Text.Right";
	}
	

	
	
}
