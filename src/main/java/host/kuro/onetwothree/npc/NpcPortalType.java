package host.kuro.onetwothree.npc;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.datatype.ItemInfo;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.forms.elements.ModalForm;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NpcPortalType extends NpcType {

    public NpcPortalType(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean interact(Player player, Item item) {
        if (player == null) return false;
        WarpWindow(player);
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

            CustomForm form = new CustomForm(this.getName())
                    .addLabel("ワープする？")
                    .addDropDown("ワールドリスト", lvList)
                    .addDropDown("プレイヤーリスト", pList);

            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                if(data == null) {
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    player.sendMessage(api.GetWarningMessage("onetwothree.warp_err"));
                    return;
                }
                // ウィンドウログ
                api.getLogWin().Write(targetPlayer, "ワープ", data.get(1).toString(), data.get(2).toString(), "", "", "", "", targetPlayer.getDisplayName());

                // ワールド指定
                String sworld = data.get(1).toString();
                if (!sworld.equals("指定なし")) {
                    Position pos = api.getServer().getLevelByName(sworld).getSpawnLocation();
                    if (pos != null) {
                        if (player.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                            TeleportMessage(player, sworld);
                            return;
                        }
                    }
                }

                // プレイヤー指定
                String splayer = data.get(2).toString();
                if (!splayer.equals("指定なし")) {
                    try {
                        PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0021"));
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
                        if (target.getDisplayName().equalsIgnoreCase(player.getDisplayName())) {
                            player.sendMessage(api.GetWarningMessage("commands.warp.err_self"));
                        } else {
                            Position pos = target.getPosition();
                            if (pos != null) {
                                if (player.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                                    TeleportMessage(player, target.getDisplayName());
                                    return;
                                }
                            }
                        }
                        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL

                    } catch (SQLException e){
                        e.printStackTrace();
                        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        api.getLogErr().Write(player, "WarpWindow : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
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