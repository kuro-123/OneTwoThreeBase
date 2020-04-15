package host.kuro.onetwothree;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.block.ItemFrameDropItemEvent;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.entity.EntityShootBowEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.inventory.InventoryOpenEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.inventory.*;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.item.Item;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.database.DatabaseArgs;
import host.kuro.onetwothree.database.DatabaseManager;
import host.kuro.onetwothree.datatype.WorldInfo;
import host.kuro.onetwothree.datatype.ZoneInfo;
import host.kuro.onetwothree.forms.CustomFormResponse;
import host.kuro.onetwothree.forms.Form;
import host.kuro.onetwothree.forms.ModalFormResponse;
import host.kuro.onetwothree.forms.SimpleFormResponse;
import host.kuro.onetwothree.forms.elements.CustomForm;
import host.kuro.onetwothree.npc.NpcMerchantType01;
import host.kuro.onetwothree.npc.NpcMerchantType02;
import host.kuro.onetwothree.npc.NpcType;
import host.kuro.onetwothree.task.SkinTask;
import host.kuro.onetwothree.task.SoundTask;
import host.kuro.onetwothree.utils.Particle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static cn.nukkit.event.entity.EntityDamageEvent.DamageCause.SUICIDE;

public class EventListener implements Listener {

    private final OneTwoThreeAPI api;
    public EventListener(OneTwoThreeAPI api) {
        this.api = api;
    }

