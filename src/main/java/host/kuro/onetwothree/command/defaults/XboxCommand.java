package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class XboxCommand extends CommandBase {

    public XboxCommand(OneTwoThreeAPI api) {
        super("xbox", api);
        this.setAliases(new String[]{"xb", "x"});
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("name", CommandParamType.STRING, true),
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // コマンドチェック
        if (!this.testPermission(sender)) return false;
        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)) player = (Player) sender;
        if (player == null) {
            api.getMessage().SendUsage(this, sender);
            return false;
        }
        if (args.length != 1) {
            api.getMessage().SendUsage(this, sender);
            return false;
        }
        try {
            // あいまい検索
            String name = api.AmbiguousSearch(args[0]);
            if (name.length() == 0) {
                name = args[0];
            }

            // XUIDチェック
            String xuid = "";
            PreparedStatement ps = null;
            ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0011"));
            ArrayList<DatabaseArgs> xargs = new ArrayList<DatabaseArgs>();
            xargs.add(new DatabaseArgs("c", name.toLowerCase()));
            ResultSet rs = api.getDB().ExecuteQuery(ps, xargs);
            xargs.clear();
            xargs = null;
            if (rs != null) {
                while(rs.next()){
                    xuid = rs.getString("xuid");
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
            if (xuid.length() <= 0) {
                api.getMessage().SendWarningMessage(Language.translate("commands.xbox.err_nothing"), player);
                return false;
            } else {
                player.showXboxProfile(xuid);
            }

        } catch (Exception e) {
            api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), player);
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return false;
        }
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS
        return true;
    }
}