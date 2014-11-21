package asf.dungeon.model.token.logic;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/20/2014.
 */
@Deprecated
public class ChasingLogic implements Logic{

        private Token token;
        private int team;
        private Token target;

        public ChasingLogic(int team, Token target) {
                this.team = team;
                this.target = target;

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
                if(token.getCommand().getTargetToken() != target){
                        token.getCommand().setTargetToken(target);
                }else{
                        if(target.getDamage().isDead()){
                                // change to sleeping

                        }
                }
                return false;
        }
}
