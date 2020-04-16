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
            lvList.add(Language.translate("onetwothree.selection.none"));
            for (Level lv : api.getServer().getLevels().values()) {
                lvList.add(lv.getName());
            }

            List<String> pList = new ArrayList<String>();
            pList.add(Language.translate("onetwothree.selection.none"));
            for (Player p : api.getServer().getOnlinePlayers().values()) {
                pList.add(p.getDisplayName());
            }

            CustomForm form = new CustomForm(this.getName())
                    .addLabel(Language.translate("npc.portal.type01.message01"))
                    .addDropDown(Language.translate("npc.portal.wlist"), lvList)
                    .addDropDown(Language.translate("npc.portal.plist"), pList);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                if(data == null) {
                    api.getMessage().SendWarningMessage(Language.translate("onetwothree.warp_err"), targetPlayer);
                    return;
                }
                // ウィンドウログ
                api.getLogWin().Write(targetPlayer, Language.translate("commands.warp.title"), data.get(1).toString(), data.get(2).toString(), "", "", "", "", targetPlayer.getDisplayName());

                // ワールド指定
                String sworld = data.get(1).toString();
                if (!sworld.equals(Language.translate("onetwothree.selection.none"))) {
                    Position pos = api.getServer().getLevelByName(sworld).getSpawnLocation();
                    if (pos != null) {
                        if (targetPlayer.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                            TeleportMessage(targetPlayer, sworld);
                            return;
                        }
                    }
                }

                // プレイヤー指定
                String splayer = data.get(2).toString();
                if (!splayer.equals(Language.translate("onetwothree.selection.none"))) {
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
                        if (!target.getDisplayName().equalsIgnoreCase(targetPlayer.getDisplayName())) {
                            Position pos = target.getPosition();
                            if (pos != null) {
                                if (targetPlayer.teleport(pos, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                                    TeleportMessage(targetPlayer, target.getDisplayName());
                                    return;
                                }
                            }
                        }
                        api.getMessage().SendWarningMessage(Language.translate("commands.warp.err_self"), targetPlayer);

                    } catch (SQLException e){
                        api.getMessage().SendErrorMessage(Language.translate("commands.warp.err"), targetPlayer);
                        api.getLogErr().Write(targetPlayer, api.getMessage().GetErrorMessage(e));
                    }
                }
            });

        } catch (Exception e) {
            api.getMessage().SendErrorMessage(Language.translate("commands.warp.err"), player);
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }

    private void TeleportMessage(Player player, String target) {
        if (player.isSpectator()) return;
        api.getMessage().SendTeleportMessage(player, target);
    }

}