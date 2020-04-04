package host.kuro.onetwothree;

import cn.nukkit.plugin.PluginBase;
import host.kuro.onetwothree.command.CommandManager;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.task.RebootTask;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Calendar;

public class BasePlugin extends PluginBase {

    private OneTwoThreeAPI api;
    private TwitterPlugin twitter;
    private boolean debug;

    @Override
    public void onEnable() {
        debug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
        this.getDataFolder().mkdirs();
        Language.load(this.getServer().getLanguage().getLang());
        // CONFIG
        saveDefaultConfig();
        // 初期ディレクトリチェック
        InitDirectory("");
        InitDirectory("skin");
        // API
        this.api = new OneTwoThreeAPI(this);
        // DB
        this.api.getDB().Connect();
        // CMD
        CommandManager.registerAll(this.api);
        // EVENT
        this.getServer().getPluginManager().registerEvents(new EventListener(this.api), this);
        // IP表示
        this.getLogger().info("IP: " + this.api.GetIpInfo());
        // アイテムデータセットアップ
        Calendar cal = Calendar.getInstance();
        int week = cal.get(Calendar.DAY_OF_WEEK);
        if (week == Calendar.SUNDAY) {
            this.getLogger().info(Language.translate("onetwothree.datasetup"));
            api.SetupNukkitItems();
        }
        // TwitterPlugin
        twitter = new TwitterPlugin(this, api);

        // 起動
        this.getLogger().info(Language.translate("onetwothree.loaded"));

        // 起動ツイート
        if (!debug) {
            api.getTwitter().Tweet("【123鯖情報】 起動しました！アプデ内容等はゲーム内！WEBで！\n\n#123鯖");
        }
    }
    public TwitterPlugin getTwitter() {
        return twitter;
    }

    @Override
    public void onDisable() {
        // ステータスクリア
        if (!debug) {
            api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0044"), null);
        }
        this.api.getDB().DisConnect();
        OneTwoThreeAPI.touch_mode.clear();
        OneTwoThreeAPI.item_price.clear();
        OneTwoThreeAPI.player_list.clear();
        this.getLogger().info(Language.translate("onetwothree.unloaded"));
    }

    private boolean InitDirectory(String opt) {
        String path;
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.toLowerCase().indexOf("windows") >= 0) {
                path = getDataFolder().getPath() + "/";
            } else {
                path = getConfig().getString("Web.DataPath");
                if (path.length() <= 0) {
                    path = getDataFolder().getPath() + "/";
                }
            }
            if (opt.length() > 0) {
                path += opt + "/";
            }
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
                f.setExecutable(true);
                f.setWritable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(null, e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), "");
        }
        return true;
    }
}