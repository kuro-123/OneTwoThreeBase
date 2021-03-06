package host.kuro.onetwothree.npc;

import cn.nukkit.Player;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.*;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

public class NpcCommonType extends NpcType {

    public NpcCommonType(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean interact(Player player, Item item) {
        if (player == null) return false;

        StringBuilder sb = new StringBuilder();
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin054, 0, false); // ウィンドウ

        String message = "";
        message = GetMessage();
        SimpleForm form = new SimpleForm(this.getName(), message);
        form.send(player, (targetPlayer, targetForm, data) -> {});
        return false;
    }
}