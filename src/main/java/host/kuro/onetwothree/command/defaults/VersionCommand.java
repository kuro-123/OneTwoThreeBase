package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VersionCommand extends CommandBase {

    public VersionCommand(OneTwoThreeAPI api) {
        super("version", api);
        this.setAliases(new String[]{"ver", "v"});
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

        String info = api.getMessage().GetVersionInfo();
        if (info.length() > 0) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            SimpleForm form = new SimpleForm("更新情報", info);
            form.send(player, (targetPlayer, targetForm, data) -> {});
        }
        return true;
    }
}