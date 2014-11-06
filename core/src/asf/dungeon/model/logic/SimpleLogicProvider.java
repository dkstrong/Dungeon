package asf.dungeon.model.logic;


import asf.dungeon.model.CharacterToken;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.Tile;
import asf.dungeon.model.Pair;
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
                        Tile tile = token.getFloorMap().getTile(x, y);
                        if (tile != null && !tile.isBlockMovement() && !tile.isStairs()) {
                                token.setMoveTarget(new Pair(x, y));
                        }
                }
        }
}
