package asf.dungeon.model.floorgen;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;

import java.io.Serializable;

/**
 * Created by Danny on 11/4/2014.
 */
public interface FloorMapGenerator extends Serializable {

        public FloorMap generate(Dungeon dungeon, int floorIndex) throws InvalidGenerationException;


}
