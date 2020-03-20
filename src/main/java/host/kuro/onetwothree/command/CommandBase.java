package host.kuro.onetwothree.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;

public abstract class CommandBase extends Command {

    protected OneTwoThreeAPI api;

    public CommandBase(String name, OneTwoThreeAPI api) {
        super(name);
        this.description = Language.translate("commands." + name + ".description");
        String usageMessage = Language.translate("commands." + name + ".usage");
        this.usageMessage = usageMessage.equals("commands." + name + ".usage") ? "/" + name : usageMessage;
        this.setPermission("onetwothree." + name);
        this.api = api;
    }

    protected OneTwoThreeAPI getAPI() {
        return api;
    }

    protected void sendUsage(CommandSender sender) {
        sender.sendMessage(new TranslationContainer("commands.generic.usage", this.usageMessage));
    }

    protected boolean testIngame(CommandSender sender) {
        if (!(sender instanceof Player)) {
            //sender.sendMessage(TextFormat.RED + Language.translate("commands.generic.ingame"));
            return false;
        }
        return true;
    }

    protected void sendPermissionMessage(CommandSender sender) {
        sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.permission"));
    }
}