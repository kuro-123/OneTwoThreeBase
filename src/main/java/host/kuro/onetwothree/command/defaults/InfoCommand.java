package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.ArrayList;

public class InfoCommand extends CommandBase {

    private Config cfg;

    public InfoCommand(OneTwoThreeAPI api) {
        super("info", api);
        this.setAliases(new String[]{"if"});
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

        // プレイヤー情報チェック
        try {
            String s_xuid = "";
            String s_xname = "";
            String s_name = "";
            String s_tag = "";
            String s_play_time = "";
            String s_money = "";
            String s_login = "";
            String s_break = "";
            String s_place = "";
            String s_kick = "";
            String s_death = "";
            String s_kill = "";
            String s_chat = "";
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(api.getConfig().getString("SqlStatement.Sql0046"));
            ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
            pargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            ResultSet rs = api.getDB().ExecuteQuery(ps, pargs);
            pargs.clear();
            pargs = null;
            if (rs != null) {
                while (rs.next()) {
                    s_xuid = rs.getString("xuid");
                    s_xname = rs.getString("xname");
                    s_name = rs.getString("name");
                    s_tag = rs.getString("tag");
                    s_play_time = rs.getString("play_time");
                    s_money = rs.getString("money");
                    s_login = rs.getString("login");
                    s_break = rs.getString("break");
                    s_place = rs.getString("place");
                    s_kick = rs.getString("kick");
                    s_death = rs.getString("death");
                    s_kill = rs.getString("kill");
                    s_chat = rs.getString("chat");
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
            StringBuilder sb = new StringBuilder();
            sb.append("XBOX ID : " + s_xuid + "\n");
            sb.append("XBOX名 : " + s_xname + "\n");
            sb.append("表示名 : " + s_name + "\n");
            sb.append("タグ : " + s_tag + "\n\n");

            int sumtime = Integer.parseInt(s_play_time);
            long byou = sumtime % 60;
            long hun = (sumtime / 60);
            hun = hun % 60;
            long ji = (sumtime / 60) / 60;
            sb.append("プレイ時間 : " + String.format("%d時間 %d分 %d秒", ji, hun, byou) + "\n\n");
            NumberFormat nfNum = NumberFormat.getNumberInstance();
            sb.append("所持金 : " + nfNum.format(Integer.parseInt(s_money)) + "\n\n");
            sb.append("ﾛｸﾞｲﾝ回数 : " + nfNum.format(Integer.parseInt(s_login)) + "\n");
            sb.append("整地回数 : " + nfNum.format(Integer.parseInt(s_break)) + "\n");
            sb.append("建築回数 : " + nfNum.format(Integer.parseInt(s_place)) + "\n");
            sb.append("キック回数 : " + nfNum.format(Integer.parseInt(s_kick)) + "\n");
            sb.append("死亡回数 : " + nfNum.format(Integer.parseInt(s_death)) + "\n");
            sb.append("殺害回数 : " + nfNum.format(Integer.parseInt(s_kill)) + "\n");
            sb.append("チャット回数 : " + nfNum.format(Integer.parseInt(s_chat)) + "\n");
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            SimpleForm form = new SimpleForm("プレイヤー情報", new String(sb));
            form.send(player, (targetPlayer, targetForm, data) -> {});

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "VersionCommand : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
        return true;
    }
}