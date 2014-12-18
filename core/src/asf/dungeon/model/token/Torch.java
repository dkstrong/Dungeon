package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
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

        public void setIgnited(boolean ignited){
                this.ignited = ignited;
                if(puzzle!=null)
                        puzzle.checkPuzzle(token.dungeon);
        }

        public void toggleIgnited(){
                ignited = !ignited;
                if(puzzle!=null)
                        puzzle.checkPuzzle(token.dungeon);
        }

        public boolean isIgnited() {
                return ignited;
        }

        public static interface Puzzle{
                public void checkPuzzle(Dungeon dungeon);
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
        }


}
