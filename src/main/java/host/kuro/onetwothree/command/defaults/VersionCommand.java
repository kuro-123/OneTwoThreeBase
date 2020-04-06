package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VersionCommand extends CommandBase {

    private Config cfg;

    public VersionCommand(OneTwoThreeAPI api) {
        super("version", api);
        this.setAliases(new String[]{"ver", "v"});
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
        StringBuilder sb = new StringBuilder();
        try {
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(api.getConfig().getString("SqlStatement.Sql0010"));
            ResultSet rs = api.getDB().ExecuteQuery(ps, null);
            if (rs != null) {
                while(rs.next()){
                    sb.append(TextFormat.GOLD);
                    sb.append("VER: ");
                    sb.append(rs.getString("version"));
                    sb.append(" (");
                    sb.append(rs.getString("add_date"));
                    sb.append(" ) -> ");
                    sb.append(TextFormat.WHITE);
                    sb.append(rs.getString("name"));
                    sb.append("\n");
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

            if (sb.length() > 0) {
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
                SimpleForm form = new SimpleForm("更新情報", new String(sb));
                form.send(player, (targetPlayer, targetForm, data) -> {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "VersionCommand : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
        return true;
    }
}