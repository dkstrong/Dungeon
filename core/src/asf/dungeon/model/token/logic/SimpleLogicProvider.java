package asf.dungeon.model.token.logic;



import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.Tile;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by danny on 10/23/14.
 */
public class SimpleLogicProvider implements LogicProvider {

        private Token token;
        private Dungeon dungeon;


        @Override
        public void setToken(Token token) {
                this.token = token;
                dungeon = token.dungeon;

        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {

                if (token.isLocatedAt(token.getCommand().getLocation())) {
                        int x = MathUtils.random.nextInt(token.getFloorMap().getWidth());
                        int y = MathUtils.random.nextInt(token.getFloorMap().getHeight());
                        Tile tile = token.getFloorMap().getTile(x, y);
                        if (tile != null && !tile.isBlockMovement() && !tile.isStairs()) {
                                token.getCommand().setLocation(x,y);

                        }
                }
                return false;
        }
}
