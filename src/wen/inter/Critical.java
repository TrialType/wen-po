package wen.inter;

import arc.math.Mathf;
import arc.struct.ObjectFloatMap;

public interface Critical {
    default float critical1() {
        return 1;
    }

    default float criticalChance1() {
        return 0;
    }

    default float critical2() {
        return 1;
    }

    default float criticalChance2() {
        return 0;
    }

    default float critical3() {
        return 1;
    }

    default float criticalChance3() {
        return 0;
    }

    default float trueCritical() {
        float value = Mathf.random();
        float multiplier = 1;

        ObjectFloatMap<Float> map = new ObjectFloatMap<>();
        map.put(criticalChance1(), critical1());
        map.put(criticalChance2(), critical2());
        map.put(criticalChance3(), critical3());
        for (float chance : map.keys()) {
            if (value <= chance) {
                multiplier *= map.get(chance, 1);
            }
        }

        return Math.max(multiplier, 0.0001f);
    }
}
