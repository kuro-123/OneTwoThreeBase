package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.task.SoundTask;

import java.util.HashMap;

public class KcCommand extends CommandBase {

    public KcCommand(OneTwoThreeAPI api) {
        super("kc", api);
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("chat", CommandParameter.ARG_TYPE_STRING, false),
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // コマンドチェック
        if (!this.testPermission(sender)) return false;
        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)) {
            player = (Player) sender;
        }
        if (args.length < 1) {
            api.getMessage().SendUsage(this, sender);
            return false;
        }
        try {
            // 権限チェック
            if (player != null) {
                if (!api.IsGameMaster(player)) {
                    api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
                    return false;
                }
            }

            // コールメッセージをツイートする
            StringBuilder sb = new StringBuilder();
            sb.append(TextFormat.RED);
            sb.append("【管理ﾁｬｯﾄ】 ");
            sb.append(TextFormat.WHITE);
            sb.append("<");
            if (player == null) {
                sb.append("鯖主");
            } else {
                sb.append(player.getDisplayName());
            }
            sb.append("> ");
            sb.append(TextFormat.GREEN);

            String msg = "";
            for (int i=0; i<args.length; i++) {
                msg += args[i] + " ";
            }
            msg = msg.trim();
            sb.append(msg);

            String message = new String(sb);
            for (Player p : api.getServer().getOnlinePlayers().values()) {
                if (!api.IsGameMaster(p)) continue;
                p.sendMessage(message);
            }
            api.getServer().getLogger().info(message);

        } catch (Exception e) {
            if (player != null) {
                api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), player);
                api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            }
            return false;
        }
        return true;
    }
}