package wen;

import arc.Events;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.*;
import wen.WContents.WBlocks;
import wen.WContents.WPlanets;
import wen.WType.MoreTechResearchDialog;

public class po extends Mod {
    public po() {}

    @Override
    public void loadContent() {
        Events.on(EventType.ClientLoadEvent.class, e -> Time.runTask(10f, () -> Vars.ui.research = new MoreTechResearchDialog()));
        WBlocks.load();
        WPlanets.load();
    }
}
