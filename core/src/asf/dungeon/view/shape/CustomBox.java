package asf.dungeon.view.shape;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by danny on 10/21/14.
 */
public class CustomBox implements Shape{
        private final static Vector3 position = new Vector3();
        private final Vector3 center = new Vector3();
        private final Vector3 dimensions = new Vector3();

        public CustomBox() {
        }

        public CustomBox(Vector3 min, Vector3 max){
                dimensions.set(max).sub(min);

                center.x = (max.x + min.x) / 2f;
                center.y = (max.y + min.y) / 2f;
                center.z = (max.z + min.z) / 2f;
        }



        @Override
        public void setFromModelInstance(ModelInstance modelInstance) {
        }

        @Override
        public boolean isVisible(Matrix4 transform, Camera cam) {
                return cam.frustum.boundsInFrustum(transform.getTranslation(position).add(center), dimensions);
        }

        @Override
        public float intersects(Matrix4 transform, Ray ray) {
                transform.getTranslation(position).add(center);
                if (Intersector.intersectRayBoundsFast(ray, position, dimensions)) {
                        final float len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
                        return position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);
                }
                return -1f;
        }

        public Vector3 getCenter() {
                return center;
        }

        @Override
        public Vector3 getDimensions() {
                return dimensions;
        }


}
