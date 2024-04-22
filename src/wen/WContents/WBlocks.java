package wen.WContents;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class WBlocks {
    public static Block test;

    public static void load() {
        test = new Block("test") {{
            requirements(Category.defense, ItemStack.empty);
        }};
    }
}
