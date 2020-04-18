package host.kuro.onetwothree.scoreboard;

import cn.nukkit.Player;
import host.kuro.onetwothree.protocol.RemoveObjectivePacket;
import host.kuro.onetwothree.protocol.ScorePacketInfo;
import host.kuro.onetwothree.protocol.SetDisplayObjectivePacket;
import host.kuro.onetwothree.protocol.SetScorePacket;

import java.util.*;

public class Scoreboard {
	
	public ScoreboardObjective objective;
	public long id;
	public Player player = null;
	public HashMap<String,ScoreboardObjective> objectives = new HashMap<>();
	
	public static long randomId() {
		Random rnd = new Random();
		long randomLong = rnd.nextLong();
        return randomLong;
	}
	
	public Scoreboard() {
		id = -randomId();
	}
	
	public ScoreboardObjective registerNewObjective(String objectiveName, Criteria criteria) {
		ScoreboardObjective obj = new ScoreboardObjective();
		obj.objectiveName = objectiveName;
		obj.criteria = criteria;
		objective = obj;
		if(!objectives.containsKey(objectiveName)) {
			objectives.put(objectiveName,obj);
		}
		return objective;
	}
	
	public ScoreboardObjective getObjective(String objectiveName) {
		ScoreboardObjective obj = null;
		if(objectives.containsKey(objectiveName)) {
			obj = objectives.get(objectiveName);
		}
		return obj;
	}
	
	public ScoreboardObjective tryGetCurrentObjective(String o) {
		ScoreboardObjective v = null;
		if(objectives.containsKey(o) && getObjective().objectiveName.contains(o)) {
			v = getObjective();
		}
		return v;
	}
	
	public void setObjective(String objectiveName) {
		if(objectives.containsKey(objectiveName)) {
			objective = objectives.get(objectiveName);
		}
	}
	
	public void unregisterObjective(String objectiveName) {
		if(objectives.containsKey(objectiveName)) {
			if(objective.objectiveName.equals(objectiveName)) {
				objective = null;
			}
			objectives.remove(objectiveName);
		}
	}
	
	public ScoreboardObjective getObjective() {
		try {
			return objective;
		} catch (NullPointerException e) {
			return null;
		}
	}

	public void onRemove() {
		RemoveObjectivePacket pk = new RemoveObjectivePacket();
		pk.objectiveName = getObjective().objectiveName;
		player.dataPacket(pk);
	}

	public void onUpdate(int sortOrder) {
		RemoveObjectivePacket pk = new RemoveObjectivePacket();
		pk.objectiveName = getObjective().objectiveName;
		player.dataPacket(pk);
		
		SetDisplayObjectivePacket pk1 = new SetDisplayObjectivePacket();
		pk1.objectiveName = getObjective().objectiveName;
		pk1.displayName = getObjective().displayName;
		pk1.criteriaName = getObjective().criteriaToString();
		pk1.displaySlot = getObjective().slotToString();
		pk1.sortOrder = sortOrder;
		player.dataPacket(pk1);
		
		HashMap<String, Score> fakeMap = new HashMap<String, Score>();
		for(Map.Entry<String,Score> e : getObjective().scores.entrySet()) {
			fakeMap.put(e.getKey(), e.getValue());
		}
		
		for(Score score : fakeMap.values()) {
			ScorePacketInfo info = new ScorePacketInfo();
			info.scoreboardId = score.scoreboardId;
			info.objectiveName = score.objective.objectiveName;
			info.score = score.scoreId;
			info.addType = 3;
			info.fakePlayer = score.fakePlayer;
			
			List<ScorePacketInfo> list = new ArrayList<ScorePacketInfo>();
			list.add(info);

			SetScorePacket pk2 = new SetScorePacket();
			pk2.type = (byte) score.addOrRemove;
			pk2.entries = list;
			player.dataPacket(pk2);
			
			if(score.addOrRemove == 1) {
				String id = score.fakeId;
				getObjective().scores.remove(id);
			}
		}
	}


}
