package wen;

import arc.Events;
import arc.util.Time;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.mod.*;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import wen.WContents.WBlocks;
import wen.WContents.WPlanets;
import wen.WContents.WStatusEffects;
import wen.WEntities.WBlocks.BuildCoreBlock;
import wen.WEntities.WBullet.Type.Critical.*;
import wen.WEntities.WBullet.Type.Other.MultistageBulletType;
import wen.WType.MoreTechResearchDialog;

import static mindustry.Vars.ui;

public class po extends Mod {
    @Override
    public void loadContent() {
        ClassMap.classes.put("BuildCoreBlock", BuildCoreBlock.class);
        ClassMap.classes.put("CriticalBulletType", CriticalBulletType.class);
        ClassMap.classes.put("CriticalArtilleryBulletType", CriticalArtilleryBulletType.class);
        ClassMap.classes.put("CriticalBasicBulletType", CriticalBasicBulletType.class);
        ClassMap.classes.put("CriticalBombBulletType", CriticalBombBulletType.class);
        ClassMap.classes.put("CriticalContinuousBulletType", CriticalContinuousBulletType.class);
        ClassMap.classes.put("CriticalContinuousFlameBulletType", CriticalContinuousFlameBulletType.class);
        ClassMap.classes.put("CriticalContinuousLaserBulletType", CriticalContinuousLaserBulletType.class);
        ClassMap.classes.put("CriticalEmpBulletType", CriticalEmpBulletType.class);
        ClassMap.classes.put("CriticalExplosionBulletType", CriticalExplosionBulletType.class);
        ClassMap.classes.put("CriticalFireBulletType", CriticalFireBulletType.class);
        ClassMap.classes.put("CriticalLaserBoltBulletType", CriticalLaserBoltBulletType.class);
        ClassMap.classes.put("CriticalLaserBulletType", CriticalLaserBulletType.class);
        ClassMap.classes.put("CriticalLightningBulletType", CriticalLightningBulletType.class);
        ClassMap.classes.put("CriticalLiquidBulletType", CriticalLiquidBulletType.class);
        ClassMap.classes.put("CriticalMassDriverBolt", CriticalMassDriverBolt.class);
        ClassMap.classes.put("CriticalMissileBulletType", CriticalMissileBulletType.class);
        ClassMap.classes.put("CriticalPointBulletType", CriticalPointBulletType.class);
        ClassMap.classes.put("CriticalPointLaserBulletType", CriticalPointLaserBulletType.class);
        ClassMap.classes.put("CriticalRailBulletType", CriticalRailBulletType.class);
        ClassMap.classes.put("CriticalSapBulletType", CriticalSapBulletType.class);
        ClassMap.classes.put("CriticalShrapnelBulletType", CriticalShrapnelBulletType.class);
        ClassMap.classes.put("CriticalSpaceLiquidBulletType", CriticalSpaceLiquidBulletType.class);
        WStatusEffects.load();
        WPlanets.load();
        WBlocks.load();
        Events.on(EventType.ClientLoadEvent.class, e -> Time.runTask(10f, () -> ui.research = new MoreTechResearchDialog()));

        ((ItemTurret) Blocks.duo).ammoTypes.put(Items.metaglass, new MultistageBulletType() {{
            damage = 10;
            onlyBoss = false;
            speed = 3;
            lifetime = 180;
            width = 1;
            height = 200;
            knockback = 1;
        }});
    }
}
