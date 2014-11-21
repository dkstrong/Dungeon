package asf.dungeon.model.token.logic;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/20/2014.
 */
public class AiLogic implements Logic {
        private Token token;
        private int team;

        public AiLogic(int team) {
                this.team = team;
        }

        @Override
        public void setToken(Token token) {

        }

        @Override
        public int getTeam() {
                return team;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                return false;
        }
}
