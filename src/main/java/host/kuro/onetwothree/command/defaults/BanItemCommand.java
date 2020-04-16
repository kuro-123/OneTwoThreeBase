package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.datatype.ItemInfo;
import host.kuro.onetwothree.task.SoundTask;

import java.util.ArrayList;
import java.util.Iterator;

public class BanItemCommand extends CommandBase {

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
            api.getMessage().SendUsage(this, sender);
            return false;
        }
        // 権限チェック
        if (!api.IsGameMaster(player)) {
            api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
            return false;
        }
        return BanItemWindow(player);
    }

    private boolean BanItemWindow(Player player) {
        try {
            ArrayList<String> ilist = new ArrayList<String>();
            ilist.add(Language.translate("onetwothree.selection.none"));
            for(Iterator<ItemInfo> iterator = api.item_info.values().iterator(); iterator.hasNext(); ) {
                ItemInfo value = iterator.next();
                ilist.add("[ ID: " + value.id + " ] < " + value.name + " > バン: " + value.ban);
            }
            CustomForm form = new CustomForm(Language.translate("commands.banitem.title"))
                    .addLabel(Language.translate("commands.banitem.message01"))
                    .addDropDown(Language.translate("commands.banitem.list"), ilist);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                try {
                    if (data == null) {
                        this.sendUsage(targetPlayer);
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }
                    // ウィンドウログ
                    api.getLogWin().Write(targetPlayer, Language.translate("commands.banitem.title"), data.get(1).toString(), "", "", "", "", "", targetPlayer.getDisplayName());

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
                        api.getMessage().SendErrorMessage(Language.translate("commands.banitem.id_err"), targetPlayer);
                        api.getLogErr().Write(targetPlayer, api.getMessage().GetErrorMessage(e));
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
                        int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0045"), args);
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

                            api.getMessage().SendInfoMessage(Language.translate("commands.banitem.success"), targetPlayer);
                        } else {
                            api.getMessage().SendWarningMessage(Language.translate("commands.banitem.fail"), targetPlayer);
                        }
                    }

                } catch (Exception e) {
                    api.getMessage().SendErrorMessage(Language.translate("commands.banitem.fail"), targetPlayer);
                    api.getLogErr().Write(targetPlayer, api.getMessage().GetErrorMessage(e));
                }
            });

        } catch (Exception e) {
            this.sendUsage(player);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
        }
        return true;
    }
}