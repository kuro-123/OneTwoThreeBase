package host.kuro.onetwothree.utils;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.CriticalParticle;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.level.particle.EntityFlameParticle;
import cn.nukkit.level.particle.ExplodeParticle;
import cn.nukkit.level.particle.FlameParticle;
import cn.nukkit.level.particle.HappyVillagerParticle;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.level.particle.LavaParticle;
import cn.nukkit.level.particle.PortalParticle;
import cn.nukkit.level.particle.RedstoneParticle;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.level.particle.SpellParticle;
import cn.nukkit.level.particle.WaterDripParticle;
import cn.nukkit.math.Vector3;
import host.kuro.onetwothree.OneTwoThreeAPI;

public class Particle {

    private static int counter = 0;
    private static OneTwoThreeAPI api = null;

    public static final void SetAPI(OneTwoThreeAPI inapi) {
        api = inapi;
    }

    public static final void SpiralMoeagari(Entity ent) {
        if (ent == null) return;
        Level level = ent.getLevel();
        double x = ent.getX();
        double y = ent.getY();
        double z = ent.getZ();
        Vector3 center = new Vector3(x, y, z);
        EntityFlameParticle particle = new EntityFlameParticle(center);
        double yaw = 0;
        y = center.y;
        for(;y < (center.y + 4);){
            x = (-Math.sin(yaw) + center.x);
            z = (Math.cos(yaw) + center.z);
            particle.setComponents(x, y, z);
            level.addParticle(particle);
            yaw += ((Math.PI * 2) / 20);
            y += 0.05;
        }
        center = null;
        particle = null;
    }

    public static final void CloudRain(Entity player) {
        if (player == null) return;
        Level level = player.getLevel();
        Location loc = player.getLocation();
        double time = 1;
        double pi = 3.14159;
        time = time+0.1/pi;

        ExplodeParticle explode = null;
        WaterDripParticle waterdrip = null;

        for(double i=0; i<=(2*pi); i+=(pi/8)) {
            double x = time*Math.cos(i);
            double y = Math.exp(-0.1*time)*Math.sin(time)+1.5;
            double z = time*Math.sin(i);

            explode = new ExplodeParticle(loc.add(x, y, z));
            level.addParticle(explode);
            explode = null;

            waterdrip = new WaterDripParticle(loc.add(x, y, z));
            level.addParticle(waterdrip);
            waterdrip = null;
        }
    }

    public static final void Heal(Entity player) {
        if (player == null) return;
        Level level = player.getLevel();
        Location loc = player.getLocation();
        double time = 1;
        double pi = 3.14159;
        time = time+0.1/pi;
        double max = (2*pi);
        double plus = (pi/8);
        HeartParticle buff = null;
        for(double i=0; i<=max; i+=plus) {
            double x = time*Math.cos(i);
            double y = Math.exp(-0.1*time)*Math.sin(time)+0.7;
            double z = time*Math.sin(i);
            buff = new HeartParticle(loc.add(x, y, z));
            level.addParticle(buff);
            buff = null;
        }
    }

    public static final void Crown(Entity player) {
        if (player == null) return;
        Level level = player.getLevel();
        Location loc = player.getLocation();
        double time = 1;
        double pi = 3.14159;
        time = time+0.1/pi;
        FlameParticle buff = null;
        for(double i=0; i<=(2*pi); i+=(pi/8)) {
            double x = time*Math.cos(i);
            double y = Math.exp(-0.1*time)*Math.sin(time)+1.5;
            double z = time*Math.sin(i);
            buff = new FlameParticle(loc.add(x, y, z));
            level.addParticle(buff);
            buff = null;
        }
    }

