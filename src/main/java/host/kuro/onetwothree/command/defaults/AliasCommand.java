package host.kuro.onetwothree.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;
import host.kuro.onetwothree.command.CommandBase;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.datatype.WorldInfo;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class AliasCommand extends CommandBase {

    private Map<String, AliasInfo> info = null;

    public AliasCommand(OneTwoThreeAPI api) {
        super("alias", api);
        this.setAliases(new String[]{"al"});
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("name", CommandParamType.STRING, true),
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // コマンドチェック
        if (!this.testPermission(sender)) return false;
        Player player = null;
        if(!(sender instanceof ConsoleCommandSender)) player = (Player) sender;

        if (args.length != 1) {
            this.sendUsage(sender);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }

        // 権限チェック
        if (player != null) {
            if (!api.IsGameMaster(player)) {
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
                return false;
            }
        }

        // あいまい検索
        String name = api.AmbiguousSearch(args[0]);
        String message = GetAliasString(name);
        if (message.length() <= 0) {
            this.sendUsage(sender);
            api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
            return false;
        }

        if (player != null) {
            // ウィンドウ
            if (message.length() > 0) {
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin017, 0, false); // WINDOW
                SimpleForm form = new SimpleForm("エイリアス情報", message);
                form.send(player, (targetPlayer, targetForm, data) -> {
                });
            }
        } else {
            // コンソール
            sender.sendMessage(message);
        }
        return true;
    }

    private String GetAliasString(String name) {
        StringBuilder sb = new StringBuilder() ;
        try {
            String xname="", host="", ip="", cid="", device_id="";
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0037"));
            ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
            pargs.add(new DatabaseArgs("c", "%" + name.toLowerCase() + "%"));
            pargs.add(new DatabaseArgs("c", "%" + name.toLowerCase() + "%"));
            ResultSet rs = api.getDB().ExecuteQuery(ps, pargs);
            pargs.clear();
            pargs = null;
            if (rs != null) {
                while (rs.next()) {
                    xname = rs.getString("xname");
                    host = rs.getString("host");
                    ip = rs.getString("ip");
                    cid = rs.getString("cid");
                    device_id = rs.getString("device_id");
                    break;
                }
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (xname == "") return "";

            // ハッシュマップ生成
            info = new HashMap<>();
            if (ip.length() > 0) MatchData("ip", ip);
            if (host.length() > 0) MatchData("host", host);
            if (cid.length() > 0) MatchData("cid", cid);
            if (device_id.length() > 0) MatchData("device_id", device_id);

            String buff = "";
            sb.append(TextFormat.YELLOW + "[ ");
            sb.append(xname);
            sb.append(" ] さんのエイリアス結果\n");
            sb.append("※CID:ｸﾗｲｱﾝﾄID DID:機器ID\n");
            sb.append("-----------------------------\n");
            sb.append(TextFormat.WHITE);

            for(Iterator<AliasInfo> iterator = info.values().iterator(); iterator.hasNext(); ) {
                AliasInfo value = iterator.next();
                String format = String.format("[XB: %s NM: %s] <IP:%s HOST:%s CID:%s DID:%s>\n", value.xname, value.name, value.ip, value.host, value.cid, value.device_id);
                sb.append(format);
            }
        } catch (Exception e) {
            api.getLogErr().Write(null, api.GetErrorMessage(e));
        }
        info.clear();
        info = null;
        return new String(sb);
    }

    private void MatchData(String kbn, String value) {
        try {
            String sql = "";
            switch (kbn) {
                case "ip"       : sql = Language.translate("Sql0038"); break;
                case "host"     : sql = Language.translate("Sql0039"); break;
                case "cid"      : sql = Language.translate("Sql0040"); break;
                case "device_id": sql = Language.translate("Sql0041"); break;
            }
            if (sql.length()<=0) return;

            PreparedStatement ps = api.getDB().getConnection().prepareStatement(sql);
            ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
            pargs.add(new DatabaseArgs("c", value));
            ResultSet rs = api.getDB().ExecuteQuery(ps, pargs);
            pargs.clear();
            pargs = null;
            if (rs != null) {
                while (rs.next()) {
                    AliasInfo buff_ai = null;
                    String xname = rs.getString("xname");
                    if (xname.length() > 0) {
                        if (info.containsKey(xname)) {
                            buff_ai = info.get(xname);
                            buff_ai.xname = xname;
                        } else {
                            buff_ai = new AliasInfo();
                            buff_ai.xname = xname;
                            buff_ai.name = "";
                            buff_ai.ip = "×";
                            buff_ai.host = "×";
                            buff_ai.cid = "×";
                            buff_ai.device_id = "×";
                        }
                    } else {
                        continue;
                    }
                    String  name = rs.getString("name");
                    if (name != null) {
                        buff_ai.name = name;
                    }
                    switch (kbn) {
                        case "ip"       : buff_ai.ip = "〇"; break;
                        case "host"     : buff_ai.host = "〇"; break;
                        case "cid"      : buff_ai.cid = "〇"; break;
                        case "device_id": buff_ai.device_id = "〇"; break;
                    }
                    info.put(xname , buff_ai);
                }
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
        } catch (Exception e) {
            api.getLogErr().Write(null, api.GetErrorMessage(e));
        }
    }
}

class AliasInfo {
    public String xname;
    public String name;
    public String ip;
    public String host;
    public String cid;
    public String device_id;
}