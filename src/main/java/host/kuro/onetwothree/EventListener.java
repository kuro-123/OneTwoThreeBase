package host.kuro.onetwothree;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.database.DatabaseManager;
import host.kuro.onetwothree.task.SkinTask;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventListener implements Listener {

    private final OneTwoThreeAPI api;
    private final Map<String, Long> playtime = new HashMap<>();

    public EventListener(OneTwoThreeAPI api) {
        this.api = api;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
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
            args.add(new DatabaseArgs("c", ""+player.getLoginChainData().getClientId()));  // cid
            args.add(new DatabaseArgs("c", ""+player.getLoginChainData().getClientUUID()));  // uuid
            args.add(new DatabaseArgs("c", player.getLoginChainData().getDeviceId()));         // devid
            args.add(new DatabaseArgs("c", player.getLoginChainData().getDeviceModel()));         // devmodel
            args.add(new DatabaseArgs("i", ""+player.getLoginChainData().getDeviceOS()));   // devos
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
                args.add(new DatabaseArgs("c", ""+player.getLoginChainData().getClientId()));  // cid
                args.add(new DatabaseArgs("c", ""+player.getLoginChainData().getClientUUID()));  // uuid
                args.add(new DatabaseArgs("c", player.getLoginChainData().getDeviceId()));         // devid
                args.add(new DatabaseArgs("c", player.getLoginChainData().getDeviceModel()));         // devmodel
                args.add(new DatabaseArgs("i", ""+player.getLoginChainData().getDeviceOS()));   // devos
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
                    while(rs_name.next()){
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
                while(rs.next()){
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
    	player.setOp(true);
    	player.setGamemode(1);

        // ステータス更新
        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
        args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));          // xuid
        int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0006"), args);
        args.clear();
        args = null;

        // バージョン情報表示
        StringBuilder sb = new StringBuilder();
        try {
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(api.getConfig().getString("SqlStatement.Sql0010"));
            ResultSet rs = api.getDB().ExecuteQuery(ps, null);
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
        } catch (Exception e) {
        }
        if (sb.length() > 0) {
            // ログイン中は画面が見えないためディレイ送信
            api.getServer().getInstance().getScheduler().scheduleDelayedTask(new Task() {
                @Override
                public void onRun(int currentTick) {
                    player.sendMessage(new String(sb));
                }
            }, 200, true);
        }

        // プレイヤー情報更新(LOGIN)
        ArrayList<DatabaseArgs> largs = new ArrayList<DatabaseArgs>();
        largs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
        ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0020"), largs);
        largs.clear();
        largs = null;

        // 経過時間計測開始
        playtime.put(player.getLoginChainData().getXUID(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

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
            args.add(new DatabaseArgs("i", ""+ptime));
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));       // xuid
            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0007"), args);
        } else {
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));       // xuid
            int ret = api.getDB().ExecuteUpdate(api.getConfig().getString("SqlStatement.Sql0008"), args);
        }
        args.clear();
        args = null;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        try {
            // プレイヤー情報更新(BREAK)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", event.getPlayer().getLoginChainData().getXUID()));          // xuid
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
            // プレイヤー情報更新(PLACE)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", event.getPlayer().getLoginChainData().getXUID()));          // xuid
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
                    Player killer = (Player)damager;
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
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    }
}