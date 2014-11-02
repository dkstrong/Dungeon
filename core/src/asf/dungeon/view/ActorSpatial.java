package asf.dungeon.view;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import asf.dungeon.view.shape.Shape;

/**
 * Created by danny on 10/20/14.
 */
public class ActorSpatial implements Spatial {

        private boolean initialized = false;
        private DungeonWorld world;
        public final String assetLocation;
        public ModelInstance modelInstance;
        public Shape shape;
        public AnimationController animController;
        public Environment environment;
        public CullType cullType = CullType.Dynamic;
        public final Vector3 translationBase = new Vector3();
        public final Vector3 translation = new Vector3();
        public final Quaternion rotation = new Quaternion();
        public final Vector3 scale = new Vector3(1, 1, 1);
        protected ActorControl actorControl;

        public ActorSpatial(ModelInstance modelInstance, Shape shape, Environment environment) {
                this.modelInstance = modelInstance;
                this.assetLocation = null;
                this.shape = shape;
                this.environment = environment;
        }

        public ActorSpatial(String assetLocation, Shape shape, Environment environment) {
                this.assetLocation = assetLocation;
                this.shape = shape;
                this.environment = environment;
        }

        public <T extends ActorControl> T setControl(T gameControl) {
                this.actorControl = gameControl;
                if (initialized)
                        actorControl.start(this);
                return gameControl;
        }

        public ActorControl getControl(){
                return actorControl;
        }

        public <T extends ActorControl> T removeControl() {
                ActorControl actorControlTemp = actorControl;
                actorControl = null;
                return (T) actorControlTemp;
        }

        public void preload(DungeonWorld world){
                this.world  = world;
                if(assetLocation != null)
                        world.assetManager.load(assetLocation, Model.class);
        }

        public void init(AssetManager assetManager) {

                if (assetLocation != null) {
                        if (!assetManager.isLoaded(assetLocation, Model.class))
                                throw new Error("Asset not loaded");

                        Model model = assetManager.get(assetLocation);
                        modelInstance = new ModelInstance(model);
                }

                initialized = true;

                if (shape != null) {
                        shape.setFromModelInstance(modelInstance);
                }

                if (modelInstance.animations.size > 0) {
                        animController = new AnimationController(modelInstance);
                        //GdxInfo.model(modelInstance.model);
                        //animController.setAnimation(modelInstance.animations.get(0).id, 100);
                }

                if (actorControl != null)
                        actorControl.start(this);
        }


        public void update(final float delta) {

                if (actorControl != null)
                        actorControl.update(delta);

                if (animController != null) {
                        animController.update(delta);
                }

        }

        public void render(float delta) {
                modelInstance.transform.set(
                        translation.x + translationBase.x, translation.y + translationBase.y, translation.z + translationBase.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        scale.x, scale.y, scale.z
                );



                if (!isVisible(world.modelBatch.getCamera()))
                        return;


                world.modelBatch.render(modelInstance, environment);
        }

        private boolean isVisible(Camera cam) {
                if (cullType == CullType.Always)
                        return false;
                if (cullType == CullType.Never || shape == null)
                        return true;
                return shape.isVisible(modelInstance.transform, cam);
        }

        public CullType getCullType() {
                return cullType;
        }

        public void setCullType(CullType cullType) {
                this.cullType = cullType;
        }

        /**
         * @return -1 on no intersection,
         * or when there is an intersection: the squared distance between the center of this
         * object and the point on the ray closest to this object when there is intersection.
         */
        public float intersects(Ray ray) {
                return shape == null ? -1f : shape.intersects(modelInstance.transform, ray);
        }

        @Override
        public void dispose() {
                // if modelIntance isnt loaded from the AssetManager
                // then we need to dispose the model ourseleves
                if (assetLocation == null)
                        if (modelInstance != null)
                                modelInstance.model.dispose();
                initialized = false;
        }

        public boolean isInitialized(){
                return initialized;
        }

}
