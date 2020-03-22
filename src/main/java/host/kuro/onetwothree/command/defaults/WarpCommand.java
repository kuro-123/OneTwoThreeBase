package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.forms.FormResponse;
import host.kuro.onetwothree.forms.elements.CustomForm;

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
    	if (!this.testPermission(sender)) {
            return false;
        }

        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)){
            player = (Player) sender;
        }
        if (player == null) {
            this.sendUsage(sender);
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
        return true;
    }

    private boolean WarpWindow(Player player) {
        try {
            List<String> lvList = new ArrayList<String>();
            lvList.add("指定なし");
            for (Level lv : api.getServer().getLevels().values()) {
                lvList.add(lv.getName());
            }
            if (OneTwoThreeAPI.wp_world.containsKey(player)) {
                OneTwoThreeAPI.wp_world.remove(player);
            }
            OneTwoThreeAPI.wp_world.put(player, lvList);

            List<String> pList = new ArrayList<String>();
            pList.add("指定なし");
            for (Player p : api.getServer().getOnlinePlayers().values()) {
                pList.add(p.getDisplayName());
            }
            if (OneTwoThreeAPI.wp_player.containsKey(player)) {
                OneTwoThreeAPI.wp_player.remove(player);
            }
            OneTwoThreeAPI.wp_player.put(player, pList);

            CustomForm form = new CustomForm("ワープウィンドウ")
                    .addLabel("下記のいずれかを指定しワープします")
                    .addDropDown("ワールドリスト", lvList)
                    .addDropDown("プレイヤーリスト", pList);

            form.send(player, (targetPlayer, targetForm, data) -> {
                if(data == null) {
                    this.sendUsage(targetPlayer);
                    return;
                }

                int cnt;

                // ワールド指定
                String world_idx = data.get(1).toString();
                int widx = Integer.parseInt(world_idx);
                if (widx > 0) {
                    List<String> worlds = OneTwoThreeAPI.wp_world.get(targetPlayer);
                    String world = worlds.get(widx);
                    Position pos = api.getServer().getLevelByName(world).getSpawnLocation();
                    if (pos != null) {
                        if (player.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                            TeleportMessage(player, world);
                        }
                    }
                    OneTwoThreeAPI.wp_world.remove(targetPlayer);
                    return;
                }
                OneTwoThreeAPI.wp_world.remove(targetPlayer);

                // プレイヤー指定
                String player_idx = data.get(2).toString();
                int pidx = Integer.parseInt(player_idx);
                if (pidx > 0) {
                    List<String> players = OneTwoThreeAPI.wp_player.get(targetPlayer);
                    String pname = players.get(pidx);
                    if (pname.length() > 0) {
                        try {
                            PreparedStatement ps = api.getDB().getConnection().prepareStatement(api.getConfig().getString("SqlStatement.Sql0021"));
                            ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
                            pargs.add(new DatabaseArgs("c", pname.toLowerCase()));
                            ResultSet rs = api.getDB().ExecuteQuery(ps, pargs);
                            pargs.clear();
                            pargs = null;
                            if (rs != null) {
                                while (rs.next()) {
                                    pname = rs.getString("xname");
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
                        } catch (SQLException e){
                            e.printStackTrace();
                        }
                    }
                    Player target = api.getServer().getPlayerExact(pname);
                    if (target.getDisplayName().equalsIgnoreCase(player.getDisplayName())) {
                        player.sendMessage(api.GetWarningMessage("commands.warp.err_self"));
                    } else {
                        Position pos = target.getPosition();
                        if (pos != null) {
                            if (player.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                                TeleportMessage(player, target.getDisplayName());
                            }
                        }
                    }
                    OneTwoThreeAPI.wp_player.remove(targetPlayer);
                    return;
                }
                OneTwoThreeAPI.wp_player.remove(targetPlayer);
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void TeleportMessage(Player player, String target) {
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
        api.getServer().broadcastMessage(new String(sb));
    }
}