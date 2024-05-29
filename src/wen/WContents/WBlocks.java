package wen.WContents;

import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.type.ItemStack.with;

public class WBlocks {
    public static Block test, GEO_defaultCore;

    public static void load() {
        test = new Block("test") {{
            requirements(Category.effect, ItemStack.empty);
        }};
//        GEO_defaultCore = new CoreBlock("geo-default-core") {{
//            requirements(Category.effect, with(Items.titanium, 100, Items.thorium, 100, Items.plastanium, 100));
//            alwaysUnlocked = true;
//
//            unitType = UnitTypes.beta;
//            health = 520;
//            itemCapacity = 200;
//            size = 3;
//
//            unitCapModifier = 5;
//        }};
    }
}
