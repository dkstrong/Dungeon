package asf.dungeon.board.logic;


import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.Dungeon;
import asf.dungeon.board.FloorTile;
import asf.dungeon.board.Pair;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by danny on 10/23/14.
 */
public class SimpleLogicProvider implements LogicProvider {

        private CharacterToken token;
        private Dungeon dungeon;


        @Override
        public void setToken(CharacterToken token) {
                this.token = token;
                dungeon = token.dungeon;

        }

        @Override
        public void updateLogic(float delta) {
                if (!token.hasMoveTarget()) {
                        int x = MathUtils.random.nextInt(token.getFloorMap().getWidth());
                        int y = MathUtils.random.nextInt(token.getFloorMap().getHeight());
                        FloorTile tile = token.getFloorMap().getTile(x, y);
                        if (!tile.isBlockMovement() && !tile.isStairs()) {
                                token.setMoveTarget(new Pair(x, y));
                        }
                }
        }
}
