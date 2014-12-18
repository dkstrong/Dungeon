package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.Symbol;
import asf.dungeon.model.floorgen.room.Doorway;
import asf.dungeon.model.floorgen.room.Room;
import asf.dungeon.model.floorgen.room.UtRoomSpawn;
import asf.dungeon.model.token.quest.TorchQuest;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.IdentityMap;

/**
 * Created by Daniel Strong on 12/17/2014.
 */
public class Torch implements TokenComponent {
        private Token token;
        private boolean ignited;
        private Puzzle puzzle;

        public Torch(Token token, boolean ignited, Puzzle puzzle) {
                this.token = token;
                this.ignited = ignited;
                this.puzzle = puzzle;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public void toggleIgnited(){
                ignited = !ignited;
                if(puzzle!=null)
                        puzzle.checkPuzzle(token.dungeon);
                // TODO: somehow i need to pass this information on to HudSpatial through a listener so it can output something like "you head a loud clang"
                // whenever the puzzle is solved or unsolved. The way to do this might be more obvious after i add pressure switch puzzle

        }

        public boolean isIgnited() {
                return ignited;
        }

        public Puzzle getPuzzle() {
                return puzzle;
        }

        public static interface Puzzle extends Symbol {
                public void checkPuzzle(Dungeon dungeon);
                public boolean isSolved();
        }

        public static abstract class CombinationPuzzle implements Puzzle{
                /**
                 * if the provided torches have the corresponding ignited values, then onSolved() will be perfomed, otherwise onUnsolved() with be performed
                 */
                private final IdentityMap<Torch, Boolean> torches = new IdentityMap <Torch, Boolean>(3);

                public void putTorch(Token token, boolean ignited){
                        Torch t = token.get(Torch.class);
                        if(t == null) throw new IllegalArgumentException("token must have a torch");
                        torches.put(t, ignited);
                }
                @Override
                public void checkPuzzle(Dungeon dungeon) {
                        for (IdentityMap.Entry<Torch, Boolean> entry : torches) {
                                if(entry.key.isIgnited() != entry.value){
                                        onUnsolved();
                                        return;
                                }
                        }
                        onSolved();
                }

                public abstract void onSolved();

                protected abstract void onUnsolved();
        }

        public static class CombinationDoorPuzzle extends CombinationPuzzle{

                private final Tile doorTile;

                public CombinationDoorPuzzle(Tile doorTile) {
                        this.doorTile = doorTile;
                }

                @Override
                public void onSolved() {
                        doorTile.setDoorLocked(false);
                        doorTile.setDoorForcedOpen(true);
                }

                @Override
                protected void onUnsolved() {
                        doorTile.setDoorLocked(true);
                        doorTile.setDoorForcedOpen(false);
                }

                @Override
                public boolean isSolved() {
                        return !doorTile.isDoorLocked();
                }

                @Override
                public float getIntensity() {
                        return 0.2f;
                }

                @Override
                public void spawnToken(Dungeon dungeon, FloorMap floorMap, Room lootRoom, Tile[][] validLocations) {
                        Token torchToken = new Token(dungeon, "Torch", ModelId.Torch);
                        torchToken.add(new Torch(torchToken, false, this));
                        torchToken.add(new TorchQuest());
                        Pair loc = UtRoomSpawn.getRandomLocToSpawnCharacter(dungeon, floorMap, lootRoom, validLocations);
                        dungeon.newToken(torchToken, floorMap, loc.x, loc.y);
                        putTorch(torchToken, true);
                }

                @Override
                public void lockDoor(Dungeon dungeon, FloorMap floorMap, Doorway doorwway, Tile tile) {
                        tile.setDoorLocked(true, this);
                        checkPuzzle(dungeon);
                }

                @Override
                public TextureAttribute getDoorTexAttribute(TextureAttribute[] doorLockedTexAttribute) {
                        return doorLockedTexAttribute[doorLockedTexAttribute.length-2]; // corresponds to FastFloorSpatial.init()
                }
        }


}
