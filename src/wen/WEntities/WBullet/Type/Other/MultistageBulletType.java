package wen.WEntities.WBullet.Type.Other;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.ai.types.MissileAI;
import mindustry.content.StatusEffects;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.blocks.ControlBlock;
import wen.WContents.WStatusEffects;
import wen.WEntities.WBullet.Bullet.MultistageBullet;

import java.util.Arrays;

import static mindustry.Vars.net;
import static mindustry.Vars.world;

public class MultistageBulletType extends BasicBulletType {
    public boolean onlyBoss = true;

    public Color[] frontColors = {Color.gray, Color.blue, Color.red, Color.yellow};
    public float[] frontA = {0.2f, 0.6f, 1f, 1.6f};
    public float[] frontB = {0.1f, 0.3f, 0.5f, 0.8f};
    public float[] frontX = {0, 0, 0, 0};
    public float[] frontY = {23, 34, 45, 46};
    public float[] frontStroke = {1, 1, 1, 1};
    public float[] frontRotation = {0, 0, 0, 0};
    public float[] frontBreak = {120, 120, 120, 120};
    public float[] frontDamage = {0.25f, 0.5f, 0.75f, 1f};
    public float[] frontLength = {200, 200, 200, 200};
    public float[] frontSpeed = {1.5f, 1.5f, 1.5f, 1.5f};

    public Color[] backColors = {Color.gray, Color.blue, Color.red, Color.yellow};
    public float[] backA = {0.12f, 0.18f, 0.22f, 0.26f};
    public float[] backB = {0.03f, 0.05f, 0.1f, 0.16f};
    public float[] backX = {0, 0, 0, 0};
    public float[] backY = {-35, -46, -57, -68};
    public float[] backStroke = {1, 1, 1, 1};
    public float[] backRotation = {0, 0, 0, 0};
    public float[] backBreak = {60, 15, 15, 15};
    public float[] backDamage = {3, 3, 3, 3};
    public float[] backSpeed = {2, 2, 2, 2};
    public float[] backKnock = {4, 4, 4, 4};

    public MultistageBulletType() {
        super();

        collidesTiles = false;
        pierce = true;
        pierceCap = 2;
    }

    @Override
    public void draw(Bullet b) {
        super.draw(b);

        if (b instanceof MultistageBullet m && (backKnock.length > 0 || frontSpeed.length > 0)) {
            if (m.hitTimer >= 0) {
                if (!m.move && m.damageTimer < frontBreak[m.index]) {
                    int i = m.index;
                    float x = frontX[i], y = frontY[i];
                    float el = Mathf.sqrt(x * x + y * y), eo = Angles.angle(x, y) - 90;
                    Draw.color(frontColors[i]);
                    Lines.stroke(frontStroke[i]);
                    Lines.ellipse(b.x + Angles.trnsx(b.rotation() + eo, el),
                            b.y + Angles.trnsy(b.rotation() + eo, el), 36,
                            frontB[i], frontA[i], frontRotation[i] + b.rotation());
                }
                for (int i = m.index + 1; i < frontColors.length; i++) {
                    float x = frontX[i], y = frontY[i];
                    float el = Mathf.sqrt(x * x + y * y), eo = Angles.angle(x, y) - 90;
                    Draw.color(frontColors[i]);
                    Lines.stroke(frontStroke[i]);
                    Lines.ellipse(b.x + Angles.trnsx(b.rotation() + eo, el),
                            b.y + Angles.trnsy(b.rotation() + eo, el), 36,
                            frontB[i], frontA[i], frontRotation[i] + b.rotation());
                }
            } else {
                for (int i = 0; i < frontColors.length; i++) {
                    float x = frontX[i], y = frontY[i];
                    float el = Mathf.sqrt(x * x + y * y), eo = Angles.angle(x, y) - 90;
                    Draw.color(frontColors[i]);
                    Lines.stroke(frontStroke[i]);
                    Lines.ellipse(b.x + Angles.trnsx(b.rotation() + eo, el),
                            b.y + Angles.trnsy(b.rotation() + eo, el), 36,
                            frontB[i], frontA[i], frontRotation[i] + b.rotation());
                }
                float time = 0;
                for (int i = 0; i < backColors.length; i++) {
                    time += backBreak[i];
                    if (b.time < time) {
                        float x = backX[i], y = backY[i];
                        float el = Mathf.sqrt(x * x + y * y), eo = Angles.angle(x, y) - 90;
                        Draw.color(backColors[i]);
                        Lines.stroke(backStroke[i]);
                        Lines.ellipse(b.x + Angles.trnsx(b.rotation() + eo, el),
                                b.y + Angles.trnsy(b.rotation() + eo, el), 36,
                                backB[i], backA[i], backRotation[i] + b.rotation());
                    }
                }
            }
        }
    }

