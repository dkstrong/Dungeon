package asf.dungeon.model.item;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.Symbol;
import asf.dungeon.model.floorgen.room.Doorway;
import asf.dungeon.model.floorgen.room.Room;
import asf.dungeon.model.floorgen.room.UtRoomSpawn;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

/**
 * Created by Danny on 11/5/2014.
 */
public class KeyItem extends AbstractItem implements Item, Symbol {


        public static enum Type{
                Silver, Gold, Red;
        }

        private final Type type;

        public KeyItem(Type type) {
                this.type = type;
        }

        @Override
        public ModelId getModelId() {
                switch(type){
                        case Silver:
                                return ModelId.Key;
                        case Gold:
                                return ModelId.Key2;
                        case Red:
                                return ModelId.Key3;
                }
                throw new AssertionError(type);
        }

        @Override
        public String getName() {
                return type.name()+" Key";
        }

        @Override
        public String getDescription() {
                return "This key can be used to open a locked "+ type.name()+" door.";
        }

        @Override
        public String getVagueName() { return getName();}

        @Override
        public String getVagueDescription() { return getDescription();}

        @Override
        public boolean isIdentified(Token token) { return true; }

        @Override
        public void identifyItem(Token token) {

        }

        public Type getType() {
                return type;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                KeyItem that = (KeyItem) o;

                if (type != that.type) return false;

                return true;
        }

        @Override
        public int hashCode() {
                return type != null ? type.hashCode() : 0;
        }

        @Override
        public float getIntensity() {
                return (type.ordinal()) / 10f;
        }

        @Override
        public void spawnToken(Dungeon dungeon, FloorMap floorMap, Room lootRoom, Tile[][] validLocations) {

                Pair pair= UtRoomSpawn.getRandomLocToSpawnCharacter(dungeon, floorMap, lootRoom, validLocations);
                if(dungeon.rand.random.nextBoolean()){
                        dungeon.newCrateToken(floorMap, ModelId.CeramicPitcher.name(), ModelId.CeramicPitcher, this, pair.x, pair.y);
                }else{
                        dungeon.newLootToken(floorMap, this, pair.x, pair.y);
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
