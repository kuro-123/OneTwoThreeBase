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
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.item.ItemPrice;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PriceCommand extends CommandBase {

    private Config cfg;

    public PriceCommand(OneTwoThreeAPI api) {
        super("price", api);
        this.setAliases(new String[]{"pr", "p"});
        commandParameters.clear();
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
        return PriceWindow(player);
    }

    private boolean PriceWindow(Player player) {
        try {
            ArrayList<String> ilist = new ArrayList<String>();
            ilist.add("指定なし");
            for(Iterator<ItemPrice> iterator = api.item_price.values().iterator(); iterator.hasNext(); ) {
                ItemPrice value = iterator.next();
                ilist.add(value.name + " [ID: " + value.id + "] -> 現価格: " + value.price);
            }
            CustomForm form = new CustomForm("価格設定")
                .addLabel("価格の設定が行えます")
                .addDropDown("アイテムリスト", ilist)
                .addInput("設定価格", "数値入力(0～5,000)", "0");
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                    try {
                        if (data == null) {
                            this.sendUsage(targetPlayer);
                            api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                            return;
                        }
                        // 価格
                        String sprice = data.get(2).toString();

                        // ID
                        ItemPrice ip = null;
                        String sid = "";
                        String sindex = data.get(1).toString();
                        int index = Integer.parseInt(sindex);
                        index--;

                        for(Iterator<ItemPrice> iterator = api.item_price.values().iterator(); iterator.hasNext(); ) {
                            ip = iterator.next();
                            if (ip.index == index) {
                                break;
                            }
                        }
                        if (ip != null) {
                            // アイテム情報更新
                            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                            args.add(new DatabaseArgs("i", sprice));
                            args.add(new DatabaseArgs("c", targetPlayer.getDisplayName()));
                            args.add(new DatabaseArgs("i",""+ip.id));
                            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0024"), args);
                            args.clear();
                            args = null;
                            if (ret > 0) {
                                // メモリ更新
                                ip.price = Integer.parseInt(sprice);
                                api.item_price.put(index, ip);

                                player.sendMessage(api.GetWarningMessage("commands.price.success"));
                                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS
                            } else {
                                player.sendMessage(api.GetWarningMessage("commands.price.fail"));
                                api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                            }
                        }

                    } catch (Exception e) {
                        this.sendUsage(targetPlayer);
                        api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                        e.printStackTrace();
                    }
            });
            ilist.clear();

        } catch (Exception e) {
            this.sendUsage(player);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            e.printStackTrace();
        }
        return true;
    }
}