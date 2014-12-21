package asf.dungeon.model.token.puzzle;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Symbol;
import asf.dungeon.model.Tile;

/**
 * concrete class for combination puzzles that cause a door to lock or unlock
 *
 * Created by Daniel Strong on 12/18/2014.
 */
public class CombinationDoorPuzzle extends CombinationPuzzle implements Symbol {
        private Tile doorTile;

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
        public void lockDoor(Dungeon dungeon, FloorMap floorMap, Tile tile) {
                doorTile = tile;
                tile.setDoorLocked(true, this);
                checkPuzzle(dungeon);
        }
}
