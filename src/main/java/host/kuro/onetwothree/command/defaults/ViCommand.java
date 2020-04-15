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
import host.kuro.onetwothree.datatype.ItemInfo;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class ViCommand extends CommandBase {

    public ViCommand(OneTwoThreeAPI api) {
        super("vi", api);
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
        // 権限チェック
        if (!api.IsGameMaster(player)) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
            return false;
        }
        return ViewInventoryWindow(player);
    }

    private boolean ViewInventoryWindow(Player player) {
        ArrayList<String> rlist = new ArrayList<String>();
        rlist.add("指定なし");
        rlist.add("訪問");
        rlist.add("住民");
        rlist.add("ＧＭ");
        rlist.add("パイ");
        rlist.add("鯖主");
        CustomForm form = new CustomForm("インベントリ検索")
                .addLabel("検索条件に沿ってプレイヤーの持ち物を検索します")
                .addLabel(TextFormat.YELLOW + "※ログイン時に更新のためリアルタイムではありません")
                .addDropDown("権限", rlist)
                .addInput("名前指定", "あいまい検索、指定なしもＯＫ")
                .addInput("アイテム指定", "あいまい検索、指定なしもＯＫ");
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
        form.send(player, (targetPlayer, targetForm, data) -> {
            try {
                if (data == null) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                // ウィンドウログ
                api.getLogWin().Write(targetPlayer, "インベントリ検索", data.get(1).toString(), data.get(2).toString(), data.get(3).toString(), data.get(4).toString(), "", "", targetPlayer.getDisplayName());

                String rank = data.get(2).toString();
                String name = data.get(3).toString();
                String item = data.get(4).toString();

                if (rank.equals("指定なし")) {
                    rank = "";
                }
                PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0067"));
                ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
                pargs.add(new DatabaseArgs("c", "%"+name.toLowerCase()+"%"));
                pargs.add(new DatabaseArgs("c", "%"+name.toLowerCase()+"%"));
                pargs.add(new DatabaseArgs("c", "%"+rank.toLowerCase()+"%"));
                pargs.add(new DatabaseArgs("c", "%"+item.toLowerCase()+"%"));
                ResultSet rs = api.getDB().ExecuteQuery(ps, pargs);
                pargs.clear();
                pargs = null;
                StringBuilder sb = new StringBuilder();
                int count = 0;
                if (rs != null) {
                    while (rs.next()) {
                        count++;
                        sb.append(TextFormat.WHITE + String.format("%s (%s) [ %s ]\n", rs.getString("xbox名"), rs.getString("名前"), rs.getString("権限")));
                        sb.append(TextFormat.YELLOW + String.format("アイテム: %s:%s - %s, [%d個]\n", rs.getString("アイテムid"), rs.getString("アイテムメタ"), rs.getString("アイテム名"), rs.getInt("数量")));
                        sb.append(TextFormat.WHITE + "---------------\n");
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
                api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
                SimpleForm result = new SimpleForm("インベントリ検索結果", "ヒット:" + count + "件\n\n" + new String(sb));
                result.send(targetPlayer, (targetPlayer2, targetForm2, data2) -> {});

            } catch (Exception e) {
                this.sendUsage(targetPlayer);
                api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                api.getLogErr().Write(targetPlayer, api.GetErrorMessage(e));
            }
        });
        return true;
    }
}