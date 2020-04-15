package host.kuro.onetwothree.utils;

import cn.nukkit.Player;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.database.DatabaseArgs;

import java.util.ArrayList;

public class LogPay {

    protected OneTwoThreeAPI api;

    public LogPay(OneTwoThreeAPI api) {
        this.api = api;
    }

    public void Write(Player player, String action, String kind, String amount) {
        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
        args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
        args.add(new DatabaseArgs("i", ""+api.GetRank(player)));
        args.add(new DatabaseArgs("c", action));
        args.add(new DatabaseArgs("c", kind));
        args.add(new DatabaseArgs("c", amount));
        api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0057"), args);
        args.clear();
        args = null;
    }

}
