package asf.dungeon.view;

import asf.dungeon.model.token.Token;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Daniel Strong on 12/19/2014.
 */
public abstract class AbstractTokenSpatial implements Spatial{

        protected final Vector3 translation = new Vector3();
        protected final Quaternion rotation = new Quaternion();
        protected float visU = 0; // how visible this object is, 0 = not drawn, 1 = fully visible, inbetween for partially visible

        /**
         * The token that this spatial is a view for
         * @return
         */
        public abstract Token getToken();
        /**
         * @return -1 on no intersection,
         * or when there is an intersection: the squared distance between the center of this
         * object and the point on the ray closest to this object when there is intersection.
         */
        public abstract float intersects(Ray ray);
}
