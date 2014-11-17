package asf.dungeon.model.token.logic;


import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.TokenComponent;

/**
 * Created by danny on 10/23/14.
 */
public interface Logic extends TokenComponent{

        public void setToken(Token token);

        public int getTeam();

}