    public void hitEntity1(Bullet b, Hitboxc entity, float health) {
        boolean wasDead = entity instanceof Unit u && u.dead;

        if (entity instanceof Healthc h) {
            if (pierceArmor) {
                h.damagePierce(b.damage);
            } else {
                h.damage(b.damage);
            }
        }

        if (entity instanceof Unit unit) {
            Tmp.v3.set(0, knockback * 80f * knockMultiplier(b)).rotate(b.rotation());
            if (impact) Tmp.v3.setAngle(b.rotation() + (knockback < 0 ? 180f : 0f));
            unit.impulse(Tmp.v3);
            unit.apply(status, statusDuration);

            Events.fire(new EventType.UnitDamageEvent().set(unit, b));
        }

        if (!wasDead && entity instanceof Unit unit && unit.dead) {
            Events.fire(new EventType.UnitBulletDestroyEvent(unit, b));
        }

        handlePierce(b, health, entity.x(), entity.y());
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        if (frontBreak.length != 0) {
            MultistageBullet m = (MultistageBullet) b;
            boolean hit1 = hit1(b);
            boolean hit2 = hit2(b);
            if (hit1) {
                if (b.collided.size == 1) {
                    hitEntity1(b, entity, health);
                    super.hit(b, b.x, b.y);

                    float bx = b.x, by = b.y, ex = entity.x(), ey = entity.y();
                    float angle = Angles.angle(ex, ey, bx, by);
                    float len = (float) Math.sqrt((bx - ex) * (bx - ex) + (by - ey) * (by - ey));
                    float size = entity.hitSize();
                    if (len > size) {
                        float angle2 = Angles.angleDist(angle, b.rotation());
                        float h = Mathf.sinDeg(angle2) * len;
                        float length = Mathf.cosDeg(angle2) * len - Mathf.sqrt(len * len - h * h);
                        b.x += Angles.trnsx(-b.rotation(), length);
                        b.y += Angles.trnsy(-b.rotation(), length);
                    }
                    bx = b.x;
                    by = b.y;
                    angle = Angles.angle(ex, ey, bx, by);
                    float length = (float) Math.sqrt((bx - ex) * (bx - ex) + (by - ey) * (by - ey));
                    float rotate = angle - ((Unit) entity).rotation;
                    m.ux = Angles.trnsx(rotate, length);
                    m.uy = Angles.trnsy(rotate, length);
                    m.ro = b.rotation() - ((Unit) entity).rotation;
                    m.target = (Unit) entity;
                    m.hitTimer = 0;
                    m.index = 0;
                }
            } else if (hit2 && m.hitTimer == 0) {
                hitEntity1(b, entity, health);
                super.hit(b, b.x, b.y);

                float bx = b.x, by = b.y, ex = entity.x(), ey = entity.y();
                float angle = Angles.angle(ex, ey, bx, by);
                float len = (float) Math.sqrt((bx - ex) * (bx - ex) + (by - ey) * (by - ey));
                float size = entity.hitSize();
                if (len > size) {
                    float angle2 = Angles.angleDist(angle, b.rotation());
                    float h = Mathf.sinDeg(angle2) * len;
                    float length = Mathf.cosDeg(angle2) * len - Mathf.sqrt(len * len - h * h);
                    b.x += Angles.trnsx(-b.rotation(), length);
                    b.y += Angles.trnsy(-b.rotation(), length);
                }
                bx = b.x;
                by = b.y;
                angle = Angles.angle(ex, ey, bx, by);
                float length = (float) Math.sqrt((bx - ex) * (bx - ex) + (by - ey) * (by - ey));
                float rotate = angle - ((Unit) entity).rotation;
                m.ux = Angles.trnsx(rotate, length);
                m.uy = Angles.trnsy(rotate, length);
                m.ro = b.rotation() - ((Unit) entity).rotation;
                m.target = (Unit) entity;
                m.hitTimer = 0;
                m.index = 0;
            }
        } else {
            hitEntity1(b, entity, health);
        }
    }

