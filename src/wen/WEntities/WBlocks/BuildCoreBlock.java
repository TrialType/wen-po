package wen.WEntities.WBlocks;

import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.Stat;

import static mindustry.Vars.state;

public class BuildCoreBlock extends CoreBlock {
    public int max = 2;

    public BuildCoreBlock(String name) {
        super(name);
    }

    @Override
    public boolean canBreak(Tile tile) {
        if (tile.build instanceof CoreBuild c) {
            return c.team.cores().size > 1;
        }
        return state.isEditor();
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (tile == null) return false;
        if (state.isEditor()) return true;
        CoreBuild core = team.core();
        tile.getLinkedTilesAs(this, tempTiles);
        if (!tempTiles.contains(o -> !o.floor().allowCorePlacement || o.block() instanceof CoreBlock) ||
                (core != null && team.cores().size < max)) {
            return true;
        }
        if (core == null || (!state.rules.infiniteResources && !core.items.has(requirements, state.rules.buildCostMultiplier)))
            return false;

        return tile.block() instanceof CoreBlock && size > tile.block().size && (!requiresCoreZone || tempTiles.allMatch(o -> o.floor().allowCorePlacement));
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.add(new Stat("maxCoreNum"), max);
    }

    public class BuildCoreBuild extends CoreBuild {
    }
}
