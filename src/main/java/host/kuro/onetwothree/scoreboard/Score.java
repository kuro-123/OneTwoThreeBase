package host.kuro.onetwothree.scoreboard;

public class Score {
	
	public long scoreboardId;
	public ScoreboardObjective objective;
	public String fakePlayer;
	public int scoreId;
	public long id;
	public boolean isFake;
	public int addOrRemove = 0;
	
	public boolean modified = false;
	public String fakeId;
	
	public Score(ScoreboardObjective o, String f) {
		scoreboardId = -Scoreboard.randomId();
		objective = o;
		fakePlayer = f;
		isFake = true;
	}
	
	public void setScore(int id) {
		this.scoreId = id;
	}
	
	public int getScore() {
		return this.scoreId;
	}

}
