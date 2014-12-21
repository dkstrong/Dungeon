package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.puzzle.Puzzle;
import asf.dungeon.model.token.puzzle.PuzzlePiece;

/**
 * Created by Daniel Strong on 12/17/2014.
 */
public class Torch implements TokenComponent, PuzzlePiece {
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
                // TODO: somehow i need to pass this information on to HudSpatial through a listener so it can output something like "you heard a loud clang"
                // whenever the puzzle is solved or unsolved. The way to do this might be more obvious after i add pressure switch puzzle

        }

        public boolean isIgnited() {
                return ignited;
        }

        @Override
        public boolean getValue() {
                return ignited;
        }

        public Puzzle getPuzzle() {
                return puzzle;
        }


}
