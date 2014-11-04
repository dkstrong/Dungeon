package asf.dungeon.board.pathfinder;

import asf.dungeon.board.Pair;

/**
 * Created by danny on 10/23/14.
 */
public interface DynamicMovementCostProvider {

        /**
         * return dynamic movement cost for this tile (movment cost caused by
         * moving characters etc)
         * @param loc
         * @return eg return 1 for character blocking path, return 0 for clear path
         */
        public int getMovementCost(Pair loc);
}
