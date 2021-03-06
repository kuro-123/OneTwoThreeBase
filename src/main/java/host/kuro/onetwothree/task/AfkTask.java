package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import org.w3c.dom.Text;

import java.util.HashMap;

public class AfkTask extends AsyncTask {

    private final OneTwoThreeAPI api;
    private HashMap<Player, Integer> afk = null;

    public AfkTask(OneTwoThreeAPI api) {
        this.api = api;
        afk = new HashMap<>();
    }

    @Override
    public void onRun() {
        int value;
        boolean kick = false;
        for (Player p : api.getServer().getOnlinePlayers().values()) {
            if (afk.containsKey(p)) {
                value = afk.get(p);
                value++;
                afk.put(p, value);
            } else {
                value = 0;
                afk.put(p, value);
            }
            if (value > 2) {
                api.getMessage().SendAutoKick(p);
                p.kick(Language.translate("onetwothree.kick.long"));
                afk.remove(p);
                kick = true;
            }
        }
        if (kick) {
            api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin001, 0, false); // INFO
        }
    }

    public void ResetAfk(Player p) {
        afk.put(p, 0);
    }
}
