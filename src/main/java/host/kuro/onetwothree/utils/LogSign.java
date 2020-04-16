package host.kuro.onetwothree.utils;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemSign;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.database.DatabaseArgs;

import java.util.ArrayList;

public class LogSign {

    protected OneTwoThreeAPI api;

    public LogSign(OneTwoThreeAPI api) {
        this.api = api;
    }

    public void Write(Player player, Block sign, String[] lines) {
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", sign.getLevel().getName()));
            args.add(new DatabaseArgs("i", ""+sign.getFloorX()));
            args.add(new DatabaseArgs("i", ""+sign.getFloorY()));
            args.add(new DatabaseArgs("i", ""+sign.getFloorZ()));
            args.add(new DatabaseArgs("c", lines[0]));
            args.add(new DatabaseArgs("c", lines[1]));
            args.add(new DatabaseArgs("c", lines[2]));
            args.add(new DatabaseArgs("c", lines[3]));
            args.add(new DatabaseArgs("c", player.getDisplayName()));
            args.add(new DatabaseArgs("c", lines[0]));
            args.add(new DatabaseArgs("c", lines[1]));
            args.add(new DatabaseArgs("c", lines[2]));
            args.add(new DatabaseArgs("c", lines[3]));
            args.add(new DatabaseArgs("c", player.getDisplayName()));
            int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0061"), args);
            args.clear();
            args = null;
        } catch (Exception e) {
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
        }
    }
}
