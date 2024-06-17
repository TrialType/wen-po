package wen.WEntities.WBlocks;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ContinuousTurret;
import mindustry.world.blocks.defense.turrets.Turret;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class ExpendBlock extends Block {
    private final BulletType bullet = new BulletType() {{
        speed = 16;
        despawnEffect = shootEffect = Fx.none;
    }};
    public Effect lightEffect;
    public Effect centerEffect;
    public int max = 4;
    public float damageBoost = 0.3f;
    public float boost = 0.3f;
    public float range = 100;

    public ExpendBlock(String name) {
        super(name);
        update = true;
        configurable = true;

        config(Integer.class, (bu, i) -> {
            ExpendBuild e = (ExpendBuild) bu;
            Building b = world.build(i);
            if (e.turrets.indexOf(i) >= 0) {
                e.turrets.remove(e.turrets.indexOf(i));
            } else if (b instanceof Turret.TurretBuild && e.turrets.size < max) {
                e.turrets.add(i);
            }
        });
    }

    public class ExpendBuild extends Building {
        Seq<Integer> turrets = new Seq<>(max);
        Seq<Bullet> bullets = new Seq<>();
        float centerReload;
        float centerTimer = 0;
        float lightReload;
        float lightTimer = 0;

        @Override
        public void updateTile() {
            if (centerEffect != null) {
                centerReload = centerEffect.lifetime;
                centerTimer += Time.delta;
                if (centerTimer >= centerReload) {
                    centerEffect.at(this);
                    centerTimer = 0;
                }
            }
            if (lightEffect != null) {
                lightReload = lightEffect.lifetime;
                lightTimer += Time.delta;
            }

            turrets.removeAll(i -> world.build(i) == null || !world.build(i).isAdded() ||
                    !(world.build(i) instanceof Turret.TurretBuild));
            bullets.removeAll(b -> b == null || !b.isAdded());
            turrets.each(t -> {
                Building b = world.build(t);
                if (lightTimer >= lightReload) {
                    lightEffect.at(b);
                }
                b.health -= b.maxHealth * 0.005f * Time.delta;
                if (b.health < 0) {
                    b.dead = true;
                    b.kill();
                }
                if (!b.dead && b.block.canOverdrive) {
                    b.applyBoost((1 + boost), 2 * Time.delta);
                }
                Turret.TurretBuild tb = (Turret.TurretBuild) b;
                if (tb.wasShooting) {
                    BulletType bu = tb.peekAmmo();
                    bullet.pierce = bu.pierce;
                    bullet.pierceCap = bu.pierceCap;
                    bullet.pierceBuilding = bu.pierceBuilding;
                    bullet.pierceArmor = bu.pierceArmor;
                    bullet.reflectable = bu.reflectable;
                    bullet.hittable = bu.hittable;
                    bullet.absorbable = bu.absorbable;
                    if (b instanceof ContinuousTurret.ContinuousTurretBuild ct) {
                        bullet.lifetime = ct.lastLength / 16;
                        for (Turret.BulletEntry e : ct.bullets) {
                            if (e.bullet.type instanceof ContinuousBulletType c) {
                                bullet.damage /= c.damageInterval;
                            } else {
                                bullet.damage = e.bullet.damage;
                            }
                            bullet.create(tb, tb.x, tb.y, tb.rotation);
                        }
                    } else {
                        bullet.damage = damageBoost * bu.damage;
                        bullet.lifetime = bu.range / 16;
                        bullet.create(tb, tb.x, tb.y, tb.rotation);
                    }
                }
            });
            turrets.removeAll(t -> world.build(t) == null || world.build(t).dead);
            if (lightTimer >= lightReload) {
                lightTimer = 0;
            }
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (this.within(other, range) && other.team == this.team) {
                configure(other.pos());
                return false;
            }

            return true;
        }

        @Override
        public void drawConfigure() {
            Drawf.square(x, y, tile.block().size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f));
            Drawf.circles(x, y, range * tilesize);
            Draw.reset();
            turrets.each(i -> {
                var link = world.build(i);
                Drawf.square(link.x, link.y, link.block.size * tilesize / 2f + 1f, Pal.place);
            });
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(turrets.size);
            for (int i = 0; i < turrets.size; i++) {
                write.i(turrets.get(i));
            }
        }

        @Override
        public void read(Reads read, byte version) {
            super.read(read, version);
            int num = read.i();
            for (int i = 0; i < num; i++) {
                turrets.add(read.i());
            }
        }
    }
}