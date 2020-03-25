package host.kuro.onetwothree;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.database.DatabaseManager;
import host.kuro.onetwothree.forms.CustomFormResponse;
import host.kuro.onetwothree.forms.Form;
import host.kuro.onetwothree.forms.ModalFormResponse;
import host.kuro.onetwothree.forms.SimpleFormResponse;
import host.kuro.onetwothree.task.SkinTask;
import host.kuro.onetwothree.task.SoundTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class EventListener implements Listener {

    private final OneTwoThreeAPI api;
    private final Map<String, Long> playtime = new HashMap<>();
    public EventListener(OneTwoThreeAPI api) {
        this.api = api;
    }

    @EventHandler
    public void onPlayerPreLogin(PlayerPreLoginEvent event) {
        Player player = event.getPlayer();
        // check xboxauth
        if (!player.getLoginChainData().isXboxAuthed()) {
            event.setCancelled();
            return;
        }

        try {
            // ネットワーク取得
            String ip = player.getAddress();
            String host = api.GetHostInfo(ip);

            // SQL
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));          // xuid
            args.add(new DatabaseArgs("i", "0"));     // rank
            args.add(new DatabaseArgs("c", player.getLoginChainData().getUsername()));         // xname
            args.add(new DatabaseArgs("c", host));      // host
            args.add(new DatabaseArgs("c", ip));      // ip
            args.add(new DatabaseArgs("c", "" + player.getLoginChainData().getClientId()));  // cid
            args.add(new DatabaseArgs("c", "" + player.getLoginChainData().getClientUUID()));  // uuid
            args.add(new DatabaseArgs("c", player.getLoginChainData().getDeviceId()));         // devid
            args.add(new DatabaseArgs("c", player.getLoginChainData().getDeviceModel()));         // devmodel
            args.add(new DatabaseArgs("i", "" + player.getLoginChainData().getDeviceOS()));   // devos
            args.add(new DatabaseArgs("c", player.getLoginChainData().getGameVersion()));       // version
            args.add(new DatabaseArgs("c", ""));  // play_status
            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0004"), args);
            args.clear();
            args = null;
            if (ret == DatabaseManager.DUPLICATE) {
                // 既に登録済み
                args = new ArrayList<DatabaseArgs>();
                args.add(new DatabaseArgs("c", host));      // host
                args.add(new DatabaseArgs("c", ip));      // ip
                args.add(new DatabaseArgs("c", "" + player.getLoginChainData().getClientId()));  // cid
                args.add(new DatabaseArgs("c", "" + player.getLoginChainData().getClientUUID()));  // uuid
                args.add(new DatabaseArgs("c", player.getLoginChainData().getDeviceId()));         // devid
                args.add(new DatabaseArgs("c", player.getLoginChainData().getDeviceModel()));         // devmodel
                args.add(new DatabaseArgs("i", "" + player.getLoginChainData().getDeviceOS()));   // devos
                args.add(new DatabaseArgs("c", player.getLoginChainData().getGameVersion()));       // version
                args.add(new DatabaseArgs("c", ""));       // play_status
                args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));       // xuid
                ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0005"), args);
                args.clear();
                args = null;
            }

            // ニックネーム取得
            String nickname = "";
            try {
                PreparedStatement ps = api.getDB().getConnection().prepareStatement(api.getConfig().getString("SqlStatement.Sql0009"));
                ArrayList<DatabaseArgs> xargs = new ArrayList<DatabaseArgs>();
                xargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
                ResultSet rs_name = api.getDB().ExecuteQuery(ps, xargs);
                xargs.clear();
                xargs = null;
                if (rs_name != null) {
                    while (rs_name.next()) {
                        nickname = rs_name.getString("name");
                        break;
                    }
                }
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
                if (rs_name != null) {
                    rs_name.close();
                    rs_name = null;
                }
            } catch (Exception e) {
                event.setCancelled();
            }
            if (nickname.length() > 0) {
                player.setDisplayName(nickname);
                player.setDataProperty(new StringEntityData(4, nickname), false); // 4 = DATA_NAMETAG
            }

            // プレイヤー情報チェック
            boolean hit = false;
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(api.getConfig().getString("SqlStatement.Sql0019"));
            ArrayList<DatabaseArgs> pargs = new ArrayList<DatabaseArgs>();
            pargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            ResultSet rs = api.getDB().ExecuteQuery(ps, pargs);
            pargs.clear();
            pargs = null;
            if (rs != null) {
                while (rs.next()) {
                    hit = true;
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
            if (!hit) {
                // なければ登録
                ArrayList<DatabaseArgs> iargs = new ArrayList<DatabaseArgs>();
                iargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0018"), iargs);
                iargs.clear();
                iargs = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // スキンタスク起動
        SkinTask task = new SkinTask(api, player);
        task.start();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 権限設定
        player.setOp(false);
        player.setGamemode(Player.SURVIVAL);
        int rank = api.GetRank(player);
        if (rank > 1) {
            player.setOp(true);
        }
        if (rank > 3) {
            player.setGamemode(Player.CREATIVE);
        }

        String allowname = api.getConfig().getString("GameSettings.AllowCreative");
        Level lv = api.getServer().getLevelByName(allowname);
        player.setSpawn(lv.getSpawnLocation());

        api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin014, 0, false); // ドアノック

        // ステータス更新
        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
        args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));          // xuid
        int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0006"), args);
        args.clear();
        args = null;

        // プレイヤー情報更新(LOGIN)
        ArrayList<DatabaseArgs> largs = new ArrayList<DatabaseArgs>();
        largs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
        ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0020"), largs);
        largs.clear();
        largs = null;

        // 経過時間計測開始
        playtime.put(player.getLoginChainData().getXUID(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // メモリ関連削除
        Form.playersForm.remove(player.getName());
        OneTwoThreeAPI.touch_mode.remove(player);

        // 経過時間計測
        int ptime = 0;
        String xuid = player.getLoginChainData().getXUID();
        if (playtime.containsKey(xuid)) {
            long start = playtime.get(xuid);
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            ptime = (int) (timeElapsed /= 1000); // 秒換算
        }

        // プレイ時間,ステータス更新
        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
        if (ptime > 0) {
            args.add(new DatabaseArgs("i", "" + ptime));
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));       // xuid
            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0007"), args);
        } else {
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));       // xuid
            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0008"), args);
        }
        args.clear();
        args = null;

        api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin061, 0, false); // ドアクローズ
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            Player player = event.getPlayer();
            // タッチモード
            if (OneTwoThreeAPI.touch_mode.containsKey(player)) {
                if (OneTwoThreeAPI.touch_mode.get(player)) {
                    Block block = event.getBlock();
                    player.sendMessage(api.GetInfoMessage(api.GetBlockInfoMessage(block)));
                    event.setCancelled();
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        try {
            Player player = event.getPlayer();

            if (api.IsTouchmode(player)) {
                api.GetWarningMessage(Language.translate("onetwothree.othermode"));
                event.setCancelled();
                return;
            }

            // 一時しのぎ
            if (player.getLevel().getName().equals("lobby") || player.getLevel().getName().equals("city")) {
                int rank = api.GetRank(player);
                if (rank < 3) {
                    player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    event.setCancelled();
                    return;
                }
            }

            // 自然ワールドでドロップしたアイテムには印をつけておく
            if (!(player.getLevel().getName().equals("lobby") || player.getLevel().getName().equals("city"))) {
                String symbol = api.getConfig().getString("GameSettings.ItemTag");
                if (symbol.length() > 0) {
                    Item[] drops = event.getDrops();
                    for (Item item: drops) {
                        item.setCustomName(item.getName()+symbol);
                    }
                }
            }

            // プレイヤー情報更新(BREAK)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));          // xuid
            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0012"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        try {
            Player player = event.getPlayer();

            if (api.IsTouchmode(player)) {
                api.GetWarningMessage(Language.translate("onetwothree.othermode"));
                event.setCancelled();
                return;
            }

            // 一時しのぎ
            if (player.getLevel().getName().equals("lobby") || player.getLevel().getName().equals("city")) {
                int rank = api.GetRank(player);
                if (rank < 3) {
                    player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    event.setCancelled();
                    return;
                }
            }

            // プレイヤー情報更新(PLACE)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));          // xuid
            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0013"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        try {
            // プレイヤー情報更新(KICK)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", event.getPlayer().getLoginChainData().getXUID()));          // xuid
            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0014"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerKickEvent event) {
        try {
            // プレイヤー情報更新(DEATH)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", event.getPlayer().getLoginChainData().getXUID()));          // xuid
            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0016"), args);
            args.clear();
            args = null;

            Entity entity = event.getPlayer();
            EntityDamageEvent cause = entity.getLastDamageCause();
            if (cause instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();
                if (damager instanceof Player) {
                    Player killer = (Player) damager;
                    // プレイヤー情報更新(KILL)
                    ArrayList<DatabaseArgs> kargs = new ArrayList<DatabaseArgs>();
                    args.add(new DatabaseArgs("c", killer.getLoginChainData().getXUID()));          // xuid
                    ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0015"), kargs);
                    kargs.clear();
                    kargs = null;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        try {
            Entity entity = event.getEntity();

            // 自然ワールドでドロップしたアイテムには印をつけておく
            if (!(entity.getLevel().getName().equals("lobby") || entity.getLevel().getName().equals("city"))) {
                String symbol = api.getConfig().getString("GameSettings.ItemTag");
                if (symbol.length() > 0) {
                    Item[] drops = event.getDrops();
                    for (Item item: drops) {
                        item.setCustomName(item.getName()+symbol);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        try {
            // プレイヤー情報更新(CHAT)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", event.getPlayer().getLoginChainData().getXUID()));          // xuid
            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0017"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        try {
            Player player = event.getPlayer();
            String sFrom = event.getFrom().getLevel().getName();
            String sTo = event.getTo().getLevel().getName();
            if (sFrom.equals(sTo)) return;
            String allowname = api.getConfig().getString("GameSettings.AllowCreative");
            if (!event.getTo().getLevel().getName().equals("lobby") && !event.getTo().getLevel().getName().equals("city")) {
                //if (!sTo.equals(allowname)) {
                player.setGamemode(Player.SURVIVAL);
                player.sendMessage(api.GetWarningMessage(Language.translate("onetwothree.forceSurvival")));
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin005, 0, false); // forcemode
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        try {
            Player player = event.getPlayer();
            String sRespawn = event.getRespawnPosition().getLevel().getName();
            String allowname = api.getConfig().getString("GameSettings.AllowCreative");
            if (!sRespawn.equals(allowname)) {
                Level lv = api.getServer().getLevelByName(allowname);
                if (lv != null) {
                    event.setRespawnPosition(lv.getSpawnLocation());
                    player.setSpawn(lv.getSpawnLocation());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        try {
            Player player = event.getPlayer();

            // 一時しのぎ
            int rank = api.GetRank(player);
            if (rank < 3) {
                player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                event.setCancelled();
                return;
            }

            int newmode = event.getNewGamemode();
            String lvname = player.getLevel().getName();
            String allowname = api.getConfig().getString("GameSettings.AllowCreative");
            if (!lvname.equals("lobby") && !lvname.equals("city")) {
                if (newmode != Player.SURVIVAL) {
                    player.sendMessage(api.GetWarningMessage(Language.translate("onetwothree.gamemod_err")));
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    event.setCancelled();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void formResponded(PlayerFormRespondedEvent event) {
        try {
            Player player = event.getPlayer();
            FormWindow window = event.getWindow();
            FormResponse response = window.getResponse();

            if (Form.playersForm.containsKey(player.getName())) {
                host.kuro.onetwothree.forms.FormResponse temp = Form.playersForm.get(player.getName());
                Form.playersForm.remove(player.getName());

                Object data;

                if (response == null || event.wasClosed()) {
                    if (temp instanceof CustomFormResponse) {
                        ((CustomFormResponse) temp).handle(player, window, null);

                    } else if (temp instanceof ModalFormResponse) {
                        ((ModalFormResponse) temp).handle(player, window, -1);

                    } else if (temp instanceof SimpleFormResponse) {
                        ((SimpleFormResponse) temp).handle(player, window, -1);
                    }
                    return;
                }

                if (window instanceof FormWindowSimple) {
                    data = ((FormResponseSimple) response).getClickedButtonId();
                    ((SimpleFormResponse) temp).handle(player, window, (int) data);
                    return;
                }

                if (window instanceof FormWindowCustom) {
                    data = new ArrayList<>(((FormResponseCustom) response).getResponses().values());
                    ((CustomFormResponse) temp).handle(player, window, (List<Object>) data);
                    return;
                }

                if (window instanceof FormWindowModal) {
                    data = ((FormResponseModal) response).getClickedButtonId();
                    ((ModalFormResponse) temp).handle(player, window, (int) data);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}