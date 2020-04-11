package host.kuro.onetwothree.npc;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.network.protocol.AddPlayerPacket;

import java.nio.charset.StandardCharsets;

public class NpcMerchant extends EntityHuman {

    public NpcMerchant(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setDataProperty(new FloatEntityData(DATA_SCALE, this.namedTag.getFloat("scale")));
    }

    @Override
    public void spawnTo(Player player) {
        if (!this.hasSpawned.containsKey(player.getLoaderId())) {
            this.hasSpawned.put(player.getLoaderId(), player);

            this.server.updatePlayerListData(this.getUniqueId(), this.getId(), this.getName(), this.skin, new Player[]{player});

            AddPlayerPacket pk = new AddPlayerPacket();
            pk.uuid = this.getUniqueId();
            pk.username = this.getName();
            pk.entityUniqueId = this.getId();
            pk.entityRuntimeId = this.getId();
            pk.x = (float) this.x;
            pk.y = (float) this.y;
            pk.z = (float) this.z;
            pk.speedX = (float) this.motionX;
            pk.speedY = (float) this.motionY;
            pk.speedZ = (float) this.motionZ;
            pk.yaw = (float) this.yaw;
            pk.pitch = (float) this.pitch;
            this.inventory.setItemInHand(Item.fromString(this.namedTag.getString("Item")));
            pk.item = this.getInventory().getItemInHand();
            pk.metadata = this.dataProperties;
            player.dataPacket(pk);

            this.inventory.setHelmet(Item.fromString(this.namedTag.getString("Helmet")));
            this.inventory.setChestplate(Item.fromString(this.namedTag.getString("Chestplate")));
            this.inventory.setLeggings(Item.fromString(this.namedTag.getString("Leggings")));
            this.inventory.setBoots(Item.fromString(this.namedTag.getString("Boots")));
            this.inventory.sendArmorContents(player);

            this.server.removePlayerListData(this.getUniqueId(), new Player[]{player});
            super.spawnTo(player);
        }
    }
}