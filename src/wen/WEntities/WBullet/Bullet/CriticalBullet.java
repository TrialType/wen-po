package wen.WEntities.WBullet.Bullet;

import arc.util.pooling.Pools;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import mindustry.gen.Hitboxc;
import wen.inter.Critical;

public class CriticalBullet extends Bullet {
    public static CriticalBullet create() {
        return Pools.obtain(CriticalBullet.class, CriticalBullet::new);
    }

    public void collision(Hitboxc other, float x, float y) {
        float boost = 1;
        if (type instanceof Critical c) {
            boost = c.trueCritical();
        }
        this.damage *= boost;
        this.type.hit(this, x, y);
        if (!this.type.pierce) {
            this.hit = true;
            this.remove();
        } else {
            this.collided.add(other.id());
        }

        BulletType var10000 = this.type;
        float var10003;
        if (other instanceof Healthc h) {
            var10003 = h.health();
        } else {
            var10003 = 0.0F;
        }

        var10000.hitEntity(this, other, var10003);

        this.damage /= boost;
    }
}
