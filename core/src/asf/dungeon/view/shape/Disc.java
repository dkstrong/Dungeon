package asf.dungeon.view.shape;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by danny on 10/20/14.
 */
public class Disc implements Shape{
        private final static Vector3 position = new Vector3();
        private final Vector3 center = new Vector3();
        private final Vector3 dimensions = new Vector3();
        private float radius;

        @Override
        public void setFromModelInstance(ModelInstance modelInstance) {
                BoundingBox bb = new BoundingBox();
                modelInstance.calculateBoundingBox(bb);
                bb.getCenter(center);
                bb.getDimensions(dimensions);
                radius = 0.5f * (dimensions.x > dimensions.z ? dimensions.x : dimensions.z);

        }

        @Override
        public boolean isVisible (Matrix4 transform, Camera cam) {
                return cam.frustum.sphereInFrustum(transform.getTranslation(position).add(center), radius);
        }

        @Override
        public float intersects (Matrix4 transform, Ray ray) {
                transform.getTranslation(position).add(center);
                final float len = (position.y - ray.origin.y) / ray.direction.y;
                final float dist2 = position.dst2(ray.origin.x + len * ray.direction.x, ray.origin.y + len * ray.direction.y, ray.origin.z + len * ray.direction.z);
                return (dist2 < radius * radius) ? dist2 : -1f;
        }

        public float getRadius(){
                return radius;
        }

        public Vector3 getCenter() {
                return center;
        }

        @Override
        public Vector3 getDimensions() {
                return dimensions;
        }
}
