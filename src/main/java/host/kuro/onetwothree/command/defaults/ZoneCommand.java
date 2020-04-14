package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.datatype.WorldInfo;
import host.kuro.onetwothree.task.SoundTask;

public class ZoneCommand extends CommandBase {

    OneTwoThreeAPI api;

    public ZoneCommand(OneTwoThreeAPI api) {
        super("zone", api);
        commandParameters.clear();
        this.api = api;
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
        // 権限チェック
        if (!api.IsGameMaster(player)) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
            player.sendTitle("");
            return false;
        }
        // 視界距離設定
        WorldInfo worldinfo = api.GetWorldInfo(player);
        if (!worldinfo.zone) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            player.sendMessage(api.GetWarningMessage(Language.translate("commands.zone.allow")));
            player.sendTitle("");
            return false;
        }
        try {
            if (!OneTwoThreeAPI.mode.containsKey(player)) {
                OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_ZONE);
                player.sendMessage(api.GetWarningMessage(Language.translate("commands.zone.modeon")));;
                player.sendTitle("ゾーンモード", TextFormat.YELLOW + "ポイント１を選択してください", 10, 100, 10);

            } else {
                if (OneTwoThreeAPI.mode.get(player) != OneTwoThreeAPI.TAP_MODE.MODE_NONE) {
                    OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_NONE);
                    player.sendMessage(api.GetWarningMessage(Language.translate("commands.zone.modeoff")));
                    player.sendTitle("");
                } else {
                    OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_ZONE);
                    player.sendMessage(api.GetWarningMessage(Language.translate("commands.zone.modeon")));
                    player.sendTitle("ゾーンモード", TextFormat.YELLOW + "ポイント１を選択してください", 10, 100, 10);
                }
            }

        } catch (Exception e) {
            player.sendMessage(api.GetErrMessage("onetwothree.cmderror"));
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS
        return true;
    }
}