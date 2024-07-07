package wen.Tools;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.func.Floatp;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.struct.IntFloatMap;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.core.World;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;
import mindustry.world.Tile;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class Damage2 {
    private static final Rect rect = new Rect();
    private static final Rect hitrect = new Rect();
    private static final Vec2 vec = new Vec2(), seg1 = new Vec2(), seg2 = new Vec2();
    private static final Seq<Damage.Collided> collided = new Seq<>();
    private static final Pool<Damage.Collided> collidePool = Pools.get(Damage.Collided.class, Damage.Collided::new);
    private static final IntSet collidedBlocks = new IntSet();
    private static final IntFloatMap damages = new IntFloatMap();

    public static void criticalCollidePoint(Bullet hitter, Team team, Effect effect, float x, float y, float critical) {
        hitter.damage *= critical;
        if (hitter.type.collidesGround) {
            Building build = world.build(World.toTile(x), World.toTile(y));
            if (build != null && hitter.damage > 0) {
                float health = build.health;
                if (build.team != team && build.collide(hitter)) {
                    build.collision(hitter);
                    hitter.type.hit(hitter, x, y);
                }
                if (hitter.type.testCollision(hitter, build)) {
                    hitter.type.hitTile(hitter, build, x, y, health, false);
                }
            }
        }
        hitter.damage /= critical;
        Units.nearbyEnemies(team, new Rect().setCentered(x, y, 1f), u -> {
            if (u.checkTarget(hitter.type.collidesAir, hitter.type.collidesGround) && u.hittable()) {
                effect.at(x, y);
                u.collision(hitter, x, y);
                hitter.collision(u, x, y);
            }
        });
    }

    public static void criticalCollideLine(Bullet hitter, Team team, Effect effect, float x, float y, float angle, float length, boolean large, boolean laser, float critical) {
        criticalCollideLine(hitter, team, effect, x, y, angle, length, large, laser, -1, critical);
    }

    public static void criticalCollideLine(Bullet hitter, Team team, Effect effect, float x, float y, float angle, float length, boolean large, boolean laser, int pierceCap, float critical) {
        length = Damage.findLength(hitter, length, laser, pierceCap);

        collidedBlocks.clear();
        vec.trnsExact(angle, length);

        if (hitter.type.collidesGround) {
            seg1.set(x, y);
            seg2.set(seg1).add(vec);
            World.raycastEachWorld(x, y, seg2.x, seg2.y, (cx, cy) -> {
                Building tile = world.build(cx, cy);
                boolean collide = tile != null && tile.collide(hitter) && hitter.checkUnderBuild(tile, cx * tilesize, cy * tilesize)
                        && ((tile.team != team && tile.collide(hitter)) || hitter.type.testCollision(hitter, tile)) && collidedBlocks.add(tile.pos());
                if (collide) {
                    collided.add(collidePool.obtain().set(cx * tilesize, cy * tilesize, tile));

                    for (Point2 p : Geometry.d4) {
                        Tile other = world.tile(p.x + cx, p.y + cy);
                        if (other != null && (large || Intersector.intersectSegmentRectangle(seg1, seg2, other.getBounds(Tmp.r1)))) {
                            Building build = other.build;
                            if (build != null && hitter.checkUnderBuild(build, cx * tilesize, cy * tilesize) && collidedBlocks.add(build.pos())) {
                                collided.add(collidePool.obtain().set((p.x + cx * tilesize), (p.y + cy) * tilesize, build));
                            }
                        }
                    }
                }
                return false;
            });
        }

        float expand = 3f;

        rect.setPosition(x, y).setSize(vec.x, vec.y).normalize().grow(expand * 2f);
        float x2 = vec.x + x, y2 = vec.y + y;

        Units.nearbyEnemies(team, rect, u -> {
            if (u.checkTarget(hitter.type.collidesAir, hitter.type.collidesGround) && u.hittable()) {
                u.hitbox(hitrect);

                Vec2 vec = Geometry.raycastRect(x, y, x2, y2, hitrect.grow(expand * 2));

                if (vec != null) {
                    collided.add(collidePool.obtain().set(vec.x, vec.y, u));
                }
            }
        });

        int[] collideCount = {0};
        collided.sort(c -> hitter.dst2(c.x, c.y));
        collided.each(c -> {
            if (hitter.damage > 0 && (pierceCap <= 0 || collideCount[0] < pierceCap)) {
                if (c.target instanceof Unit u) {
                    effect.at(c.x, c.y);
                    u.collision(hitter, c.x, c.y);
                    hitter.collision(u, c.x, c.y);
                    collideCount[0]++;
                } else if (c.target instanceof Building tile) {
                    float health = tile.health;

                    if (tile.team != team && tile.collide(hitter)) {
                        hitter.damage *= critical;
                        tile.collision(hitter);
                        hitter.damage /= critical;
                        hitter.type.hit(hitter, c.x, c.y);
                        collideCount[0]++;
                    }

                    if (hitter.type.testCollision(hitter, tile)) {
                        hitter.type.hitTile(hitter, tile, c.x, c.y, health, false);
                    }
                }
            }
        });

        collidePool.freeAll(collided);
        collided.clear();
    }

    public static void criticalCollideLaser(Bullet b, float length, boolean large, boolean laser, int pierceCap, float critical) {
        float resultLength = Damage.findPierceLength(b, pierceCap, laser, length);
        criticalCollideLine(b, b.team, b.type.hitEffect, b.x, b.y, b.rotation(), resultLength, large, laser, pierceCap, critical);
        b.fdata = resultLength;
    }

    public static void criticalDamage(Team team, float x, float y, float radius, float damage, boolean complete, boolean air, boolean ground, boolean scaled, Bullet source, Floatp critical) {
        Cons<Unit> cons = unit -> {
            if (unit.team == team || !unit.checkTarget(air, ground) || !unit.hittable() || !unit.within(x, y, radius + (scaled ? unit.hitSize / 2f : 0f))) {
                return;
            }
            boolean dead = unit.dead;
            float amount = calculateDamage(scaled ? Math.max(0, unit.dst(x, y) - unit.type.hitSize / 2) : unit.dst(x, y), radius, damage);
            unit.damage(amount * critical.get());
            if (source != null) {
                Events.fire(new EventType.UnitDamageEvent().set(unit, source));
                unit.controller().hit(source);
                if (!dead && unit.dead) {
                    Events.fire(new EventType.UnitBulletDestroyEvent(unit, source));
                }
            }
            float dst = vec.set(unit.x - x, unit.y - y).len();
            unit.vel.add(vec.setLength((1f - dst / radius) * 2f / unit.mass()));
            if (complete && damage >= 9999999f && unit.isPlayer()) {
                Events.fire(EventType.Trigger.exclusionDeath);
            }
        };
        rect.setSize(radius * 2).setCenter(x, y);
        if (team != null) {
            Units.nearbyEnemies(team, rect, cons);
        } else {
            Units.nearby(rect, cons);
        }
        if (ground) {
            if (!complete) {
                tileDamage(team, World.toTile(x), World.toTile(y), radius / tilesize,
                        damage * (source == null ? 1f : source.type.buildingDamageMultiplier), source, critical);
            } else {
                completeDamage(team, x, y, radius, damage, critical);
            }
        }
    }

    public static void tileDamage(Team team, int x, int y, float baseRadius, float damage, @Nullable Bullet source, Floatp critical) {
        Core.app.post(() -> {
            var in = world.build(x, y);
            if (in != null && in.team != team && in.block.size > 1 && in.health > damage) {
                in.damage(team, damage * Math.min((in.block.size), baseRadius * 0.4f));
                return;
            }
            float radius = Math.min(baseRadius, 100), rad2 = radius * radius;
            int rays = Mathf.ceil(radius * 2 * Mathf.pi);
            double spacing = Math.PI * 2.0 / rays;
            damages.clear();
            for (int i = 0; i <= rays; i++) {
                float dealt = 0f;
                int startX = x;
                int startY = y;
                int endX = x + (int) (Math.cos(spacing * i) * radius), endY = y + (int) (Math.sin(spacing * i) * radius);
                int xDist = Math.abs(endX - startX);
                int yDist = -Math.abs(endY - startY);
                int xStep = (startX < endX ? +1 : -1);
                int yStep = (startY < endY ? +1 : -1);
                int error = xDist + yDist;
                while (startX != endX || startY != endY) {
                    var build = world.build(startX, startY);
                    if (build != null && build.team != team) {
                        float edgeScale = 0.6f;
                        float mult = (1f - (Mathf.dst2(startX, startY, x, y) / rad2) + edgeScale) / (1f + edgeScale);
                        float next = damage * mult - dealt;
                        int p = Point2.pack(startX, startY);
                        damages.put(p, Math.max(damages.get(p), next));
                        dealt += build.health;
                        if (next - dealt <= 0) {
                            break;
                        }
                    }
                    if (2 * error - yDist > xDist - 2 * error) {
                        error += yDist;
                        startX += xStep;
                    } else {
                        error += xDist;
                        startY += yStep;
                    }
                }
            }
            for (var e : damages) {
                int cx = Point2.x(e.key), cy = Point2.y(e.key);
                var build = world.build(cx, cy);
                if (build != null) {
                    if (source != null) {
                        build.damage(source, team, e.value * critical.get());
                    } else {
                        build.damage(team, e.value * critical.get());
                    }
                }
            }
        });
    }

    private static float calculateDamage(float dist, float radius, float damage) {
        float falloff = 0.4f;
        float scaled = Mathf.lerp(1f - dist / radius, 1f, falloff);
        return damage * scaled;
    }

    private static void completeDamage(Team team, float x, float y, float radius, float damage, Floatp critical) {

        int trad = (int) (radius / tilesize);
        for (int dx = -trad; dx <= trad; dx++) {
            for (int dy = -trad; dy <= trad; dy++) {
                Tile tile = world.tile(Math.round(x / tilesize) + dx, Math.round(y / tilesize) + dy);
                if (tile != null && tile.build != null && (team == null || team != tile.team()) && dx * dx + dy * dy <= trad * trad) {
                    tile.build.damage(team, damage * critical.get());
                }
            }
        }
    }
}
