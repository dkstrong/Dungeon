package asf.dungeon.model;

import asf.dungeon.model.item.PotionItem;

/**
 * Created by Danny on 11/5/2014.
 */
public class MasterJournal  {

        //private final Map<PotionItem.Color,PotionItem.Type> potions;
        private final PotionItem.Type[] potions;

        public MasterJournal(PotionItem.Type[] potions) {

                this.potions = potions;
        }


        public PotionItem.Color getPotionColor(PotionItem.Type type){
                for (int i = 0; i < potions.length; i++) {
                        PotionItem.Type potionType = potions[i];
                        if(potionType == type){
                                return PotionItem.Color.values()[i];
                        }
                }
                return null;


        }

        public PotionItem.Type getPotionType(PotionItem.Color color){
                return potions[color.ordinal()];
        }
}
