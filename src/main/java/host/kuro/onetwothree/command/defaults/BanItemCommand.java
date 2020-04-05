package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.Config;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.item.ItemInfo;
import host.kuro.onetwothree.task.SoundTask;

import java.util.ArrayList;
import java.util.Iterator;

public class BanItemCommand extends CommandBase {

    private Config cfg;

    public BanItemCommand(OneTwoThreeAPI api) {
        super("banitem", api);
        this.setAliases(new String[]{"bi"});
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
        if (!api.IsKanri(player)) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
            return false;
        }
        return BanItemWindow(player);
    }

    private boolean BanItemWindow(Player player) {
        try {
            ArrayList<String> ilist = new ArrayList<String>();
            ilist.add("指定なし");
            for(Iterator<ItemInfo> iterator = api.item_info.values().iterator(); iterator.hasNext(); ) {
                ItemInfo value = iterator.next();
                ilist.add("[ ID: " + value.id + " ] < " + value.name + " > バン: " + value.ban);
            }
            CustomForm form = new CustomForm("ＢＡＮアイテム設定")
                    .addLabel("ＢＡＮアイテムの設定(ON/OFF)が行えます")
                    .addDropDown("アイテムリスト", ilist);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                try {
                    if (data == null) {
                        this.sendUsage(targetPlayer);
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }
                    // ウィンドウログ
                    api.getLogWin().Write(targetPlayer, "ＢＡＮアイテム設定", data.get(1).toString(), "", "", "", "", "", targetPlayer.getDisplayName());

                    // ID
                    String sitem = "";
                    int id = 0;
                    try {
                        sitem = data.get(1).toString();
                        int pos= sitem.indexOf(" ]");
                        String itemid = sitem.substring(5, pos);
                        itemid = itemid.trim();
                        id = Integer.parseInt(itemid);

                    } catch (Exception e) {
                        player.sendMessage(api.GetWarningMessage("commands.banitem.id_err"));
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        api.getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
                        return;
                    }

                    ItemInfo ip = null;
                    if (api.item_info.containsKey(id)) {
                        ip = api.item_info.get(id);
                    }
                    if (ip != null) {
                        // バンアイテム情報更新
                        int iban = 0;
                        if (ip.ban.equals("〇")) {
                            iban = 0;
                        } else {
                            iban = 1;
                        }
                        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                        args.add(new DatabaseArgs("i", ""+iban));
                        args.add(new DatabaseArgs("c", targetPlayer.getDisplayName()));
                        args.add(new DatabaseArgs("i",""+ip.id));
                        int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0045"), args);
                        args.clear();
                        args = null;
                        if (ret > 0) {
                            // メモリ更新
                            if (iban == 0) {
                                ip.ban = "";
                            } else {
                                ip.ban = "〇";
                            }
                            api.item_info.put(id, ip);
                            player.sendMessage(api.GetWarningMessage("commands.banitem.success"));
                            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS
                        } else {
                            player.sendMessage(api.GetWarningMessage("commands.banitem.fail"));
                            api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        }
                    }

                } catch (Exception e) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    e.printStackTrace();
                    api.getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
                }
            });

        } catch (Exception e) {
            this.sendUsage(player);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            e.printStackTrace();
            api.getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
        return true;
    }
}