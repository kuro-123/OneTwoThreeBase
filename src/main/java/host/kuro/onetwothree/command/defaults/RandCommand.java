package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RandCommand extends CommandBase {

    private Config cfg;

    public RandCommand(OneTwoThreeAPI api) {
        super("rand", api);
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("min", CommandParameter.ARG_TYPE_INT, false),
                new CommandParameter("max", CommandParameter.ARG_TYPE_INT, false)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // コマンドチェック
        if (!this.testPermission(sender)) return false;
        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)) player = (Player) sender;
        if (player == null) {
            this.sendUsage(sender);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        // 権限チェック
        if (!api.IsJyumin(player)) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
            return false;
        }
        if (args.length != 2) {
            this.sendUsage(sender);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        try {
            String kisu_gusu;
            int ret = -1;
            int iMin = Integer.parseInt(args[0]);
            int iMax = Integer.parseInt(args[1]);
            int iTemp = 0;
            if (iMin > iMax) {
                iTemp = iMax;
                iMax = iMin;
                iMin = iTemp;
            }
            if (iMin == iMax) {
                ret = iMax;
            } else {
                ret = api.getRand().Next(iMin, iMax);
            }
            if ((ret % 2) == 0) {
                kisu_gusu = "偶数";
            } else {
                kisu_gusu = "奇数";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(TextFormat.WHITE);
            sb.append("[");
            sb.append(TextFormat.GOLD);
            sb.append(player.getDisplayName());
            sb.append(TextFormat.WHITE);
            sb.append("]さんが引いた値 (最小:");
            sb.append(String.valueOf(iMin));
            sb.append(" 最大:");
            sb.append(String.valueOf(iMax));
            sb.append(") は -> [ ");
            sb.append(TextFormat.GREEN);
            sb.append(String.valueOf(ret));
            sb.append(TextFormat.WHITE);
            sb.append(" ] (");
            sb.append(kisu_gusu);
            sb.append(") です");
            String message = new String(sb);
            api.getServer().broadcastMessage(message);
            api.sendDiscordGreenMessage(message);
            api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin052, 0, false); // ﾋﾟｭｳ

        } catch (Exception e) {
            this.sendUsage(player);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            e.printStackTrace();
            api.getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
        return true;
    }
}