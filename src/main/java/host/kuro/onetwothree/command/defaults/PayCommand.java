package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.task.SoundTask;
import org.jline.terminal.impl.ExecPty;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;

public class PayCommand extends CommandBase {

    public PayCommand(OneTwoThreeAPI api) {
        super("pay", api);
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
        // 権限チェック
        if (!api.IsJyumin(player)) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
            return false;
        }
        return PayWindow(player);
    }

    private boolean PayWindow(Player player) {
        try {
            List<String> pList = new ArrayList<String>();
            pList.add("指定なし");
            for (Player p : api.getServer().getOnlinePlayers().values()) {
                pList.add(p.getDisplayName());
            }

            CustomForm form = new CustomForm("支払処理")
                    .addLabel("指定したプレイヤーにお金を支払えます")
                    .addDropDown("プレイヤーリスト", pList)
                    .addInput("金額(数字)", "数字を入力してください", "0");
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
            form.send(player, (targetPlayer, targetForm, data) -> {
                if(data == null) {
                    this.sendUsage(targetPlayer);
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                // ウィンドウログ
                api.getLogWin().Write(targetPlayer, "支払処理", data.get(1).toString(), data.get(2).toString(), "", "", "", "", targetPlayer.getDisplayName());

                // プレイヤー取得
                String pname = data.get(1).toString();
                Player responser = api.GetPlayerEx(pname);
                if (responser == null) {
                    targetPlayer.sendMessage(api.GetWarningMessage("commands.pay.err_01"));
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                if (targetPlayer.getName().equals(responser.getName())) {
                    targetPlayer.sendMessage(api.GetWarningMessage("commands.pay.err_00"));
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }

                // 支払金取得
                String pmoney = data.get(2).toString();
                int imoney = 0;
                try {
                    imoney = Integer.parseInt(pmoney);
                } catch (Exception ex) {
                }
                if (imoney <= 0) {
                    targetPlayer.sendMessage(api.GetWarningMessage("commands.pay.err_02"));
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }

                // 所持金取得
                int money = api.GetMoney(targetPlayer);
                if (money == -1) {
                    targetPlayer.sendMessage(api.GetWarningMessage("commands.pay.err_03"));
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                if (money < imoney) {
                    targetPlayer.sendMessage(api.GetWarningMessage("commands.pay.err_04"));
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }

                // 支払処理
                boolean ret = api.PayMoney(player, imoney);
                if (!ret) {
                    targetPlayer.sendMessage(api.GetWarningMessage("commands.pay.err_05"));
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }

                NumberFormat nfNum = NumberFormat.getNumberInstance();

                StringBuilder sb = new StringBuilder();
                sb.append(TextFormat.YELLOW);
                sb.append("[ ");
                sb.append(TextFormat.WHITE);
                sb.append(responser.getDisplayName());
                sb.append(TextFormat.YELLOW);
                sb.append(" ] さんに [ ");
                sb.append(TextFormat.WHITE);
                sb.append(nfNum.format(imoney));
                sb.append("p ");
                sb.append(TextFormat.YELLOW);
                sb.append("] 支払いました。現在の所持金は [ ");
                sb.append(TextFormat.WHITE);
                sb.append(nfNum.format(money-imoney));
                sb.append("p ");
                sb.append(TextFormat.YELLOW);
                sb.append("] です");
                String m1 = new String(sb);
                targetPlayer.sendMessage(m1);
                api.getServer().getLogger().info(m1);
                api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin071, 0, false); // レジスター

                // 加算処理
                ret = api.AddMoney(responser, imoney);
                if (!ret) {
                    targetPlayer.sendMessage(api.GetWarningMessage("commands.pay.err_06"));
                    api.PlaySound(targetPlayer, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    return;
                }
                int nowmoney = api.GetMoney(responser);
                StringBuilder sb2 = new StringBuilder();
                sb2.append(TextFormat.AQUA);
                sb2.append("[ ");
                sb2.append(TextFormat.WHITE);
                sb2.append(responser.getDisplayName());
                sb2.append(TextFormat.AQUA);
                sb2.append(" ] さんから [ ");
                sb2.append(TextFormat.WHITE);
                sb2.append(nfNum.format(imoney));
                sb2.append("p ");
                sb2.append(TextFormat.AQUA);
                sb2.append("] が支払われました。現在の所持金は [ ");
                sb2.append(TextFormat.WHITE);
                sb2.append(nfNum.format(nowmoney));
                sb2.append("p ");
                sb2.append(TextFormat.AQUA);
                sb2.append("] です");
                String m2 = new String(sb2);
                responser.sendMessage(m2);
                api.getServer().getLogger().info(m2);
                api.PlaySound(responser, SoundTask.MODE_PLAYER, SoundTask.jin071, 0, false); // レジスター
            });

        } catch (Exception e) {
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            e.printStackTrace();
            api.getLogErr().Write(player, "WarpWindow : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
            return false;
        }
        return true;
    }

    private void TeleportMessage(Player player, String target) {
        if (player.isSpectator()) return;

        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append("[ﾜｰﾌﾟ] ");
        sb.append("[ ");
        sb.append(TextFormat.WHITE);
        sb.append(player.getDisplayName());
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append(" ] -> [ ");
        sb.append(TextFormat.WHITE);
        sb.append(target);
        sb.append(TextFormat.LIGHT_PURPLE);
        sb.append(" ]");
        String message = new String(sb);
        api.getServer().broadcastMessage(message);
        api.sendDiscordGreenMessage(message);

        api.PlaySound(player, SoundTask.MODE_BROADCAST, SoundTask.jin020, 0, false); // WARP
    }
}