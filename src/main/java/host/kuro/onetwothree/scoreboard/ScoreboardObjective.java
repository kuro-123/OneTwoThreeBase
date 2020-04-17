package host.kuro.onetwothree.scoreboard;

import java.util.*;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;

public class ScoreboardObjective {
	
	public static int Ascending = 0;
	public static int Descending = 1;
	
	public int scoreboardTick = 0;
	
	public String objectiveName;
	public DisplaySlot displaySlot;
	public Criteria criteria;
	public String displayName;
	public HashMap<String, Score> scores = new HashMap<String, Score>();
	
	public int getScoreboardTick() {
		return this.scoreboardTick;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public void setDisplaySlot(DisplaySlot displaySlot) {
		this.displaySlot = displaySlot;
	}
	
	public DisplaySlot getDisplaySlot() {
		return this.displaySlot;
	}
	
	public void registerScore(String id, String fake, int value) {
		registerScore(id, fake, value, 0);
	}
	
	private void registerScore(String id, String fake, int value, int type) {
		Score score = new Score(this, fake);
		score.setScore(value);
		score.fakeId = id;
		score.addOrRemove = type;
		if(!scores.containsKey(id)) {
			scores.put(id, score);
		} else {
			Server.getInstance().getLogger().info("Score with same id is already registered!");
		}
	}
	
	public void setScore(String id, int value) {
		if(scores.containsKey(id)) {
			Score modified = scores.get(id);
			modified.setScore(value);
			scores.remove(id);
			scores.put(id, modified);
		}
	}
	
	public void setScoreText(String id, String text) {
		if(scores.containsKey(id)) {
			Score old = scores.get(id);
			old.addOrRemove = 1;
			old.fakeId = id + "_old_changed";
			
			Score nju = new Score(this, text);
			nju.setScore(old.getScore());
			nju.fakeId = id;
			scores.remove(id);
			scores.put(id, nju);
			scores.put(id + "_old_changed", old);
		}
	}
	
	public int getScore(String id) {
		int i = 0;
		if(scores.containsKey(id)) {
			Score score = scores.get(id);
			i = score.getScore();
		}
		return i;
	}
	
	public void resetScore(String id) {
		if(scores.containsKey(id)) {
			Score modified = scores.get(id);
			modified.addOrRemove = 1;
			scores.remove(id);
			scores.put(id, modified);
		}
	}
	
	public String criteriaToString() {
		String s = "";
		 switch (criteria)
         {
             case DUMMY:
                 s = "dummy";
                 break;
         }
         return s;
	}
	
	public String slotToString()
    {
        String slot = "";
        switch (displaySlot)
        {
            case SIDEBAR:
                slot = "sidebar";
                break;
            case LIST:
                slot = "list";
                break;
            case BELOWNAME:
                slot = "belowname";
                break;
        }
        return slot;
    }

}
