package wen;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import wen.WContents.WBlocks;
import wen.WContents.WPlanets;

public class po extends Mod {
    public po() {
    }

    @Override
    public void loadContent() {
        WBlocks.load();
        WPlanets.load();
    }

}
