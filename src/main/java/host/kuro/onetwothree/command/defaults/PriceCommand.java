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

public class PriceCommand extends CommandBase {

    private Config cfg;

    public PriceCommand(OneTwoThreeAPI api) {
        super("price", api);
        this.setAliases(new String[]{"pr", "p"});
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
        return PriceWindow(player);
    }

    private boolean PriceWindow(Player player) {
        try {
            ArrayList<String> ilist = new ArrayList<String>();
            ilist.add("指定なし");
            for(Iterator<ItemInfo> iterator = api.item_info.values().iterator(); iterator.hasNext(); ) {
                ItemInfo value = iterator.next();
                ilist.add("[ ID: " + value.id + " ] < " + value.name + " > 現価格: " + value.price);
            }
            CustomForm form = new CustomForm("価格設定")
                .addLabel("価格の設定が行えます")
                .addDropDown("アイテムリスト", ilist)
                .addInput("設定価格", "金額入力(0〜100)", "0");
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                    try {
                        if (data == null) {
                            this.sendUsage(targetPlayer);
                            api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                            return;
                        }
                        // ウィンドウログ
                        api.getLogWin().Write(targetPlayer, "価格設定", data.get(1).toString(), data.get(2).toString(), "", "", "", "", targetPlayer.getDisplayName());

                        // 価格
                        String sprice = "";
                        int price = 0;
                        try {
                            sprice = data.get(2).toString();
                            price = Integer.parseInt(sprice);
                            if (!(0 <= price && price <= 100)) {
                                player.sendMessage(api.GetWarningMessage("commands.price.price_err"));
                                api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                                return;
                            }
                        } catch (Exception e) {
                            player.sendMessage(api.GetWarningMessage("commands.price.price_err"));
                            api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                            api.getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
                            return;
                        }

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
                            player.sendMessage(api.GetWarningMessage("commands.price.id_err"));
                            api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                            api.getLogErr().Write(player, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
                            return;
                        }

                        ItemInfo iteminfo = null;
                        if (api.item_info.containsKey(id)) {
                            iteminfo = api.item_info.get(id);
                        }
                        if (iteminfo != null) {
                            // アイテム情報更新
                            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                            args.add(new DatabaseArgs("i", sprice));
                            args.add(new DatabaseArgs("c", targetPlayer.getDisplayName()));
                            args.add(new DatabaseArgs("i",""+iteminfo.id));
                            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0024"), args);
                            args.clear();
                            args = null;
                            if (ret > 0) {
                                // メモリ更新
                                iteminfo.price = Integer.parseInt(sprice);
                                api.item_info.put(id, iteminfo);

                                player.sendMessage(api.GetWarningMessage("commands.price.success"));
                                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS
                            } else {
                                player.sendMessage(api.GetWarningMessage("commands.price.fail"));
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