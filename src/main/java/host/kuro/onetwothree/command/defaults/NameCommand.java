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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class NameCommand extends CommandBase {

    private Config cfg;

    public NameCommand(OneTwoThreeAPI api) {
        super("name", api);
        this.setAliases(new String[]{"nm", "n"});
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("nick name", CommandParamType.STRING, true),
        });
        cfg = api.getConfig();
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
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }

        try {
            // 文字数チェック
            int len =args[0].length();
            if (len < 3 || len > 16) {
                player.sendMessage(api.GetWarningMessage("commands.name.err_len"));
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                return false;
            }

            // 半角英数チェック(_のみOK)
            if (!api.isHankakuEisu(args[0])) {
                player.sendMessage(api.GetWarningMessage("commands.name.err_eisu"));
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                return false;
            }

            // 名前重複チェック
            boolean hit = false;
            PreparedStatement ps = null;
            ps = api.getDB().getConnection().prepareStatement(cfg.getString("SqlStatement.Sql0003"));
            ArrayList<DatabaseArgs> xargs = new ArrayList<DatabaseArgs>();
            xargs.add(new DatabaseArgs("c", args[0].toLowerCase()));
            ResultSet rs_name = api.getDB().ExecuteQuery(ps, xargs);
            xargs.clear();
            xargs = null;
            if (rs_name != null) {
                while(rs_name.next()){
                    // 重複
                    hit = true;
                    break;
                }
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (rs_name != null) {
                rs_name.close();
                rs_name = null;
            }
            if (hit) {
                player.sendMessage(api.GetWarningMessage("commands.name.err_dupli"));
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                return false;
            }

            // プレイヤー表示情報から更新時間を取得
            StringBuilder sb = new StringBuilder();
            sb.append(cfg.getString("SqlStatement.Sql0001"));
            ArrayList<DatabaseArgs> dargs = new ArrayList<DatabaseArgs>();
            dargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));       // xuid
            Timestamp ts = null;

            ps = api.getDB().getConnection().prepareStatement(new String(sb));
            ResultSet rs = api.getDB().ExecuteQuery(ps, dargs);
            dargs.clear();
            dargs = null;
            if (rs != null) {
                while(rs.next()){
                    ts = rs.getTimestamp("upd_date");
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

            // 経過時間が設定値日数を超えていなければ変更不可
            if (ts != null) {
                int allow_days = cfg.getInt("NameCommand.AllowDays");
                Date dat_dt = new Date(ts.getTime());
                Date now_dt = new Date();
                long dateTimePast = dat_dt.getTime();
                long dateTimeNow   = now_dt.getTime();
                long diff = dateTimeNow - dateTimePast;
                diff = diff / 1000 / 60 / 60 / 24; // 日数換算
                if (diff < allow_days) {
                    StringBuilder mes = new StringBuilder();
                    mes.append(api.GetWarningMessage("commands.name.err_days1"));
                    mes.append(" (");
                    mes.append(Language.translate("commands.name.err_days2"));
                    mes.append(": ");
                    mes.append(allow_days);
                    mes.append(")");
                    player.sendMessage(new String(mes));
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return false;
                }
            }

            // プレイヤー表示情報へ登録(UPSERT)
            StringBuilder sb_upd = new StringBuilder();
            ArrayList<DatabaseArgs> uargs = new ArrayList<DatabaseArgs>();
            sb_upd.append(cfg.getString("SqlStatement.Sql0002"));
            uargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));       // xuid
            uargs.add(new DatabaseArgs("c", args[0]));       // name
            uargs.add(new DatabaseArgs("c", args[0]));       // name
            int ret = api.getDB().ExecuteUpdate(new String(sb_upd), uargs);
            uargs.clear();
            uargs = null;
            if (ret <= 0) {
                String mes = TextFormat.RED + Language.translate("commands.name.err_regist");
                player.sendMessage(mes);
                this.api.getServer().getLogger().warning(mes);
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                return false;
            }
            player.sendMessage(api.GetInfoMessage("commands.name.success"));

        } catch (Exception e) {
            player.sendMessage(api.GetErrMessage("onetwothree.cmderror"));
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            api.getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
            return false;
        }
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS
        return true;
    }
}