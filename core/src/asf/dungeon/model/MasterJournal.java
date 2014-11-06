package asf.dungeon.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by Danny on 11/5/2014.
 */
public class MasterJournal {

        private final Map<PotionItem.Color,PotionItem.Type> potions;

        public MasterJournal(Map<PotionItem.Color, PotionItem.Type> potions) {
                this.potions = potions;
        }


        public PotionItem.Color getPotionColor(PotionItem.Type type){
                for (Map.Entry<PotionItem.Color, PotionItem.Type> entry : potions.entrySet()) {
                        if(entry.getValue() == type){
                                return entry.getKey();
                        }
                }
                return null;


        }

        public PotionItem.Type getPotionType(PotionItem.Color color){
                return potions.get(color);
        }
}
