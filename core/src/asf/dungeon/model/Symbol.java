package asf.dungeon.model;

import asf.dungeon.model.floorgen.room.Doorway;

/**
 * Created by Daniel Strong on 12/18/2014.
 */
public interface Symbol {
        public float getIntensity();

        public void lockDoor(Dungeon dungeon, FloorMap floorMap, Doorway doorwway, Tile tile);






}
