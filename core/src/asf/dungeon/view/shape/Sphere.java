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
public final class Sphere  implements Shape{

        private static final Vector3 position = new Vector3();
        private final Vector3 center = new Vector3();
        private final Vector3 dimensions = new Vector3();
        private float radius;

        public Sphere(float radius) {
                set(radius);
        }

        public void set(float radius){
                center.set(0,0,0);
                dimensions.set(radius*2f,radius*2f, radius*2f);
                this.radius = radius;
        }

        @Override
        public void setFromModelInstance(ModelInstance modelInstance) {
                BoundingBox bounds = new BoundingBox();
                modelInstance.calculateBoundingBox(bounds);
                bounds.getCenter(center);
                bounds.getDimensions(dimensions);
                radius = dimensions.len()/2f;
        }


        @Override
        public boolean isVisible(Matrix4 transform, Camera cam) {
                return cam.frustum.sphereInFrustum(transform.getTranslation(position).add(center), radius);
        }

        @Override
        public boolean isVisible(Vector3 translation, Camera cam) {
                return cam.frustum.sphereInFrustum(position.set(translation).add(center), radius);
        }

        @Override
        public float intersects(Matrix4 transform, Ray ray) {
                transform.getTranslation(position).add(center);
                final float len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
                if (len < 0f)
                        return -1f;
                float dist2 = position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);
                return (dist2 <= radius * radius) ? dist2 : -1f;
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
