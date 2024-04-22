package wen.WType;

import arc.struct.ObjectMap;
import mindustry.content.Planets;
import mindustry.type.Item;
import mindustry.type.ItemSeq;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.ui.dialogs.ResearchDialog;

import static mindustry.Vars.content;

public class MoreTechResearchDialog extends ResearchDialog {
    @Override
    public void rebuildItems() {
        items = new ItemSeq() {
            final ObjectMap<Sector, ItemSeq> cache = new ObjectMap<>();
            {

                Planet rootPlanet = lastNode.planet != null ? lastNode.planet : content.planets().find(p -> p.techTree == lastNode);
                if (rootPlanet == null) rootPlanet = Planets.serpulo;
                for (Sector sector : rootPlanet.sectors) {
                    if (sector.hasBase()) {
                        ItemSeq cached = sector.items();
                        cache.put(sector, cached);
                        cached.each((item, amount) -> {
                            values[item.id] += Math.max(amount, 0);
                            total += Math.max(amount, 0);
                        });
                    }
                }
            }

            @Override
            public void add(Item item, int amount) {
                if (amount < 0) {
                    amount = -amount;
                    double percentage = (double) amount / get(item);
                    int[] counter = {amount};
                    cache.each((sector, seq) -> {
                        if (counter[0] == 0) return;
                        int toRemove = Math.min((int) Math.ceil(percentage * seq.get(item)), counter[0]);
                        sector.removeItem(item, toRemove);
                        seq.remove(item, toRemove);

                        counter[0] -= toRemove;
                    });
                    amount = -amount;
                }
                super.add(item, amount);
            }
        };
    }
}
