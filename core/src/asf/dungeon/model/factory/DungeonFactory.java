package asf.dungeon.model.factory;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.MasterJournal;
import asf.dungeon.model.PotionItem;
import com.badlogic.gdx.math.MathUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by danny on 10/26/14.
 */
public class DungeonFactory implements FloorMapGenerator{

        private FloorMapGenerator[] factories;
        private FloorMapGenerator[] randomFactories;

        public DungeonFactory(FloorMapGenerator[] factories, FloorMapGenerator[] randomFactories) {
                this.factories = factories;
                this.randomFactories = randomFactories;
        }


        public Dungeon makeDungeon(Dungeon.Listener listener){

                Map<PotionItem.Color,PotionItem.Type> potions = new EnumMap<PotionItem.Color,PotionItem.Type>(PotionItem.Color.class);

                List<PotionItem.Color> colors = Arrays.asList(PotionItem.Color.values());
                Collections.shuffle(colors, MathUtils.random);
                PotionItem.Type[] potionTypes = PotionItem.Type.values();
                for (int i = 0; i < potionTypes.length; i++) {
                        potions.put(colors.get(i),potionTypes[i] );
                }

                MasterJournal masterMasterJournal = new MasterJournal(potions);
                Dungeon dungeon = new Dungeon(listener, masterMasterJournal, this);
                FloorMap floorMap = dungeon.generateFloor(0);
                dungeon.setCurrentFloor(floorMap);

                return dungeon;
        }

        public FloorMap generate(Dungeon dungeon, int floorIndex){

                FloorMap floorMap;
                if(floorIndex < factories.length){
                        floorMap = factories[floorIndex].generate(dungeon, floorIndex);
                }else{
                        floorMap = randomFactories[MathUtils.random.nextInt(randomFactories.length)].generate(dungeon, floorIndex);
                }


                return floorMap;
        }

}
