package host.kuro.onetwothree.npc;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.datatype.ItemInfo;
import host.kuro.onetwothree.forms.elements.ModalForm;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.util.ArrayList;

public class NpcMerchantType01 extends NpcType {

    public NpcMerchantType01(FullChunk chunk, CompoundTag nbt) {
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
            SimpleForm form = new SimpleForm(this.getName(), "手にアイテムを持っていませんよ");
            form.send(player, (targetPlayer, targetForm, data) -> {});
            return;
        }
        if (!api.IsTagItem(item)) {
            // タグアイテム以外はエラー
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin021, 0, false); // 失敗
            SimpleForm form = new SimpleForm(this.getName(), "そのアイテムは買えないアイテムですね");
            form.send(player, (targetPlayer, targetForm, data) -> {});
            return;
        }
        ItemInfo ip = api.item_info.get(item.getId());
        if (ip == null) {
            // 価格が0以下はエラー
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin021, 0, false); // 失敗
            SimpleForm form = new SimpleForm(this.getName(), "そのアイテムは価格設定されていないみたいです");
            form.send(player, (targetPlayer, targetForm, data) -> {});
            return;
        }
        int price = ip.price;
        if (price <= 0) {
            // 価格が0以下はエラー
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin021, 0, false); // 失敗
            SimpleForm form = new SimpleForm(this.getName(), "そのアイテムは価格設定されていないみたいです");
            form.send(player, (targetPlayer, targetForm, data) -> {});
            return;
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
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.voi044, 0, false); // 男性
        form.send(player, (targetPlayer, targetForm, data) -> {
            try {
                if (data != 0) {
                    // キャンセル
                    targetPlayer.sendMessage(api.GetErrMessage("commands.sell.err_cansel"));
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                // ウィンドウログ
                api.getLogWin().Write(targetPlayer, "アイテム売却", ""+data, "", "", "", "", "", targetPlayer.getDisplayName());

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
                    targetPlayer.sendMessage(api.GetErrMessage("commands.sell.err_update"));
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                api.getLogPay().Write(targetPlayer, this.getName(), "plus", ""+sell_kingaku);

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
                targetPlayer.sendMessage(api.GetErrMessage("onetwothree.cmderror"));
                api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                api.getLogErr().Write(targetPlayer, "SellCommand : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), targetPlayer.getDisplayName());
                return;
            }
        });
    }
}