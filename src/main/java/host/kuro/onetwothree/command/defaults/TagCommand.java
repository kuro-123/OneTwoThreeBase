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

public class TagCommand extends CommandBase {

    private Config cfg;

    public TagCommand(OneTwoThreeAPI api) {
        super("tag", api);
        this.setAliases(new String[]{"tg"});
        commandParameters.clear();
        cfg = api.getConfig();
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

        // 現在のプレイヤー情報取得
        String tag = "";
        try {
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(api.getConfig().getString("SqlStatement.Sql0009"));
            ArrayList<DatabaseArgs> xargs = new ArrayList<DatabaseArgs>();
            xargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            ResultSet rs = api.getDB().ExecuteQuery(ps, xargs);
            xargs.clear();
            xargs = null;
            if (rs != null) {
                while (rs.next()) {
                    tag = rs.getString("tag");
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
            api.getLogErr().Write(player, "TagCommand : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }

        // 設定ウィンドウ
        CustomForm form = new CustomForm(Language.translate("commands.tag.window_title"))
                .addLabel(Language.translate("commands.tag.window_label01"))
                .addLabel(TextFormat.YELLOW + Language.translate("commands.tag.window_label02"))
                .addInput(Language.translate("commands.tag.window_input01"), Language.translate("commands.tag.window_input02"), tag);
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
        form.send(player, (targetPlayer, targetForm, data) -> {
            try {
                if (data == null) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                // ウィンドウログ
                api.getLogWin().Write(targetPlayer, Language.translate("commands.tag.window_title"), data.get(1).toString(), "", "", "", "", "", targetPlayer.getDisplayName());
                // タグ取得
                String upd_tag = data.get(2).toString();
                // 文字数チェック
                if (upd_tag.length() > 0) {
                    if (!IsMojisu(targetPlayer, upd_tag)) return;
                }
                // プレイヤー表示情報へ登録(UPSERT)
                StringBuilder sb_upd = new StringBuilder();
                ArrayList<DatabaseArgs> uargs = new ArrayList<DatabaseArgs>();
                sb_upd.append(cfg.getString("SqlStatement.Sql0035"));
                uargs.add(new DatabaseArgs("c", targetPlayer.getLoginChainData().getXUID()));       // xuid
                uargs.add(new DatabaseArgs("c", upd_tag));       // tag
                uargs.add(new DatabaseArgs("c", upd_tag));       // tag
                int ret = api.getDB().ExecuteUpdate(new String(sb_upd), uargs);
                uargs.clear();
                uargs = null;
                if (ret <= 0) {
                    String mes = TextFormat.RED + Language.translate("commands.tag.err_regist");
                    targetPlayer.sendMessage(mes);
                    this.api.getServer().getLogger().warning(mes);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                targetPlayer.sendMessage(api.GetInfoMessage("commands.tag.success"));
                api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS

            } catch (Exception e) {
                this.sendUsage(targetPlayer);
                api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                e.printStackTrace();
                api.getLogErr().Write(targetPlayer, "TagCommand : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), targetPlayer.getDisplayName());
            }
        });
        return true;
    }

    private boolean IsMojisu(Player player, String target) {
        // 文字数チェック
        int len =target.length();
        if (len < 3 || len > 16) {
            player.sendMessage(api.GetWarningMessage("commands.tag.err_len"));
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        return true;
    }
}
