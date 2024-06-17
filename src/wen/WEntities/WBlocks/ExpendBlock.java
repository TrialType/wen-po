package wen.WEntities.WBlocks;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.Turret;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class ExpendBlock extends Block {
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

        @Override
        public void updateTile() {
            turrets.removeAll(i -> world.build(i) == null || !world.build(i).isAdded() ||
                    !(world.build(i) instanceof Turret.TurretBuild));
            bullets.removeAll(b -> b == null || !b.isAdded());
            Groups.bullet.each(b -> {
                if (b.owner instanceof Turret.TurretBuild t && turrets.indexOf(t.pos()) >= 0 && bullets.indexOf(b) < 0) {
                    b.damage *= (damageBoost + 1);
                    bullets.add(b);
                }
            });
            turrets.each(t -> {
                Building b = world.build(t);
                b.health -= b.maxHealth * 0.02f * Time.delta;
                if (b.health < 0) {
                    b.dead = true;
                    b.kill();
                }
                if (!b.dead && b.block.canOverdrive) {
                    b.applyBoost((1 + boost), 2 * Time.delta);
                }
            });
            turrets.removeAll(t -> world.build(t) == null || world.build(t).dead);
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