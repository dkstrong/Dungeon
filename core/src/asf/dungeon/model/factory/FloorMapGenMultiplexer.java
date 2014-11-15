package asf.dungeon.model.factory;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import com.badlogic.gdx.math.MathUtils;

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
