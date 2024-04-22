package wen.WContents;

import arc.graphics.Color;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.content.TechTree;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.maps.planet.SerpuloPlanetGenerator;
import mindustry.type.Planet;

public class WPlanets {
    public static Planet GEO;

    public static void load() {
        GEO = new Planet("GEO", Planets.sun, 1, 4) {{
            generator = new SerpuloPlanetGenerator();
            meshLoader = () -> new HexMesh(this, 6);
            cloudMeshLoader = () -> new MultiMesh(
                    new HexSkyMesh(this, 11, 0.15f, 0.13f, 5, new Color().set(Pal.spore).mul(0.9f).a(0.75f), 2, 0.45f, 0.9f, 0.38f),
                    new HexSkyMesh(this, 1, 0.6f, 0.16f, 5, Color.white.cpy().lerp(Pal.spore, 0.55f).a(0.75f), 2, 0.45f, 1f, 0.41f)
            );

            alwaysUnlocked = true;
            iconColor = Color.valueOf("7d4dff");
            atmosphereColor = Color.valueOf("3c1b8f");
            startSector = 10;
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;
            tidalLock = true;
            orbitSpacing = 2f;
            totalRadius += 2.6f;
            lightSrcTo = 0.5f;
            lightDstFrom = 0.2f;
            allowWaves = true;
            allowWaveSimulation = true;
            allowSectorInvasion = true;
            allowLaunchSchematics = true;
            enemyCoreSpawnReplace = true;
            allowLaunchLoadout = true;
            defaultCore = Blocks.coreNucleus;
            iconColor = Color.valueOf("ff9266");
            enemyBuildSpeedMultiplier = 0.4f;

            allowLaunchToNumbered = false;

            updateLighting = false;

            ruleSetter = r -> {
                r.waveTeam = Team.crux;
                r.placeRangeCheck = false;
                r.showSpawns = false;
            };

            techTree = Planets.serpulo.techTree;

            TechTree.TechNode g = TechTree.nodeRoot("GEOTech1", WBlocks.defaultCore, () -> {
            });
            g.planet = GEO;
            g = TechTree.nodeRoot("GEOTech2", WBlocks.test, () -> {
            });
            g.planet = GEO;
        }};
    }
}
