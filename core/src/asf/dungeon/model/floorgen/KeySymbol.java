package asf.dungeon.model.floorgen;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.room.Doorway;
import asf.dungeon.model.floorgen.room.Room;
import asf.dungeon.model.floorgen.room.UtRoomSpawn;
import asf.dungeon.model.item.KeyItem;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

/**
 * Created by Daniel Strong on 12/18/2014.
 */
public class KeySymbol implements Symbol{
        public KeyItem.Type type;

        public KeySymbol(KeyItem.Type type) {
                this.type = type;
        }

        @Override
        public float getIntensity() {
                return (type.ordinal()) / 10f;
        }

        @Override
        public void spawnToken(Dungeon dungeon, FloorMap floorMap, Room lootRoom, Tile[][] validLocations) {
                Pair pair= UtRoomSpawn.getRandomLocToSpawnCharacter(dungeon, floorMap, lootRoom, validLocations);
                KeyItem keyItem = new KeyItem(dungeon, type);
                if(dungeon.rand.random.nextBoolean()){
                        dungeon.newCrateToken(floorMap, ModelId.CeramicPitcher.name(), ModelId.CeramicPitcher, keyItem, pair.x, pair.y);
                }else{
                        dungeon.newLootToken(floorMap, keyItem, pair.x, pair.y);
                }

        }

        @Override
        public void lockDoor(Dungeon dungeon, FloorMap floorMap, Doorway doorwway, Tile tile) {
                tile.setDoorLocked(true, this);
        }

        @Override
        public TextureAttribute getDoorTexAttribute(TextureAttribute[] doorLockedTexAttribute) {
                return doorLockedTexAttribute[type.ordinal()]; // corresponds to FastFloorSpatial.init()
        }
}
