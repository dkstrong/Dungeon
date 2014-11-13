package asf.dungeon.model.factory;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.MasterJournal;
import asf.dungeon.model.PotionItem;
import com.badlogic.gdx.math.MathUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by danny on 10/26/14.
 */
public class FloorMapGenMultiplexer implements FloorMapGenerator{

        private FloorMapGenerator[] factories;
        private FloorMapGenerator[] randomFactories;

        public FloorMapGenMultiplexer(FloorMapGenerator[] factories, FloorMapGenerator[] randomFactories) {
                this.factories = factories;
                this.randomFactories = randomFactories;
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
