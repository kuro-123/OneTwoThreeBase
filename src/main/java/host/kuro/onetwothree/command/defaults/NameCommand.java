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
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NameCommand extends CommandBase {

    public NameCommand(OneTwoThreeAPI api) {
        super("name", api);
        this.setAliases(new String[]{"nm"});
        commandParameters.clear();
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
        // 権限チェック
        if (!api.IsJyumin(player)) {
            api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
            return false;
        }
        // 現在のプレイヤー情報取得
        String nickname = "";
        Timestamp ts = null;
        try {
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0009"));
            ArrayList<DatabaseArgs> xargs = new ArrayList<DatabaseArgs>();
            xargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            ResultSet rs = api.getDB().ExecuteQuery(ps, xargs);
            xargs.clear();
            xargs = null;
            if (rs != null) {
                while (rs.next()) {
                    nickname = rs.getString("name");
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
        } catch (Exception e) {
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
        }

        // 経過時間が設定値日数を超えていなければ変更不可
        if (ts != null) {
            int allow_days = api.getConfig().getInt("NameCommand.AllowDays");
            Date dat_dt = new Date(ts.getTime());
            Date now_dt = new Date();
            long dateTimePast = dat_dt.getTime();
            long dateTimeNow   = now_dt.getTime();
            long diff = dateTimeNow - dateTimePast;
            diff = diff / 86400000; // 日数換算(1000 / 60 / 60 / 24)
            if (diff < allow_days) {
                StringBuilder mes = new StringBuilder();
                mes.append(Language.translate("commands.name.err_days1"));
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

        // 設定ウィンドウ
        CustomForm form = new CustomForm(Language.translate("commands.name.window_title"))
                .addLabel(Language.translate("commands.name.window_label01"))
                .addLabel(TextFormat.YELLOW + Language.translate("commands.name.window_label02"))
                .addInput(Language.translate("commands.name.window_input01"), Language.translate("commands.name.window_input02"), nickname);
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
        form.send(player, (targetPlayer, targetForm, data) -> {
            try {
                if (data == null) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                // ウィンドウログ
                api.getLogWin().Write(targetPlayer, Language.translate("commands.name.window_title"), data.get(1).toString(), "", "", "", "", "", targetPlayer.getDisplayName());
                // ネーム取得
                String name = data.get(2).toString();
                if (name.length() > 0) {
                    // 文字数チェック
                    if (!IsMojisu(targetPlayer, name)) return;
                    // 半角英数
                    if (!IsHankaku(targetPlayer, name)) return;
                    // 名前重複チェック
                    if (!IsDupli(targetPlayer, name)) return;
                }
                // プレイヤー表示情報へ登録(UPSERT)
                StringBuilder sb_upd = new StringBuilder();
                ArrayList<DatabaseArgs> uargs = new ArrayList<DatabaseArgs>();
                sb_upd.append(Language.translate("Sql0002"));
                uargs.add(new DatabaseArgs("c", targetPlayer.getLoginChainData().getXUID()));       // xuid
                uargs.add(new DatabaseArgs("c", name));       // name
                uargs.add(new DatabaseArgs("c", name));       // name
                int ret = api.getDB().ExecuteUpdate(new String(sb_upd), uargs);
                uargs.clear();
                uargs = null;
                if (ret <= 0) {
                    String mes = TextFormat.RED + Language.translate("commands.name.err_regist");
                    targetPlayer.sendMessage(mes);
                    this.api.getServer().getLogger().warning(mes);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                api.getMessage().SendInfoMessage(Language.translate("commands.name.success"), targetPlayer);

            } catch (Exception e) {
                api.getMessage().SendWarningMessage(Language.translate("commands.name.fail"), targetPlayer);
                api.getLogErr().Write(targetPlayer, api.getMessage().GetErrorMessage(e));
            }
        });
        return true;
    }

    private boolean IsMojisu(Player player, String target) {
        // 文字数チェック
        int len =target.length();
        if (len < 3 || len > 16) {
            api.getMessage().SendWarningMessage(Language.translate("commands.name.err_len"), player);
            return false;
        }
        return true;
    }

    private boolean IsHankaku(Player player, String target) {
        // 半角英数チェック(_のみOK)
        if (!api.isHankakuEisu(target)) {
            api.getMessage().SendWarningMessage(Language.translate("commands.name.err_eisu"), player);
            return false;
        }
        return true;
    }

    private boolean IsDupli(Player player, String target) {
        // 重複チェック
        boolean hit = false;
        try {
            PreparedStatement ps = null;
            ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0003"));
            ArrayList<DatabaseArgs> xargs = new ArrayList<DatabaseArgs>();
            xargs.add(new DatabaseArgs("c", target.toLowerCase()));
            ResultSet rs_name = api.getDB().ExecuteQuery(ps, xargs);
            xargs.clear();
            xargs = null;
            if (rs_name != null) {
                while(rs_name.next()){
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
                api.getMessage().SendWarningMessage(Language.translate("commands.name.err_dupli"), player);
                return false;
            }
        } catch (Exception e) {
            api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), player);
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }
}