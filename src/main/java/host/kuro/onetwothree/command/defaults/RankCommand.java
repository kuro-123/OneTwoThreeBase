package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
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
        if (!this.testPermission(sender)) {
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
        // 権限取得
        int rank = api.GetRank(player);
        if (rank < 3) {
            player.sendMessage(api.GetWarningMessage("commands.rank.permission"));
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        return RankWindow(player, rank);
    }

    private boolean RankWindow(Player player, int rank) {
        try {
            // 時間がないのでとりあえず(後々修正)
            ArrayList<String> rank_list = new ArrayList<String>();
            rank_list.add("0:指定なし");
            rank_list.add("1:一般");
            rank_list.add("2:信任");
            rank_list.add("3:管理");
            if (rank >= 4) {
                rank_list.add("4:鯖主");
            }
            // プレイヤー一覧
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(api.getConfig().getString("SqlStatement.Sql0025"));
            ResultSet rs = api.getDB().ExecuteQuery(ps, null);
            api.player_list.put(0, "指定なし");
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

            CustomForm form = new CustomForm("権限設定")
                    .addLabel("権限の設定が行えます")
                    .addDropDown("プレイヤー", new ArrayList<>(api.player_list.values()))
                    .addDropDown("権限リスト", rank_list);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                try {
                    if (data == null) {
                        this.sendUsage(targetPlayer);
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }
                    // プレイヤー
                    String sindex1 = data.get(1).toString();
                    int index1 = Integer.parseInt(sindex1);
                    ArrayList<String> plist = new ArrayList<>(api.player_list.values());
                    String pname = plist.get(index1);
                    OneTwoThreeAPI.player_list.clear();

                    // 権限
                    String sindex2 = data.get(2).toString();
                    int index2 = Integer.parseInt(sindex2);

                    // 元名GET
                    String moto_name = "";
                    ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                    args.add(new DatabaseArgs("c", pname));
                    PreparedStatement ps_name = api.getDB().getConnection().prepareStatement(api.getConfig().getString("SqlStatement.Sql0026"));
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
                    plist.clear();
                    plist = null;

                    if (moto_name.length() > 0) {
                        // 権限更新
                        ArrayList<DatabaseArgs> rargs = new ArrayList<DatabaseArgs>();
                        rargs.add(new DatabaseArgs("i", ""+index2));
                        rargs.add(new DatabaseArgs("c", moto_name));
                        int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0027"), rargs);
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
                            switch (index2) {
                                case 0: sb.append("0:なし"); break;
                                case 1: sb.append("1:一般"); break;
                                case 2: sb.append("2:信任"); break;
                                case 3: sb.append("3:管理"); break;
                                case 4: sb.append("4:鯖主"); break;
                            }
                            sb.append(TextFormat.YELLOW);
                            sb.append(" ] へ更新しました！");
                            api.getServer().broadcastMessage(new String(sb));
                            api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin016, 0, false); // ハープ

                        } else {
                            player.sendMessage(api.GetWarningMessage("commands.rank.fail"));
                            api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        }
                    } else {
                        player.sendMessage(api.GetWarningMessage("commands.rank.fail"));
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    }

                } catch (Exception e) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    e.printStackTrace();
                }
            });
            rank_list.clear();
            rank_list = null;

        } catch (Exception e) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            e.printStackTrace();
            return false;
        }
        return true;
    }
}