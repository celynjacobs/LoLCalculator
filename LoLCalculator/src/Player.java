public class Player {
	public String username;
	public double overallWR, champWR, compositeWR;
	public int wins, losses,champWins, champLosses, champGamesPlayed;
	public String champ;
	
	Player(String username, String champ){
		champWins = 0;
		champLosses = 0;
		wins = 0;
		losses = 0;
		this.champ = champ;
		overallWR = 50;
		this.username = username;
	}
	
	/**
	 * If we cared about saving info for champs the player is not using, we could do that using a
	 * map. Since we don't care, though, this method does not "add" champs to anything except it
	 * adds their wins and losses to the player's total in order to update their overallWR. If the
	 * "added" champ is the same as the champ the player is going to play, then the champion info is 
	 * set to whatever the info is that is passed to this method.
	 * 
	 * @param champName
	 * @param thisChampWins
	 * @param thisChampLosses
	 */
	public void addChamp(String champName, int thisChampWins, int thisChampLosses) {
		losses += thisChampLosses;
		wins += thisChampWins;
		if((wins + losses) > 0)
			overallWR = 100 * (double) wins / (wins + losses);
		else
			overallWR = 0;
		
		//System.out.println("Just added champ: " + champName + " with " + thisChampWins + " wins and " +
		//thisChampLosses + " losses to " + username + " stats and " + "overallWR is now " + overallWR);
		if(champName.equals(champ.toUpperCase())) {
			champWins = thisChampWins;
			champLosses = thisChampLosses;
			champGamesPlayed = champWins + champLosses;
			champWR = (double) (champWins) / (champWins + champLosses) * 100;
		}
			
	}
	
	private void calculateCompositeWR() {
		if (champGamesPlayed < 20) {
			double champComponent = ((double) champGamesPlayed / 20 * champWR);
			double overallComponent = (1 - ((double) champGamesPlayed / 20)) * overallWR;
			compositeWR = champComponent + overallComponent;
		}
        else 
            compositeWR = champWR;
	}
	
	public int getChampGamesPlayed() {
		return champWins + champLosses;
	}
	
	public double getChampWR() {
		if(champGamesPlayed == 0)
			return -1;
		else
			return champWR;
	}
	
	public double getCompositeWR() {
		calculateCompositeWR();
		return compositeWR;
	}
}
