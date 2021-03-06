package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.forms.elements.ModalForm;
import host.kuro.onetwothree.datatype.ItemInfo;
import host.kuro.onetwothree.task.SoundTask;

import java.util.ArrayList;

public class SellCommand extends CommandBase {

    public SellCommand(OneTwoThreeAPI api) {
        super("sell", api);
        this.setAliases(new String[]{"se"});
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
        if (!api.IsJyumin(player)) {
            api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
            return false;
        }
        try {
            Item item = player.getInventory().getItemInHand();
            if (item == null) {
                // 手持ちなしはエラー
                api.getMessage().SendWarningMessage(Language.translate("commands.sell.err_itemnone"), player);
                return false;
            }
            if (!api.IsTagItem(item)) {
                // タグアイテム以外はエラー
                api.getMessage().SendWarningMessage(Language.translate("commands.sell.err_tagitem"), player);
                return false;
            }
            ItemInfo ip = api.item_info.get(item.getId());
            int price = ip.price;
            if (price <= 0) {
                // 価格が0以下はエラー
                api.getMessage().SendWarningMessage(Language.translate("commands.sell.err_price"), player);
                return false;
            }

            String item_name = item.getName();
            int suryo = item.getCount();
            int kingaku = price * suryo;
            StringBuilder sb = new StringBuilder();
            sb.append("手持ちアイテム [ ");
            sb.append(item_name);
            sb.append(" ]\n\n");
            sb.append("数量 : " + suryo + "個\n");
            sb.append("金額 : " + kingaku + "p\n\n");
            sb.append("売却しますか？");
            ModalForm form = new ModalForm("アイテム売却", new String(sb),"はい","いいえ");
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                try {
                    if (data != 0) {
                        // キャンセル
                        api.getMessage().SendWarningMessage(Language.translate("commands.sell.err_cansel"), targetPlayer);
                        return;
                    }
                    // ウィンドウログ
                    api.getLogWin().Write(targetPlayer, "アイテム売却", ""+data, "", "", "", "", "", targetPlayer.getDisplayName());

                    Item sell_item = targetPlayer.getInventory().getItemInHand();
                    if (!api.IsTagItem(sell_item)) {
                        // タグアイテム以外はエラー
                        api.getMessage().SendWarningMessage(Language.translate("commands.sell.err_tagitem"), targetPlayer);
                        return;
                    }
                    ItemInfo sell_ip = api.item_info.get(sell_item.getId());
                    int sell_price = sell_ip.price;
                    if (sell_price <= 0) {
                        // 価格が0以下はエラー
                        api.getMessage().SendWarningMessage(Language.translate("commands.sell.err_price"), targetPlayer);
                        return;
                    }
                    String sell_item_name = sell_item.getName();
                    int sell_suryo = sell_item.getCount();
                    int sell_kingaku = sell_price * sell_suryo;

                    // 金額更新
                    ArrayList<DatabaseArgs> margs = new ArrayList<DatabaseArgs>();
                    margs.add(new DatabaseArgs("c", targetPlayer.getLoginChainData().getXUID()));
                    margs.add(new DatabaseArgs("i", ""+sell_kingaku));
                    //margs.add(new DatabaseArgs("i", ""+sell_kingaku));
                    int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0029"), margs);
                    margs.clear();
                    margs = null;
                    if (ret <= 0) {
                        // 更新エラー
                        api.getMessage().SendWarningMessage(Language.translate("commands.sell.err_update"), targetPlayer);
                        return;
                    }

                    // アイテムクリア
                    targetPlayer.getInventory().setItemInHand(new ItemBlock(new BlockAir()));

                    // メッセージ
                    StringBuilder ssb = new StringBuilder();
                    ssb.append("手持ちアイテム [ ");
                    ssb.append(TextFormat.YELLOW);
                    ssb.append(sell_item_name);
                    ssb.append(" ");
                    ssb.append(sell_suryo + "個 ");
                    ssb.append(TextFormat.WHITE);
                    ssb.append("] を [ ");
                    ssb.append(TextFormat.YELLOW);
                    ssb.append(sell_kingaku);
                    ssb.append("p ");
                    ssb.append(TextFormat.WHITE);
                    ssb.append("] で売却しました！");
                    targetPlayer.sendMessage(new String(ssb));
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin071, 0, false); // レジスタ

                } catch (Exception e) {
                    api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), targetPlayer);
                    api.getLogErr().Write(targetPlayer, api.getMessage().GetErrorMessage(e));
                    return;
                }
            });

        } catch (Exception e) {
            api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), player);
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }
}