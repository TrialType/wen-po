package wen.WType;

import arc.struct.Seq;
import mindustry.content.TechTree;
import mindustry.type.Planet;

public class WPlanet extends Planet {
    public Seq<TechTree.TechNode> techTrees = new Seq<>();

    public WPlanet(String name, Planet parent, float radius) {
        super(name, parent, radius);
    }
    public WPlanet(String name, Planet parent, float radius, int sectorSize) {
        super(name, parent, radius, sectorSize);
    }

}
