package asf.dungeon.view;

import asf.dungeon.board.Token;

/**
 * Created by danny on 10/26/14.
 */
public interface TokenControl {
        public void start(TokenSpatial tokenSpatial);

        public void end();

        public void update(float delta);

        public Token getToken();
}
