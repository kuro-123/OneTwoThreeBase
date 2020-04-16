package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ListCommand extends CommandBase {

    private static OneTwoThreeAPI api;

    public ListCommand(OneTwoThreeAPI api) {
        super("list", api);
        this.api = api;
        this.setAliases(new String[]{"li"});
        commandParameters.clear();
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // コマンドチェック
        if (!this.testPermission(sender)) return false;
        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)) player = (Player) sender;

        try {
            String buff = GetListString(player);
            if (buff.length() > 0) {
                if(!(sender instanceof ConsoleCommandSender)){
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
                    SimpleForm form = new SimpleForm("プレイヤーリスト", buff);
                    form.send(player, (targetPlayer, targetForm, data) -> {
                    });
                } else {
                    sender.sendMessage(buff);
                }
            }
        } catch (Exception e) {
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
        }
        return true;
    }

    public static String GetListString(Player player) {
        StringBuilder sb = new StringBuilder();
        try {
            int onlineCount = 0;
            for (Player p : api.getServer().getOnlinePlayers().values()) {
                sb.append(TextFormat.WHITE);
                sb.append("[");
                sb.append(api.GetRankName(p));
                sb.append("] ");
                sb.append(p.getDisplayName());
                sb.append(TextFormat.YELLOW);
                sb.append(" (場所: ");
                sb.append(p.getLevel().getName());
                sb.append("<");
                sb.append(p.getFloorX());
                sb.append(",");
                sb.append(p.getFloorY());
                sb.append(",");
                sb.append(p.getFloorZ());
                sb.append("> ");
                if (player != null) {
                    double distance = player.getLocation().distanceSquared(p.getLocation());
                    distance = Math.sqrt(distance);
                    sb.append("→ 距離: ");
                    sb.append((int) distance);
                }
                sb.append(")\n");
                onlineCount++;
            }
            sb.append(TextFormat.GOLD);
            sb.append("\nオンライン人数 [ ");
            sb.append(""+onlineCount);
            sb.append("人 ]");
        } catch (Exception e) {
            api.getLogErr().Write(player, api.getMessage().GetErrorMessage(e));
            return "";
        }
        return new String(sb);
    }
}