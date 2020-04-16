package host.kuro.onetwothree.npc;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.network.protocol.AddPlayerPacket;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.utils.MtRand;

import java.math.BigDecimal;

public class NpcType extends EntityHuman {

    protected OneTwoThreeAPI api;
    protected int free_times = 0;
    protected int free_next = 10;
    private static final MtRand random = new MtRand(System.currentTimeMillis());

    public NpcType(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        api = OneTwoThreeAPI.getInstance();
    }

    private Player GetTarget() {
        Player target = null;
        double target_distance = 9.0D;
        for (Player player : this.getLevel().getPlayers().values()) {
            double distance = this.distanceSquared(player);
            if (distance < target_distance) {
                target = player;
                target_distance = distance;
            }
        }
        return target;
    }

    public float CheckMotionY(float value) {
        if (value < 1.0f) return value;
        Level level = this.getLevel();
        BigDecimal bd = new BigDecimal(value);
        BigDecimal bdex = bd.setScale(0, BigDecimal.ROUND_UP);
        int max = bdex.intValue();
        int i = 1;
        for(i= 1; i<max; i++) {
            int block_id = level.getBlockIdAt(this.getFloorX(), this.getFloorY() - i, this.getFloorZ());
            if (block_id != Block.AIR) {
                return (float)i;
            }
        }
        return value;
    }

    public void updateMove(int tickDiff) {
        this.motionX = 0;
        this.motionZ = 0;

        Player target = this.GetTarget();
        if (target != null) {
            double x = target.getX() - this.getX();
            double y = target.getY() - this.getY();
            double z = target.getZ() - this.getZ();
            double diff = Math.abs(x) + Math.abs(z);
            this.yaw = Math.toDegrees(-Math.atan2(x / diff, z / diff));
            this.pitch = y == 0 ? 0 : Math.toDegrees(-Math.atan2(y, Math.sqrt(x * x + z * z)));
            free_times = 0;
        } else {
            free_times++;
        }

        // キョロキョロ
        if (free_times > 60) {
            if ((free_times % free_next) == 0) {
                long value = random.Next(-450, 450);
                double dvalue = (double)value / 10.0D;
                this.pitch = dvalue;
                free_next = random.Next(4, 80);
                value = random.Next(-45, 45);
                this.yaw +=value;
            }
        }

        if (this.onGround) {
            this.motionY = 0;
        } else {
            if (this.level.getBlockIdAt((int) this.x, (int) this.boundingBox.getMaxY(), (int) this.z) == 8 || this.level.getBlockIdAt((int) this.x, (int) this.boundingBox.getMaxY(), (int) this.z) == 9) { //item is fully in water or in still water
                this.motionY -= this.getGravity() * -0.015;
            } else if (this.isInsideOfWater()) {
                this.motionY = this.getGravity() - 0.06; //item is going up in water, don't let it go back down too fast
            } else {
                this.motionY -= this.getGravity(); //item is not in water
            }
            this.move(motionX, motionY, motionZ);
        }
        this.updateMovement();
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        if (!this.isAlive()) {
            if (++this.deadTicks >= 23) {
                this.close();
                return false;
            }
            return true;
        }

        int tickDiff = currentTick - this.lastUpdate;
        this.lastUpdate = currentTick;
        this.entityBaseTick(tickDiff);

        this.updateMove(tickDiff);
        return true;
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.despawnFromAll();
            if (this.level != null) {
                SmokeParticle particle = new SmokeParticle(this);
                this.level.addParticle(particle);
                particle = null;
            }
            if (this.chunk != null) {
                this.chunk.removeEntity(this);
                this.chunk = null;
            }
            if (this.level != null) {
                this.level.removeEntity(this);
            }
            this.namedTag = null;
        }
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

            // スケーリング
            try {
                String sscale = getDataProperty(776).getData().toString();
                Float scale = Float.parseFloat(sscale);
                this.setScale(scale);
            } catch (Exception e) {
                //api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            }
        }
    }

    public boolean interact(Player player, Item item) {
        return false;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return false;
    }
}