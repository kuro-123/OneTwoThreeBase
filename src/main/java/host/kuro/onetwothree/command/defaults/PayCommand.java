package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.task.SoundTask;

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
            api.getMessage().SendUsage(this, sender);
            return false;
        }
        // 権限チェック
        if (!api.IsJyumin(player)) {
            api.getMessage().SendWarningMessage(Language.translate("onetwothree.rank_err"), player);
            return false;
        }
        return PayWindow(player);
    }

    private boolean PayWindow(Player player) {
        try {
            List<String> pList = new ArrayList<String>();
            pList.add(Language.translate("onetwothree.selection.none"));
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
                    api.getMessage().SendWarningMessage(Language.translate("commands.pay.err_01"), targetPlayer);
                    return;
                }
                if (targetPlayer.getName().equals(responser.getName())) {
                    api.getMessage().SendWarningMessage(Language.translate("commands.pay.err_00"), targetPlayer);
                    return;
                }

                // 支払金取得
                String pmoney = data.get(2).toString();
                int imoney = 0;
                try {
                    imoney = Integer.parseInt(pmoney);
                } catch (Exception e) {
                }
                if (imoney <= 0) {
                    api.getMessage().SendWarningMessage(Language.translate("commands.pay.err_02"), targetPlayer);
                    return;
                }

                // 所持金取得
                int money = api.GetMoney(targetPlayer);
                if (money == -1) {
                    api.getMessage().SendWarningMessage(Language.translate("commands.pay.err_03"), targetPlayer);
                    return;
                }
                if (money < imoney) {
                    api.getMessage().SendWarningMessage(Language.translate("commands.pay.err_04"), targetPlayer);
                    return;
                }

                // 支払処理
                boolean ret = api.PayMoney(targetPlayer, imoney);
                if (!ret) {
                    api.getMessage().SendWarningMessage(Language.translate("commands.pay.err_05"), targetPlayer);
                    return;
                }
                api.getLogPay().Write(targetPlayer, "WarpCommand", "minus", ""+imoney);

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
                    api.getMessage().SendWarningMessage(Language.translate("commands.pay.err_06"), targetPlayer);
                    return;
                }
                api.getLogPay().Write(responser, "PayCommand", "plus", ""+imoney);

                int nowmoney = api.GetMoney(responser);
                StringBuilder sb2 = new StringBuilder();
                sb2.append(TextFormat.AQUA);
                sb2.append("[ ");
                sb2.append(TextFormat.WHITE);
                sb2.append(targetPlayer.getDisplayName());
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
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return false;
        }
        return true;
    }
}