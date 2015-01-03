package asf.dungeon.view.token;

import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.token.SpikeTrap;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.AnimFactory;
import asf.dungeon.utility.BetterAnimationController;
import asf.dungeon.utility.BetterModelInstance;
import asf.dungeon.view.DungeonWorld;
import asf.dungeon.view.Spatial;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by danny on 10/20/14.
 */
public class SpikeTrapTokenSpatial extends AbstractTokenSpatial implements Spatial, SpikeTrap.Listener{

        private boolean initialized = false;
        private BetterModelInstance modelInstance;
        private BetterAnimationController animController;
        private boolean revealed = false;

        public SpikeTrapTokenSpatial(DungeonWorld world, Token token) {
                super(world, token);
        }

        public void preload(DungeonWorld world) {

                world.assetManager.load(world.assetMappings.getAssetLocation(token.getModelId()), Model.class);

                SpikeTrap spikeTrap = token.get(SpikeTrap.class);
                if(spikeTrap == null)
                        throw new IllegalStateException("Token must have a spike trap");
                spikeTrap.setListener(this);

        }

        public void init(AssetManager assetManager) {
                initialized = true;

                Model model = assetManager.get(world.assetMappings.getAssetLocation(token.getModelId()));
                modelInstance = new BetterModelInstance(model);

                AnimFactory.createIdleAnim(modelInstance);
                animController = new BetterAnimationController(modelInstance);
        }


        public void update(final float delta) {
                if(!revealed) return;
                FogState fogState = world.floorSpatial.fogMap==null ? FogState.Visible : world.floorSpatial.fogMap.getFogState(token.location.x, token.location.y);
                float minVisU =0;
                float maxVisU = 1;
                if(fogState == FogState.Visible){
                        visU += delta * .65f;
                }else{
                        visU -= delta * .75f;
                        if (fogState == FogState.Visited || fogState == FogState.MagicMapped) {
                                minVisU = .3f;
                        }
                }
                visU = MathUtils.clamp(visU, minVisU, maxVisU);

                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
                        //colorAttribute.color.a = visU;
                        if(fogState == FogState.MagicMapped){
                                colorAttribute.color.set(visU*0.7f,visU*.8f,visU,1);
                        }else{
                                colorAttribute.color.set(visU,visU,visU,1);
                        }
                }

                world.getWorldCoords(token.getLocation(), translation);
                rotation.set(world.assetMappings.getRotation(token.getDirection()));

                if (animController != null)
                        animController.update(delta);

        }

        public void render(float delta) {
                if(!revealed) return;
                if (visU <= 0) return;
                if (world.hudSpatial.isMapViewMode()){
                        if (!world.cam.frustum.sphereInFrustumWithoutNearFar(translation, 5)) return;
                }else if (world.hudSpatial.localPlayerToken != null && world.hudSpatial.localPlayerToken.getLocation().distance(token.getLocation()) > 16) return;


                modelInstance.transform.set(
                        translation.x, translation.y, translation.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        1, 1, 1
                );

                world.modelBatch.render(modelInstance, world.environment);


        }



        @Override
        public void onSpikeTrapHidden() {
                animController.animate("Idle", 1, 1, null, 0);
                revealed =false;
        }

        @Override
        public void onSpikeTrapTriggered() {
                // TODO: need to slow down the nimation some in the model, for now i wil ljust run it at a lower speed
                // TODO: Spike Trap model also needs to be changed to have 9 larger spikes instead of 16 smaller ones, as it is now its hard to see on android
                animController.animate("Activate", 1, .85f, null, 0);
                revealed =true;
                visU = .85f;
        }

        @Override
        public void onSpikeTrapDefused() {
                animController.animate("Idle", 1, 1, null, 0);
                revealed =true;
                visU = .85f;
        }





        public Token getToken() {
                return token;
        }

        /**
         * @return -1 on no intersection,
         * or when there is an intersection: the squared distance between the center of this
         * object and the point on the ray closest to this object when there is intersection.
         */
        @Override
        public float intersects(Ray ray) {
                return world.floorSpatial.tileBox.intersects(modelInstance.transform, ray);
        }


        @Override
        public void dispose() {
                super.dispose();
                if (this.token != null){
                        token.get(SpikeTrap.class).setListener(null);
                }

                initialized = false;
        }

        public boolean isInitialized() {
                return initialized;
        }


}
