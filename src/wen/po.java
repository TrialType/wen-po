package wen;

import mindustry.mod.*;
import wen.WContents.WBlocks;
import wen.WContents.WPlanets;

public class po extends Mod {
    public po() {}
    @Override
    public void loadContent() {
        WBlocks.load();
        WPlanets.load();
    }
}
