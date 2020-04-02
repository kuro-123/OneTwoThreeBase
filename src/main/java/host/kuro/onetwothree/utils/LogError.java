package host.kuro.onetwothree.utils;

import cn.nukkit.Player;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.database.DatabaseArgs;

import java.util.ArrayList;

public class LogError {

    protected OneTwoThreeAPI api;

    public LogError(OneTwoThreeAPI api) {
        this.api = api;
    }

    public void Write(Player player, String place, String error, String message) {
        if (error.length() > 500) {
            error = error.substring(0, 500);
        }
        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
        if (player == null) {
            args.add(new DatabaseArgs("c", ""));
            args.add(new DatabaseArgs("i", ""+0));
        } else {
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            args.add(new DatabaseArgs("i", ""+api.GetRank(player)));
        }
        args.add(new DatabaseArgs("c", place));
        args.add(new DatabaseArgs("c", error));
        args.add(new DatabaseArgs("c", message));
        api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0032"), args);
        args.clear();
        args = null;
    }

}
