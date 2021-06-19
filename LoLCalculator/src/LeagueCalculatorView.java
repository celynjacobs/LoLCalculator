import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This class creates the GUI for the LeagueCalculator application. It allows the user to
 * enter the usernames of every player on their team and update their profile pages on
 * na.op.gg before calculating the chance that the user's team will win a game in Platinum Elo
 * in League of Legends. This application has two variations: one which is entirely Java-based and the other
 * that is backed by a Javascript algorithm; both variations scrape player statistics from na.op.gg. Current setup
 * is Java-only and Javascript variation requires some additional library management.
 * @author Celyn Jacobs
 * Last Updated: 12/23/2020
 *
 */
public class LeagueCalculatorView extends Application {
	private TextField TopUsername, JgUsername, MidUsername, BotUsername, SupUsername;
	private TextField TopChamp, JgChamp, MidChamp, BotChamp, SupChamp;
	private Label TopLabel, JgLabel, MidLabel, BotLabel, SupLabel, infoLabel, UsernameLabel, ChampLabel,
	instructionLabel;
	private Button updateBtn, calculateBtn;
	private GridPane grid;
	private BorderPane window, left;
	private String instructions = "\nWelcome to Celyn's League of Legends Five-Factor Win Chance Calculator!\n" +
		    "Enter teammate usernames in the appropriate boxes on the left. When all of the usernames are\n" +
		    "entered, click Update Players to make sure the data we use is up to date. After the update finishes,\n" +
		    "begin entering champion names for each player as they become available. Once you have entered all\n" +
		    "of the player and champion names and updated the players, click Calculate!\n\n";
	private String consoleText = "";
	
	/**
	 * Start method override that creates the GUI. The GUI is a 1350x750 window with sections
	 * for the user to supply each teammate's username and their champion name. It contains an
	 * instructional text section below the input text fields on the left side of the window. On the
	 * right side, the application provides informational messages to the user, including alerts
	 * when player stats are being update or when calculations are occurring involving player statistics.
	 */
	@Override
	public void start(Stage stage) throws Exception {
		// TODO Auto-generated method stub
		left = new BorderPane();
		window = new BorderPane();
		grid = new GridPane();
		
		left.setTop(grid);
		
		window.setPadding(new Insets(50));
		grid.setHgap(20);
		grid.setVgap(20);
		
		Scene scene = new Scene(window,1350,750);
		
		initializeElems();
		
		makeButtons();
		
		fillGrid();
		
		setDefaultText(); //To be used for testing
		
		left.setCenter(instructionLabel);
		window.setCenter(left);
		window.setRight(infoLabel);
		
		stage.setScene(scene);
		stage.setTitle("Celyn's League of Legends Win Chance Calculator");
		
		LeagueCalculatorScraper.setView(this);//associates this view with the scraper. Need this so GUI updates work.
		stage.show();
	}
	
	/**
	 * This method adds the given informational text to the right side of the window. If
	 * text length gets too long, deletes old text and adds new text.
	 * 
	 * @param text This is the text to be added to the info section of the window 
	 */
	public void update(String text) {
		if(consoleText.length() > 700)
			consoleText = text;
		else
			consoleText += text;
		infoLabel.setText(consoleText);
	}
	
	/**
	 * This method is used for testing purposes and supplies default usernames/champion names.
	 */
	private void setDefaultText() {
		TopUsername.setText("Zekú");
		JgUsername.setText("TheJackal666");
		MidUsername.setText("qman37");
		BotUsername.setText("fernanda12x");
		SupUsername.setText("ranger51");
		
		TopChamp.setText("galio");
		JgChamp.setText("vi");
		MidChamp.setText("lux");
		BotChamp.setText("vayne");
		SupChamp.setText("sona");
	}
	
	/**
	 * This method creates the Update Players and Calculate! buttons that trigger scraping
	 * activities either using the LeagueCalculatorScraper class or the Javascript algorithm.
	 */
	private void makeButtons() {
		updateBtn = new Button("Update Players");
		updateBtn.setOnAction((click) -> {
			if(!checkUsernames())
				alertBoxesEmpty();
			else {
				update("Now updating players. Please wait...\t\t\t\t\t\t\t\n");
		
				LeagueCalculatorScraper.updatePlayers( getUsernames() );
			}
		});
		
		calculateBtn = new Button("Calculate!");
		calculateBtn.setOnAction((click) -> {
			if(!checkUsernames() || !checkChamps())
				alertBoxesEmpty();
			else {
				update("Now calculating your team's chance of winning the game! Please wait...\n\n");
				Thread sideThread = new Thread(()->{
					LeagueCalculatorScraper.scrapeAndCalculateWinChance(getChampsAndUsernames());
					//LeagueCalculatorScraper.runJavaScriptCalculator(getArgsForJSAlgorithm());
				});
				sideThread.start();
			}
		});
	}
	
	private String[] getArgsForJSAlgorithm() {
		List<String> argsList = getChampsAndUsernames();
		String[] args = new String[10];
		
		for(int i = 0; i < 10; ++i)
			args[i] = argsList.get(i);
		
		return args;
	}
	
