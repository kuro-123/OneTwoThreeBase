package host.kuro.onetwothree.npc;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.*;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.datatype.ItemInfo;
import host.kuro.onetwothree.forms.elements.ModalForm;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.util.ArrayList;

public class NpcMerchantType02 extends NpcType {

    public NpcMerchantType02(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean interact(Player player, Item item) {
        if (player == null) return false;
        SellWindow(player);
        return false;
    }

    private void SellWindow(Player player) {
        Item item = player.getInventory().getItemInHand();
        if (item == null) {
            // 手持ちなしはエラー
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin021, 0, false); // 失敗
            SimpleForm form = new SimpleForm(this.getName(), Language.translate("npc.merchant.type02.message01"));
            form.send(player, (targetPlayer, targetForm, data) -> {});
            return;
        }
        if (!api.IsTagItem(item)) {
            // タグアイテム以外はエラー
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin021, 0, false); // 失敗
            SimpleForm form = new SimpleForm(this.getName(), Language.translate("npc.merchant.type02.message02"));
            form.send(player, (targetPlayer, targetForm, data) -> {});
            return;
        }
        ItemInfo ip = api.item_info.get(item.getId());
        if (ip == null) {
            // 価格が0以下はエラー
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin021, 0, false); // 失敗
            SimpleForm form = new SimpleForm(this.getName(), Language.translate("npc.merchant.type02.message03"));
            form.send(player, (targetPlayer, targetForm, data) -> {});
            return;
        }
        int price = ip.price;
        if (price <= 0) {
            // 価格が0以下はエラー
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin021, 0, false); // 失敗
            SimpleForm form = new SimpleForm(this.getName(), Language.translate("npc.merchant.type02.message03"));
            form.send(player, (targetPlayer, targetForm, data) -> {});
            return;
        }

        String message = api.getMessage().GetSellMessage(item, price);
        ModalForm form = new ModalForm(Language.translate("npc.merchant.sell"), message,Language.translate("onetwothree.yes"),Language.translate("onetwothree.no"));
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.voi050, 0, false); // 女性
        form.send(player, (targetPlayer, targetForm, data) -> {
            try {
                if (data != 0) {
                    // キャンセル
                    api.getMessage().SendWarningMessage(Language.translate("commands.sell.err_cansel"), targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                // ウィンドウログ
                api.getLogWin().Write(targetPlayer, Language.translate("npc.merchant.sell"), ""+data, "", "", "", "", "", targetPlayer.getDisplayName());

                Item sell_item = targetPlayer.getInventory().getItemInHand();
                ItemInfo sell_ip = api.item_info.get(sell_item.getId());
                int sell_price = sell_ip.price;
                String sell_item_name = sell_item.getName();
                int sell_suryo = sell_item.getCount();
                int sell_kingaku = sell_price * sell_suryo;

                // 金額更新
                ArrayList<DatabaseArgs> margs = new ArrayList<DatabaseArgs>();
                margs.add(new DatabaseArgs("c", targetPlayer.getLoginChainData().getXUID()));
                margs.add(new DatabaseArgs("i", ""+sell_kingaku));
                int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0029"), margs);
                margs.clear();
                margs = null;
                if (ret <= 0) {
                    // 更新エラー
                    api.getMessage().SendWarningMessage(Language.translate("commands.sell.err_update"), targetPlayer);
                    return;
                }
                api.getLogPay().Write(targetPlayer, this.getName(), "plus", ""+sell_kingaku);

                // アイテムクリア
                targetPlayer.getInventory().setItemInHand(new ItemBlock(new BlockAir()));

                // メッセージ
                api.getMessage().SendSelledMessage(targetPlayer, sell_item_name, sell_suryo, sell_kingaku);

            } catch (Exception e) {
                api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), targetPlayer);
                api.getLogErr().Write(targetPlayer, api.getMessage().GetErrorMessage(e));
            }
        });
    }
}