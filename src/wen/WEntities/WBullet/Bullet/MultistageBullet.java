package wen.WEntities.WBullet.Bullet;

import arc.util.pooling.Pools;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

public class MultistageBullet extends Bullet {
    //for hit state
    public float ux = 0, uy = 0, ro = 0;

    //after hit unit
    public float hitTimer = -1;

    //for damage
    public float step = 0;
    public float damageTimer = 0;
    public int index = 0;
    public boolean move = false;
    public Unit target = null;

    public static MultistageBullet create() {
        return Pools.obtain(MultistageBullet.class, MultistageBullet::new);
    }

    @Override
    public void add() {
        if (!this.added) {
            this.index__all = Groups.all.addIndex(this);
            this.index__bullet = Groups.bullet.addIndex(this);
            this.index__draw = Groups.draw.addIndex(this);
            this.type.init(this);
            this.added = true;

            this.ux = 0;
            this.uy = 0;
            this.ro = 0;
            this.hitTimer = -1;
            this.step = 0;
            this.damageTimer = 0;
            this.index = -1;
            this.move = false;
            this.target = null;

            this.updateLastPosition();
        }
    }
}
