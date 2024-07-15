package wen.WContents;

import mindustry.type.StatusEffect;

public class WStatusEffects {
    public static StatusEffect bulletUnMove;

    public static void load(){
        bulletUnMove = new StatusEffect("bullet-un-move"){{
            show = false;
            speedMultiplier = 0;
        }};
    }
}
