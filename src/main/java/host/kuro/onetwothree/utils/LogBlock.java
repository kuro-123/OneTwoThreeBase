package host.kuro.onetwothree.utils;

import cn.nukkit.Player;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.database.DatabaseArgs;

import java.util.ArrayList;

public class LogBlock {

    protected OneTwoThreeAPI api;

    public LogBlock(OneTwoThreeAPI api) {
        this.api = api;
    }

    public void Write(Player player, String level, String x, String y, String z, String act, String block_id, String block_meta, String block_name, String custom_name) {
        if (player == null) return;
        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
        args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
        args.add(new DatabaseArgs("i", ""+api.GetRank(player)));
        args.add(new DatabaseArgs("i", ""+player.getGamemode()));
        args.add(new DatabaseArgs("c", level));
        args.add(new DatabaseArgs("i", x));
        args.add(new DatabaseArgs("i", y));
        args.add(new DatabaseArgs("i", z));
        args.add(new DatabaseArgs("c", act));
        args.add(new DatabaseArgs("i", block_id));
        args.add(new DatabaseArgs("i", block_meta));
        args.add(new DatabaseArgs("c", block_name));
        args.add(new DatabaseArgs("c", custom_name));
        api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0033"), args);
        args.clear();
        args = null;
    }

}
