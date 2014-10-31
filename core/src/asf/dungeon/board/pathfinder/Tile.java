package asf.dungeon.board.pathfinder;

/**
 * Created by danny on 10/21/14.
 */
public interface Tile {

        public boolean isBlockMovement();

        public int getMovementCost();
}