    @Override
    public void hit(Bullet b, float x, float y) {
        if (frontBreak.length == 0) {
            super.hit(b, x, y);
        }
    }

    @Override
    public void update(Bullet b) {
        updateTrail(b);
        updateWeaving(b);
        updateTrailEffects(b);
        updateBulletInterval(b);
        MultistageBullet m = (MultistageBullet) b;
        if (m.hitTimer < 0) {
            updateHoming(b);
            b.damage = damage * damageMultiplier(b);
            b.initVel(b.rotation(), speed * speedMultiplier(b, false));
        } else {
            Unit u = m.target;
            if (u == null || u.dead || u.health <= 0) {
                b.remove();
            } else {
                u.apply(WStatusEffects.bulletUnMove, 3 * Time.delta);
                m.hitTimer += Time.delta;
                float ux = m.ux;
                float uy = m.uy;
                float length = Mathf.sqrt(ux * ux + uy * uy);
                float rotate = Angles.angle(ux, uy);
                b.x = u.x + Angles.trnsx(rotate + u.rotation, length);
                b.y = u.y + Angles.trnsy(rotate + u.rotation, length);
                b.rotation(u.rotation + m.ro);
                b.keepAlive = true;
                b.hit = true;

                if (!m.move) {
                    m.damageTimer += Time.delta;
                    if (m.damageTimer >= frontBreak[m.index]) {
                        u.damage(m.damage * frontDamage[m.index]);
                        m.damageTimer = 0;
                        m.move = true;
                    }
                } else {
                    float len = frontSpeed[m.index], all = frontLength[m.index];
                    Tmp.v3.trns(b.rotation(), Math.min(len, all - m.step));
                    u.vel.add(Tmp.v3);
                    m.step += len;
                    if (m.step >= all) {
                        m.move = false;
                        m.step = 0;
                        if (m.index == frontBreak.length - 1) {
                            m.remove();
                        } else {
                            m.index++;
                        }
                    }
                }
            }
        }
    }

    @Override
    public float damageMultiplier(Bullet b) {
        if (b.time == 0 || hit1(b)) {
            return super.damageMultiplier(b);
        } else {
            float time = 0;
            float boost = 1;
            for (int i = 0; i < backBreak.length; i++) {
                time += backBreak[i];
                if (b.time >= time) {
                    boost *= backDamage[i];
                }
            }
            return super.damageMultiplier(b) * boost;
        }
    }

    public float speedMultiplier(Bullet b, boolean always) {
        if (!always && (b.time == 0 || hit1(b))) {
            return 1;
        } else {
            float time = 0;
            float boost = 1;
            for (int i = 0; i < backBreak.length; i++) {
                time += backBreak[i];
                if (b.time >= time) {
                    boost *= backSpeed[i];
                }
            }
            return boost;
        }
    }

    public float knockMultiplier(Bullet b) {
        float time = 0;
        float boost = 1;
        for (int i = 0; i < backBreak.length; i++) {
            time += backBreak[i];
            if (b.time >= time) {
                boost *= backKnock[i];
            }
        }
        return boost;
    }

