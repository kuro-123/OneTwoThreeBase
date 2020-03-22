package host.kuro.onetwothree;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.database.DatabaseManager;
import host.kuro.onetwothree.forms.elements.SimpleForm;
import host.kuro.onetwothree.item.ItemPrice;
import host.kuro.onetwothree.task.SkinTask;
import host.kuro.onetwothree.task.SoundTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static HashMap<Integer, ItemPrice> item_price = new HashMap<>();

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

    // 一定期間スリープする
    public void Sleep(int millsec) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        while(true) {
            endTime = System.currentTimeMillis();
            if ((endTime - startTime) > millsec) {
                break;
            }
            Thread.yield();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
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

    // サウンド再生
    public void PlaySound(Player player, int mode, String sound, int wait, boolean stop) {
        SoundTask task = new SoundTask(this, player, mode, sound, wait, stop);
        task.start();
    }

    public void SetupNukkitItems() {
        try {
            for (Class c : Item.list) {
                if (c != null) {
                    String name = c.getSimpleName();
                    name = name.replace("Block", "");
                    name = name.replace("Item", "");
                    Item item = Item.fromString(name);
                    if (item != null) {
                        int id = item.getId();
                        // ステータス更新
                        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
                        args.add(new DatabaseArgs("i", ""+id));
                        args.add(new DatabaseArgs("c", name));
                        int ret = getDB().ExecuteUpdate(getConfig().getString("SqlStatement.Sql0022"), args);
                        args.clear();
                        args = null;
                    }
                }
            }
            PreparedStatement ps = getDB().getConnection().prepareStatement(getConfig().getString("SqlStatement.Sql0023"));
            ResultSet rs = getDB().ExecuteQuery(ps, null);
            if (rs != null) {
                int i = 0;
                while(rs.next()){
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int price = rs.getInt("price");
                    item_price.put(id, new ItemPrice(i, id, name, price));
                    i++;
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
            e.printStackTrace();
        }
    }
}