package asf.dungeon.view;

import asf.dungeon.model.FxId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
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
public class Pooled3dModelSpatial implements Spatial, FxManager.PooledFx {

        public ModelInstance modelInstance;
        public AnimationController animController;
        public final Vector3 translationBase = new Vector3();
        public final Vector3 translation = new Vector3();
        public final Quaternion rotation = new Quaternion();
        public final Vector3 scale = new Vector3(1, 1, 1);
        protected DungeonWorld world;
        protected float visU = 0; // how visible this object is, 0 = not drawn, 1 = fully visible, inbetween for partially visible

        // current active
        private FxId fxId;
        private int mode; // 1 = static location on worldDestLoc, 2 =  follow targetTokenSpatial, 3 = projectile

        private float duration;

        private Token attackerToken;
        private TokenSpatial targetTokenSpatial;
        private final Pair destLoc = new Pair();
        private final Vector3 worldMoveDir = new Vector3(), worldStartLoc = new Vector3(), worldDestLoc = new Vector3();


        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
        }

        @Override
        public void init(AssetManager assetManager) {
        }

        private void setModel(){
                if(modelInstance != null)
                         return;
                Model model = world.fxManager.getModel(fxId);
                modelInstance = new ModelInstance(model);
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

        @Override
        public void set(FxId fxId, Pair location, float duration){
                this.fxId = fxId;
                setModel();
                mode = 1;
                destLoc.set(location);
                targetTokenSpatial = null;
                this.duration = duration;
                world.getWorldCoords(location, worldDestLoc);
                translation.set(worldDestLoc);
                rotation.idt();
        }

        @Override
        public void set(FxId fxId, TokenSpatial followTokenSpatial, float duration){
                this.fxId = fxId;
                setModel();
                mode = 2;
                this.targetTokenSpatial =followTokenSpatial;
                this.duration = duration;
        }


        @Override
        public void set(FxId fxId, Token attacker, Token target, Pair destLoc) {
                this.fxId = fxId;
                setModel();
                this.mode = 3;
                this.attackerToken = attacker;
                this.destLoc.set(destLoc);
                world.getWorldCoords(attacker.getMove().getLocationFloatX(), attacker.getMove().getLocationFloatY(), worldStartLoc);
                if (target == null) {
                        targetTokenSpatial = null;
                        world.getWorldCoords(destLoc.x, destLoc.y, worldDestLoc);
                } else {
                        targetTokenSpatial = world.getTokenSpatial(target);
                        if (target.getMove() == null) world.getWorldCoords(target.getLocation().x, target.getLocation().y, worldDestLoc);
                        else world.getWorldCoords(target.getMove().getLocationFloatX(), target.getMove().getLocationFloatY(), worldDestLoc);

                }
                worldMoveDir.set(worldDestLoc).sub(worldStartLoc);
                UtMath.normalize(worldMoveDir);
                rotation.setFromCross(Vector3.Z, worldMoveDir);
                translation.set(worldStartLoc);

        }


        @Override
        public void update(final float delta) {

                if(mode == 1 || mode == 2){
                        duration -= delta;
                        if(duration <=0){
                                deactivate();
                                return;
                        }

                        if(targetTokenSpatial != null){
                                translation.set(targetTokenSpatial.translation);
                                //rotation.set(targetTokenSpatial.rotation) // TODO: do i want to follow rotation too?
                        }

                }else if(mode == 3){
                        if (attackerToken == null || !attackerToken.getAttack().hasProjectile()) {
                                deactivate();
                                return;
                        } else if (attackerToken.getAttack().getEffectiveProjectileU() < 0) {
                                return;
                        }


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

                //
                // Common update code (visibility of material, animation update)

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


        }

        @Override
        public void render(float delta) {
                if( visU <=0){
                        return;
                }

                if(mode == 3){
                        // the projectile actually spawns at the beginning of the attack animation leaving about
                        // a half second of time between projectile spawna nd when it should show up
                        // we check the effective projectile u to make sure it is positive to make sure that the
                        // projectile should be visible
                        if (attackerToken == null || attackerToken.getAttack().getEffectiveProjectileU() < 0) {
                                return;
                        }
                }

                modelInstance.transform.set(
                        translation.x + translationBase.x, translation.y + translationBase.y, translation.z + translationBase.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        scale.x, scale.y, scale.z
                );

                //Gdx.app.log("ProjectileSpatial", "render: " + translation);

                world.modelBatch.render(modelInstance, world.environment);

        }

        public void deactivate() {
                mode = 0;
                attackerToken = null;
                visU = 0;
                duration =0;
        }

        public boolean isActive() {
                return mode != 0;
        }

        @Override
        public TokenSpatial getTokenSpatial() {
                return targetTokenSpatial;
        }

        @Override
        public FxId getFxId() {
                return fxId;
        }

        @Override
        public int getMode() {
                return mode;
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isInitialized() {
                return isActive();
        }

}
