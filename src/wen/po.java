package wen;

import arc.Core;
import arc.Events;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.mod.*;
import wen.WContents.TechTrees;
import wen.WContents.WBlocks;
import wen.WContents.WPlanets;
import wen.WType.MoreTechResearchDialog;

import static mindustry.Vars.mods;

public class po extends Mod {
    @Override
    public void loadContent() {
        WBlocks.load();
        WPlanets.load();
        TechTrees.load();
        Events.on(EventType.ClientLoadEvent.class, e -> Time.runTask(10f, () -> Vars.ui.research = new MoreTechResearchDialog()));
        Events.on(EventType.ContentInitEvent.class, e -> rebuildFromFile());
    }

    public void rebuildFromFile() {
        for (Mods.LoadedMod mod : mods.orderedMods()) {
            if (mod.name.contains("wen-po")) {
                if (Core.bundle.get("@GEOTech1") != null) {
                    UnlockableContent content = null;
                    String name = Core.bundle.get("@GEOTech1");
                    for (ContentType type : ContentType.all) {
                        content = Vars.content.getByName(type, name);
                        if (content != null) {
                            break;
                        }
                    }
                    if (content != null) {
                        TechTrees.geo_node1.content = content;
                    }
                }
                if (Core.bundle.get("@GEOTech2") != null) {
                    UnlockableContent content = null;
                    String name = Core.bundle.get("@GEOTech2");
                    for (ContentType type : ContentType.all) {
                        content = Vars.content.getByName(type, name);
                        if (content != null) {
                            break;
                        }
                    }
                    if (content != null) {
                        TechTrees.geo_node2.content = content;
                    }
                }
            }
        }
    }
}
