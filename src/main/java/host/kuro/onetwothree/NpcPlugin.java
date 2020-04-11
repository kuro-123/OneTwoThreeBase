package host.kuro.onetwothree;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.stream.FastByteArrayOutputStream;
import cn.nukkit.nbt.tag.*;
import host.kuro.onetwothree.npc.NpcMerchant;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NpcPlugin {

    public static enum NPC_KIND {
        KIND_NONE,
        KIND_MERCHANT_TYPE01,
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
    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private float scale = 1.0f;

    private static final int SKIN_SIZE = 64 * 32 * 4;
    private byte[] data = new byte[SKIN_SIZE];

    public NpcPlugin(OneTwoThreeAPI api) {
        this.api = api;
        Entity.registerEntity(NpcMerchant.class.getSimpleName(), NpcMerchant.class);
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
    public void SetYaw(float yaw) {
        this.yaw = yaw;
    }
    public void SetPitch(float pitch) {
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
                .putFloat("scale", 1);
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

    public boolean SetMerchantSpawn(Player player, Position pos, NPC_KIND kind, String name, String tag, Item hand, Item helmet, Item chestplate, Item leggings, Item boots, Float yaw, Float pitch, Float scale) {
        if (OneTwoThreeAPI.npc_list.containsKey(name)) return false;

        File file = new File(api.getServer().getPluginPath() + "/skin/kuro.png");
        BufferedImage image;
        try {
            image = ImageIO.read(new URL("http://kuro.host/mcbe/data/skin/test.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.parseBufferedImage(image);

        SetPosition(pos);

        Skin skin = new Skin();
        skin.setSkinData(image);
        SetSkin(skin);

        SetName(name);
        SetTag(tag);
        SetHand(hand);
        SetHelmet(helmet);
        SetChestplate(chestplate);
        SetLeggings(leggings);
        SetBoots(boots);
        SetYaw(yaw);
        SetPitch(pitch);
        SetScale(scale);
        CompoundTag nbt = this.nbt();

        Entity npc = Entity.createEntity("NpcMerchant", player.chunk, nbt);
        npc.setNameTag(name);
        npc.setNameTagVisible(true);
        npc.setNameTagAlwaysVisible(true);
        npc.spawnToAll();
        return true;
    }
}
