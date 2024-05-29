package wen.WContents;

import mindustry.content.Blocks;
import mindustry.content.TechTree;

public class TechTrees {
    public static
    TechTree.TechNode geo_node1 = TechTree.nodeRoot("GEOTech1", Blocks.copperWall, () -> {
    });
    public static
    TechTree.TechNode geo_node2 = TechTree.nodeRoot("GEOTech2", Blocks.copperWall, () -> {
    });

    public static void load() {
        geo_node1.planet = geo_node2.planet = WPlanets.GEO;
    }
}
