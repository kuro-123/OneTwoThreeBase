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

        try {
            if (!OneTwoThreeAPI.touch_mode.containsKey(player)) {
                OneTwoThreeAPI.touch_mode.put(player, true);
                player.sendMessage(api.GetInfoMessage(Language.translate("commands.touch.modeon")));;

            } else {
                if (OneTwoThreeAPI.touch_mode.get(player)) {
                    OneTwoThreeAPI.touch_mode.put(player, false);
                    player.sendMessage(api.GetInfoMessage(Language.translate("commands.touch.modeoff")));
                } else {
                    OneTwoThreeAPI.touch_mode.put(player, true);
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