package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.block.BlockConcrete;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.datatype.WorldInfo;
import host.kuro.onetwothree.datatype.ZoneInfo;
import host.kuro.onetwothree.task.SoundTask;

import java.util.ArrayList;

public class ZoneCommand extends CommandBase {

    OneTwoThreeAPI api;

    public ZoneCommand(OneTwoThreeAPI api) {
        super("zone", api);
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("action", CommandParamType.STRING, true),
        });
        this.api = api;
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
        // 権限チェック
        if (!api.IsGameMaster(player)) {
            api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
            player.sendTitle("");
            return false;
        }
        // ゾーン許可設定
        WorldInfo worldinfo = api.GetWorldInfo(player);
        if (!worldinfo.zone) {
            api.getMessage().SendWarningMessage(Language.translate("commands.zone.allow"), player);
            player.sendTitle("");
            return false;
        }
        if (args.length == 1) {
            if (args[0].toLowerCase().equals("del")) {
                return DeleteZone(player);
            }
        }
        try {
            if (!OneTwoThreeAPI.mode.containsKey(player)) {
                OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_ZONE);
                api.getMessage().SendInfoMessage(Language.translate("commands.zone.modeon"), player);
                player.sendTitle("ゾーンモード", TextFormat.YELLOW + "ポイント１を選択してください", 10, 100, 10);

            } else {
                if (OneTwoThreeAPI.mode.get(player) != OneTwoThreeAPI.TAP_MODE.MODE_NONE) {
                    OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_NONE);
                    api.getMessage().SendInfoMessage(Language.translate("commands.zone.modeoff"), player);
                    player.sendTitle("");
                } else {
                    OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_ZONE);
                    api.getMessage().SendInfoMessage(Language.translate("commands.zone.modeon"), player);
                    player.sendTitle("ゾーンモード", TextFormat.YELLOW + "ポイント１を選択してください", 10, 100, 10);
                }
            }

        } catch (Exception e) {
            api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), player);
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }

    private boolean DeleteZone(Player player) {
        ZoneInfo zi = api.IsInsideInfo(player.getLocation());
        if (zi == null) {
            api.getMessage().SendWarningMessage(Language.translate("commands.zone.inside"), player);
            return false;
        }
        try {
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", zi.level));
            args.add(new DatabaseArgs("i", ""+zi.x1));
            args.add(new DatabaseArgs("i", ""+zi.z1));
            args.add(new DatabaseArgs("i", ""+zi.x2));
            args.add(new DatabaseArgs("i", ""+zi.z2));
            int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0056"), args);
            args.clear();
            args = null;
            if (ret <= 0) {
                api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), player);
                return false;
            }
            String key = zi.level+zi.x1+zi.z1+zi.x2+zi.z2;
            api.zone_info.remove(key);

            // ブロードキャスト通知
            StringBuilder sb = new StringBuilder();
            sb.append(TextFormat.YELLOW);
            sb.append("[ ");
            sb.append(TextFormat.WHITE);
            sb.append(player.getDisplayName());
            sb.append(TextFormat.YELLOW);
            sb.append(" ] さんがゾーン設定を削除しました");
            String message = new String(sb);
            api.getMessage().SendBroadcastInfoMessage(message);

        } catch (Exception e) {
            api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), player);
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }
}