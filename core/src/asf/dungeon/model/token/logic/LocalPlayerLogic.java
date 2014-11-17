package asf.dungeon.model.token.logic;

import asf.dungeon.model.Direction;
import asf.dungeon.model.token.Token;

/**
 * Created by danny on 10/26/14.
 */
public class LocalPlayerLogic implements Logic {

        private Token token;
        private int team;

        public LocalPlayerLogic(int team) {
                this.token = token;
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

                return false;
        }
}
