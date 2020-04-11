package host.kuro.onetwothree;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.*;
import host.kuro.onetwothree.datatype.NpcInfo;
import host.kuro.onetwothree.npc.NpcCommonType;
import host.kuro.onetwothree.npc.NpcType;
import host.kuro.onetwothree.npc.NpcMerchantType01;
import host.kuro.onetwothree.npc.NpcMerchantType02;
import host.kuro.onetwothree.task.SoundTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NpcPlugin {

    public static enum NPC_KIND {
        KIND_NONE,
        KIND_COMMON_TYPE01,
        KIND_COMMON_TYPE02,
        KIND_MERCHANT_TYPE01,
        KIND_MERCHANT_TYPE02,
    };

    private final OneTwoThreeAPI api;
    private Skin skin = null;
    private Position p = null;
    private String name = "";
    private String tag = "";
    private Item hand = null;
    private Item helmet = null;
    private Item chestplate = null;
    private Item leggings = null;
    private Item boots = null;
    private Float yaw = 0.0F;
    private Float pitch = 0.0F;
    private float scale = 1.0f;

    private static final int SKIN_SIZE = 64 * 32 * 4;
    private byte[] data = new byte[SKIN_SIZE];

    public NpcPlugin(OneTwoThreeAPI api) {
        this.api = api;
        Entity.registerEntity(NpcMerchantType01.class.getSimpleName(), NpcMerchantType01.class);
        Entity.registerEntity(NpcMerchantType02.class.getSimpleName(), NpcMerchantType02.class);
        Entity.registerEntity(NpcCommonType.class.getSimpleName(), NpcCommonType.class);
    }
    public void SetPosition(Position pos) {
        this.p = pos;
    }
    public void SetSkin(Skin skin) {
        this.skin = skin;
    }
    public void SetName(String name) {
        this.name = name;
    }
    public void SetTag(String tag) {
        this.tag = tag;
    }
    public void SetHand(Item item) {
        this.hand = item;
    }
    public void SetHelmet(Item item) {
        this.helmet = item;
    }
    public void SetChestplate(Item item) {
        this.chestplate = item;
    }
    public void SetLeggings(Item item) {
        this.leggings = item;
    }
    public void SetBoots(Item item) {
        this.boots = item;
    }
    public void SetYaw(Float yaw) {
        this.yaw = yaw;
    }
    public void SetPitch(Float pitch) {
        this.pitch = pitch;
    }
    public void SetScale(float scale) {
        this.scale = scale;
    }

    public CompoundTag nbt() {
        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<>("Pos")
                        .add(new DoubleTag("", p.x))
                        .add(new DoubleTag("", p.y))
                        .add(new DoubleTag("", p.z)))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", yaw))
                        .add(new FloatTag("", pitch)))
                .putBoolean("Invulnerable", true)
                .putString("NameTag", name)
                .putList(new ListTag<StringTag>("Commands"))
                .putList(new ListTag<StringTag>("PlayerCommands"))
                .putBoolean("npc", true)
                .putFloat("scale", scale);
        if (skin != null) {
            CompoundTag skinTag = new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putInt("SkinImageWidth", skin.getSkinData().width)
                    .putInt("SkinImageHeight", skin.getSkinData().height)
                    .putString("ModelId", skin.getSkinId())
                    .putString("CapeId", skin.getCapeId())
                    .putByteArray("CapeData", skin.getCapeData().data)
                    .putInt("CapeImageWidth", skin.getCapeData().width)
                    .putInt("CapeImageHeight", skin.getCapeData().height)
                    .putByteArray("SkinResourcePatch", skin.getSkinResourcePatch().getBytes(StandardCharsets.UTF_8))
                    .putByteArray("GeometryData", skin.getGeometryData().getBytes(StandardCharsets.UTF_8))
                    .putByteArray("AnimationData", skin.getAnimationData().getBytes(StandardCharsets.UTF_8))
                    .putBoolean("PremiumSkin", skin.isPremium())
                    .putBoolean("PersonaSkin", skin.isPersona())
                    .putBoolean("CapeOnClassicSkin", skin.isCapeOnClassic());
            nbt.putCompound("Skin", skinTag);
        }

        nbt.putBoolean("ishuman", true);
        if (hand != null) nbt.putString("Item", hand.getName());
        if (helmet != null) nbt.putString("Helmet", helmet.getName());
        if (chestplate != null) nbt.putString("Chestplate", chestplate.getName());
        if (leggings != null) nbt.putString("Leggings", leggings.getName());
        if (boots != null) nbt.putString("Boots", boots.getName());
        return nbt;
    }

    public void parseBufferedImage(BufferedImage image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y), true);
                outputStream.write(color.getRed());
                outputStream.write(color.getGreen());
                outputStream.write(color.getBlue());
                outputStream.write(color.getAlpha());
            }
        }
        image.flush();
        this.data = outputStream.toByteArray();
    }

    public boolean SetNpcSpawn(Player player, Position pos, NpcInfo npc_info, Item hand, Item helmet, Item chestplate, Item leggings, Item boots, Float yaw, Float pitch) {
        try {
            // スキン読み込み
            BufferedImage image;
            try {
                image = ImageIO.read(new URL(npc_info.url));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.parseBufferedImage(image);
            Skin skin = new Skin();
            skin.setSkinData(image);

            // 位置設定
            SetPosition(pos);

            // 各種設定
            SetSkin(skin);
            SetName(npc_info.name);
            SetTag(npc_info.tag);
            SetHand(hand);
            SetHelmet(helmet);
            SetChestplate(chestplate);
            SetLeggings(leggings);
            SetBoots(boots);
            SetYaw(yaw);
            SetPitch(pitch);
            SetScale(npc_info.scale);
            CompoundTag nbt = this.nbt();

            // 名前
            String npc_name = npc_info.name;
            if (npc_info.tag.length() > 0) {
                npc_name += "\n" + npc_info.tag;
            }

            // クラス名
            String entity_namme = "NpcMerchantType01";
            switch (npc_info.kind) {
                case KIND_COMMON_TYPE01: entity_namme = "NpcCommonType"; break;
                case KIND_COMMON_TYPE02: entity_namme = "NpcCommonType"; break;
                case KIND_MERCHANT_TYPE01: entity_namme = "NpcMerchantType01"; break;
                case KIND_MERCHANT_TYPE02: entity_namme = "NpcMerchantType02"; break;
            }

            // エンティティー生成
            Entity npc = Entity.createEntity(entity_namme, player.chunk, nbt);
            if (npc != null) {
                if (npc instanceof NpcType) {
                    ((NpcType)npc).SetAPI(api);

                } else if (npc instanceof NpcCommonType) {
                    String message = "";
                    switch (npc_info.kind) {
                        case KIND_COMMON_TYPE01: message = api.getConfig().getString("Npc.Type01"); break;
                        case KIND_COMMON_TYPE02: message = api.getConfig().getString("Npc.Type02"); break;
                    }
                    ((NpcCommonType)npc).SetMessage(message);
                }
                npc.setNameTag(npc_name);
                npc.setNameTagVisible(true);
                npc.setNameTagAlwaysVisible(true);
                npc.spawnToAll();
            }

        } catch (Exception e) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            e.printStackTrace();
            api.getLogErr().Write(player, "SetNpcSpawn : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
            return false;
        }
        return true;
    }
}
