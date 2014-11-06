package asf.dungeon.model;

import com.badlogic.gdx.utils.Array;

import java.util.List;

/**
 * Created by Danny on 11/5/2014.
 */
public class Journal {

        private final Array<PotionItem.Type> potions = new Array<PotionItem.Type>(false,16, PotionItem.Type.class);

        public Journal() {
        }

        public void learn(PotionItem.Type type){
                if(!knows(type))
                        potions.add(type);
        }

        public boolean knows(PotionItem.Type type){
                return potions.contains(type, true);
        }
}
