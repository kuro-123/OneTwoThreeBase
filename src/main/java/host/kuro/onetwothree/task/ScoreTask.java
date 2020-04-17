package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.scheduler.AsyncTask;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.scoreboard.Scoreboard;

public class ScoreTask extends AsyncTask {

	private final OneTwoThreeAPI api;

	public ScoreTask(OneTwoThreeAPI api) {
		this.api = api;
	}

	@Override
	public void onRun() {
		for(Player player : api.getServer().getOnlinePlayers().values()) {
			if(api.getScoreboard(player) != null) {
				Scoreboard b = api.getScoreboard(player);
				api.boards.remove(player);
				b.onUpdate();
				api.boards.put(player, b);
			}
		}
	}
}