    @EventHandler
    public void onPlayerPreLogin(PlayerPreLoginEvent event) {
        Player player = event.getPlayer();
        try {
            // XBOX認証チェック
            if (!player.getLoginChainData().isXboxAuthed()) {
                event.setCancelled();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onPlayerPreLogin : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        try {
            if (!api.CheckCid(player)) {
                api.getServer().getLogger().info("過去バン:" + player.getDisplayName());
                player.close("", "過去にバンされているためログインできません", true);
                event.setCancelled();
                return;
            }
            if (!api.CheckXuid(player)) {
                api.getServer().getLogger().info("過去バン:" + player.getDisplayName());
                player.close("", "過去にバンされているためログインできません", true);
                event.setCancelled();
                return;
            }

            // ネットワーク取得
            String ip = player.getAddress();
            //String host = api.GetHostInfo(ip);
            String host = ""; // 一時的 重いので後々非同期でやらせる

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
            int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0004"), args);
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
                ret = api.getDB().ExecuteUpdate(Language.translate("Sql0005"), args);
                args.clear();
                args = null;
            }

            // プレイヤー情報チェック
            boolean hit = false;
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0019"));
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
                // なければ登録 info
                ArrayList<DatabaseArgs> iargs = new ArrayList<DatabaseArgs>();
                iargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                iargs.add(new DatabaseArgs("i", "0"));
                ret = api.getDB().ExecuteUpdate(Language.translate("Sql0018"), iargs);
                iargs.clear();
                iargs = null;
            }

            // プレイヤー財布チェック
            hit = false;
            ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0059"));
            ArrayList<DatabaseArgs> waargs = new ArrayList<DatabaseArgs>();
            waargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            ResultSet rs_wa = api.getDB().ExecuteQuery(ps, waargs);
            waargs.clear();
            waargs = null;
            if (rs_wa != null) {
                while (rs_wa.next()) {
                    hit = true;
                    break;
                }
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (rs_wa != null) {
                rs_wa.close();
                rs_wa = null;
            }
            if (!hit) {
                // なければ登録 wallet
                ArrayList<DatabaseArgs> wargs = new ArrayList<DatabaseArgs>();
                wargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
                ret = api.getDB().ExecuteUpdate(Language.translate("Sql0058"), wargs);
                wargs.clear();
                wargs = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onPlayerLogin : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 階段半ブロバグ対応
        player.setCheckMovement(false);

        // 視界距離設定
        WorldInfo worldinfo = api.GetWorldInfo(player);
        if (worldinfo.viewdistance > 0) {
            player.setViewDistance(worldinfo.viewdistance);
        }

        // 権限設定
        player.setOp(false);
        player.setGamemode(Player.SURVIVAL);
        int rank = api.GetRank(player);
        api.play_rank.put(player, rank);
        if (rank >= 1) {
            player.setOp(true);
        }

        // 初期値設定
        if (rank >= 1) {
            String second = api.getConfig().getString("GameSettings.SecondSpawn");
            player.setSpawn(api.getServer().getLevelByName(second).getSpawnLocation());
        } else {
            String first = api.getConfig().getString("GameSettings.FirstSpawn");
            player.setSpawn(api.getServer().getLevelByName(first).getSpawnLocation());
        }

        api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin014, 0, false); // ドアノック

        // ステータス更新
        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
        args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));          // xuid
        int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0006"), args);
        args.clear();
        args = null;

        // プレイヤー情報更新(LOGIN)
        ArrayList<DatabaseArgs> largs = new ArrayList<DatabaseArgs>();
        largs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
        ret = api.getDB().ExecuteUpdate(Language.translate("Sql0020"), largs);
        largs.clear();
        largs = null;

        // ニックネーム/タグ取得
        String nickname = "";
        String tag = "";
        try {
            PreparedStatement ps = api.getDB().getConnection().prepareStatement(Language.translate("Sql0009"));
            ArrayList<DatabaseArgs> xargs = new ArrayList<DatabaseArgs>();
            xargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
            ResultSet rs_name = api.getDB().ExecuteQuery(ps, xargs);
            xargs.clear();
            xargs = null;
            if (rs_name != null) {
                while (rs_name.next()) {
                    nickname = rs_name.getString("name");
                    tag = rs_name.getString("tag");
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
            api.getLogErr().Write(player, "onPlayerJoin : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
        player.setDisplayName(player.getName());
        if (nickname != null) {
            if (nickname.length() > 0) {
                player.setDisplayName(nickname);
                player.setDataProperty(new StringEntityData(4, nickname), false); // 4 = DATA_NAMETAG
            }
        }
        player.setNameTagVisible(true);
        player.setNameTagAlwaysVisible(true);
        if (tag != null) {
            if (tag.length() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(player.getDisplayName());
                if (tag.length() > 0) {
                    sb.append("\n");
                    sb.append(TextFormat.WHITE);
                    sb.append(tag);
                }
                player.setNameTag(new String(sb));
            } else {
                player.setNameTag(player.getDisplayName());
            }
        } else {
            player.setNameTag(player.getDisplayName());
        }

        // スキンタスク起動
        SkinTask task = new SkinTask(api, player);
        task.start();

        // 参加メッセージ
        StringBuilder sb_join = new StringBuilder();
        sb_join.append(TextFormat.YELLOW);
        sb_join.append(player.getDisplayName());
        sb_join.append("さん ");
        sb_join.append(api.GetRankColor(player));
        sb_join.append("<");
        sb_join.append(api.GetRankName(player));
        sb_join.append("> ");
        sb_join.append(TextFormat.YELLOW);
        sb_join.append("が参加しました");
        String message = new String(sb_join);
        event.setJoinMessage(message);
        api.sendDiscordBlueMessage(message);

        // 経過時間計測開始
        api.play_time.put(player.getLoginChainData().getXUID(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        api.getAfk().ResetAfk(player);

        // 経過時間計測
        int ptime = 0;
        String xuid = player.getLoginChainData().getXUID();
        if (api.play_time.containsKey(xuid)) {
            long start = api.play_time.get(xuid);
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            ptime = (int) (timeElapsed /= 1000); // 秒換算
        }

        // プレイ時間,ステータス更新
        ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
        if (ptime > 0) {
            args.add(new DatabaseArgs("i", "" + ptime));
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));       // xuid
            int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0007"), args);
        } else {
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));       // xuid
            int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0008"), args);
        }
        args.clear();
        args = null;

        // 退出メッセージ
        StringBuilder sb_quit = new StringBuilder();
        sb_quit.append(TextFormat.YELLOW);
        sb_quit.append(player.getDisplayName());
        sb_quit.append("さん ");
        sb_quit.append(api.GetRankColor(player));
        sb_quit.append("<");
        sb_quit.append(api.GetRankName(player));
        sb_quit.append("> ");
        sb_quit.append(TextFormat.YELLOW);
        sb_quit.append("が退出しました");
        String message = new String(sb_quit);
        event.setQuitMessage(message);
        api.sendDiscordGrayMessage(message);

        // メモリ関連削除
        Form.playersForm.remove(player.getName());
        OneTwoThreeAPI.mode.remove(player);
        OneTwoThreeAPI.npc_info.remove(player);
        OneTwoThreeAPI.select_seq.remove(player);
        OneTwoThreeAPI.select_one.remove(player);
        OneTwoThreeAPI.select_two.remove(player);
        OneTwoThreeAPI.tip_wait.remove(player);
        api.play_time.remove(player);
        api.play_rank.remove(player);

        api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin061, 0, false); // ドアクローズ
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof NpcType) {
            ((NpcType)entity).interact(event.getPlayer(), event.getItem());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        try {
            // タッチモード
            if (OneTwoThreeAPI.mode != null) {
                if (OneTwoThreeAPI.mode.containsKey(player)) {
                    if (OneTwoThreeAPI.mode.get(player) == OneTwoThreeAPI.TAP_MODE.MODE_TOUCH) {
                        Block block = event.getBlock();
                        player.sendMessage(api.GetInfoMessage(api.GetBlockInfoMessage(block)));
                        event.setCancelled();
                        return;
                    }
                    else if (OneTwoThreeAPI.mode.get(player) == OneTwoThreeAPI.TAP_MODE.MODE_KUROVIEW) {
                        Block block = event.getBlock();
                        api.OpenKuroView(player, block);
                        event.setCancelled();
                        return;
                    }
                    else if (OneTwoThreeAPI.mode.get(player) == OneTwoThreeAPI.TAP_MODE.MODE_NPC) {
                        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin008, 0, false); // SUCCESS
                        OneTwoThreeAPI.mode.put(player, OneTwoThreeAPI.TAP_MODE.MODE_NONE);
                        Block block = event.getBlock();
                        api.SpawnNpc(player, block);
                        event.setCancelled();
                        return;
                    }
                    else if (OneTwoThreeAPI.mode.get(player) == OneTwoThreeAPI.TAP_MODE.MODE_ZONE) {
                        api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin053, 0, false); // point
                        Block block = event.getBlock();
                        int ret = api.Selection(player, block.getLocation());
                        if (ret == 1) {
                            player.sendTitle("ゾーンモード", TextFormat.YELLOW + "ポイント２を選択してください", 10, 100, 10);
                        } else if (ret == 2) {
                            // 2点設定完了
                            api.SetZoneRank(player);
                        }
                        return;
                    }
                }
            }

            // BANアイテム
            if (api.IsBanItem(player, event.getItem())) {
                event.setCancelled();
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onPlayerInteract : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        api.getAfk().ResetAfk(player);
        try {
            if (api.IsTouchmode(player) != OneTwoThreeAPI.TAP_MODE.MODE_NONE) {
                api.GetWarningMessage(Language.translate("onetwothree.othermode"));
                event.setCancelled();
                return;
            }

            // 整地許可チェック
            WorldInfo worldinfo = api.GetWorldInfo(player);
            if (!api.IsGameMaster(player)) {
                if (!worldinfo.bbreak) {
                    player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    event.setCancelled();
                    return;
                }
            }

            // タグアイテム許可の場合はタグ付けする
            if (worldinfo.tagitem) {
                String symbol = api.getConfig().getString("GameSettings.ItemTag");
                if (symbol.length() > 0) {
                    Item[] drops = event.getDrops();
                    for (Item item: drops) {
                        item.setCustomName(item.getName()+symbol);
                    }
                }
                // ランダムプレゼント抽選
                api.RandomPresent(player, symbol);
            }

            // ブロックログ
            Block block = event.getBlock();
            int bid   = block.getId();
            int bmeta = block.getDamage();
            String bname = block.getName();
            String cname = "none";
            Item item = event.getItem();
            if (item != null) {
                cname = item.getCustomName();
            }
            api.getLogBlock().Write(player, block.getLevel().getName(), ""+block.getFloorX(), ""+block.getFloorY(), ""+block.getFloorZ(), "break", ""+bid, ""+bmeta, bname, cname);

            // プレイヤー情報更新(BREAK)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));          // xuid
            int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0012"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onBlockBreak : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        api.getAfk().ResetAfk(player);
        try {
            if (api.IsTouchmode(player) != OneTwoThreeAPI.TAP_MODE.MODE_NONE) {
                api.GetWarningMessage(Language.translate("onetwothree.othermode"));
                event.setCancelled();
                return;
            }

            Block block = event.getBlock();

            // BANアイテム
            if (api.IsBanItem(player, block.toItem())) {
                event.setCancelled();
                return;
            }

            // 建築許可チェック
            if (!api.IsGameMaster(player)) {
                WorldInfo worldinfo = api.GetWorldInfo(player);
                if (!worldinfo.bplace) {
                    player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                    event.setCancelled();
                    return;
                }
            }

            // ブロックログ
            int bid   = block.getId();
            int bmeta = block.getDamage();
            String bname = block.getName();
            String cname = "none";
            Item item = event.getItem();
            if (item != null) {
                cname = item.getCustomName();
            }
            api.getLogBlock().Write(player, block.getLevel().getName(), ""+block.getFloorX(), ""+block.getFloorY(), ""+block.getFloorZ(), "place", ""+bid, ""+bmeta, bname, cname);

            // プレイヤー情報更新(PLACE)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));          // xuid
            int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0013"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onBlockPlace : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        try {
            // プレイヤー情報更新(KICK)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", event.getPlayer().getLoginChainData().getXUID()));          // xuid
            int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0014"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onPlayerKick : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        Entity damager = event.getDamager();
        if (!(damager instanceof Player)) return;

        // PVP許可チェック
        Player player = (Player)damager;
        if (!api.IsGameMaster(player)) {
            WorldInfo worldinfo = api.GetWorldInfo(player);
            if (!worldinfo.pvp) {
                player.sendMessage(api.GetWarningMessage("onetwothree.not_pvp"));
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) return;
        Player player = null;
        if (entity instanceof Player) {
            player = (Player)entity;
        }
        if (player == null) return;

        try {
            // プレイヤー情報更新(DEATH)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));          // xuid
            int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0016"), args);
            args.clear();
            args = null;

            // キラー取得
            Player killer = null;
            String killername = "";
            String killitem = "";
            EntityDamageEvent cause = entity.getLastDamageCause();
            if (cause != null) {
                if (cause instanceof EntityDamageByEntityEvent) {
                    Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();
                    if (damager instanceof Player) {
                        killer = (Player) damager;
                        Item head = Item.get(Item.SKULL,3,1);
                        head.setCustomName(player.getDisplayName() + "の首");
                        ((Player)killer).getInventory().addItem(head);
                        // プレイヤー情報更新(KILL)
                        ArrayList<DatabaseArgs> kargs = new ArrayList<DatabaseArgs>();
                        kargs.add(new DatabaseArgs("c", killer.getLoginChainData().getXUID()));          // xuid
                        ret = api.getDB().ExecuteUpdate(Language.translate("Sql0015"), kargs);
                        kargs.clear();
                        kargs = null;
                    }
                }
            }
            if (killer != null) {
                if (killer instanceof Player) {
                    killername = ((Player)killer).getName();
                    PlayerInventory inv = ((Player)killer).getInventory();
                    killitem = inv.getItemInHand().getName();
                }
            }

            String cause_name = "";
            if (cause != null) {
                EntityDamageEvent.DamageCause causeid = cause.getCause();
                switch (causeid) {
                    case ENTITY_ATTACK: cause_name = "死因: 攻撃"; break;
                    case PROJECTILE: cause_name = "死因: 射抜"; break;
                    case SUICIDE: cause_name = "死因: 自殺"; break;
                    case VOID: cause_name = "死因: 奈落"; break;
                    case FALL: cause_name = "死因: 落下"; break;
                    case SUFFOCATION: cause_name = "死因: 窒息"; break;
                    case LAVA: cause_name = "死因: 溶岩"; break;
                    case FIRE: cause_name = "死因: 焼死"; break;
                    case FIRE_TICK: cause_name = "死因: 炎"; break;
                    case DROWNING: cause_name = "死因: 溺死"; break;
                    case CONTACT: cause_name = "死因: サボテン"; break;
                    case BLOCK_EXPLOSION: cause_name = "死因: 爆発"; break;
                    case ENTITY_EXPLOSION: cause_name = "死因: 爆発"; break;
                    case MAGIC: cause_name = "死因: 魔法"; break;
                    case LIGHTNING: cause_name = "死因: 雷"; break;
                    default: cause_name = "死因: 不明"; break;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(TextFormat.YELLOW);
                sb.append("[ ");
                sb.append(player.getDisplayName());
                sb.append(" さん ] が あひ～！ [ ");
                sb.append(TextFormat.RED);
                sb.append(cause_name);
                sb.append(TextFormat.YELLOW);
                sb.append(" ]");
                if (killername.length() > 0) {
                    sb.append(TextFormat.YELLOW);
                    sb.append(" [ 殺害者: ");
                    sb.append(TextFormat.RED);
                    sb.append(killername);
                    sb.append(TextFormat.YELLOW);
                    sb.append(" ]");
                }
                if (killitem.length() > 0) {
                    sb.append(TextFormat.YELLOW);
                    sb.append(" [ ｱｲﾃﾑ: ");
                    sb.append(TextFormat.RED);
                    sb.append(killitem);
                    sb.append(TextFormat.YELLOW);
                    sb.append(" ]");
                }
                String message = new String(sb);
                api.getServer().broadcastMessage(message);
                api.sendDiscordYellowMessage(message);
            }

            Particle.SpiralFlame(player, player.getLevel(), player.getX(), player.getY(), player.getZ());
            api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin002, 0, false); // ボンッ

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onPlayerDeath : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        try {
            Entity entity = event.getEntity();
            // タグアイテム許可の場合はタグ付けする
            WorldInfo worldinfo = api.GetWorldInfo(entity.getLevel());
            if (worldinfo.tagitem) {
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
            api.getLogErr().Write(null, "onEntityDeath : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), "");
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        api.getAfk().ResetAfk(player);
        try {
            String message = event.getMessage();

            // Discord連携
            if (api.getConfig().getString("Discord.botToken").length() > 0) {
                api.sendDiscordMessage(player, message);
            }

            // 権限カラー
            String color = api.GetRankColor(player);
            event.setMessage(color + message);

            // チャット分離
            WorldInfo worldinfo = api.GetWorldInfo(player);
            if (worldinfo.splitchat) {
                for (Player p : player.getLevel().getPlayers().values()) {
                    p.sendMessage(color + message);
                }
                event.setCancelled();
                return;
            }

            // プレイヤー情報更新(CHAT)
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            args.add(new DatabaseArgs("c", event.getPlayer().getLoginChainData().getXUID()));          // xuid
            int ret = api.getDB().ExecuteUpdate(Language.translate("Sql0017"), args);
            args.clear();
            args = null;

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onPlayerChat : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        api.getAfk().ResetAfk(player);
        try {
            String sFrom = event.getFrom().getLevel().getName();
            String sTo = event.getTo().getLevel().getName();
            if (sFrom.equals(sTo)) return;

            // クリエのままワープするとアイテム消去
            if (player.isCreative()) {
                player.getInventory().clearAll();
            }

            // モード違反があれば強制サバイバル
            Level lv = event.getTo().getLevel();
            if (lv == null) return;
            WorldInfo worldinfo = api.GetWorldInfo(lv);
            int gamemode = player.getGamemode();
            switch (gamemode) {
                case Player.SURVIVAL:
                    break;
                case Player.CREATIVE:
                    if (!worldinfo.creative) {
                        player.setGamemode(Player.SURVIVAL);
                    }
                    break;
                case Player.SPECTATOR:
                    if (!worldinfo.spectator) {
                        player.setGamemode(Player.SURVIVAL);
                    }
                    break;
                case Player.ADVENTURE:
                    if (!worldinfo.adventure) {
                        player.setGamemode(Player.SURVIVAL);
                    }
                    break;
            }

            // 入場曲設定
            if (!sFrom.equals(sTo)) {
                if (sTo.indexOf("nature") >=0) {
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin024, 2000, false); // 資源ワールドIN
                }
                else if (sTo.indexOf("city") >=0) {
                    api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin025, 2000, false); // 街ワールドIN
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onPlayerTeleport : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        api.getAfk().ResetAfk(player);
        try {
            // スポーン時にモード違反があれば強制サバイバル転送
            Level lv = event.getRespawnPosition().getLevel();
            if (lv == null) return;

            WorldInfo worldinfo = api.GetWorldInfo(player);
            int gamemode = player.getGamemode();
            switch (gamemode) {
                case Player.SURVIVAL:
                    break;
                case Player.CREATIVE:
                    if (!worldinfo.creative) {
                        player.setGamemode(Player.SURVIVAL);
                        event.setRespawnPosition(lv.getSpawnLocation());
                        player.setSpawn(lv.getSpawnLocation());
                    }
                    break;
                case Player.SPECTATOR:
                    if (!worldinfo.spectator) {
                        player.setGamemode(Player.SURVIVAL);
                        event.setRespawnPosition(lv.getSpawnLocation());
                        player.setSpawn(lv.getSpawnLocation());
                    }
                    break;
                case Player.ADVENTURE:
                    if (!worldinfo.adventure) {
                        player.setGamemode(Player.SURVIVAL);
                        event.setRespawnPosition(lv.getSpawnLocation());
                        player.setSpawn(lv.getSpawnLocation());
                    }
                    break;
            }

            // 視界距離設定
            if (worldinfo.viewdistance > 0) {
                player.setViewDistance(worldinfo.viewdistance);
            }

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onPlayerRespawn : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        api.getAfk().ResetAfk(player);
        try {
            if (api.IsGameMaster(player)) return;

            // ゲームモードチェック
            WorldInfo worldinfo = api.GetWorldInfo(player);
            if (worldinfo == null) return;

            StringBuilder sb = new StringBuilder();
            sb.append(TextFormat.RED);
            sb.append("【強制ログアウト】 [");
            sb.append(TextFormat.WHITE);
            sb.append(player.getDisplayName());
            sb.append(TextFormat.RED);
            sb.append("] さんがゲームモードを変更しようとしたため強制的にログアウトされました");

            int newmode = event.getNewGamemode();
            switch (newmode) {
                case Player.SURVIVAL:
                    if (!worldinfo.survival) {
                        api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin001, 0, false); // ブブー
                        event.setCancelled();
                        player.close("", "ゲームモードの変更は許可されていないため閉じられました。リログしてください", true);
                        String message = new String(sb);
                        api.getServer().broadcastMessage(message);
                        api.sendDiscordRedMessage(message);
                        return;
                    }
                    break;
                case Player.CREATIVE:
                    if (!worldinfo.creative) {
                        api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin001, 0, false); // ブブー
                        event.setCancelled();
                        player.close("", "ゲームモードの変更は許可されていないため閉じられました。リログしてください", true);
                        String message = new String(sb);
                        api.getServer().broadcastMessage(message);
                        api.sendDiscordRedMessage(message);
                        return;
                    }
                    break;
                case Player.SPECTATOR:
                    if (!worldinfo.spectator) {
                        api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin001, 0, false); // ブブー
                        event.setCancelled();
                        player.close("", "ゲームモードの変更は許可されていないため閉じられました。リログしてください", true);
                        String message = new String(sb);
                        api.getServer().broadcastMessage(message);
                        api.sendDiscordRedMessage(message);
                        return;
                    }
                    break;
                case Player.ADVENTURE:
                    api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin001, 0, false); // ブブー
                    event.setCancelled();
                    player.close("", "ゲームモードの変更は許可されていないため閉じられました。リログしてください", true);
                    String message = new String(sb);
                    api.getServer().broadcastMessage(message);
                    api.sendDiscordRedMessage(message);
                    return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(player, "onPlayerGameModeChange : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void formResponded(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        try {
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
            api.getLogErr().Write(player, "formResponded : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), player.getDisplayName());
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        // ベッド許可チェック
        WorldInfo worldinfo = api.GetWorldInfo(player);
        if (worldinfo == null) return;
        if (!worldinfo.bed) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onItemFrameDropItem(ItemFrameDropItemEvent event) {
        Player player = event.getPlayer();
        // BANアイテム
        if (api.IsBanItem(player, event.getItem())) {
            event.setCancelled();
            return;
        }
        // 破壊チェック
        WorldInfo worldinfo = api.GetWorldInfo(player);
        if (worldinfo == null) return;
        if (!worldinfo.bbreak) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        // BANアイテム
        if (api.IsBanItem(player, event.getItem())) {
            event.setCancelled();
            return;
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        // BANアイテム
        if (api.IsBanItem(player, event.getItem())) {
            event.setCancelled();
            return;
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        // BANアイテム
        if (api.IsBanItem(player, event.getItem())) {
            event.setCancelled();
            return;
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        // BANアイテム
        if (api.IsBanItem(player, event.getItem())) {
            event.setCancelled();
            return;
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        Entity entity = event.getEntity();
        Player player = null;
        if (entity instanceof Player) {
            player = (Player)player;
        }
        // BANアイテム
        if (api.IsBanItem(player, event.getBow())) {
            event.setCancelled();
            return;
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        String[] array = message.split(" ");
        if (array.length <= 0) return;
        String cmd = "";
        String arg1 = "";
        String arg2 = "";
        String arg3 = "";
        String arg4 = "";
        String arg5 = "";
        String arg6 = "";
        for (int i=0; i<=6; i++) {
            try {
                switch (i) {
                    case 0: cmd = array[i]; break;
                    case 1: arg1 = array[i]; break;
                    case 2: arg2 = array[i]; break;
                    case 3: arg3 = array[i]; break;
                    case 4: arg4 = array[i]; break;
                    case 5: arg5 = array[i]; break;
                    case 6: arg6 = array[i]; break;
                }
            } catch(Exception e) {
                break;
            }
        }

        Player player = event.getPlayer();
        api.getAfk().ResetAfk(player);

        if ((cmd.equals("/plugins")) ||
            (cmd.equals("/seed")) ||
            (cmd.equals("/help")) ||
            (cmd.equals("/stop")) ||
            (cmd.equals("/defaultgamemode")) ||
            (cmd.equals("/ban")) ||
            (cmd.equals("/ban-ip")) ||
            (cmd.equals("/banlist")) ||
            (cmd.equals("/pardon")) ||
            (cmd.equals("/pardon-ip")) ||
            (cmd.equals("/me")) ||
            (cmd.equals("/difficulty")) ||
            (cmd.equals("/kick")) ||
            (cmd.equals("/op")) ||
            (cmd.equals("/deop")) ||
            (cmd.equals("/whitelist")) ||
            (cmd.equals("/save-on")) ||
            (cmd.equals("/save-off")) ||
            (cmd.equals("/save-all")) ||
            (cmd.equals("/give")) ||
            (cmd.equals("/effect")) ||
            (cmd.equals("/enchant")) ||
            (cmd.equals("/particle")) ||
            (cmd.equals("/gamerule")) ||
            (cmd.equals("/gamemode")) ||
            (cmd.equals("/kill")) ||
            (cmd.equals("/spawnpoint")) ||
            (cmd.equals("/setworldspawn")) ||
            (cmd.equals("/tp")) ||
            (cmd.equals("/time")) ||
            (cmd.equals("/title")) ||
            (cmd.equals("/reload")) ||
            (cmd.equals("/weather")) ||
            (cmd.equals("/xp")) ||
            (cmd.equals("/status")) ||
            (cmd.equals("/timings"))) {
            if (!api.IsGameMaster(player)) {
                api.PlaySound(player, SoundTask.MODE_PLAYER, SoundTask.jin007, 0, false); // FAIL
                player.sendMessage(api.GetWarningMessage("onetwothree.rank_err"));
                event.setCancelled();
                return;
            }
        }
        api.getLogCmd().Write(player, cmd, arg1, arg2, arg3, arg4, arg5, arg6, player.getDisplayName());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        api.getAfk().ResetAfk(player);

        // 過度な処理を防ぐ
        if (api.tip_wait.containsKey(player)) {
            long tipwait = api.tip_wait.get(player);
            if (System.currentTimeMillis()-tipwait <= 1500) {
                return;
            }
        }

        ZoneInfo zi = api.IsInsideInfo(player.getLocation());
        if (zi == null) return;
        // 土地価格表示
        int minX = Math.min(zi.x1, zi.x2);
        int minZ = Math.min(zi.z1, zi.z2);
        int maxX = Math.max(zi.x1, zi.x2);
        int maxZ = Math.max(zi.z1, zi.z2);
        int price = (maxX - minX) * (maxZ - minZ);
        switch (zi.rank) {
            case "A": price = price * 1000; break;
            case "B": price = price * 750; break;
            case "C": price = price * 500; break;
            case "D": price = price * 250; break;
        }
        player.sendTip(TextFormat.LIGHT_PURPLE + "物件情報 - [ " + TextFormat.YELLOW + zi.rank + "ランク" + TextFormat.LIGHT_PURPLE + " ] - 価格: " + api.comma_format.format(price) + "p");
        api.tip_wait.put(player, System.currentTimeMillis());
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        api.getLogSign().Write(event.getPlayer(), event.getBlock(), lines);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = event.getPlayer();
        Inventory inv = event.getInventory();
        try {
            String lv,kind;
            int x,y,z,ret;
            ArrayList<DatabaseArgs> args = new ArrayList<DatabaseArgs>();
            if (inv instanceof ChestInventory) {
                lv = ((ChestInventory)inv).getHolder().getLevel().getName();
                x = ((ChestInventory)inv).getHolder().getFloorX();
                y = ((ChestInventory)inv).getHolder().getFloorY();
                z = ((ChestInventory)inv).getHolder().getFloorZ();
                args.add(new DatabaseArgs("c", lv));
                args.add(new DatabaseArgs("i", ""+x));
                args.add(new DatabaseArgs("i", ""+y));
                args.add(new DatabaseArgs("i", ""+z));
                ret = api.getDB().ExecuteUpdate(Language.translate("Sql0062"), args);
                kind = "ChestInventory";

            } else if (inv instanceof DoubleChestInventory) {
                lv = ((DoubleChestInventory)inv).getHolder().getLevel().getName();
                x = ((DoubleChestInventory)inv).getHolder().getFloorX();
                y = ((DoubleChestInventory)inv).getHolder().getFloorY();
                z = ((DoubleChestInventory)inv).getHolder().getFloorZ();
                args.add(new DatabaseArgs("c", lv));
                args.add(new DatabaseArgs("i", ""+x));
                args.add(new DatabaseArgs("i", ""+y));
                args.add(new DatabaseArgs("i", ""+z));
                ret = api.getDB().ExecuteUpdate(Language.translate("Sql0062"), args);
                kind = "DoubleChestInventory";

            } else if (inv instanceof ShulkerBoxInventory) {
                lv = ((ShulkerBoxInventory)inv).getHolder().getLevel().getName();
                x = ((ShulkerBoxInventory)inv).getHolder().getFloorX();
                y = ((ShulkerBoxInventory)inv).getHolder().getFloorY();
                z = ((ShulkerBoxInventory)inv).getHolder().getFloorZ();
                args.add(new DatabaseArgs("c", lv));
                args.add(new DatabaseArgs("i", ""+x));
                args.add(new DatabaseArgs("i", ""+y));
                args.add(new DatabaseArgs("i", ""+z));
                ret = api.getDB().ExecuteUpdate(Language.translate("Sql0062"), args);
                kind = "ShulkerBoxInventory";

            } else if (inv instanceof PlayerInventory) {
                lv = "NONE";
                x = 0;
                y = 0;
                z = 0;
                args.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
                ret = api.getDB().ExecuteUpdate(Language.translate("Sql0063"), args);
                kind = "PlayerInventory";
            } else {
                lv = "NONE";
                x = 0;
                y = 0;
                z = 0;
                kind = "NONE";
            }
            args.clear();
            args = null;

            ArrayList<DatabaseArgs> iargs = new ArrayList<DatabaseArgs>();

            int max = inv.getSize();
            for (int i=0; i<=max; i++) {
                Item item = inv.getItem(i);
                if (item.getId() != Item.AIR) {
                    iargs.add(new DatabaseArgs("c", lv));
                    iargs.add(new DatabaseArgs("i", ""+x));
                    iargs.add(new DatabaseArgs("i", ""+y));
                    iargs.add(new DatabaseArgs("i", ""+z));
                    iargs.add(new DatabaseArgs("c", kind));
                    iargs.add(new DatabaseArgs("c", player.getLoginChainData().getXUID()));
                    iargs.add(new DatabaseArgs("c", ""+i));
                    iargs.add(new DatabaseArgs("i", ""+item.getId()));
                    iargs.add(new DatabaseArgs("i", ""+item.getDamage()));
                    iargs.add(new DatabaseArgs("c", ""+item.getName()));
                    iargs.add(new DatabaseArgs("c", ""+item.getCustomName()));
                    iargs.add(new DatabaseArgs("i", ""+item.getCount()));
                    ret = api.getDB().ExecuteUpdate(Language.translate("Sql0064"), iargs);
                    iargs.clear();
                }
            }
            iargs = null;

        } catch (Exception e) {
            e.printStackTrace();
            api.getLogErr().Write(null, "onInventoryOpen : " + e.getStackTrace()[1].getMethodName(), e.getMessage() + " " + e.getStackTrace(), "");
        }
    }
}