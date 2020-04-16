package host.kuro.onetwothree.task;

import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.BasePlugin;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import twitter4j.Status;
import twitter4j.Twitter;

import java.text.SimpleDateFormat;
import java.util.List;

public class TwitterTask extends Task {

    private final BasePlugin plugin;
    private final OneTwoThreeAPI api;
    private final Twitter tw;

    public TwitterTask(BasePlugin plugin, OneTwoThreeAPI api, Twitter tw) {
        this.plugin = plugin;
        this.api = api;
        this.tw = tw;
    }

    @Override
    public void onRun(int i) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy年M月d日(E曜) H時m分");
        try {
            List<Status> statuses = tw.getHomeTimeline();

            StringBuilder sb = new StringBuilder();
            sb.append(TextFormat.BOLD);
            sb.append(TextFormat.GOLD);
            sb.append("[" + Language.translate("twitter.title") + " 直近5]\n");
            int count = 0;
            for (Status status : statuses) {
                if (count > 5) break;
                String mes = status.getText();
                String sub = mes.substring(0, 2);
                if (sub.equals("RT")) continue;

                mes = mes.replace("\r\n", "\n");
                mes = mes.replace("\n\n", "\n");
                if (mes.length() > 32) {
                    mes = mes.substring(0, 32);
                }

                String name = status.getUser().getName();
                if (name.length() > 8) {
                    name = name.substring(0, 8);
                }
                sb.append(TextFormat.GREEN);
                sb.append(TextFormat.UNDERLINE);
                sb.append(name);
                sb.append(" - ");
                sb.append(sdf.format(status.getCreatedAt().getTime()));
                sb.append("\n");
                sb.append(TextFormat.WHITE);
                sb.append(mes);
                sb.append("\n");
                count++;
            }
            api.getMessage().SendWarningMessage(new String(sb), true);

        } catch (Exception e) {
            api.getLogErr().Write(null, api.getMessage().GetErrorMessage(e));
        }
    }
}
