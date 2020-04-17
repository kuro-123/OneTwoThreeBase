package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class WorldCommand extends CommandBase {

    public WorldCommand(OneTwoThreeAPI api) {
        super("world", api);
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("name", CommandParamType.STRING, true),
        });
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
        if (args.length != 1) {
            api.getMessage().SendUsage(this, sender);
            return false;
        }
        // 権限チェック
        if (!api.IsGameMaster(player)) {
            api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
            return false;
        }
        return WorldWindow(player, args[0]);
    }

    private boolean WorldWindow(Player player, String levelname) {
        try {
            boolean hit = false;
            for (Level lv : api.getServer().getLevels().values()) {
                if (lv.getName().indexOf(levelname) >= 0) {
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                this.sendUsage(player);
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                return false;
            }

            String s_manager01 = "";
            String s_manager02 = "";
            String s_manager03 = "";
            boolean b_zone = false;
            int i_viewdistance = 16;
            boolean b_splitchat = false;
            int i_hunger_speed = 10;
            boolean b_master = true;
            boolean b_survival = true;
            boolean b_creative = false;
            boolean b_spectator = false;
            boolean b_adventure = false;
            boolean b_fly = false;
            boolean b_break = false;
            boolean b_place = false;
            boolean b_pvp = false;
            boolean b_tagitem = false;
            boolean b_crafting = false;
            boolean b_bed = false;
            boolean b_effect = false;
            boolean b_enchant = false;
            boolean b_mob = false;
            boolean b_boss = false;
            String s_updater = "";

            PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0047"));
            ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
            pargs.add(new DatabaseArgs("c", levelname));
            ResultSet rs = api.getDB().ExecuteQuery(ps, pargs);
            pargs.clear();
            pargs = null;
            if (rs != null) {
                while (rs.next()) {
                    b_master = rs.getBoolean("master");
                    s_manager01 = rs.getString("manager01");
                    s_manager02 = rs.getString("manager02");
                    s_manager03 = rs.getString("manager03");
                    b_zone = rs.getBoolean("zone");
                    i_viewdistance = rs.getInt("viewdistance");
                    b_splitchat = rs.getBoolean("splitchat");
                    i_hunger_speed = rs.getInt("hunger_speed");
                    b_survival = rs.getBoolean("survival");
                    b_creative = rs.getBoolean("creative");
                    b_spectator = rs.getBoolean("spectator");
                    b_adventure = rs.getBoolean("adventure");
                    b_fly = rs.getBoolean("fly");
                    b_break = rs.getBoolean("break");
                    b_place = rs.getBoolean("place");
                    b_pvp = rs.getBoolean("pvp");
                    b_tagitem = rs.getBoolean("tagitem");
                    b_crafting = rs.getBoolean("crafting");
                    b_bed = rs.getBoolean("bed");
                    b_effect = rs.getBoolean("effect");
                    b_enchant = rs.getBoolean("enchant");
                    b_mob = rs.getBoolean("mob");
                    b_boss = rs.getBoolean("boss");
                    s_updater = rs.getString("updater");
                    break;
                }
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            CustomForm form = new CustomForm("ワールド設定 [ " + levelname + " ]")
                    .addLabel("各種設定を行います (最終更新者: " + s_updater + " )")
                    .addToggle("特殊権限モード", b_master)
                    .addInput("特殊権限者01", "名前", s_manager01)
                    .addInput("特殊権限者02", "名前", s_manager02)
                    .addInput("特殊権限者03", "名前", s_manager03)
                    .addToggle("ゾーン制度", b_zone)
                    .addInput("視界距離(8～32)", "8～32", ""+i_viewdistance)
                    .addToggle("チャット分離", b_splitchat)
                    .addInput("空腹速度(0～20)", "0～20", ""+i_hunger_speed)
                    .addToggle("サバイバル", b_survival)
                    .addToggle("クリエイティブ", b_creative)
                    .addToggle("スペクテイター", b_spectator)
                    .addToggle("アドベンチャー", b_adventure)
                    .addToggle("飛行許可(特殊以外)", b_fly)
                    .addToggle("整地許可(特殊以外)", b_break)
                    .addToggle("建築許可(特殊以外)", b_place)
                    .addToggle("PVP", b_pvp)
                    .addToggle("タグアイテムドロップ", b_tagitem)
                    .addToggle("クラフト許可", b_crafting)
                    .addToggle("ベッド設置許可", b_bed)
                    .addToggle("エフェクト使用許可", b_effect)
                    .addToggle("エンチャント使用許可", b_enchant)
                    .addToggle("モブ出現", b_mob)
                    .addToggle("ボス出現", b_boss);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                try {
                    if (data == null) {
                        this.sendUsage(targetPlayer);
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        return;
                    }
                    // ウィンドウログ
                    api.getLogWin().Write(targetPlayer, "ワールド設定", "", "","","","", "", targetPlayer.getDisplayName());

                    boolean master = ((boolean) data.get(1));
                    String manager01 = data.get(2).toString();
                    String manager02 = data.get(3).toString();
                    String manager03 = data.get(4).toString();
                    boolean zone =  ((boolean) data.get(5));
                    String buff = data.get(6).toString();
                    int viewdistance =  Integer.parseInt(buff);
                    boolean splitchat =  ((boolean) data.get(7));
                    buff = data.get(8).toString();
                    int hungerspeed =  Integer.parseInt(buff);
                    boolean survival =  ((boolean) data.get(9));
                    boolean creative =  ((boolean) data.get(10));
                    boolean spectator =  ((boolean) data.get(11));
                    boolean adventure =  ((boolean) data.get(12));
                    boolean fly =  ((boolean) data.get(13));
                    boolean sbreak =  ((boolean) data.get(14));
                    boolean place =  ((boolean) data.get(15));
                    boolean pvp =  ((boolean) data.get(16));
                    boolean tagitem =  ((boolean) data.get(17));
                    boolean crafting =  ((boolean) data.get(18));
                    boolean bed =  ((boolean) data.get(19));
                    boolean effect =  ((boolean) data.get(20));
                    boolean enchant =  ((boolean) data.get(21));
                    boolean mob =  ((boolean) data.get(22));
                    boolean boss = ((boolean) data.get(23));

                    ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                    args.add(new DatabaseArgs("c", levelname));
                    args.add(new DatabaseArgs("b", ""+master));
                    args.add(new DatabaseArgs("c", manager01));
                    args.add(new DatabaseArgs("c", manager02));
                    args.add(new DatabaseArgs("c", manager03));
                    args.add(new DatabaseArgs("b", ""+zone));
                    args.add(new DatabaseArgs("i", ""+viewdistance));
                    args.add(new DatabaseArgs("b", ""+splitchat));
                    args.add(new DatabaseArgs("i", ""+hungerspeed));
                    args.add(new DatabaseArgs("b", ""+survival));
                    args.add(new DatabaseArgs("b", ""+creative));
                    args.add(new DatabaseArgs("b", ""+spectator));
                    args.add(new DatabaseArgs("b", ""+adventure));
                    args.add(new DatabaseArgs("b", ""+fly));
                    args.add(new DatabaseArgs("b", ""+sbreak));
                    args.add(new DatabaseArgs("b", ""+place));
                    args.add(new DatabaseArgs("b", ""+pvp));
                    args.add(new DatabaseArgs("b", ""+tagitem));
                    args.add(new DatabaseArgs("b", ""+crafting));
                    args.add(new DatabaseArgs("b", ""+bed));
                    args.add(new DatabaseArgs("b", ""+effect));
                    args.add(new DatabaseArgs("b", ""+enchant));
                    args.add(new DatabaseArgs("b", ""+mob));
                    args.add(new DatabaseArgs("b", ""+boss));
                    args.add(new DatabaseArgs("b", targetPlayer.getDisplayName()));
                    // --
                    args.add(new DatabaseArgs("c", levelname));
                    args.add(new DatabaseArgs("b", ""+master));
                    args.add(new DatabaseArgs("c", manager01));
                    args.add(new DatabaseArgs("c", manager02));
                    args.add(new DatabaseArgs("c", manager03));
                    args.add(new DatabaseArgs("b", ""+zone));
                    args.add(new DatabaseArgs("i", ""+viewdistance));
                    args.add(new DatabaseArgs("b", ""+splitchat));
                    args.add(new DatabaseArgs("i", ""+hungerspeed));
                    args.add(new DatabaseArgs("b", ""+survival));
                    args.add(new DatabaseArgs("b", ""+creative));
                    args.add(new DatabaseArgs("b", ""+spectator));
                    args.add(new DatabaseArgs("b", ""+adventure));
                    args.add(new DatabaseArgs("b", ""+fly));
                    args.add(new DatabaseArgs("b", ""+sbreak));
                    args.add(new DatabaseArgs("b", ""+place));
                    args.add(new DatabaseArgs("b", ""+pvp));
                    args.add(new DatabaseArgs("b", ""+tagitem));
                    args.add(new DatabaseArgs("b", ""+crafting));
                    args.add(new DatabaseArgs("b", ""+bed));
                    args.add(new DatabaseArgs("b", ""+effect));
                    args.add(new DatabaseArgs("b", ""+enchant));
                    args.add(new DatabaseArgs("b", ""+mob));
                    args.add(new DatabaseArgs("b", ""+boss));
                    args.add(new DatabaseArgs("c", targetPlayer.getDisplayName()));
                    int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0048"), args);
                    args.clear();
                    args = null;

                    // ブロードキャスト通知
                    StringBuilder sb = new StringBuilder();
                    sb.append(TextFormat.YELLOW);
                    sb.append("[ ");
                    sb.append(TextFormat.WHITE);
                    sb.append(targetPlayer.getDisplayName());
                    sb.append(TextFormat.YELLOW);
                    sb.append(" ] により、ワールド [ ");
                    sb.append(TextFormat.WHITE);
                    sb.append(levelname);
                    sb.append(TextFormat.YELLOW);
                    sb.append(" ] の設定が変更されました");

                    // メモリ更新
                    WorldInfo worldinfo = new WorldInfo();
                    worldinfo.master	  = master;
                    worldinfo.manager01   = manager01;
                    worldinfo.manager02   = manager02;
                    worldinfo.manager03   = manager03;
                    worldinfo.zone        = zone;
                    worldinfo.viewdistance= viewdistance;
                    worldinfo.splitchat   = splitchat;
                    worldinfo.hunger_speed = hungerspeed;
                    worldinfo.survival    = survival;
                    worldinfo.creative    = creative;
                    worldinfo.spectator   = spectator;
                    worldinfo.adventure   = adventure;
                    worldinfo.fly         = fly;
                    worldinfo.bbreak      = sbreak;
                    worldinfo.bplace       = place;
                    worldinfo.pvp         = pvp;
                    worldinfo.tagitem     = tagitem;
                    worldinfo.crafting    = crafting;
                    worldinfo.bed         = bed;
                    worldinfo.effect      = effect;
                    worldinfo.enchant     = enchant;
                    worldinfo.mob         = mob;
                    worldinfo.boss        = boss;
                    worldinfo.updater     = targetPlayer.getDisplayName();
                    api.world_info.put(levelname, worldinfo);

                    String message = new String(sb);
                    api.getMessage().SendBroadcastInfoMessage(message);

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