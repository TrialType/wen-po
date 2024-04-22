package wen;

import arc.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import wen.WContents.WBlocks;
import wen.WContents.WPlanets;
import wen.WType.MoreTechResearchDialog;

public class po extends Mod {
    public po() {
        //Events.on(EventType.ClientLoadEvent.class, e -> Time.runTask(10f, () -> Vars.ui.research = new MoreTechResearchDialog()));
    }

    @Override
    public void loadContent() {
        WBlocks.load();
        WPlanets.load();
    }

}
