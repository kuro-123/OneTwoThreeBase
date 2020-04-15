package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class IdkCommand extends CommandBase {

    public IdkCommand(OneTwoThreeAPI api) {
        super("imadonkurai", api);
        this.setAliases(new String[]{"imadonkurai", "idk"});
        commandParameters.clear();
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
        try {
            String xuid = player.getLoginChainData().getXUID();
            long play_time = -1;
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0043"));
            ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
            pargs.add(new DatabaseArgs("c", xuid));
            ResultSet rs = api.getDB().ExecuteQuery(ps, pargs);
            pargs.clear();
            pargs = null;
            if (rs != null) {
                while (rs.next()) {
                    play_time = rs.getLong("play_time");
                    break;
                }
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (play_time == -1) {
                this.sendUsage(sender);
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                return false;
            }
           long keika = 0;
            if (api.play_time.containsKey(xuid)) {
                long start = api.play_time.get(xuid);
                long finish = System.currentTimeMillis();
                keika = (finish - start) / 1000;
            }
            keika += play_time;
            long byou = keika % 60;
            long hun = (keika / 60);
            hun = hun % 60;
            long ji = (keika / 60) / 60;

            if (hun <= 0) {
                this.sendUsage(sender);
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                return false;
            }

            String message = String.format("\nあなたのプレイ時間は [ %d時間 %d分 %d秒です ]", ji, hun, byou);
            player.sendMessage(message);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS

        } catch (Exception e) {
            player.sendMessage(api.GetErrMessage("onetwothree.cmderror"));
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            api.getLogErr().Write(player, "IdkCommand : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
            return false;
        }
        return true;
    }
}