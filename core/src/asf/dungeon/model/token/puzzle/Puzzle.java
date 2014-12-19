package asf.dungeon.model.token.puzzle;

import asf.dungeon.model.Dungeon;

/**
 * Created by Daniel Strong on 12/18/2014.
 */
public interface Puzzle {
        public void checkPuzzle(Dungeon dungeon);
        public boolean isSolved();
}
