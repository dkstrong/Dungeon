package asf.dungeon.model.token.puzzle;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.utils.IdentityMap;

/**
 * a generic combination puzzle, to solve all puzzle pieces must have the correct boolean value.
 *
 * Combination Puzzle uses generic Puzzle Pieces which allows for puzzle pieces of different kinds
 *
 * for instance a mix of a pressure plate and torch pieces
 *
 * Created by Daniel Strong on 12/18/2014.
 */
public abstract class CombinationPuzzle implements Puzzle{
        /**
         * if the provided torches have the corresponding ignited values, then onSolved() will be perfomed,
         * otherwise onUnsolved() with be performed
         */
        private final IdentityMap<PuzzlePiece, Boolean> torches = new IdentityMap <PuzzlePiece, Boolean>(3);

        public void addPiece(Token token,  boolean requiredValue){
                PuzzlePiece puzzlePiece = token.get(PuzzlePiece.class);
                if(puzzlePiece == null) throw new IllegalArgumentException("token must have puzzle piece");
                torches.put(puzzlePiece, requiredValue);
        }

        @Override
        public void checkPuzzle(Dungeon dungeon) {
                for (IdentityMap.Entry<PuzzlePiece, Boolean> entry : torches) {
                        if(entry.key.getValue() != entry.value){
                                if(isSolved())
                                        onUnsolved();
                                return;
                        }
                }
                if(!isSolved())
                        onSolved();
        }

        public abstract void onSolved();

        protected abstract void onUnsolved();
}
