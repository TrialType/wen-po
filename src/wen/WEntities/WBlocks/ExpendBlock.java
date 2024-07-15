package wen.WEntities.WBlocks;

import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.Effect;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.TimedKillUnit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Weapon;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ContinuousTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.Stat;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class ExpendBlock extends Block {
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

    @Override
    public void setStats() {
        super.setStats();

        stats.add(new Stat("maxLinks"), max);
        stats.add(new Stat("damageBoost"), damageBoost);
        stats.add(new Stat("boost"), boost);
        stats.add(new Stat("range"), range);
    }

    public class ExpendBuild extends Building {
        Seq<Integer> turrets = new Seq<>(max);
        float centerReload;
        float centerTimer = 0;
        float lightReload = 1;
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
            turrets.each(t -> {
                Building b = world.build(t);
                if (lightTimer >= lightReload) {
                    lightEffect.at(b);
                }
                b.health -= b.maxHealth * 0.005f * Time.delta;
                if (b.health < 0) {
                    b.dead = true;
                    b.kill();
                    return;
                }
                if (!b.dead && b.block.canOverdrive) {
                    b.applyBoost((1 + boost), 2 * Time.delta);
                }
                Turret.TurretBuild tb = (Turret.TurretBuild) b;
                ContinuousTurret.ContinuousTurretBuild ct;
                if (tb instanceof ContinuousTurret.ContinuousTurretBuild) {
                    ct = (ContinuousTurret.ContinuousTurretBuild) tb;
                } else {
                    ct = null;
                }
                Turret tu = (Turret) tb.block;
                if (isShooting(tu, tb)) {
                    int counter = tb.barrelCounter;
                    float X = tu.shootX;
                    float Y = tu.shootY;
                    ShootPattern s = tu.shoot;
                    BulletType bu = tb.peekAmmo().copy();
                    if (bu != null) {
                        if (bu instanceof ContinuousBulletType c) {
                            bu.damage /= c.damageInterval;
                            bu.lifetime = 1;
                            if (c instanceof ContinuousFlameBulletType f) {
                                f.lengthInterp = Interp.one;
                            } else if (c instanceof ContinuousLaserBulletType l) {
                                l.fadeTime = 0;
                            }
                        } else if (bu instanceof PointLaserBulletType) {
                            bu.lifetime = 1;
                        }
                        if (ct != null) {
                            if (bu instanceof ContinuousBulletType c) {
                                s.shoot(counter, (x, y, ro, delay, move) -> {
                                    Time.run(delay, () -> {
                                        Tmp.v1.trns(tb.rotation, Y + y);
                                        Tmp.v2.trns(tb.rotation - 90, X + x);
                                        Bullet blu = bu.create(tb, team, b.x + Tmp.v1.x + Tmp.v2.x,
                                                b.y + Tmp.v1.y + Tmp.v2.y, tb.rotation + ro +
                                                        Mathf.range(tu.inaccuracy + bu.inaccuracy),
                                                bu.damage * damageBoost / c.damageInterval,
                                                1, Mathf.clamp(Mathf.dst(b.x + Tmp.v1.x, b.y +
                                                                Tmp.v1.y, tb.targetPos.x, tb.targetPos.y) / bu.range,
                                                        tu.minRange / bu.range, tb.range() / bu.range), null);
                                        Tmp.v1.trns(tb.rotation + 90, ct.lastLength + b.y);
                                        blu.aimX = Tmp.v1.x;
                                        blu.aimY = Tmp.v1.y;
                                    });
                                }, () -> {
                                });
                            } else if (bu instanceof PointLaserBulletType) {
                                s.shoot(counter, (x, y, ro, delay, move) -> {
                                    Time.run(delay, () -> {
                                        Tmp.v1.trns(tb.rotation, Y + y);
                                        Tmp.v2.trns(tb.rotation - 90, X + x);
                                        Bullet blu = bu.create(tb, team,
                                                b.x + Tmp.v1.x + Tmp.v2.x, b.y + Tmp.v1.y + Tmp.v2.y,
                                                tb.rotation + ro + Mathf.range(tu.inaccuracy + bu.inaccuracy),
                                                bu.damage * damageBoost, 1, 1, null
                                        );
                                        blu.aimX = blu.x + Angles.trnsx(tb.rotation, ct.lastLength);
                                        blu.aimY = blu.y + Angles.trnsy(tb.rotation, ct.lastLength);
                                    });
                                }, () -> {
                                });
                            } else {
                                s.shoot(counter, (x, y, ro, delay, move) -> {
                                    Time.run(delay, () -> {
                                        Tmp.v1.trns(tb.rotation, Y + y);
                                        Tmp.v2.trns(tb.rotation - 90, X + x);
                                        bu.create(tb, team, b.x + Tmp.v1.x + Tmp.v2.x,
                                                b.y + Tmp.v1.y + Tmp.v2.y, tb.rotation +
                                                        ro + Mathf.range(tu.inaccuracy + bu.inaccuracy),
                                                bu.damage * damageBoost,
                                                1, 1, null
                                        );
                                    });
                                }, () -> {
                                });
                            }
                        } else if (bu instanceof PointBulletType || bu instanceof ArtilleryBulletType) {
                            s.shoot(counter, (x, y, ro, delay, move) -> {
                                Time.run(delay, () -> {
                                    Tmp.v1.trns(tb.rotation, Y + y);
                                    Tmp.v2.trns(tb.rotation - 90, X + x);
                                    bu.create(tb, team, b.x + Tmp.v1.x + Tmp.v2.x,
                                            b.y + Tmp.v1.y + Tmp.v2.y, tb.rotation + ro +
                                                    Mathf.range(tu.inaccuracy + bu.inaccuracy),
                                            bu.damage * damageBoost, 1,
                                            Mathf.clamp(Mathf.dst(b.x + Tmp.v1.x, b.y +
                                                            Tmp.v1.y, tb.targetPos.x, tb.targetPos.y) / bu.range,
                                                    tu.minRange / bu.range, tb.range() / bu.range),
                                            null
                                    );
                                });
                            }, () -> {
                            });
                        } else if (bu.spawnUnit != null && bu.spawnUnit.constructor.get() instanceof TimedKillUnit) {
                            s.shoot(counter, (x, y, ro, delay, move) -> {
                                Time.run(delay, () -> {
                                    Tmp.v1.trns(tb.rotation, Y + y);
                                    Tmp.v2.trns(tb.rotation - 90, X + x);
                                    if (bu.spawnUnit.weapons.any()) {
                                        for (Weapon w : bu.spawnUnit.weapons) {
                                            w.bullet.damage = w.bullet.damage * damageBoost;
                                        }
                                    }
                                    bu.init();
                                    bu.load();
                                    bu.create(tb, team, b.x + Tmp.v1.x + Tmp.v2.x,
                                            b.y + Tmp.v1.y + Tmp.v2.y, tb.rotation + ro +
                                                    Mathf.range(tu.inaccuracy + bu.inaccuracy),
                                            bu.damage * damageBoost,
                                            1, 1, null
                                    );
                                });
                            }, () -> {
                            });
                        } else {
                            s.shoot(counter, (x, y, ro, delay, move) -> {
                                Time.run(delay, () -> {
                                    Tmp.v1.trns(tb.rotation, Y + y);
                                    Tmp.v2.trns(tb.rotation - 90, X + x);
                                    bu.create(tb, team, b.x + Tmp.v1.x + Tmp.v2.x,
                                            b.y + Tmp.v1.y + Tmp.v2.y, tb.rotation + ro +
                                                    Mathf.range(tu.inaccuracy + bu.inaccuracy),
                                            bu.damage * damageBoost,
                                            1, 1, null
                                    );
                                });
                            }, () -> {
                            });
                        }
                    }
                }
            });
            turrets.removeAll(t -> world.build(t) == null || world.build(t).dead);
            if (lightTimer >= lightReload) {
                lightTimer = 0;
            }
        }

        public boolean isShooting(Turret t, Turret.TurretBuild build) {
            ContinuousTurret.ContinuousTurretBuild ct = null;
            if (build instanceof ContinuousTurret.ContinuousTurretBuild) {
                ct = (ContinuousTurret.ContinuousTurretBuild) build;
            }
            return (build.hasAmmo() && build.efficiency > 0 && (t.alwaysShooting ||

                    (((ct == null && ((t.coolant != null && t.coolant.efficiency(build) > 0 &&
                            build.reloadCounter <= t.coolant.amount * t.coolant.efficiency(build) * edelta() *
                                    (t.coolant instanceof ConsumeLiquidFilter filter ?
                                            filter.getConsumed(build).heatCapacity : 1f) * t.coolantMultiplier) ||
                            (!(t.coolant != null && t.coolant.efficiency(build) > 0) && build.reloadCounter <= 0))) ||
                            (ct != null && ct.canConsume())) &&

                            build.shootWarmup >= t.minWarmup && build.wasShooting))) ||

                    (build instanceof LaserTurret.LaserTurretBuild l && l.bullets.any());
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
                if (link != null) {
                    Drawf.square(link.x, link.y, link.block.size * tilesize / 2f + 1f, Pal.place);
                }
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