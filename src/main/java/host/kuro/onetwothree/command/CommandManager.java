package host.kuro.onetwothree.command;

import cn.nukkit.command.CommandMap;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.defaults.NameCommand;
import host.kuro.onetwothree.command.defaults.WarpCommand;

public class CommandManager {

    public static void registerAll(OneTwoThreeAPI api) {
    	CommandMap map = api.getServer().getCommandMap();
        map.register("OneTwoThreeBase", new WarpCommand(api));
        map.register("OneTwoThreeBase", new NameCommand(api));
    }
}