package asf.dungeon.model.token.logic;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Sector;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/20/2014.
 */
@Deprecated
public class SleepingLogic implements Logic{

        private Token token;
        private int team;
        private Sector sector;

        public SleepingLogic( int team, Sector sector) {
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
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                Array<Token> tokensInSector = token.getFloorMap().getTokensAt(sector);

                for (Token t : tokensInSector) {
                        if(t.getLogic() != null && t.getLogic().getTeam() != team){

                        }
                }


                return false;
        }
}
