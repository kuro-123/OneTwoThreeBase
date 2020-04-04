package host.kuro.onetwothree.command;

import cn.nukkit.command.CommandMap;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.defaults.*;

public class CommandManager {

    public static void registerAll(OneTwoThreeAPI api) {
        try {
            // 無効化
            String disableCommands = api.getConfig().getString("Command.Disable");
            if (disableCommands.length() > 0) {
                String[] cmds = disableCommands.split(",", 0);
                if (cmds.length > 0) {
                    for (String target : cmds) {
                        api.getServer().getCommandMap().getCommands().remove(target);
                    }
                }
            }
            // 登録
            CommandMap map = api.getServer().getCommandMap();
            map.register("OneTwoThreeBase", new WarpCommand(api));
            map.register("OneTwoThreeBase", new NameCommand(api));
            map.register("OneTwoThreeBase", new XboxCommand(api));
            map.register("OneTwoThreeBase", new TouchCommand(api));
            map.register("OneTwoThreeBase", new VersionCommand(api));
            map.register("OneTwoThreeBase", new PriceCommand(api));
            map.register("OneTwoThreeBase", new RankCommand(api));
            map.register("OneTwoThreeBase", new SellCommand(api));
            map.register("OneTwoThreeBase", new RebootCommand(api));
            map.register("OneTwoThreeBase", new CallCommand(api));
            map.register("OneTwoThreeBase", new ListCommand(api));
            map.register("OneTwoThreeBase", new TagCommand(api));
            map.register("OneTwoThreeBase", new AliasCommand(api));
            map.register("OneTwoThreeBase", new IdkCommand(api));
        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(null, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), "");
        }
    }
}