    public static final void DoubleHelix(Entity player) {
        if (player == null) return;
        Level level = player.getLevel();
        Location loc = player.getLocation();
        double radio  = 5;
        FlameParticle buff = null;
        for(double i=5; i>0; i-=0.1){
            radio = i/3;
            double x = radio*Math.cos(3*i);
            double y = 5-i;
            double z = radio*Math.sin(3*i);
            buff = new FlameParticle(loc.add(x, y, z));
            level.addParticle(buff);
            buff = null;
        }
        for(double i=5; i>0; i-=0.1){
            radio = i/3;
            double x = radio*Math.cos(3*i);
            double y = 5-i;
            double z = radio*Math.sin(3*i);
            buff = new FlameParticle(loc.add(x, y, z));
            level.addParticle(buff);
            buff = null;
        }
    }

    public static final void Dring(Entity player) {
        if (player == null) return;
        Level level = player.getLevel();
        Location loc = player.getLocation();
        double time = 1;
        double pi = 3.14159;
        time = time+0.1/pi;
        HappyVillagerParticle buff = null;
        for(double i=0; i<=50; i+=(pi/16)) {
            double radio = 1.5;
            double x = radio*Math.cos(i)*Math.sin(time);
            double y = radio*Math.cos(time)+1.5;
            double z = radio*Math.sin(i)*Math.sin(time);
            buff = new HappyVillagerParticle(loc.add(x, y, z));
            level.addParticle(buff);
            buff = null;
        }
    }

