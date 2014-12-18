package asf.dungeon.model.floorgen;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.room.Doorway;
import asf.dungeon.model.floorgen.room.Room;
import asf.dungeon.model.floorgen.room.UtRoomSpawn;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.Torch;
import asf.dungeon.model.token.quest.TorchQuest;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

/**
 * Created by Daniel Strong on 12/18/2014.
 */
public class PuzzleSymbol implements Symbol{
        public Torch.Puzzle puzzle;

        public PuzzleSymbol(Torch.Puzzle puzzle) {
                this.puzzle = puzzle;
        }

        @Override
        public float getIntensity() {
                return 0.2f;
        }

        @Override
        public void spawnToken(Dungeon dungeon, FloorMap floorMap, Room lootRoom, Tile[][] validLocations) {
                Token torchToken = new Token(dungeon, "Torch", ModelId.Torch);
                torchToken.add(new Torch(torchToken, false, puzzle));
                torchToken.add(new TorchQuest());
                Pair loc = UtRoomSpawn.getRandomLocToSpawnCharacter(dungeon, floorMap, lootRoom, validLocations);
                dungeon.newToken(torchToken, floorMap, loc.x, loc.y);

                ((Torch.CombinationDoorPuzzle)puzzle).putTorch(torchToken, true);
        }

        @Override
        public void lockDoor(Dungeon dungeon, FloorMap floorMap, Doorway doorwway, Tile tile) {
                tile.setDoorLocked(true, this);
                puzzle.checkPuzzle(dungeon);
        }

        @Override
        public TextureAttribute getDoorTexAttribute(TextureAttribute[] doorLockedTexAttribute) {
                return doorLockedTexAttribute[doorLockedTexAttribute.length-2]; // corresponds to FastFloorSpatial.init()
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                PuzzleSymbol that = (PuzzleSymbol) o;

                if (!puzzle.equals(that.puzzle)) return false;

                return true;
        }

        @Override
        public int hashCode() {
                return puzzle.hashCode();
        }
}
