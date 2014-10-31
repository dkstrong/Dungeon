package asf.dungeon.view.shape;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by danny on 10/20/14.
 */
public class Box implements Shape{
        private final static Vector3 position = new Vector3();
        private final Vector3 center = new Vector3();
        private final Vector3 dimensions = new Vector3();

        @Override
        public void setFromModelInstance(ModelInstance modelInstance) {
                BoundingBox bb = new BoundingBox();
                modelInstance.calculateBoundingBox(bb);
                bb.getCenter(center);
                bb.getDimensions(dimensions);
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
