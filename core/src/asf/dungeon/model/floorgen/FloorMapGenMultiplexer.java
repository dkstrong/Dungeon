package asf.dungeon.model.floorgen;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;

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
                        floorMap = randomFactories[dungeon.rand.random.nextInt(randomFactories.length)].generate(dungeon, floorIndex);
                }


                return floorMap;
        }

}
