package wen;

import arc.Events;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.mod.*;
import wen.WContents.WPlanets;
import wen.WEntities.WBlocks.BuildCoreBlock;
import wen.WType.MoreTechResearchDialog;

import static mindustry.Vars.ui;

public class po extends Mod {
    @Override
    public void loadContent() {
        ClassMap.classes.put("BuildCoreBlock", BuildCoreBlock.class);
        WPlanets.load();
        Events.on(EventType.ClientLoadEvent.class, e -> Time.runTask(10f, () -> ui.research = new MoreTechResearchDialog()));
    }
}
