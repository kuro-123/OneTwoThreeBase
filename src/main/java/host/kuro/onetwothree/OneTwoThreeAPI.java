package host.kuro.onetwothree;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.database.DatabaseManager;
import host.kuro.onetwothree.forms.elements.SimpleForm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OneTwoThreeAPI {
    // メンバー
    private static OneTwoThreeAPI instance = null;
    private BasePlugin plugin;
    private DatabaseManager db;

    public OneTwoThreeAPI(BasePlugin plugin) {
        // インスタンス
    	instance = this;
        this.plugin = plugin;
        this.db = new DatabaseManager(instance);
    }
    public static OneTwoThreeAPI getInstance() {
        return instance;
    }
    public Server getServer() { return plugin.getServer(); }
    public DatabaseManager getDB() {
        return this.db;
    }
    public String getDataFolderPath() {
        return this.plugin.getDataFolder().getPath();
    }
    public Config getConfig() {
        return this.plugin.getConfig();
    }
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }
    public PluginLogger getLogger() { return this.plugin.getLogger(); }

    // 各種メモリデータ
    public static HashMap<Player, Boolean> touch_mode = new HashMap<>();
    public static HashMap<Player, List<String>> wp_world = new HashMap<>();
    public static HashMap<Player, List<String>> wp_player = new HashMap<>();

    // IPアドレスを取得
    public String GetIpInfo() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            return "---.---.---.---";
        }
    }

    // ホスト取得
    public String GetHostInfo(String address) {
        try {
            //オブジェクトを取得
            InetAddress ia = InetAddress.getByName(address);
            String lstrRet = ia.getHostName();
            ia = null;
            return lstrRet;
        } catch (Exception e) {
            return "---.---.---.---";
        }
    }

    // プラグインタイトルメッセージ
    public String GetMessageTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.GREEN);
        sb.append("[");
        sb.append(Language.translate("onetwothree.name"));
        sb.append("] ");
        return new String(sb);
    }

    // 情報メッセージ
    public String GetInfoMessage(String target) {
        StringBuilder sb = new StringBuilder();
        sb.append(GetMessageTitle());
        sb.append(TextFormat.WHITE);
        sb.append(Language.translate(target));
        return new String(sb);
    }

    // 警告メッセージ
    public String GetWarningMessage(String target) {
        StringBuilder sb = new StringBuilder();
        sb.append(GetMessageTitle());
        sb.append(TextFormat.YELLOW);
        sb.append(Language.translate(target));
        return new String(sb);
    }

    // エラーメッセージ
    public String GetErrMessage(String target) {
        StringBuilder sb = new StringBuilder();
        sb.append(GetMessageTitle());
        sb.append(TextFormat.RED);
        sb.append(Language.translate(target));
        return new String(sb);
    }

    // 半角英数チェック
    public boolean isHankakuEisu(String target) {
        target.replace("_", "");    // _はセーフ
        return Pattern.matches("^[0-9a-zA-Z]+$", target);
    }

    // あいまいプレイヤー検索
    public String AmbiguousSearch(String name) {
        String ret = "";
        Player target = Server.getInstance().getPlayer(name);
        if (target == null) {
            IPlayer itarget = Server.getInstance().getOfflinePlayer(name);
            if (itarget != null) {
                ret = itarget.getName();
            }
        } else {
            ret = target.getName();
        }
        return ret;
    }

    public boolean IsTouchmode(Player player) {
        if (!touch_mode.containsKey(player)) {
            return false;
        }
        return touch_mode.get(player);
    }

    public String GetBlockInfoMessage(Block block) {
        if (block == null) return "";

        String id = ((Integer)block.getId()).toString();
        String meta = ((Integer)block.getDamage()).toString();
        String name = block.getName();

        StringBuilder sb = new StringBuilder();
        sb.append(TextFormat.WHITE);
        sb.append(name);
        sb.append(" -> ");
        sb.append(TextFormat.GREEN);
        sb.append(id);
        sb.append(":");
        sb.append(meta);
        sb.append(TextFormat.YELLOW);
        sb.append(") - ");
        sb.append("位置(");
        sb.append(" X:" + block.getFloorX());
        sb.append(" Y:" + block.getFloorY());
        sb.append(" Z:" + block.getFloorZ());
        sb.append(")");
        return new String(sb);
    }

    // 更新情報ウィンドウ通知
    public void ShowUpdateWindow(Player player) {
        StringBuilder sb = new StringBuilder();
        try {
            PreparedStatement ps = getDB().getConnection().prepareStatement(getConfig().getString("SqlStatement.Sql0010"));
            ResultSet rs = getDB().ExecuteQuery(ps, null);
            if (rs != null) {
                while(rs.next()){
                    sb.append(TextFormat.GOLD);
                    sb.append("VER: ");
                    sb.append(rs.getString("version"));
                    sb.append(" (");
                    sb.append(rs.getString("add_date"));
                    sb.append(" ) -> ");
                    sb.append(TextFormat.WHITE);
                    sb.append(rs.getString("name"));
                    sb.append("\n");
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

            if (sb.length() > 0) {
                // ログイン中は画面が見えないためディレイ送信
                getServer().getInstance().getScheduler().scheduleDelayedTask(new Task() {
                    @Override
                    public void onRun(int currentTick) {
                        SimpleForm form = new SimpleForm("更新情報", new String(sb));
                        form.send(player, (targetPlayer, targetForm, data) -> {
                            if(data == -1) return;
                        });
                    }
                }, 200, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}