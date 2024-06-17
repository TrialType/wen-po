package wen.WContents;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import wen.WEntities.WBlocks.ExpendBlock;

public class WBlocks {
    public static Block test1,test2;
    public static void load(){
        test1 = new ExpendBlock("test1"){{
            damageBoost = 0;
            boost = 2;
            size = 3;
            range = 1000;
            requirements(Category.effect, ItemStack.with(Items.copper, 2000, Items.lead, 2000,
                    Items.graphite, 2000, Items.silicon, 2000, Items.titanium, 2000));
        }};
        test1 = new ExpendBlock("test2"){{
            damageBoost = 2;
            boost = 0;
            size = 3;
            range = 1000;
            requirements(Category.effect, ItemStack.with(Items.copper, 2000, Items.lead, 2000,
                    Items.graphite, 2000, Items.silicon, 2000, Items.titanium, 2000));
        }};
    }
}
