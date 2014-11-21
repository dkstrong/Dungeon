package asf.dungeon.model.token.logic;


import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.utils.Array;

/**
 * chases and attacks any character tokens not on the same team.
 *
 * mainly used for balance testing
 *
 */
public class FullAgroLogic implements Logic {

        private Token token;
        private int team;

        public FullAgroLogic( int team) {
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

                if(token.getCommand().getTargetToken()== null || token.getCommand().getTargetToken().getDamage().isDead()){
                        int targetTeam = 1;
                        if(team == targetTeam) targetTeam = 0;

                        Array<Token> tokens = token.getFloorMap().getTokensOnTeam(targetTeam);
                        for (Token targetToken : tokens) {
                                token.getCommand().setTargetToken(targetToken);
                                break;
                        }
                }

                return false;
        }
}
