package wen.WBlock;

import mindustry.gen.Building;
import mindustry.world.blocks.storage.CoreBlock;

public class UnitNumberBooster extends CoreBlock {

    public UnitNumberBooster(String name) {
        super(name);
    }
    public class UnitNumberBoostBuild extends CoreBuild{
        public boolean owns(Building core, Building tile){
            return false;
        }
    }
}
