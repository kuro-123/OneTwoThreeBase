package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.forms.elements.ModalForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class SellCommand extends CommandBase {

    private Config cfg;

    public SellCommand(OneTwoThreeAPI api) {
        super("sell", api);
        this.setAliases(new String[]{"se"});
        commandParameters.clear();
        cfg = api.getConfig();
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)){
            player = (Player) sender;
        }
        if (player == null) {
            this.sendUsage(sender);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }

        try {
            Item item = player.getInventory().getItemInHand();
            String cus = item.getCustomName();
            if (cus.length() > 0) {
                String symbol = api.getConfig().getString("GameSettings.ItemTag");
                if (symbol.length() > 0) {
                    if (cus.indexOf(symbol) > 0) {
                        ModalForm form = new ModalForm("あああ","いいい","はい","いいえ");
                        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
                        form.send(player, (targetPlayer, targetForm, data) -> {
                            try {
                                if (data == 0) {

                                }
                            } catch (Exception e) {
                                targetPlayer.sendMessage(api.GetErrMessage("onetwothree.cmderror"));
                                api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                                return;
                            }
                        });
                    }
                }
            }
            player.sendMessage(api.GetErrMessage("onetwothree.cmderror"));
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL

        } catch (Exception e) {
            player.sendMessage(api.GetErrMessage("onetwothree.cmderror"));
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }
        return true;
    }
}