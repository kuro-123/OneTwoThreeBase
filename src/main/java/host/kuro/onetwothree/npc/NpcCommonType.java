package host.kuro.onetwothree.npc;

import cn.nukkit.Player;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.*;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

public class NpcCommonType extends NpcType {

    private String message;

    public NpcCommonType(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public void SetMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        Player player = null;
        if (!(source instanceof EntityDamageByEntityEvent)) return false;
        if (!(((EntityDamageByEntityEvent) source).getDamager() instanceof Player)) return false;

        player = (Player)((EntityDamageByEntityEvent) source).getDamager();
        if (player == null) return false;

        StringBuilder sb = new StringBuilder();
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin054, 0, false); // ウィンドウ
        SimpleForm form = new SimpleForm(this.getName(), message);
        form.send(player, (targetPlayer, targetForm, data) -> {});
        return false;
    }
}