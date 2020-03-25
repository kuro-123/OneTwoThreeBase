package host.kuro.onetwothree.task;

import cn.nukkit.scheduler.Task;
import host.kuro.onetwothree.OneTwoThreeAPI;

public class MainTask extends Task {
    private final OneTwoThreeAPI api;

    public MainTask(OneTwoThreeAPI api) {
        this.api = api;
    }

    @Override
    public void onRun(int i) {
    }
}
