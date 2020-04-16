package host.kuro.onetwothree.utils;

import cn.nukkit.Player;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.database.DatabaseArgs;

import java.util.ArrayList;

public class LogWindow {

    protected OneTwoThreeAPI api;

    public LogWindow(OneTwoThreeAPI api) {
        this.api = api;
    }

    public void Write(Player player, String win, String res1, String res2, String res3, String res4, String res5, String res6, String message) {
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            if (player == null) {
                args.add(new DatabaseArgs("c", ""));
                args.add(new DatabaseArgs("i", ""+0));
            } else {
                args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
                args.add(new DatabaseArgs("i", ""+api.GetRank(player)));
            }
            args.add(new DatabaseArgs("c", win));
            args.add(new DatabaseArgs("c", res1));
            args.add(new DatabaseArgs("c", res2));
            args.add(new DatabaseArgs("c", res3));
            args.add(new DatabaseArgs("c", res4));
            args.add(new DatabaseArgs("c", res5));
            args.add(new DatabaseArgs("c", res6));
            args.add(new DatabaseArgs("c", message));
            api.getDB().ExecuteUpdate(Language.translate("Sql0031"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
        }
    }
}
