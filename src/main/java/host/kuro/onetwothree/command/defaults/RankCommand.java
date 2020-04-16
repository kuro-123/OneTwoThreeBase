package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class RankCommand extends CommandBase {

    public RankCommand(OneTwoThreeAPI api) {
        super("rank", api);
        this.setAliases(new String[]{"ra", "r"});
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
        int rank = api.GetRank(player);
        if (rank < 2) {
            api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
            return false;
        }
        return RankWindow(player, rank);
    }

    private boolean RankWindow(Player player, int rank) {
        try {
            // 時間がないのでとりあえず(後々修正)
            ArrayList<String> rank_list = new ArrayList<String>();
            rank_list.add("0:訪問");
            rank_list.add("1:住民");
            rank_list.add("2:ＧＭ");
            if (rank >= 3) {
                rank_list.add("3:パイ");
            }
            if (rank >= 4) {
                rank_list.add("4:鯖主");
            }
            // プレイヤー一覧
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0025"));
            ResultSet rs = api.getDB().ExecuteQuery(ps, null);
            api.player_list.put(0, Language.translate("onetwothree.selection.none"));
            int i = 1;
            if (rs != null) {
                while (rs.next()) {
                    api.player_list.put(i, rs.getString("名前"));
                    i++;
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

            List<String> plist = new ArrayList<>(api.player_list.values());
            CustomForm form = new CustomForm("権限設定")
                    .addLabel("権限の設定が行えます")
                    .addDropDown("プレイヤー",  plist)
                    .addDropDown("権限リスト", rank_list);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                try {
                    if (data == null) {
                        this.sendUsage(targetPlayer);
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }

                    // ウィンドウログ
                    api.getLogWin().Write(targetPlayer, "権限設定", data.get(1).toString(), data.get(2).toString(), "", "", "", "", targetPlayer.getDisplayName());

                    // プレイヤー
                    String pname = data.get(1).toString();
                    // 権限
                    String rname = data.get(2).toString();
                    String rbuff = rname.substring(0, 1);
                    int iRank = Integer.parseInt(rbuff);

                    // 元名GET
                    String moto_name = "";
                    ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                    args.add(new DatabaseArgs("c", pname));
                    PreparedStatement ps_name = api.getDB().getConnection().prepareStatement(Language.translate("Sql0026"));
                    ResultSet rs_name = api.getDB().ExecuteQuery(ps_name, args);
                    args.clear();
                    args = null;
                    if (rs_name != null) {
                        while (rs_name.next()) {
                            moto_name = rs_name.getString("元名");
                            break;
                        }
                    }
                    if (ps_name != null) {
                        ps_name.close();
                        ps_name = null;
                    }
                    if (rs_name != null) {
                        rs_name.close();
                        rs_name = null;
                    }

                    if (moto_name.length() > 0) {
                        // 権限更新
                        ArrayList<DatabaseArgs> rargs = new ArrayList<DatabaseArgs>();
                        rargs.add(new DatabaseArgs("i", ""+iRank));
                        rargs.add(new DatabaseArgs("c", moto_name));
                        int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0027"), rargs);
                        rargs.clear();
                        rargs = null;
                        if (ret > 0) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(TextFormat.GOLD);
                            sb.append("[権限] ");
                            sb.append(TextFormat.YELLOW);
                            sb.append(targetPlayer.getDisplayName());
                            sb.append("さんが [ ");
                            sb.append(TextFormat.WHITE);
                            sb.append(pname);
                            sb.append(TextFormat.YELLOW);
                            sb.append(" ] さんの権限を [ ");
                            sb.append(TextFormat.WHITE);
                            switch (iRank) {
                                case 0: sb.append("0:訪問"); break;
                                case 1: sb.append("1:住民"); break;
                                case 2: sb.append("2:ＧＭ"); break;
                                case 3: sb.append("3:パイ"); break;
                                case 4: sb.append("4:鯖主"); break;
                            }
                            sb.append(TextFormat.YELLOW);
                            sb.append(" ] へ更新しました！");
                            String message = new String(sb);

                            api.getMessage().SendInfoMessage(message, true);
                            api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin016, 0, false); // ハープ

                        } else {
                            api.getMessage().SendWarningMessage(Language.translate("commands.rank.fail"), targetPlayer);
                        }
                    } else {
                        api.getMessage().SendWarningMessage(Language.translate("commands.rank.fail"), targetPlayer);
                    }

                } catch (Exception e) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    api.getLogErr().Write(targetPlayer, api.getMessage().GetErrorMessage(e));
                }
            });

        } catch (Exception e) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }
}