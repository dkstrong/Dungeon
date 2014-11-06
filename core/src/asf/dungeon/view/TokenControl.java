package asf.dungeon.view;

import asf.dungeon.model.Token;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by danny on 10/26/14.
 */
public interface TokenControl extends Disposable {
        public void start(TokenSpatial tokenSpatial);

        public void end();

        public void update(float delta);

        public Token getToken();
}
