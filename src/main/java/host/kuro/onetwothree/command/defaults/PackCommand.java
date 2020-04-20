package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.network.protocol.ResourcePackDataInfoPacket;
import cn.nukkit.network.protocol.ResourcePackStackPacket;
import cn.nukkit.network.protocol.ResourcePacksInfoPacket;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.resourcepacks.ResourcePackManager;
import cn.nukkit.resourcepacks.ZippedResourcePack;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.NpcPlugin;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.datatype.NpcInfo;
import host.kuro.onetwothree.datatype.WorldInfo;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.npc.NpcType;
import host.kuro.onetwothree.task.SoundTask;

import java.io.File;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PackCommand extends CommandBase {

    public PackCommand(OneTwoThreeAPI api) {
        super("pack", api);
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
        return PackWindow(player);
    }

    private boolean PackWindow(Player player) {
        try {
            ArrayList<String> plist = new ArrayList<String>();
            plist.add("サウンドオンリー版");
            plist.add("ビジュアル版");
            CustomForm form = new CustomForm("リソースパック")
                    .addLabel("指定のリソースパックをダウンロードします。複数持っている場合はアプリのストレージ設定から「使いたいリソースパックのみ」にしてください。")
                    .addDropDown("パックリスト", plist);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                try {
                    if (data == null) {
                        this.sendUsage(targetPlayer);
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }
                    // ウィンドウログ
                    api.getLogWin().Write(targetPlayer, "リソースパック", "", "","","","", "", targetPlayer.getDisplayName());

                    String packname = data.get(1).toString();
                    File pack_file;
                    if (packname.equals("サウンドオンリー版")) {
                        pack_file = new File(api.getServer().getDataPath() + "/123-soundonly.mcpack");
                        api.getMessage().SendInfoMessage("サウンドオンリー版のダウンロードを開始します！", targetPlayer);
                    } else {
                        pack_file = new File(api.getServer().getDataPath() + "/123-visual.mcpack");
                        api.getMessage().SendInfoMessage("ビジュアル版のダウンロードを開始します！", targetPlayer);
                    }

                    ResourcePackManager a = api.getServer().getResourcePackManager();
                    //api.getServer().getResourcePackManager().ChangeResourcePack(pack);
                    ResourcePack resourcePack = new ZippedResourcePack(pack_file);

                    //ResourcePacksInfoPacket infoPacket = new ResourcePacksInfoPacket();
                    //List<ResourcePack> loadedResourcePacks = new ArrayList<>();
                    //loadedResourcePacks.add(resourcePack);
                    //ResourcePack[] resourcePackStack = loadedResourcePacks.toArray(new ResourcePack[0]);
                    //infoPacket.resourcePackEntries = resourcePackStack;
                    //infoPacket.mustAccept = api.getServer().getForceResources();
                    //targetPlayer.dataPacket(infoPacket);

                    // リソパダウンロード開始
                    api.getServer().getLogger().info("packId : " + resourcePack.getPackId());
                    api.getServer().getLogger().info("getPackSize : " + resourcePack.getPackSize());
                    api.getServer().getLogger().info("maxChunkSize : " + 524288);
                    double buff = Math.ceil((double)resourcePack.getPackSize() / (double)524288.00D);
                    api.getServer().getLogger().info("chunkCount : " + buff);
                    api.getServer().getLogger().info("chunkCount : " + (int)buff);
                    api.getServer().getLogger().info("compressedPackSize : " + resourcePack.getPackSize());
                    api.getServer().getLogger().info("sha256 : " + resourcePack.getSha256());

                    ResourcePackDataInfoPacket dataInfoPacket = new ResourcePackDataInfoPacket();
                    dataInfoPacket.packId = resourcePack.getPackId();
                    dataInfoPacket.maxChunkSize = 524288; //megabyte
                    dataInfoPacket.chunkCount = (int)Math.ceil((double)resourcePack.getPackSize() / (double)dataInfoPacket.maxChunkSize);
                    dataInfoPacket.compressedPackSize = resourcePack.getPackSize();
                    dataInfoPacket.sha256 = resourcePack.getSha256();
                    dataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_RESOURCE;
                    targetPlayer.dataPacket(dataInfoPacket);

                    // リソパ登録
                    api.pack_info.put(targetPlayer, resourcePack);

                } catch (Exception e) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    api.getLogErr().Write(targetPlayer, api.getMessage().GetErrorMessage(e));
                }
            });

        } catch (Exception e) {
            this.sendUsage(player);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
        }
        return true;
    }
}