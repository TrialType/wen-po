package wen.WContents;

import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BuildVisibility;

import static mindustry.type.ItemStack.with;

public class WBlocks {
    public static Block test, defaultCore;

    public static void load() {
        test = new Block("test") {{
            requirements(Category.defense, ItemStack.empty);
        }};
        defaultCore = new CoreBlock("default-core") {{
            requirements(Category.effect, BuildVisibility.editorOnly, with(Items.copper, 1000, Items.lead, 800));
            alwaysUnlocked = true;

            isFirstTier = true;
            unitType = UnitTypes.alpha;
            health = 1100;
            itemCapacity = 4000;
            size = 3;

            unitCapModifier = 8;
        }};
    }
}
