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

    private Config cfg;
    OneTwoThreeAPI api;

    public TouchCommand(OneTwoThreeAPI api) {
        super("touch", api);
        this.setAliases(new String[]{"tc", "t"});
        commandParameters.clear();
        this.api = api;
        cfg = api.getConfig();
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
        if (!api.IsJyumin(player)) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
            return false;
        }
        try {
            if (!OneTwoThreeAPI.mode.containsKey(player)) {
                OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_TOUCH);
                player.sendMessage(api.GetInfoMessage(Language.translate("commands.touch.modeon")));;

            } else {
                if (OneTwoThreeAPI.mode.get(player) != OneTwoThreeAPI.TAP_MODE.MODE_NONE) {
                    OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_NONE);
                    player.sendMessage(api.GetInfoMessage(Language.translate("commands.touch.modeoff")));
                } else {
                    OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_TOUCH);
                    player.sendMessage(api.GetInfoMessage(Language.translate("commands.touch.modeon")));
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