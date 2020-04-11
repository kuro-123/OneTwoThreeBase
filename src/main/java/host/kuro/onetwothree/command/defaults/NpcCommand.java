package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.Entity;
import cn.nukkit.utils.Config;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.NpcPlugin;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.datatype.NpcInfo;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.npc.NpcType;
import host.kuro.onetwothree.task.SoundTask;

import java.util.ArrayList;

public class NpcCommand extends CommandBase {

    public NpcCommand(OneTwoThreeAPI api) {
        super("npc", api);
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("action", CommandParamType.STRING, true),
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // コマンドチェック
        if (!this.testPermission(sender)) return false;
        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)) player = (Player) sender;
        if (player == null) {
            this.sendUsage(sender);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        if (args.length != 1) {
            this.sendUsage(sender);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        // 権限チェック
        if (!api.IsNushi(player)) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
            return false;
        }
        // モードチェック
        if (OneTwoThreeAPI.mode.containsKey(player)) {
            if (OneTwoThreeAPI.mode.get(player) != OneTwoThreeAPI.TAP_MODE.MODE_NONE) {
                player.sendMessage(api.GetInfoMessage(Language.translate("commands.npc.othermode")));
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                return false;
            }
        }

        if (args[0].equals("add")) {
            return NpcWindow(player);
        }
        else {
            return NpcDeleteWindow(player);
        }
    }

    private boolean NpcDeleteWindow(Player player) {
        try {
            ArrayList<String> npc_list = new ArrayList<String>();
            for (Entity entity : player.getLevel().getEntities()) {
                if (entity instanceof NpcType) {
                    npc_list.add(entity.getName());
                }
            }
            CustomForm form = new CustomForm("NPC削除")
                    .addLabel("選択したNPCを削除します")
                    .addDropDown("NPCリスト", npc_list);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                try {
                    if (data == null) {
                        this.sendUsage(targetPlayer);
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }
                    // ウィンドウログ
                    api.getLogWin().Write(targetPlayer, "NPC削除", data.get(1).toString(), "","","","", "", targetPlayer.getDisplayName());

                    // タイプ
                    String entity_name = data.get(1).toString();
                    for (Entity entity : player.getLevel().getEntities()) {
                        if (entity instanceof NpcType) {
                            if (entity.getName().equals(entity_name)) {
                                entity.close();
                            }
                        }
                    }
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS

                } catch (Exception e) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    e.printStackTrace();
                    api.getLogErr().Write(targetPlayer, "NpcDeleteWindow : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
                }
            });

        } catch (Exception e) {
            this.sendUsage(player);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            e.printStackTrace();
            api.getLogErr().Write(player, "NpcDeleteWindow : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
        return true;
    }

    private boolean NpcWindow(Player player) {
        try {
            ArrayList<String> tlist = new ArrayList<String>();
            tlist.add("指定なし");
            tlist.add("一般NPC-男性");
            tlist.add("一般NPC-女性");
            tlist.add("商人NPC-男性");
            tlist.add("商人NPC-女性");

            ArrayList<String> slist = new ArrayList<String>();
            slist.add("指定なし");
            slist.add("スキン01");
            slist.add("スキン02");
            slist.add("スキン03");
            slist.add("スキン04");
            slist.add("スキン05");
            slist.add("スキン06");
            slist.add("スキン07");
            slist.add("スキン08");
            slist.add("スキン09");
            slist.add("スキン10");
            slist.add("スキン11");
            slist.add("スキン12");
            slist.add("スキン13");
            slist.add("スキン14");
            slist.add("スキン15");
            slist.add("スキン16");
            slist.add("スキン17");
            slist.add("スキン18");
            slist.add("スキン19");
            slist.add("スキン20");
            slist.add("スキン21");
            slist.add("スキン22");
            slist.add("スキン23");

            CustomForm form = new CustomForm("NPC設定")
                    .addLabel("設定後にタップした位置に設置できます")
                    .addDropDown("タイプリスト", tlist)
                    .addDropDown("スキン選択", slist)
                    .addInput("名前(2～16文字)", "2～16文字", "")
                    .addInput("タグ(0～16文字)", "0～16文字", "")
                    .addInput("スケール(0.5～1.5)", "0.5～1.5", "1.0");
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                try {
                    if (data == null) {
                        this.sendUsage(targetPlayer);
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }
                    // ウィンドウログ
                    api.getLogWin().Write(targetPlayer, "NPC設定", data.get(1).toString(), data.get(2).toString(), data.get(3).toString(), data.get(4).toString(), data.get(5).toString(), "", targetPlayer.getDisplayName());

                    // タイプ
                    String type = data.get(1).toString();
                    NpcPlugin.NPC_KIND kind = NpcPlugin.NPC_KIND.KIND_COMMON_TYPE01;
                    switch (type) {
                        case "一般NPC-男性":
                            kind = NpcPlugin.NPC_KIND.KIND_COMMON_TYPE01;
                            break;
                        case "一般NPC-女性":
                            kind = NpcPlugin.NPC_KIND.KIND_COMMON_TYPE02;
                            break;
                        case "商人NPC-男性":
                            kind = NpcPlugin.NPC_KIND.KIND_MERCHANT_TYPE01;
                            break;
                        case "商人NPC-女性":
                            kind = NpcPlugin.NPC_KIND.KIND_MERCHANT_TYPE02;
                            break;
                        default:
                            targetPlayer.sendMessage(api.GetInfoMessage(Language.translate("commands.npc.err_choise")));
                            api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                            return;
                    }

                    // スキンタイプ
                    String path = api.getConfig().getString("Web.WebPath");
                    String skintype = data.get(2).toString();
                    String url = "";
                    switch (skintype) {
                        case "スキン01": url = path + "skin/npc/skin01.png"; break;
                        case "スキン02": url = path + "skin/npc/skin02.png"; break;
                        case "スキン03": url = path + "skin/npc/skin03.png"; break;
                        case "スキン04": url = path + "skin/npc/skin04.png"; break;
                        case "スキン05": url = path + "skin/npc/skin05.png"; break;
                        case "スキン06": url = path + "skin/npc/skin06.png"; break;
                        case "スキン07": url = path + "skin/npc/skin07.png"; break;
                        case "スキン08": url = path + "skin/npc/skin08.png"; break;
                        case "スキン09": url = path + "skin/npc/skin09.png"; break;
                        case "スキン10": url = path + "skin/npc/skin10.png"; break;
                        case "スキン11": url = path + "skin/npc/skin11.png"; break;
                        case "スキン12": url = path + "skin/npc/skin12.png"; break;
                        case "スキン13": url = path + "skin/npc/skin13.png"; break;
                        case "スキン14": url = path + "skin/npc/skin14.png"; break;
                        case "スキン15": url = path + "skin/npc/skin15.png"; break;
                        case "スキン16": url = path + "skin/npc/skin16.png"; break;
                        case "スキン17": url = path + "skin/npc/skin17.png"; break;
                        case "スキン18": url = path + "skin/npc/skin18.png"; break;
                        case "スキン19": url = path + "skin/npc/skin19.png"; break;
                        case "スキン20": url = path + "skin/npc/skin20.png"; break;
                        case "スキン21": url = path + "skin/npc/skin21.png"; break;
                        case "スキン22": url = path + "skin/npc/skin22.png"; break;
                        case "スキン23": url = path + "skin/npc/skin23.png"; break;
                        default:
                            targetPlayer.sendMessage(api.GetInfoMessage(Language.translate("commands.npc.err_choise")));
                            api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                            return;
                    }

                    // 名前
                    String name = data.get(3).toString();
                    if (!IsMojisu(targetPlayer, name)) {
                        return;
                    }

                    // タグ
                    String tag = data.get(4).toString();

                    // スケール
                    Float scale = 1.0F;
                    String buff = data.get(5).toString();
                    try {
                        scale = Float.parseFloat(buff);
                    } catch (Exception ex) {
                        targetPlayer.sendMessage(api.GetInfoMessage(Language.translate("commands.npc.err_scale")));
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }
                    if (!(0.5F <= scale && scale <= 1.5F)) {
                        targetPlayer.sendMessage(api.GetInfoMessage(Language.translate("commands.npc.err_scale")));
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }

                    // NPC情報格納
                    NpcInfo npcinfo = new NpcInfo(kind, url, name, tag, scale);
                    OneTwoThreeAPI.npc_info.put(targetPlayer, npcinfo);

                    // モードON
                    OneTwoThreeAPI.mode.put(targetPlayer, OneTwoThreeAPI.TAP_MODE.MODE_NPC);
                    targetPlayer.sendMessage(api.GetInfoMessage(Language.translate("commands.npc.modeon")));;
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS

                } catch (Exception e) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    e.printStackTrace();
                    api.getLogErr().Write(targetPlayer, "NpcWindow : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
                }
            });

        } catch (Exception e) {
            this.sendUsage(player);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            e.printStackTrace();
            api.getLogErr().Write(player, "NpcWindow : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
        return true;
    }

    private boolean IsMojisu(Player player, String target) {
        // 文字数チェック
        int len =target.length();
        if (len < 2 || len > 16) {
            player.sendMessage(api.GetWarningMessage("commands.npc.err_len"));
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        return true;
    }
}