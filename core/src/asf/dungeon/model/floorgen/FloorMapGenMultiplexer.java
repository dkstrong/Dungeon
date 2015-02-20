package asf.dungeon.model.floorgen;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;

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

        public FloorMap generate(Dungeon dungeon, FloorType floorType, int floorIndex){

                FloorMap floorMap = null;
                int tries = 0;
                do{
                        try{
                                if(floorIndex < factories.length){
                                        FloorType ft = floorIndex == 0 ? FloorType.Grassy : floorIndex == 1 ? FloorType.Church : FloorType.Dungeon;
                                        floorMap = factories[floorIndex].generate(dungeon, ft, floorIndex);
                                }else{
                                        floorMap = randomFactories[dungeon.rand.random.nextInt(randomFactories.length)].generate(dungeon, null, floorIndex);
                                }
                        }catch(InvalidGenerationException ex){
                                System.err.println("FloorMapGenMultiplexer: Unable to generate floor index: "+floorIndex);
                                if(++tries > 10) throw ex;
                        }
                }while(floorMap == null);

                return floorMap;
        }

}
