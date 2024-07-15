package wen.WEntities.WBullet.Type.Critical;

import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.ai.types.MissileAI;
import mindustry.entities.*;
import mindustry.entities.bullet.LiquidBulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.blocks.ControlBlock;
import wen.WEntities.WBullet.Bullet.CriticalBullet;
import wen.inter.Critical;

import static mindustry.Vars.*;

public class CriticalLiquidBulletType extends LiquidBulletType implements Critical {
    public float criticalChance1 = 0.2f, criticalChance2 = 0.2f, criticalChance3 = 0.2f;
    public float critical1 = 1.2f, critical2 = 0.2f, critical3 = 0.2f;

    public @Nullable Bullet create(@Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY) {
        if (!Mathf.chance(createChance)) return null;
        if (ignoreSpawnAngle) angle = 0;
        if (spawnUnit != null) {
            if (!net.client()) {
                Unit spawned = spawnUnit.create(team);
                spawned.set(x, y);
                spawned.rotation = angle;
                if (spawnUnit.missileAccelTime <= 0f) {
                    spawned.vel.trns(angle, spawnUnit.speed);
                }
                if (spawned.controller() instanceof MissileAI ai) {
                    if (shooter instanceof Unit unit) {
                        ai.shooter = unit;
                    }
                    if (shooter instanceof ControlBlock control) {
                        ai.shooter = control.unit();
                    }
                }
                spawned.add();
            }
            if (killShooter && owner instanceof Healthc h && !h.dead()) h.kill();
            return null;
        }
        CriticalBullet bullet = CriticalBullet.create();
        bullet.type = this;
        bullet.owner = owner;
        bullet.team = team;
        bullet.time = 0f;
        bullet.originX = x;
        bullet.originY = y;
        if (!(aimX == -1f && aimY == -1f)) {
            bullet.aimTile = world.tileWorld(aimX, aimY);
        }
        bullet.aimX = aimX;
        bullet.aimY = aimY;
        bullet.initVel(angle, speed * velocityScl);
        if (backMove) {
            bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
        } else {
            bullet.set(x, y);
        }
        bullet.lifetime = lifetime * lifetimeScl;
        bullet.data = data;
        bullet.drag = drag;
        bullet.hitSize = hitSize;
        bullet.mover = mover;
        bullet.damage = (damage < 0 ? this.damage : damage) * bullet.damageMultiplier();
        if (bullet.trail != null) {
            bullet.trail.clear();
        }
        bullet.add();
        if (keepVelocity && owner instanceof Velc v) bullet.vel.add(v.vel());
        return bullet;
    }

    @Override
    public float critical1() {
        return critical1;
    }

    @Override
    public float criticalChance1() {
        return criticalChance1;
    }

    @Override
    public float critical2() {
        return critical2;
    }

    @Override
    public float criticalChance2() {
        return criticalChance2;
    }

    @Override
    public float critical3() {
        return critical3;
    }

    @Override
    public float criticalChance3() {
        return criticalChance3;
    }
}
