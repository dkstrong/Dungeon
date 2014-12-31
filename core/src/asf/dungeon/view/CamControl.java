package asf.dungeon.view;

import asf.dungeon.utility.UtMath;
import asf.dungeon.view.token.AbstractTokenSpatial;
import com.badlogic.gdx.graphics.Camera;

/**
 * Created by Daniel Strong on 12/30/14.
 */
public interface CamControl {
        public void resize(int width, int height);
        public void update(float delta);

        public void setChaseTarget(AbstractTokenSpatial chaseTarget);
        public AbstractTokenSpatial getChaseTarget();

        public float getZoom();
        public void setZoom(float amount);

        public boolean isChasing();
        public void setChasing(boolean chasing);
        public void drag(int screenX, int screenY);

        public Camera getCamera();
}
