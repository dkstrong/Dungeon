package asf.dungeon.model;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.room.Doorway;
import asf.dungeon.model.floorgen.room.Room;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

/**
 * Created by Daniel Strong on 12/18/2014.
 */
public interface Symbol {
        public float getIntensity();

        public void spawnToken(Dungeon dungeon, FloorMap floorMap, Room lootRoom, Tile[][] validLocations);

        public void lockDoor(Dungeon dungeon, FloorMap floorMap, Doorway doorwway, Tile tile);

        public TextureAttribute getDoorTexAttribute(TextureAttribute[] doorLockedTexAttribute);






}