    public static final void Edita(Player player, Level level, Vector3 loc, int cr, int cg, int cb) {
        if (player == null) return;
        if (player.isSpectator()) return;
        double pi = 3.14159;
        double phi = 0;
        phi += pi/15;
        double radio;
        double x;
        double y;
        double z;
        double max = 2 * pi;
        double plus = pi/40;
        DustParticle buff = null;
        for(double i=0; i<max; i+=plus) {
            for(double a=1; a<10; ++a) {
                radio = 1+a;
                if(radio < 5){
                    x = radio*Math.cos(i)*Math.sin(phi);
                    y = radio*Math.cos(phi)+1.5;
                    z = radio*Math.sin(i)*Math.sin(phi);
                    //level.addParticle(new DustParticle(loc.add(x, y, z), cr+Utility.rand(0, 20),cg+Utility.rand(0, 20),cb+Utility.rand(0, 20)));
                    buff = new DustParticle(loc.add(x, y, z), cr, cg, cb);
                    level.addParticle(buff);
                    buff = null;
                } else {
                    for(double b=1; b<5; ++b) {
                        radio = 6-b;
                        if(radio > 1){
                            x = radio*Math.cos(i)*Math.sin(phi);
                            y = radio*Math.cos(phi)+1.5;
                            z = radio*Math.sin(i)*Math.sin(phi);
                            buff = new DustParticle(loc.add(x, y, z), cr, cg, cb);
                            level.addParticle(buff);
                            buff = null;
                        } else {
                            for(double c = 1; c<5; ++c) {
                                radio = 1+c;
                                if (radio < 5) {
                                    x = radio*Math.cos(i)*Math.sin(phi);
                                    y = radio*Math.cos(phi)+1.5;
                                    z = radio*Math.sin(i)*Math.sin(phi);
                                    buff = new DustParticle(loc.add(x, y, z), cr, cg, cb);
                                    level.addParticle(buff);
                                    buff = null;
                                } else {
                                    for(double d=1; d<5; ++d) {
                                        x = radio*Math.cos(i)*Math.sin(phi);
                                        y = radio*Math.cos(phi)+1.5;
                                        z = radio*Math.sin(i)*Math.sin(phi);
                                        buff = new DustParticle(loc.add(x, y, z), cr, cg, cb);
                                        level.addParticle(buff);
                                        buff = null;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static final void Laser(Entity player) {
        if (player == null) return;
        Level level = player.getLevel();
        Vector3 direction = player.getDirectionVector();
        for(double i=0; i<40; ++i) {
            double x = i*direction.x+player.x;
            double y = i*direction.y+player.y;
            double z = i*direction.z+player.z;
            level.addParticle(new FlameParticle(new Vector3(x, y+1, z)));
        }
    }

    public static final void Party(Entity player) {
        if (player == null) return;
        Level level = player.getLevel();
        Location loc = player.getLocation();
        double pi = 3.14159;
        for(double i=0; i<=16; i+=pi/22){
            double radio = Math.sin(i);
            double y = Math.cos(i);
            for(double a=0; a<pi*4; a+=pi/8){
                double x = Math.cos(a)*radio;
                double z = Math.sin(a)*radio;
                level.addParticle(new DustParticle(loc.add(x, i, z), api.getRand().Next(1, 255), api.getRand().Next(1, 255), api.getRand().Next(1, 255)));
            }
        }
    }

    public static final void Spiral(Player player, int r, int g, int b, boolean sound) {
        if (player == null) return;
        Level level = player.getLevel();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        Vector3 center = new Vector3(x, y, z);
        Vector3 sound_center = center;
        DustParticle particle = new DustParticle(center, r, g, b, 1);
        double yaw = 0;
        y = center.y;
        for(;y < (center.y + 4);){
            x = (-Math.sin(yaw) + center.x);
            z = (Math.cos(yaw) + center.z);
            particle.setComponents(x, y, z);
            level.addParticle(particle);
            yaw += ((Math.PI * 2) / 20);
            y += 0.05;
        }
        center = null;
        particle = null;
        if (sound) {
            level.addSound(sound_center.add(0.5, 0.5, 0.5), Sound.MOB_ENDERMEN_PORTAL);
        }
    }

    public static final void SpiralLargeExplode(Player player) {
        if (player == null) return;
        Level level = player.getLevel();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        Vector3 center = new Vector3(x, y, z);
        Vector3 sound_center = center;
        ExplodeParticle particle = new ExplodeParticle(center);
        double yaw = 0;
        y = center.y;
        for(;y < (center.y + 4);){
            x = (-Math.sin(yaw) + center.x);
            z = (Math.cos(yaw) + center.z);
            particle.setComponents(x, y, z);
            level.addParticle(particle);
            yaw += ((Math.PI * 2) / 20);
            y += 0.05;
        }
        center = null;
        particle = null;
        level.addSound(sound_center.add(0.5, 0.5, 0.5), Sound.MOB_ENDERMEN_PORTAL);
    }

    public static final void SpiralRedCross(Player player) {
        if (player == null) return;
        Level level = player.getLevel();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        Vector3 center = new Vector3(x, y, z);
        Vector3 sound_center = center;
        CriticalParticle particle = new CriticalParticle(center);
        double yaw = 0;
        y = center.y;
        for(;y < (center.y + 4);){
            x = (-Math.sin(yaw) + center.x);
            z = (Math.cos(yaw) + center.z);
            particle.setComponents(x, y, z);
            level.addParticle(particle);
            yaw += ((Math.PI * 2) / 20);
            y += 0.05;
        }
        center = null;
        particle = null;
        level.addSound(sound_center.add(0.5, 0.5, 0.5), Sound.MOB_ENDERMEN_PORTAL);
    }

    public static final void SpiralFlame(Player player) {
        if (player == null) return;
        Level level = player.getLevel();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        Vector3 center = new Vector3(x, y, z);
        Vector3 sound_center = center;
        LavaParticle particle = new LavaParticle(center);
        double yaw = 0;
        y = center.y;
        for(;y < (center.y + 4);){
            x = (-Math.sin(yaw) + center.x);
            z = (Math.cos(yaw) + center.z);
            particle.setComponents(x, y, z);
            level.addParticle(particle);
            yaw += ((Math.PI * 2) / 20);
            y += 0.05;
        }
        center = null;
        particle = null;
        level.addSound(sound_center.add(0.5, 0.5, 0.5), Sound.RANDOM_EXPLODE);
    }

    public static final void SpiralFlame(Entity entity) {
        if (entity == null) return;
        Level level = entity.getLevel();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        Vector3 center = new Vector3(x, y, z);
        Vector3 sound_center = center;
        LavaParticle particle = new LavaParticle(center);
        double yaw = 0;
        y = center.y;
        for(;y < (center.y + 4);){
            x = (-Math.sin(yaw) + center.x);
            z = (Math.cos(yaw) + center.z);
            particle.setComponents(x, y, z);
            level.addParticle(particle);
            yaw += ((Math.PI * 2) / 20);
            y += 0.05;
        }
        center = null;
        particle = null;

        level.addSound(sound_center.add(0.5, 0.5, 0.5), Sound.RANDOM_EXPLODE);
        counter++;
    }

    public static final void SpiralFlame(Player player, Level level, double inx, double iny, double inz) {
        if (player == null) return;
        double x = inx;
        double y = iny;
        double z = inz;
        Vector3 center = new Vector3(x, y, z);
        Vector3 sound_center = center;
        LavaParticle particle = new LavaParticle(center);
        double yaw = 0;
        y = center.y;
        for(;y < (center.y + 4);){
            x = (-Math.sin(yaw) + center.x);
            z = (Math.cos(yaw) + center.z);
            particle.setComponents(x, y, z);
            level.addParticle(particle);
            yaw += ((Math.PI * 2) / 20);
            y += 0.05;
        }
        center = null;
        particle = null;
        level.addSound(sound_center.add(0.5, 0.5, 0.5), Sound.RANDOM_EXPLODE);
    }

    public static final void SpiralFlame(Entity entity, Level level, double inx, double iny, double inz) {
        if (entity == null) return;
        double x = inx;
        double y = iny;
        double z = inz;
        Vector3 center = new Vector3(x, y, z);
        Vector3 sound_center = center;
        LavaParticle particle = new LavaParticle(center);
        double yaw = 0;
        y = center.y;
        for(;y < (center.y + 4);){
            x = (-Math.sin(yaw) + center.x);
            z = (Math.cos(yaw) + center.z);
            particle.setComponents(x, y, z);
            level.addParticle(particle);
            yaw += ((Math.PI * 2) / 20);
            y += 0.05;
        }
        center = null;
        particle = null;
        level.addSound(sound_center.add(0.5, 0.5, 0.5), Sound.RANDOM_EXPLODE);
    }

    private static final void SmokeRedStone(Location loc) {
        double x = 0;
        double y = 0;
        double z = 0;

        SmokeParticle particle_s = null;
        particle_s = new SmokeParticle(loc.getDirectionVector());
        RedstoneParticle particle_r = null;
        particle_r = new RedstoneParticle(loc.getDirectionVector());

        x = loc.getX() + 0.6; z = loc.getZ() + 0.6; y = loc.getY() + 0.8;
        particle_s.setComponents(x, y, z); loc.getLevel().addParticle(particle_s);
        x = loc.getX() - 0.6; z = loc.getZ() - 0.6; y = loc.getY() + 0.8;
        particle_s.setComponents(x, y, z); loc.getLevel().addParticle(particle_s);
        x = loc.getX() + 0.6; z = loc.getZ() - 0.6; y = loc.getY() + 0.8;
        particle_s.setComponents(x, y, z); loc.getLevel().addParticle(particle_s);
        x = loc.getX() - 0.6; z = loc.getZ() + 0.6; y = loc.getY() + 0.8;
        particle_s.setComponents(x, y, z); loc.getLevel().addParticle(particle_s);

        x = loc.getX() + 0.5; z = loc.getZ() + 0.5; y = loc.getY() + 1.0;
        particle_r.setComponents(x, y, z); loc.getLevel().addParticle(particle_r);
        x = loc.getX() - 0.5; z = loc.getZ() - 0.5; y = loc.getY() + 1.0;
        particle_r.setComponents(x, y, z); loc.getLevel().addParticle(particle_r);
        x = loc.getX() + 0.5; z = loc.getZ() - 0.5; y = loc.getY() + 1.0;
        particle_r.setComponents(x, y, z); loc.getLevel().addParticle(particle_r);
        x = loc.getX() - 0.5; z = loc.getZ() + 0.5; y = loc.getY() + 1.0;
        particle_r.setComponents(x, y, z); loc.getLevel().addParticle(particle_r);

        x = loc.getX() + 0.4; z = loc.getZ() + 0.4; y = loc.getY() + 1.2;
        particle_s.setComponents(x, y, z); loc.getLevel().addParticle(particle_s);
        x = loc.getX() - 0.4; z = loc.getZ() - 0.4; y = loc.getY() + 1.2;
        particle_s.setComponents(x, y, z); loc.getLevel().addParticle(particle_s);
        x = loc.getX() + 0.4; z = loc.getZ() - 0.4; y = loc.getY() + 1.2;
        particle_s.setComponents(x, y, z); loc.getLevel().addParticle(particle_s);
        x = loc.getX() - 0.4; z = loc.getZ() + 0.4; y = loc.getY() + 1.2;
        particle_s.setComponents(x, y, z); loc.getLevel().addParticle(particle_s);

        particle_s = null;
        particle_r = null;
    }

    private static final void PortalSplash(Location loc, int no) {
        double x = 0;
        double y = 0;
        double z = 0;

        PortalParticle particle_rs = null;
        particle_rs = new PortalParticle(loc.getDirectionVector());

        double add_x = 0;
        double add_y = 0;
        double add_z = 0;

        int rand = 0;
        for (int i=0; i<no; i++) {
            rand = api.getRand().Next(0, 1);
            if (rand == 0) {
                add_x = (api.getRand().Next(1, 9) / 10.0f);
            } else {
                add_x = 0 - (api.getRand().Next(1, 9) / 10.0f);
            }

            add_y = 1.0 + (api.getRand().Next(1, 9) / 10.0f);

            rand = api.getRand().Next(0, 1);
            if (rand == 0) {
                add_z = (api.getRand().Next(1, 9) / 10.0f);
            } else {
                add_z = 0 - (api.getRand().Next(1, 9) / 10.0f);
            }

            x = loc.getX() + add_x; z = loc.getFloorZ() + add_z; y = loc.getFloorY() + add_y;
            particle_rs.setComponents(x, y, z); loc.getLevel().addParticle(particle_rs);
        }
        particle_rs = null;
    }

    private static final void FlameSplash(Location loc, int no) {
        double x = 0;
        double y = 0;
        double z = 0;

        FlameParticle particle_rs = null;
        particle_rs = new FlameParticle(loc.getDirectionVector());

        double add_x = 0;
        double add_y = 0;
        double add_z = 0;

        int rand = 0;
        for (int i=0; i<no; i++) {
            rand = api.getRand().Next(0, 1);
            if (rand == 0) {
                add_x = (api.getRand().Next(1, 9) / 10.0f);
            } else {
                add_x = 0 - (api.getRand().Next(1, 9) / 10.0f);
            }

            add_y = 0.8 + (api.getRand().Next(1, 9) / 10.0f) + (api.getRand().Next(1, 9) / 10.0f);

            rand = api.getRand().Next(0, 1);
            if (rand == 0) {
                add_z = (api.getRand().Next(1, 9) / 10.0f);
            } else {
                add_z = 0 - (api.getRand().Next(1, 9) / 10.0f);
            }

            x = loc.getX() + add_x; z = loc.getFloorZ() + add_z; y = loc.getFloorY() + add_y;
            particle_rs.setComponents(x, y, z); loc.getLevel().addParticle(particle_rs);
        }
        particle_rs = null;
    }

    private static final void ColorDust(Location loc, int no) {
        double x = 0;
        double y = 0;
        double z = 0;

        DustParticle particle_rs = null;

        double add_x = 0;
        double add_y = 0;
        double add_z = 0;

        int rand = 0;
        for (int i=0; i<20; i++) {
            rand = api.getRand().Next(0, 1);
            if (rand == 0) {
                add_x = (api.getRand().Next(1, 9) / 10.0f);
            } else {
                add_x = 0 - (api.getRand().Next(1, 9) / 10.0f);
            }

            add_y = 0.8 + (api.getRand().Next(1, 9) / 10.0f);

            rand = api.getRand().Next(0, 1);
            if (rand == 0) {
                add_z = (api.getRand().Next(1, 9) / 10.0f);
            } else {
                add_z = 0 - (api.getRand().Next(1, 9) / 10.0f);
            }

            particle_rs = new DustParticle(loc.getDirectionVector(), api.getRand().Next(0, 255),api.getRand().Next(0, 255),api.getRand().Next(0, 255));
            x = loc.getX() + add_x; z = loc.getFloorZ() + add_z; y = loc.getFloorY() + add_y;
            particle_rs.setComponents(x, y, z); loc.getLevel().addParticle(particle_rs);
            particle_rs = null;
        }
    }

    private static final void BlackDust(Location loc, int no) {
        double x = 0;
        double y = 0;
        double z = 0;

        DustParticle particle_rs = null;

        double add_x = 0;
        double add_y = 0;
        double add_z = 0;

        int rand = 0;
        for (int i=0; i<20; i++) {
            rand = api.getRand().Next(0, 1);
            if (rand == 0) {
                add_x = (api.getRand().Next(1, 9) / 10.0f);
            } else {
                add_x = 0 - (api.getRand().Next(1, 9) / 10.0f);
            }

            add_y = 0.8 + (api.getRand().Next(1, 9) / 10.0f);

            rand = api.getRand().Next(0, 1);
            if (rand == 0) {
                add_z = (api.getRand().Next(1, 9) / 10.0f);
            } else {
                add_z = 0 - (api.getRand().Next(1, 9) / 10.0f);
            }

            particle_rs = new DustParticle(loc.getDirectionVector(), 0, 0, 0);
            x = loc.getX() + add_x; z = loc.getFloorZ() + add_z; y = loc.getFloorY() + add_y;
            particle_rs.setComponents(x, y, z); loc.getLevel().addParticle(particle_rs);
            particle_rs = null;
        }
    }

    public static final void BlackSpellDust(Location loc) {
        double x = 0;
        double y = 0;
        double z = 0;

        SpellParticle particle_rs = null;

        double add_x = 0;
        double add_y = 0;
        double add_z = 0;

        int rand = api.getRand().Next(0, 1);
        if (rand == 0) {
            add_x = (api.getRand().Next(1, 9) / 10.0f);
        } else {
            add_x = 0 - (api.getRand().Next(1, 9) / 10.0f);
        }

        add_y = 0.8 + (api.getRand().Next(1, 9) / 10.0f);

        rand = api.getRand().Next(0, 1);
        if (rand == 0) {
            add_z = (api.getRand().Next(1, 9) / 10.0f);
        } else {
            add_z = 0 - (api.getRand().Next(1, 9) / 10.0f);
        }

        particle_rs = new SpellParticle(loc.getDirectionVector(), 0, 0, 0);
        x = loc.getX() + add_x; z = loc.getFloorZ() + add_z; y = loc.getFloorY() + add_y;
        particle_rs.setComponents(x, y, z); loc.getLevel().addParticle(particle_rs);
        particle_rs = null;
    }

    private static final void ColorSpellDust(Location loc) {
        double x = 0;
        double y = 0;
        double z = 0;

        SpellParticle particle_rs = null;

        double add_x = 0;
        double add_y = 0;
        double add_z = 0;

        int rand = api.getRand().Next(0, 1);
        if (rand == 0) {
            add_x = (api.getRand().Next(1, 9) / 10.0f);
        } else {
            add_x = 0 - (api.getRand().Next(1, 9) / 10.0f);
        }

        add_y = 0.8 + (api.getRand().Next(1, 9) / 10.0f);

        rand = api.getRand().Next(0, 1);
        if (rand == 0) {
            add_z = (api.getRand().Next(1, 9) / 10.0f);
        } else {
            add_z = 0 - (api.getRand().Next(1, 9) / 10.0f);
        }

        particle_rs = new SpellParticle(loc.getDirectionVector(), api.getRand().Next(0, 255),api.getRand().Next(0, 255),api.getRand().Next(0, 255));
        x = loc.getX() + add_x; z = loc.getFloorZ() + add_z; y = loc.getFloorY() + add_y;
        particle_rs.setComponents(x, y, z); loc.getLevel().addParticle(particle_rs);
        particle_rs = null;
    }

    private static final void EpicMode010(Location loc) {
        SmokeRedStone(loc);
    }

    private static final void EpicMode020(Location loc) {
        PortalSplash(loc, 20);
    }

    private static final void EpicMode030(Location loc) {
        ColorDust(loc, 20);
    }

    private static final void EpicMode040(Location loc) {
        PortalSplash(loc, 20);
    }

    private static final void EpicMode050(Location loc) {
        SmokeRedStone(loc);
        FlameSplash(loc, 20);
    }

    private static final void EpicMode060(Location loc) {
        SmokeRedStone(loc);
        PortalSplash(loc, 40);
    }

    private static final void EpicMode070(Location loc) {
        SmokeRedStone(loc);
        PortalSplash(loc, 80);
    }

    private static final void EpicMode080(Location loc) {
        SmokeRedStone(loc);
        PortalSplash(loc, 80);
        ColorDust(loc, 50);
    }

    private static final void EpicMode090(Location loc) {
        SmokeRedStone(loc);
        PortalSplash(loc, 80);
        ColorDust(loc, 80);
        FlameSplash(loc, 20);
    }

    private static final void EpicMode100(Location loc) {
        SmokeRedStone(loc);
        PortalSplash(loc, 60);
        BlackDust(loc, 60);
        BlackSpellDust(loc);
        FlameSplash(loc, 20);
    }

    private static final void EpicMode200(Location loc) {
        SmokeRedStone(loc);
        PortalSplash(loc, 60);
        ColorDust(loc, 60);
        ColorSpellDust(loc);
        FlameSplash(loc, 20);
    }

    public static final void EpicMode(Player player, int no) {
        switch (no) {
            case  10: EpicMode010(player.getLocation()); break;
            case  20: EpicMode020(player.getLocation()); break;
            case  30: EpicMode030(player.getLocation()); break;
            case  40: EpicMode040(player.getLocation()); break;
            case  50: EpicMode050(player.getLocation()); break;
            case  60: EpicMode060(player.getLocation()); break;
            case  70: EpicMode070(player.getLocation()); break;
            case  80: EpicMode080(player.getLocation()); break;
            case  90: EpicMode090(player.getLocation()); break;
            case 100: EpicMode100(player.getLocation()); break;
            case 200: EpicMode200(player.getLocation()); break;
        }
    }
    public static final void EpicMode(Entity player, int no) {
        switch (no) {
            case  10: EpicMode010(player.getLocation()); break;
            case  20: EpicMode020(player.getLocation()); break;
            case  30: EpicMode030(player.getLocation()); break;
            case  40: EpicMode040(player.getLocation()); break;
            case  50: EpicMode050(player.getLocation()); break;
            case  60: EpicMode060(player.getLocation()); break;
            case  70: EpicMode070(player.getLocation()); break;
            case  80: EpicMode080(player.getLocation()); break;
            case  90: EpicMode090(player.getLocation()); break;
            case 100: EpicMode100(player.getLocation()); break;
            case 200: EpicMode200(player.getLocation()); break;
        }
    }
}
