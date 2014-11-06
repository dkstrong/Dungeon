package asf.dungeon.model.factory;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;

/**
 * Created by Danny on 11/4/2014.
 */
public interface FloorMapGenerator {

        public FloorMap generate(Dungeon dungeon, int floorIndex);


}
