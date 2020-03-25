package host.kuro.onetwothree;

import cn.nukkit.plugin.PluginBase;
import host.kuro.onetwothree.command.CommandManager;

import java.io.File;

public class BasePlugin extends PluginBase {

    private OneTwoThreeAPI api;

    @Override
    public void onEnable() {
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

        this.getLogger().info(Language.translate("onetwothree.loaded"));
        this.getLogger().info("IP: " + this.api.GetIpInfo());

        // アイテムデータセットアップ
        this.getLogger().info(Language.translate("onetwothree.datasetup"));
        api.SetupNukkitItems();

        // TwitterPlugin
        TwitterPlugin tw = new TwitterPlugin(this, api);
    }

    @Override
    public void onDisable() {
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
        }
        return true;
    }
}