package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.inventory.Inventory;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;

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
            api.getMessage().SendUsage(this, sender);
            return false;
        }
        Inventory inv = player.getInventory();
        if (inv != null) {
            player.getInventory().clearAll();
            api.getMessage().SendInfoMessage(Language.translate("commands.ri.success"), player);

        } else {
            api.getMessage().SendWarningMessage(Language.translate("commands.ri.err"), player);
            return false;
        }
        return true;
    }
}