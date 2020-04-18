package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.npc.NpcType;

public class AgreeTask extends AsyncTask {

    private final OneTwoThreeAPI api;
    private final Player player;

    public AgreeTask(OneTwoThreeAPI api, Player player) {
        this.api = api;
        this.player = player;
    }

    @Override
    public void onRun() {
        api.getMessage().SendAgreementMessage(player);
    }
}
