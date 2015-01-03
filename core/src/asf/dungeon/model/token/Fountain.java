package asf.dungeon.model.token;

import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.token.puzzle.Puzzle;
import asf.dungeon.model.token.puzzle.PuzzlePiece;
import asf.dungeon.model.token.quest.FountainQuest;

/**
 * Created by Danny on 12/1/2014.
 */
public class Fountain implements TokenComponent, PuzzlePiece {
        private final Token token;
        private PotionItem.Type fountainType;
        private Puzzle puzzle;

        public Fountain(Token token, PotionItem.Type fountainType) {
                this(token, fountainType, null);
        }

        public Fountain(Token token, PotionItem.Type fountainType, Puzzle puzzle) {
                this.token = token;
                setFountainType(fountainType);
                this.puzzle = puzzle;
        }

        @Override
        public boolean update(float delta) {

                return false;
        }

        public void consumeFountain(Token target){
                // TODO: this gets called from the user input and does not happen inside the dungeon loop
                // i may need to enque this to happen inside update(), for now it seems to be fine though
                PotionItem.doPotionEffects(target, fountainType);
                Journal journal = target.get(Journal.class);
                if (journal != null)
                        journal.learn(this);
                setFountainType(null);
        }

        public void setFountainType(PotionItem.Type fountainType) {
                this.fountainType = fountainType;
                FountainQuest fq = token.get(FountainQuest.class);
                if(fountainType == null && fq != null){
                        token.remove(fq);
                }else if(fountainType != null && fq == null){
                        token.add(new FountainQuest());
                }
                if(puzzle!=null)
                        puzzle.checkPuzzle(token.dungeon);
        }

        public PotionItem.Type getFountainType() {
                return fountainType;
        }

        public PotionItem.Color getFountainColor(){
                return token.dungeon.getMasterJournal().getPotionColor(fountainType);
        }

        public boolean isConsumed() {
                return fountainType == null;
        }

        @Override
        public boolean getValue() {
                return fountainType == null;
        }
}
