package host.kuro.onetwothree.task;

import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;

public class RebootTask extends AsyncTask {

    private int HTKT_SEQ = 0;
    private String message = "";
    private final OneTwoThreeAPI api;

    public RebootTask(OneTwoThreeAPI api) {
        this.api = api;
    }

    @Override
    public void onRun() {
        HTKT_SEQ++;
        switch(HTKT_SEQ) {
            case 1:
                message = TextFormat.LIGHT_PURPLE + "皆様、再起動の時間です！30秒後に自動的に再起動されます！";
                break;
            case 2:
                message = TextFormat.LIGHT_PURPLE + "20秒前です！ご注意ください！";
                break;
            case 3:
                message = TextFormat.LIGHT_PURPLE + "10秒前です！ご注意ください！";
                break;
            case 4:
                // 再起動
                message = "";
                api.getServer().shutdown();
                break;
        }
        if (message.length() <= 0) return;
        api.getServer().broadcastMessage(message);
        api.sendDiscordRedMessage(message);
    }
}
