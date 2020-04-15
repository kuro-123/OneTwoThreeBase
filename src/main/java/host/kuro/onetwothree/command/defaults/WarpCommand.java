package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class WarpCommand extends CommandBase {

    public WarpCommand(OneTwoThreeAPI api) {
        super("warp", api);
        this.setAliases(new String[]{"wp", "w"});

        // command parameters
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("point name", CommandParamType.STRING, true),
        });
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
        if (args.length != 1) {
            // 引数なしはフォーム選択
            return WarpWindow(player);
        }
        String target = args[0].toLowerCase();
        for (Level lv : api.getServer().getLevels().values()) {
            if (target.equals(lv.getName().toLowerCase())) {
                // HITしたらそのワールドのスポーン地点まで飛ぶ
                Position pos = lv.getSpawnLocation();
                if (player.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                    TeleportMessage(player, lv.getName());
                }
                return true;
            }
        }
        target = api.AmbiguousSearch(target);
        if (target.equalsIgnoreCase(player.getDisplayName())) {
            player.sendMessage(api.GetWarningMessage("commands.warp.err_self"));
        } else {
            for (Player p : api.getServer().getOnlinePlayers().values()) {
                if (target.equals(p.getDisplayName().toLowerCase()) || target.equals(p.getName().toLowerCase())) {
                    Location pos = p.getLocation();
                    if (player.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                        TeleportMessage(player, p.getDisplayName());
                    }
                    return true;
                }
            }
        }
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
        return false;
    }

    private boolean WarpWindow(Player player) {
        try {
            List<String> lvList = new ArrayList<String>();
            lvList.add("指定なし");
            for (Level lv : api.getServer().getLevels().values()) {
                lvList.add(lv.getName());
            }

            List<String> pList = new ArrayList<String>();
            pList.add("指定なし");
            for (Player p : api.getServer().getOnlinePlayers().values()) {
                pList.add(p.getDisplayName());
            }

            CustomForm form = new CustomForm("ワープウィンドウ")
                    .addLabel("下記のいずれかを指定しワープします 支払金額:150p")
                    .addDropDown("ワールドリスト", lvList)
                    .addDropDown("プレイヤーリスト", pList);

            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                if(data == null) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                // ウィンドウログ
                api.getLogWin().Write(targetPlayer, "ワープ", data.get(1).toString(), data.get(2).toString(), "", "", "", "", targetPlayer.getDisplayName());

                // ワールド指定
                String sworld = data.get(1).toString();
                if (!sworld.equals("指定なし")) {
                    Position pos = api.getServer().getLevelByName(sworld).getSpawnLocation();
                    if (pos != null) {
                        int money = api.GetMoney(targetPlayer);
                        if (money == -1) {
                            targetPlayer.sendMessage(api.GetWarningMessage("commands.warp.err_money_01"));
                            api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                            return;
                        }
                        //if (money < 150) {
                        //    player.sendMessage(api.GetWarningMessage("commands.warp.err_money_02"));
                        //    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        //    return;
                        //}
                        // ワールドへワープ
                        if (targetPlayer.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                            boolean ret = api.PayMoney(targetPlayer, 150);
                            if (!ret) {
                                targetPlayer.sendMessage(api.GetWarningMessage("commands.warp.err_money_03"));
                                api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                                return;
                            } else {
                                TeleportMessage(targetPlayer, sworld);
                                money = api.GetMoney(targetPlayer);
                                targetPlayer.sendMessage(TextFormat.YELLOW + "現在の所持金:" + money + "p");
                            }
                            api.getLogPay().Write(targetPlayer, "WarpCommand", "minus", ""+150);
                            return;
                        }
                    }
                }

                // プレイヤー指定
                String splayer = data.get(2).toString();
                if (!splayer.equals("指定なし")) {
                    try {
                        PreparedStatement ps = api.getDB().getConnection().prepareStatement(api.getConfig().getString("SqlStatement.Sql0021"));
                        ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
                        pargs.add(new DatabaseArgs("c", splayer.toLowerCase()));
                        ResultSet rs = api.getDB().ExecuteQuery(ps, pargs);
                        pargs.clear();
                        pargs = null;
                        if (rs != null) {
                            while (rs.next()) {
                                splayer = rs.getString("xname");
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

                        Player target = api.getServer().getPlayerExact(splayer);
                        if (target.getDisplayName().equalsIgnoreCase(targetPlayer.getDisplayName())) {
                            targetPlayer.sendMessage(api.GetWarningMessage("commands.warp.err_self"));
                        } else {
                            Position pos = target.getPosition();
                            if (pos != null) {
                                int money = api.GetMoney(targetPlayer);
                                if (money == -1) {
                                    targetPlayer.sendMessage(api.GetWarningMessage("commands.warp.err_money_01"));
                                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                                    return;
                                }
                                //if (money < 150) {
                                //    player.sendMessage(api.GetWarningMessage("commands.warp.err_money_02"));
                                //    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                                //    return;
                                //}
                                // 人へワープ
                                if (targetPlayer.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                                    boolean ret = api.PayMoney(targetPlayer, 150);
                                    if (!ret) {
                                        targetPlayer.sendMessage(api.GetWarningMessage("commands.warp.err_money_03"));
                                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                                        return;
                                    } else {
                                        TeleportMessage(targetPlayer, target.getDisplayName());
                                        money = api.GetMoney(targetPlayer);
                                        targetPlayer.sendMessage(TextFormat.YELLOW + "現在の所持金:" + money + "p");
                                    }
                                    api.getLogPay().Write(targetPlayer, "WarpCommand", "minus", ""+150);
                                    return;
                                }
                            }
                        }
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL

                    } catch (SQLException e){
                        e.printStackTrace();
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        api.getLogErr().Write(targetPlayer, "WarpWindow : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), targetPlayer.getDisplayName());
                    }
                }
            });

        } catch (Exception e) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            e.printStackTrace();
            api.getLogErr().Write(player, "WarpWindow : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
            return false;
        }
        return true;
    }

    private void TeleportMessage(Player player, String target) {
        if (player.isSpectator()) return;

        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append("[ﾜｰﾌﾟ] ");
        sb.append("[ ");
        sb.append(TextFormat.WHITE);
        sb.append(player.getDisplayName());
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append(" ] -> [ ");
        sb.append(TextFormat.WHITE);
        sb.append(target);
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append(" ]");
        String message = new String(sb);
        api.getServer().broadcastMessage(message);
        api.sendDiscordGreenMessage(message);

        api.PlaySound(player, SoundTask.MODE_BROADCAST, SoundTask.jin020, 0, false); // WARP
    }
}