    public boolean hit1(Bullet b) {
        if (b.collided.isEmpty()) {
            return false;
        }
        Unit u = Groups.unit.getByID(b.collided.first());
        return (u == null && b.collided.first() >= 0) ||
                (u != null && (!onlyBoss || u.hasEffect(StatusEffects.boss)));
    }

    public boolean hit2(Bullet b) {
        if (b.collided.isEmpty()) {
            return false;
        }
        Unit u;
        return (u = Groups.unit.getByID(b.collided.get(b.collided.size - 1))) != null && (!onlyBoss || u.hasEffect(StatusEffects.boss));
    }

    @Override
    public void load() {
        super.load();

        int max = Math.max(frontColors.length, Math.max(frontA.length, Math.max(frontB.length, Math.max(frontX.length,
                Math.max(frontY.length, Math.max(frontStroke.length, Math.max(frontRotation.length,
                        Math.max(frontBreak.length, Math.max(frontDamage.length,
                                Math.max(frontSpeed.length, frontLength.length))))))))));
        frontColors = loadColor(max, frontColors);
        frontA = loadArray(max, frontA);
        frontB = loadArray(max, frontB);
        frontX = loadArray(max, frontX);
        frontY = loadArray(max, frontY);
        frontStroke = loadArray(max, frontStroke);
        frontRotation = loadArray(max, frontRotation);
        frontBreak = loadArray(max, frontBreak);
        frontDamage = loadArray(max, frontDamage);
        frontLength = loadArray(max, frontLength);
        frontSpeed = loadArray(max, frontSpeed);

        max = Math.max(backColors.length, Math.max(backA.length, Math.max(backB.length, Math.max(backX.length,
                Math.max(backY.length, Math.max(backStroke.length, Math.max(backRotation.length,
                        Math.max(backBreak.length, Math.max(backDamage.length,
                                Math.max(backSpeed.length, backKnock.length))))))))));
        backColors = loadColor(max, backColors);
        backA = loadArray(max, backA);
        backB = loadArray(max, backB);
        backX = loadArray(max, backX);
        backY = loadArray(max, backY);
        backStroke = loadArray(max, backStroke);
        backRotation = loadArray(max, backRotation);
        backBreak = loadArray(max, backBreak);
        backDamage = loadArray(max, backDamage);
        backSpeed = loadArray(max, backSpeed);
        backKnock = loadArray(max, backKnock);
    }

    public float[] loadArray(int len, float[] def) {
        if (def.length < len) {
            float[] s = new float[len];
            System.arraycopy(def, 0, s, 0, def.length);
            Arrays.fill(s, def.length, len - 1, def.length == 0 ? 1 : def[def.length - 1]);
            return s;
        }
        return def;
    }

    public Color[] loadColor(int len, Color[] def) {
        if (def.length < len) {
            Color[] s = new Color[len];
            System.arraycopy(def, 0, s, 0, def.length);
            Arrays.fill(s, def.length, len - 1, def.length == 0 ? Color.white : def[def.length - 1]);
            return s;
        }
        return def;
    }

    public @Nullable Bullet create(@Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY) {
        if (!Mathf.chance(createChance)) return null;
        if (ignoreSpawnAngle) angle = 0;
        if (spawnUnit != null) {
            //don't spawn units clientside!
            if (!net.client()) {
                Unit spawned = spawnUnit.create(team);
                spawned.set(x, y);
                spawned.rotation = angle;
                //immediately spawn at top speed, since it was launched
                if (spawnUnit.missileAccelTime <= 0f) {
                    spawned.vel.trns(angle, spawnUnit.speed);
                }
                //assign unit owner
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
            //Since bullet init is never called, handle killing shooter here
            if (killShooter && owner instanceof Healthc h && !h.dead()) h.kill();

            //no bullet returned
            return null;
        }

        MultistageBullet bullet = MultistageBullet.create();
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
        //reset trail
        if (bullet.trail != null) {
            bullet.trail.clear();
        }
        bullet.add();

        if (keepVelocity && owner instanceof Velc v) bullet.vel.add(v.vel());
        return bullet;
    }
}