package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.Config;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.task.SoundTask;

public class TouchCommand extends CommandBase {

    OneTwoThreeAPI api;

    public TouchCommand(OneTwoThreeAPI api) {
        super("touch", api);
        this.setAliases(new String[]{"tc", "t"});
        commandParameters.clear();
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
        if (!api.IsJyumin(player)) {
            api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
            return false;
        }
        // スキルチェック
        if (!api.IsGameMaster(player)) {
            int count = api.GetBreakPlaceCount(player);
            if (count < 50000) {
                api.getMessage().SendWarningMessage(Language.translate("onetwothree.skill_err"), player);
                return false;
            }
        }
        try {
            if (!OneTwoThreeAPI.mode.containsKey(player)) {
                OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_TOUCH);
                api.getMessage().SendInfoMessage(Language.translate("commands.touch.modeon"), player);
            } else {
                if (OneTwoThreeAPI.mode.get(player) != OneTwoThreeAPI.TAP_MODE.MODE_NONE) {
                    OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_NONE);
                    api.getMessage().SendInfoMessage(Language.translate("commands.touch.modeoff"), player);
                } else {
                    OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_TOUCH);
                    api.getMessage().SendInfoMessage(Language.translate("commands.touch.modeon"), player);
                }
            }

        } catch (Exception e) {
            api.getMessage().SendErrorMessage(Language.translate("onetwothree.cmderror"), player);
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }
}