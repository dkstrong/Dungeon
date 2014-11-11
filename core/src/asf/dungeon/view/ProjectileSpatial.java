package asf.dungeon.view;

import asf.dungeon.model.CharacterToken;
import asf.dungeon.model.FogMap;
import asf.dungeon.model.FogState;
import asf.dungeon.model.Pair;
import asf.dungeon.utility.MoreMath;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by danny on 10/20/14.
 */
public class ProjectileSpatial implements Spatial {

        private boolean initialized = false;
        public final String assetLocation;
        public Environment environment;
        public ModelInstance modelInstance;
        public AnimationController animController;
        public final Vector3 translationBase = new Vector3();
        public final Vector3 translation = new Vector3();
        public final Quaternion rotation = new Quaternion();
        public final Vector3 scale = new Vector3(1, 1, 1);
        protected DungeonWorld world;
        protected float visU = 0; // how visible this object is, 0 = not drawn, 1 = fully visible, inbetween for partially visible

        // current active
        private CharacterToken sourceToken;
        private final Pair destLoc = new Pair();
        private final Vector3 worldMoveDir = new Vector3(), worldStartLoc = new Vector3(), worldDestLoc = new Vector3();

        public ProjectileSpatial(DungeonWorld world, Environment environment) {
                this.world = world;
                this.assetLocation = "Models/Projectiles/Arrow.g3db";
                this.environment = environment;
        }

        @Override
        public void preload(DungeonWorld world) {
                if (assetLocation != null)
                        world.assetManager.load(assetLocation, Model.class);
        }

        @Override
        public void init(AssetManager assetManager) {

                if (assetLocation != null) {
                        if (!assetManager.isLoaded(assetLocation, Model.class))
                                throw new Error("asset not loaded");

                        Model model = assetManager.get(assetLocation);
                        modelInstance = new ModelInstance(model);
                }


                initialized = true;

                if (modelInstance.animations.size > 0) {
                        animController = new AnimationController(modelInstance);
                        //GdxInfo.model(modelInstance.model);
                        //animController.setAnimation(modelInstance.animations.get(0).id, 100);
                }

                for (Material material : modelInstance.materials) {
                        material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                }

                translationBase.set(0, 4, 0);


        }

        public void shootProjectile(CharacterToken source, Pair destLoc) {
                this.sourceToken = source;
                this.destLoc.set(destLoc);
                world.getWorldCoords(source.getLocationFloatX(), source.getLocationFloatY(), worldStartLoc);
                world.getWorldCoords(destLoc, worldDestLoc);
                worldMoveDir.set(worldDestLoc).sub(worldStartLoc);
                MoreMath.normalize(worldMoveDir);
                translation.set(worldStartLoc);
                rotation.setFromCross(Vector3.Z, worldMoveDir);
                //Gdx.app.log("ProjectileSpatial", "shoot: " + destLoc + " start: " + translation + "end: " + worldDestLoc + " moveDir: " + worldMoveDir);
        }


        @Override
        public void update(final float delta) {
                if (sourceToken == null || !sourceToken.hasProjectile()) {
                        reset();
                        return;
                } else if (sourceToken.getEffectiveProjectileU() < 0) {
                        return;
                }

                // this token is only visible if on the same floor
                // as the local player, and if fog mapping is enabled
                if (sourceToken.getFloorMap() == world.getLocalPlayerToken().getFloorMap() && world.getLocalPlayerToken().isFogMappingEnabled()) {
                        FogMap fogMap = world.getLocalPlayerToken().getFogMap(world.getLocalPlayerToken().getFloorMap());
                        if (fogMap != null) {
                                FogState fogState = fogMap.getFogState(destLoc.x, destLoc.y);
                                if (fogState == FogState.Visible) {
                                        visU += delta * .5f;
                                } else {
                                        visU -= delta * .75f;
                                }
                                visU = MathUtils.clamp(visU, 0, 1);
                        }
                } else {
                        visU = 1; // force rendering
                }


                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
                        colorAttribute.color.a = visU;
                }

                if (animController != null) {
                        animController.update(delta);
                }


                MoreMath.interpolate(
                        Interpolation.pow3,
                        sourceToken.getEffectiveProjectileU(),
                        worldStartLoc,
                        worldDestLoc,
                        translation);

                //Gdx.app.log("ProjectileSpatial", "update: " + translation);


        }

        @Override
        public void render(float delta) {
                if (sourceToken == null || sourceToken.getEffectiveProjectileU() < 0) {
                        return;
                }

                if (world.getLocalPlayerToken().isFogMappingEnabled()) {
                        if (visU <= 0) {
                                return;
                        }
                }
                modelInstance.transform.set(
                        translation.x + translationBase.x, translation.y + translationBase.y, translation.z + translationBase.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        scale.x, scale.y, scale.z
                );


                world.modelBatch.render(modelInstance, environment);

        }

        public void reset() {
                sourceToken = null;
                visU = 0;
        }

        public boolean isActive() {
                return sourceToken != null;
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

        @Override
        public boolean isInitialized() {
                return initialized;
        }

}
