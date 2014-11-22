package asf.dungeon.view;

import asf.dungeon.model.Pair;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
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
        private Token attackerToken;
        private TokenSpatial targetTokenSpatial;
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

        public void shootProjectile(Token attacker, Token targetToken, Pair destLoc) {
                if (targetToken != null)
                        shootProjectile(attacker, targetToken);
                else
                        shootProjectile(attacker, destLoc);

        }

        public void shootProjectile(Token attacker, Pair destLoc) {
                this.attackerToken = attacker;
                targetTokenSpatial = null;
                this.destLoc.set(destLoc);
                world.getWorldCoords(attacker.getMove().getLocationFloatX(), attacker.getMove().getLocationFloatY(), worldStartLoc);
                world.getWorldCoords(destLoc.x, destLoc.y, worldDestLoc);

                worldMoveDir.set(worldDestLoc).sub(worldStartLoc);
                UtMath.normalize(worldMoveDir);
                rotation.setFromCross(Vector3.Z, worldMoveDir);

                translation.set(worldStartLoc);
        }

        public void shootProjectile(Token attacker, Token targetToken) {
                this.attackerToken = attacker;
                targetTokenSpatial = world.getTokenSpatial(targetToken);

                world.getWorldCoords(attacker.getMove().getLocationFloatX(), attacker.getMove().getLocationFloatY(), worldStartLoc);
                if (targetToken.getMove() == null) {
                        world.getWorldCoords(targetToken.getLocation().x, targetToken.getLocation().y, worldDestLoc);
                } else {
                        world.getWorldCoords(targetToken.getMove().getLocationFloatX(), targetToken.getMove().getLocationFloatY(), worldDestLoc);
                }


                worldMoveDir.set(worldDestLoc).sub(worldStartLoc);
                UtMath.normalize(worldMoveDir);
                rotation.setFromCross(Vector3.Z, worldMoveDir);

                translation.set(worldStartLoc);

                //Gdx.app.log("ProjectileSpatial", "shoot: " + destLoc + " start: " + translation + "end: " + worldDestLoc + " moveDir: " + worldMoveDir);
        }


        @Override
        public void update(final float delta) {
                if (attackerToken == null || !attackerToken.getAttack().hasProjectile()) {
                        reset();
                        return;
                } else if (attackerToken.getAttack().getEffectiveProjectileU() < 0) {
                        return;
                }

                if (targetTokenSpatial != null)
                        visU = targetTokenSpatial.visU;
                else {
                        if (attackerToken.getFloorMap() == world.getLocalPlayerToken().getFloorMap() && world.getLocalPlayerToken().getFogMapping() != null) {
                                FogMap fogMap = world.getLocalPlayerToken().getFogMapping().getFogMap(world.getLocalPlayerToken().getFloorMap());

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
                                visU = 1;
                        }
                }


                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
                        colorAttribute.color.a = visU;
                }

                if (animController != null)
                        animController.update(delta);

                //world.getWorldCoords(targetToken.getLocationFloatX(), targetToken.getLocationFloatY(), worldDestLoc);
                //worldMoveDir.set(worldDestLoc).sub(worldStartLoc);
                //MoreMath.normalize(worldMoveDir);
                //rotation.setFromCross(Vector3.Z, worldMoveDir);

                UtMath.interpolate(
                        Interpolation.pow3,
                        attackerToken.getAttack().getEffectiveProjectileU(),
                        worldStartLoc,
                        worldDestLoc,
                        translation);

                //Gdx.app.log("ProjectileSpatial", "update: " + translation);


        }

        @Override
        public void render(float delta) {
                if (attackerToken == null || attackerToken.getAttack().getEffectiveProjectileU() < 0) {
                        return;
                }

                if (visU <= 0) {
                        return;
                }

                modelInstance.transform.set(
                        translation.x + translationBase.x, translation.y + translationBase.y, translation.z + translationBase.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        scale.x, scale.y, scale.z
                );

                //Gdx.app.log("ProjectileSpatial", "render: " + translation);

                world.modelBatch.render(modelInstance, environment);

        }

        public void reset() {
                attackerToken = null;
                visU = 0;
        }

        public boolean isActive() {
                return attackerToken != null;
        }

        @Override
        public void dispose() {

                initialized = false;
        }

        @Override
        public boolean isInitialized() {
                return initialized;
        }

}
