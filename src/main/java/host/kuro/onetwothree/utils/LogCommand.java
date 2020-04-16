package host.kuro.onetwothree.utils;

import cn.nukkit.Player;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.database.DatabaseArgs;

import java.util.ArrayList;

public class LogCommand {

    protected OneTwoThreeAPI api;

    public LogCommand(OneTwoThreeAPI api) {
        this.api = api;
    }

    public void Write(Player player, String cmd, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String message) {
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            if (player == null) {
                args.add(new DatabaseArgs("c", ""));
                args.add(new DatabaseArgs("i", ""+0));
            } else {
                args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
                args.add(new DatabaseArgs("i", ""+api.GetRank(player)));
            }
            args.add(new DatabaseArgs("c", cmd));
            args.add(new DatabaseArgs("c", arg1));
            args.add(new DatabaseArgs("c", arg2));
            args.add(new DatabaseArgs("c", arg3));
            args.add(new DatabaseArgs("c", arg4));
            args.add(new DatabaseArgs("c", arg5));
            args.add(new DatabaseArgs("c", arg6));
            args.add(new DatabaseArgs("c", message));
            api.getDB().ExecuteUpdate(Language.translate("Sql0030"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
        }
    }
}
