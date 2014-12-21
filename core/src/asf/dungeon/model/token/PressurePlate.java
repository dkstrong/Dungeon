package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.puzzle.Puzzle;
import asf.dungeon.model.token.puzzle.PuzzlePiece;

/**
 * Created by Daniel Strong on 12/17/2014.
 */
public class PressurePlate implements TokenComponent, PuzzlePiece {
        private Token token;
        private boolean pressed;
        private Puzzle puzzle;

        public PressurePlate(Token token, Puzzle puzzle) {
                this.token = token;
                this.puzzle = puzzle;
                token.setBlocksPathing(false);
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
        }

        @Override
        public boolean update(float delta) {
                for (Token t : token.floorMap.getTokens()) {
                        if(t!= token && t.isLocatedAt(token.location)){
                                if(!pressed){
                                        pressed = true;
                                        if(puzzle != null)
                                                puzzle.checkPuzzle(token.dungeon);
                                }
                                return false;
                        }
                }
                if(pressed){
                        pressed = false;
                        if(puzzle != null)
                                puzzle.checkPuzzle(token.dungeon);
                }

                return false;
        }

        @Override
        public boolean getValue() {
                return pressed;
        }

        public Puzzle getPuzzle() {
                return puzzle;
        }


}
