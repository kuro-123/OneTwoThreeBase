package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;

public class WarpCommand extends CommandBase {

    public WarpCommand(OneTwoThreeAPI api) {
        super("warp", api);
        this.setAliases(new String[]{"wp", "w"});

        // command parameters
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("point name", CommandParamType.STRING, true),
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
    	if (!this.testPermission(sender)) {
            return false;
        }
        if (args.length != 1) {
            this.sendUsage(sender);
            return false;
        }

        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)){
            player = (Player) sender;
        }
        if (player == null) {
            this.sendUsage(sender);
            return false;
        }

        String target = args[0].toLowerCase();
        for (Level lv : api.getServer().getLevels().values()) {
            if (target.equals(lv.getName().toLowerCase())) {
                // HITしたらそのワールドのスポーン地点まで飛ぶ
                Position pos = lv.getSpawnLocation();
                player.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND);
                return true;
            }
        }
        return true;
    }
}