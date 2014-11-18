package asf.dungeon.model.token.logic;



import asf.dungeon.model.Direction;
import asf.dungeon.model.Tile;
import asf.dungeon.model.token.Token;

/**
 * Created by danny on 10/23/14.
 */
public class ExplorerLogic implements Logic {

        private Token token;
        private int team;

        public ExplorerLogic( int team) {
                this.team = team;
        }

        @Override
        public void setToken(Token token) {
                this.token = token;
        }

        @Override
        public int getTeam() {
                return team;
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {

                if (token.isLocatedAt(token.getCommand().getLocation())) {
                        int x = token.dungeon.rand.random.nextInt(token.getFloorMap().getWidth());
                        int y = token.dungeon.rand.random.nextInt(token.getFloorMap().getHeight());
                        Tile tile = token.getFloorMap().getTile(x, y);
                        if (tile != null && !tile.isBlockMovement() && !tile.isStairs()) {
                                token.getCommand().setLocation(x,y);

                        }
                }
                return false;
        }
}
