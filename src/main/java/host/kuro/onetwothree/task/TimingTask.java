package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.npc.NpcType;

public class TimingTask extends AsyncTask {

    private int TIMING_SEQ = 0;
    private final OneTwoThreeAPI api;

    public TimingTask(OneTwoThreeAPI api) {
        this.api = api;
    }

    @Override
    public void onRun() {
        TIMING_SEQ++;
        switch(TIMING_SEQ) {
            case 1:
            case 2:
            case 3:
                break;
            case 4:
                // ワールドタスク起動
                if (api.world_task_id == -1) {
                    WorldTask task = new WorldTask(api);
                    TaskHandler th = api.getServer().getScheduler().scheduleRepeatingTask(task, 200);
                    api.world_task_id = th.getTaskId();
                }
                TIMING_SEQ = 0;
                break;
        }
    }
}
