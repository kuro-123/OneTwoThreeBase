package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.npc.NpcType;

import java.util.ArrayList;

public class CraftTask extends AsyncTask {

    private final OneTwoThreeAPI api;
    private final Player player;
    private final ArrayList<Item> old;
    private final Item result;

    public CraftTask(OneTwoThreeAPI api, Player player, ArrayList<Item> old, Item result) {
        this.api = api;
        this.player = player;
        this.old = old;
        this.result = result;
    }

    @Override
    public void onRun() {
        String symbol = api.getConfig().getString("GameSettings.ItemTag");
        if (symbol.length() <= 0) return;

        PlayerInventory nowInventory = player.getInventory();
        int max = nowInventory.getSize();
        for (int i=0; i<max; i++) {
            Item nowitem = nowInventory.getItem(i);
            Item olditem = old.get(i);
            if (nowitem.getId() == olditem.getId()) continue;
            if (nowitem.getId() == result.getId()) {
                nowitem.setCustomName(nowitem.getName()+symbol);
                player.getInventory().setItem(i, nowitem);
            }
        }
        old.clear();
    }
}
