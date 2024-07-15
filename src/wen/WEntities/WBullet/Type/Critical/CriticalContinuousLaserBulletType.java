package wen.WEntities.WBullet.Type.Critical;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.ai.types.MissileAI;
import mindustry.content.StatusEffects;
import mindustry.entities.*;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.blocks.ControlBlock;
import wen.Tools.Damage2;
import wen.WEntities.WBullet.Bullet.CriticalBullet;
import wen.inter.Critical;

import static mindustry.Vars.*;

public class CriticalContinuousLaserBulletType extends ContinuousLaserBulletType implements Critical {
    public float criticalChance1 = 0.2f, criticalChance2 = 1, criticalChance3 = 1;
    public float critical1 = 1.2f, critical2 = 1, critical3 = 1;

    public void applyDamage(Bullet b) {
        Damage2.criticalCollideLine(b, b.team, hitEffect, b.x, b.y, b.rotation(), currentLength(b), largeHit, laserAbsorb, pierceCap);
    }

    @Override
    public void hit(Bullet b, float x, float y) {
        hitEffect.at(x, y, b.rotation(), hitColor);
        hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
        Effect.shake(hitShake, hitShake, b);
        if (fragOnHit) {
            createFrags(b, x, y);
        }
        createPuddles(b, x, y);
        createIncend(b, x, y);
        createUnits(b, x, y);
        if (suppressionRange > 0) {
            Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, new Vec2(b.x, b.y));
        }
        createSplashDamage(b, x, y);
        for (int i = 0; i < lightning; i++) {
            Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage * trueCritical(),
                    b.x, b.y, b.rotation() + Mathf.range(lightningCone / 2) + lightningAngle,
                    lightningLength + Mathf.random(lightningLengthRand));
        }
    }

    public void createSplashDamage(Bullet b, float x, float y) {
        if (splashDamageRadius > 0 && !b.absorbed) {
            Damage2.criticalDamage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier() * trueCritical(),
                    splashDamagePierce, collidesAir, collidesGround, scaledSplashDamage, b, this::trueCritical);
            if (status != StatusEffects.none) {
                Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
            }
            if (heals()) {
                indexer.eachBlock(b.team, x, y, splashDamageRadius, Building::damaged, other -> {
                    healEffect.at(other.x, other.y, 0f, healColor, other.block);
                    other.heal(healPercent / 100f * other.maxHealth() + healAmount * trueCritical());
                });
            }
            if (makeFire) {
                indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
            }
        }
    }

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
