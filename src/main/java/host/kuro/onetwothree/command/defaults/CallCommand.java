package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.task.SoundTask;

import java.util.HashMap;

public class CallCommand extends CommandBase {

    public CallCommand(OneTwoThreeAPI api) {
        super("call", api);
        this.setAliases(new String[]{"ca"});
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
        try {
            // 権限チェック
            if (!api.IsJyumin(player)) {
                api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
                return false;
            }
            // スキルチェック
            if (!api.IsGameMaster(player)) {
                int count = api.GetChatCount(player);
                if (count < 7000) {
                    api.getMessage().SendWarningMessage(Language.translate("onetwothree.skill_err"), player);
                    return false;
                }
            }
            // クールダウンチェック
            if (OneTwoThreeAPI.systemcall_timing != 0) {
                long now = System.currentTimeMillis();
                long keika = now - OneTwoThreeAPI.systemcall_timing;
                if (keika < (1000 * 60 * 15)) { // 15分クール
                    api.getMessage().SendUsage(this, sender);
                    return false;
                }
            }
            OneTwoThreeAPI.systemcall_timing = System.currentTimeMillis();

            // コールメッセージをツイートする
            api.getMessage().SendSystemCall(player);

        } catch (Exception e) {
            api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), player);
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }
}