	private List<String> getUsernames() {
		List<String> usernameList = new ArrayList<String> ();
		usernameList.add(TopUsername.getText());
		usernameList.add(JgUsername.getText());
		usernameList.add(MidUsername.getText());
		usernameList.add(BotUsername.getText());
		usernameList.add(SupUsername.getText());
		return usernameList;
	}
	
	/**
	 * This method returns a list of the usernames and champions for each player on the team
	 * in a specific order for writing to the file: {TopUsername, TopChamp, JgUsername...}
	 * 
	 * This will make the format of the output file:
	 * TopUsername
	 * TopChamp
	 * JgUsername
	 * JgChamp
	 * ...
	 * 
	 * @return
	 */
	private List<String> getChampsAndUsernames(){
		List<String> usernameList = new ArrayList<String> ();
		usernameList.add(TopUsername.getText());
		usernameList.add(TopChamp.getText());
		usernameList.add(JgUsername.getText());
		usernameList.add(JgChamp.getText());
		usernameList.add(MidUsername.getText());
		usernameList.add(MidChamp.getText());
		usernameList.add(BotUsername.getText());
		usernameList.add(BotChamp.getText());
		usernameList.add(SupUsername.getText());
		usernameList.add(SupChamp.getText());
		return usernameList;
	}
	
	private boolean checkUsernames() {
		if(TopUsername.getText().equals("") || JgUsername.getText().equals("") ||
				MidUsername.getText().equals("") || BotUsername.getText().equals("") ||
				SupUsername.getText().equals(""))
			return false;
		return true;
	}
	
	private boolean checkChamps() {
		if(TopChamp.getText().equals("") || JgChamp.getText().equals("") ||
				MidChamp.getText().equals("") || BotChamp.getText().equals("") ||
				SupChamp.getText().equals(""))
			return false;
		return true;
	}
	
	private void initializeElems() {
		TopLabel = new Label();
		TopLabel.setText("Top: ");
		JgLabel = new Label();
		JgLabel.setText("Jungler: ");
		MidLabel = new Label();
		MidLabel.setText("Middle: ");
		BotLabel = new Label();
		BotLabel.setText("Bot/ADC: ");
		SupLabel = new Label();
		SupLabel.setText("Support: ");
		
		
		TopUsername = new TextField();
		JgUsername = new TextField();
		MidUsername = new TextField();
		BotUsername = new TextField();
		SupUsername = new TextField();
		
		TopChamp = new TextField();
		JgChamp = new TextField();
		MidChamp = new TextField();
		BotChamp = new TextField();
		SupChamp = new TextField();
		
		UsernameLabel = new Label();
		UsernameLabel.setText("Player Username");
		ChampLabel = new Label();
		ChampLabel.setText("Champion name");
		infoLabel = new Label();
		instructionLabel = new Label();
		instructionLabel.setText(instructions);
	}
	
	private void fillGrid() {
		grid.add(UsernameLabel, 1, 0);
		grid.add(ChampLabel, 2, 0);
		
		grid.add(TopLabel, 0, 1); //(node, colIndex, rowIndex
		grid.add(JgLabel, 0, 2); //(node, colIndex, rowIndex
		grid.add(MidLabel, 0, 3); //(node, colIndex, rowIndex
		grid.add(BotLabel, 0, 4); //(node, colIndex, rowIndex
		grid.add(SupLabel, 0, 5); //(node, colIndex, rowIndex
		
		grid.add(TopUsername, 1, 1);
		grid.add(JgUsername, 1, 2);
		grid.add(MidUsername, 1, 3);
		grid.add(BotUsername, 1, 4);
		grid.add(SupUsername, 1, 5);
		
		grid.add(TopChamp, 2, 1);
		grid.add(JgChamp, 2, 2);
		grid.add(MidChamp, 2, 3);
		grid.add(BotChamp, 2, 4);
		grid.add(SupChamp, 2, 5);
		
		grid.add(updateBtn, 0, 6);
		grid.add(calculateBtn, 2, 6);
		
		grid.add(instructionLabel, 1, 7);
	}
	
	private void alertBoxesEmpty() {
		Stage alert = new Stage();
		BorderPane alertWindow = new BorderPane();
		Scene alertScene = new Scene(alertWindow, 400, 200);
		
		alert.setScene(alertScene);
		alert.setTitle("Message");
		
		Label infoLabel = new Label();
		infoLabel.setText("One or more required fields are empty!\nPlease check that you've \nfilled"
				+ " in all of the \nrequired fields");
		alertWindow.setCenter(infoLabel);
		
		VBox box = new VBox();
		Button okBtn = new Button("OK");
		okBtn.setOnAction((click) -> {
			alert.close();
		});
		box.getChildren().addAll(new Label(),okBtn);
		
		alertWindow.setRight(box);
		box.setSpacing(150);//might need to adjust this
	
		alert.showAndWait();
	}
	
}
