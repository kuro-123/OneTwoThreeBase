package host.kuro.onetwothree.task;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import host.kuro.onetwothree.Language;
import host.kuro.onetwothree.OneTwoThreeAPI;

import java.io.File;

public class WorldTask extends AsyncTask {

    private int WORLD_SEQ = 0;
    private String message = "";
    private Level level = null;
    private String level_name = "";
    private final OneTwoThreeAPI api;
    public int id = -1;

    public WorldTask(OneTwoThreeAPI api) {
        this.api = api;
    }

    @Override
    public void onRun() {
        WORLD_SEQ++;
        switch(WORLD_SEQ) {
            case 1:
                int rand_value = api.getRand().Next(1, 3);
                level = api.getServer().getLevelByName("nature" + rand_value);
                level_name = level.getName();
                message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.one");
                break;
            case 2:
                message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.two");
                level.save(true);
                break;
            case 3:
                message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.three");
                level.save(true);
                break;
            case 4:
                Level city = api.getServer().getLevelByName("city");
                if (city != null) {
                    for (Entity entity : level.getEntities()) {
                        if (entity instanceof Player) {
                            Player player = (Player)entity;
                            player.teleport(city.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + player.getDisplayName() + "> " + Language.translate("onetwothree.world.four");
                            api.getMessage().SendWarningMessage(message, true);
                        }
                    }
                }
                // ワールドUNLOAD
                message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.five");
                if (!api.getServer().unloadLevel(level)) {
                    message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.six");
                    WORLD_SEQ = 7;
                }
                break;
            case 5:
                message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.seven");
                try {
                    String path = api.getServer().getDataPath() + "worlds/" + level_name;
                    deleteDir(path);
                } catch (Exception e) {
                    api.getLogErr().Write(null, api.getMessage().GetErrorMessage(e));
                    message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.eight");
                    WORLD_SEQ = 7;
                }
                break;
            case 6:
                long seed = api.getRand().Next();
                Class<? extends Generator> generator = null;
                generator = Generator.getGenerator("normal");

                // ワールド生成
                message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.nine");
                if (!api.getServer().generateLevel(level_name, seed, generator)) {
                    message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.ten");
                    WORLD_SEQ = 7;
                }
                break;

            case 7:
                // ワールドLOAD
                message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.eleven");
                if (!api.getServer().loadLevel(level_name)) {
                    message = TextFormat.LIGHT_PURPLE + Language.translate("onetwothree.world.remake") + " <" + level_name + "> " + Language.translate("onetwothree.world.twelve");
                } else {
                    api.PlaySound(null, SoundTask.MODE_BROADCAST, SoundTask.jin010, 0, false);
                }

                // SPAWNPOINT CHECK
                level = api.getServer().getLevelByName(level_name);
                level.setSpawnLocation(level.getSafeSpawn());
                break;

            case 8:
                message = "";
                if (this.id >= 0) {
                    api.getServer().getScheduler().cancelTask(id);
                }
                api.world_task_id = -1;
                break;
        }
        if (message.length() <= 0) return;
        api.getMessage().SendWarningMessage(message, true);
        //api.getMessage().SendBroadcastActionBar(message, true);
    }

    private void deleteDir(String path) {
        File filePath = new File(path);
        String[] list = filePath.list();
        for(String file : list) {
            File f = new File(path + File.separator + file);
            if(f.isDirectory()) {
                deleteDir(path + File.separator + file);
            }else {
                f.delete();
            }
        }
        filePath.delete();
    }
}
