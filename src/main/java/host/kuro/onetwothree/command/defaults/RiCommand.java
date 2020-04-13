package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RiCommand extends CommandBase {

    public RiCommand(OneTwoThreeAPI api) {
        super("ri", api);
        commandParameters.clear();
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

        Inventory inv = player.getInventory();
        if (inv != null) {
            player.getInventory().clearAll();
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS
            player.sendMessage(api.GetErrMessage("commands.ri.success"));

        } else {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            player.sendMessage(api.GetErrMessage("commands.ri.err"));
            return false;
        }

        return true;
    }
}