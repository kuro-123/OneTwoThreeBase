package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.npc.NpcType;

public class LagTask extends AsyncTask {

    private int LAG_SEQ = 0;
    private String message = "";
    private final OneTwoThreeAPI api;
    public int id = -1;

    public LagTask(OneTwoThreeAPI api) {
        this.api = api;
    }

    @Override
    public void onRun() {
        LAG_SEQ++;
        switch(LAG_SEQ) {
            case 1:
                message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.lag.one");
                break;
            case 2:
                message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.lag.two");
                break;
            case 3:
                int i = 0;
                for (Level level : api.getServer().getLevels().values()) {
                    for (Entity entity : level.getEntities()) {
                        if (!(entity instanceof Player) && !(entity instanceof NpcType)) {
                            entity.close();
                            i++;
                        }
                    }
                }
                message = TextFormat.LIGHT_PURPLE + String.format(Language.translate("onetwothree.lag.three") + " <除去数 : %d個>", i);
                break;
            case 4:
                message = "";
                if (this.id >= 0) {
                    api.getServer().getScheduler().cancelTask(id);
                }
                api.lag_task_id = -1;
                break;
        }
        if (message.length() <= 0) return;
        api.getMessage().SendWarningMessage(message, true);
    }
}
