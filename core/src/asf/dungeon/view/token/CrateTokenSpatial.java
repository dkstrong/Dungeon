package asf.dungeon.view.token;

import asf.dungeon.model.FxId;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.BetterModelInstance;
import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Daniel Strong on 12/20/2014.
 */
public class CrateTokenSpatial extends AbstractTokenSpatial {
        private boolean initialized = false;
        private BetterModelInstance modelInstance;

        public CrateTokenSpatial(DungeonWorld world, Token token) {
                super(world, token);
        }

        public void preload(DungeonWorld world) {

                world.assetManager.load(world.assetMappings.getAssetLocation(token.modelId), Model.class);


        }

        public void init(AssetManager assetManager) {
                initialized = true;
                Model model = assetManager.get(world.assetMappings.getAssetLocation(token.modelId));
                modelInstance = new BetterModelInstance(model);

        }

        private float dead = 0;

        public void update(final float delta) {

                FogState fogState = world.floorSpatial.fogMap == null ? FogState.Visible : world.floorSpatial.fogMap.getFogState(token.location.x, token.location.y);
                float minVisU = 0;
                float maxVisU = 1;
                if (fogState == FogState.Visible) {
                        visU += delta * .65f;
                } else {
                        visU -= delta * .75f;
                        // torch can be seen in the fog of war and in magic mapping
                        if (fogState == FogState.Visited || fogState == FogState.MagicMapped) {
                                minVisU = .3f;
                        }
                }

                visU = MathUtils.clamp(visU, minVisU, maxVisU);

                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
                        //colorAttribute.color.a = visU;
                        if (fogState == FogState.MagicMapped) {
                                colorAttribute.color.set(visU * 0.7f, visU * .8f, visU, 1);
                        } else {
                                colorAttribute.color.set(visU, visU, visU, 1);
                        }
                }

                world.getWorldCoords(token.location, translation);
                rotation.set(world.assetMappings.getRotation(token.direction));

                if (minVisU == 0 || visU != minVisU) {
                        // if not fog blocked
                        if(dead ==0 && token.damage.isDead()){
                                world.fxManager.spawnEffect(FxId.CrateWoodExplosion, translation.x,translation.y+3f, translation.z, 1f);
                        }
                }

                if(token.damage.isDead()){
                        dead+=delta;
                }



        }

        @Override
        public void render(float delta) {
                if (visU <= 0 || dead > 0f) return;
                if (world.hudSpatial.isMapViewMode()){
                        if (!world.cam.frustum.sphereInFrustumWithoutNearFar(translation, 5)) return;
                }else if (world.hudSpatial.localPlayerToken != null && world.hudSpatial.localPlayerToken.location.distance(token.location) > 16) return;

                modelInstance.transform.set(
                        translation.x, translation.y, translation.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        1, 1, 1
                );

                world.modelBatch.render(modelInstance, world.environment);
        }

        @Override
        public float intersects(Ray ray) {
                return world.floorSpatial.tileBox.intersects(modelInstance.transform, ray);
        }

        @Override
        public boolean isInitialized() {
                return initialized;
        }

        @Override
        public void dispose() {
                super.dispose();

                initialized = false;
        }
}
