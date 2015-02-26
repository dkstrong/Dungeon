package asf.dungeon.view;

import asf.dungeon.model.FxId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.token.AbstractTokenSpatial;
import asf.dungeon.view.token.CharacterTokenSpatial;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
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
        public final Vector3 translation = new Vector3();
        public final Quaternion rotation = new Quaternion();
        public final Vector3 scale = new Vector3(1, 1, 1);
        protected DungeonWorld world;
        protected float visU = 0; // how visible this object is, 0 = not drawn, 1 = fully visible, inbetween for partially visible

        // current active
        private FxId fxId;
        private int mode; // 1 = static location on worldDestLoc, 2 =  follow targetTokenSpatial, 3 = projectile, 4 = spawned projectile ready to shoot

        private float duration;

        private Token attackerToken;
        private CharacterTokenSpatial attackerTokenSpatial;
        private AbstractTokenSpatial targetTokenSpatial;
        private final Pair destLoc = new Pair();
        private final Vector3 worldMoveDir = new Vector3(), worldStartLoc = new Vector3(), worldDestLoc = new Vector3();


        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
        }

        @Override
        public void init(AssetManager assetManager) {
        }

        private void setModel() {
                if (modelInstance != null)
                        return;
                Model model = world.fxManager.getModel(fxId);
                modelInstance = new ModelInstance(model);
                if (modelInstance.animations.size > 0) {
                        animController = new AnimationController(modelInstance);
                        //GdxInfo.model(modelInstance.model);
                        //animController.setAnimation(modelInstance.animations.get(0).id, 100);
                }

                //for (Material material : modelInstance.materials) {
                //        material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                //}
        }

        @Override
        public void set(FxId fxId, float x, float y, float z, float duration) {
                this.fxId = fxId;
                setModel();
                mode = 1;
                world.getMapCoords(x, y, z, destLoc);
                targetTokenSpatial = null;
                this.duration = duration;

                translation.set(x, y, z);
                rotation.idt();
        }

        @Override
        public void set(FxId fxId, AbstractTokenSpatial followTokenSpatial, float duration) {
                this.fxId = fxId;
                setModel();
                mode = 2;
                this.targetTokenSpatial = followTokenSpatial;
                this.duration = duration;
        }


        @Override
        public void set(FxId fxId, Token attacker, Token target, Pair destLoc) {
                this.fxId = fxId;
                setModel();
                this.mode = 3;
                this.attackerToken = attacker;
                attackerTokenSpatial = (CharacterTokenSpatial) world.getTokenSpatial(attackerToken);
                this.destLoc.set(destLoc);
                attackerTokenSpatial.getWeaponAttachmentTranslation(worldStartLoc);
                if (target == null) {
                        targetTokenSpatial = null;
                        world.getWorldCoords(destLoc.x, destLoc.y, worldDestLoc);
                } else {
                        targetTokenSpatial = world.getTokenSpatial(target);
                        if (target.move == null) world.getWorldCoords(target.location.x, target.location.y, worldDestLoc);
                        else world.getWorldCoords(target.move.getFloatLocation(), worldDestLoc);

                }
                worldDestLoc.y = worldStartLoc.y;
                worldMoveDir.set(worldDestLoc).sub(worldStartLoc).nor();
                rotation.setFromCross(Vector3.Z, worldMoveDir);
                translation.set(worldStartLoc);

        }

        @Override
        public void set(FxId fxId, Token attacker, Token target) {
                this.fxId = fxId;
                setModel();
                mode = 4;
                this.attackerToken = attacker;
                attackerTokenSpatial = (CharacterTokenSpatial) world.getTokenSpatial(attackerToken);
                destLoc.set(attacker.location);
                attackerTokenSpatial.getWeaponAttachmentTranslation(worldStartLoc);
                if (target == null) {
                        targetTokenSpatial = null;
                        world.getWorldCoords(destLoc.x, destLoc.y, worldDestLoc);
                } else {
                        targetTokenSpatial = world.getTokenSpatial(target);
                        if (target.move == null) world.getWorldCoords(target.location.x, target.location.y, worldDestLoc);
                        else world.getWorldCoords(target.move.getFloatLocation(), worldDestLoc);

                }
                worldDestLoc.y = worldStartLoc.y;
                worldMoveDir.set(worldDestLoc).sub(worldStartLoc).nor();
                rotation.setFromCross(Vector3.Z, worldMoveDir);

        }


        @Override
        public void update(final float delta) {

                if (mode == 1 || mode == 2) {
                        duration -= delta;
                        if (duration <= 0) {
                                deactivate();
                                return;
                        }

                        if (targetTokenSpatial != null) {
                                translation.set(targetTokenSpatial.translation);
                                //rotation.set(targetTokenSpatial.rotation) // TODO: do i want to follow rotation too?
                                if (targetTokenSpatial.token.damage != null && targetTokenSpatial.token.damage.isDead()) {
                                        duration = 0;
                                }
                        }
                        translation.y = 4; // hover it off the ground some
                } else if (mode == 4) {
                        if (attackerTokenSpatial.getToken().damage != null && attackerTokenSpatial.getToken().damage.isDead()) {
                                deactivate();
                                return;
                        }

                        if (attackerTokenSpatial.getToken().attack == null || !attackerTokenSpatial.getToken().attack.isAttacking()) {
                                deactivate();
                                return;
                        }

                        WeaponItem weapon = attackerTokenSpatial.getToken().attack.getWeapon();
                        if(weapon.bow){
                                // bow
                                attackerTokenSpatial.getWeaponAttachmentTranslation(translation);
                                //rotation.set(attackerTokenSpatial.rotation);
                                worldMoveDir.set(worldDestLoc).sub(translation).nor();
                                rotation.setFromCross(Vector3.Z, worldMoveDir);
                        }else{
                                // staff
                                attackerTokenSpatial.getWeaponAttachmentTranslation(translation);
                                //rotation.set(attackerTokenSpatial.rotation);
                                worldMoveDir.set(worldDestLoc).sub(translation).nor();
                                rotation.setFromCross(Vector3.Z, worldMoveDir);
                        }

                } else if (mode == 3) {
                        if (attackerToken == null || !attackerToken.attack.hasProjectile() || attackerToken.damage.isDead()) {
                                deactivate();
                                return;
                        } else if (attackerToken.attack.getEffectiveProjectileU() < 0) {
                                return;
                        }


                        //world.getWorldCoords(targetToken.getLocationFloatX(), targetToken.getLocationFloatY(), worldDestLoc);
                        //worldMoveDir.set(worldDestLoc).sub(worldStartLoc);
                        //MoreMath.normalize(worldMoveDir);
                        //rotation.setFromCross(Vector3.Z, worldMoveDir);

                        if (attackerToken.attack.getEffectiveProjectileU() > 0.25f) {
                                UtMath.interpolate(
                                        Interpolation.pow3,
                                        attackerToken.attack.getEffectiveProjectileU(),
                                        worldStartLoc,
                                        worldDestLoc,
                                        translation);
                        } else {
                                translation.set(worldStartLoc);
                        }


                        //Gdx.app.log("ProjectileSpatial", "update: " + translation);
                }

                //
                // Common update code (visibility of material, animation update)

                FogState fogState;
                if (targetTokenSpatial != null) {
                        if (world.getLocalPlayerToken() != null && world.getLocalPlayerToken().fogMapping != null) {
                                fogState = world.getLocalPlayerToken().fogMapping.getCurrentFogMap().getFogState(targetTokenSpatial.token.location.x, targetTokenSpatial.token.location.y);
                        } else {
                                fogState = FogState.Visible;
                        }
                        visU = targetTokenSpatial.visU;

                } else {
                        if (world.getLocalPlayerToken() != null && world.getLocalPlayerToken().fogMapping != null) {
                                fogState = world.getLocalPlayerToken().fogMapping.getCurrentFogMap().getFogState(destLoc.x, destLoc.y);
                        } else {
                                fogState = FogState.Visible;
                        }

                        if (fogState == FogState.Visible || fogState == FogState.MagicMapped) visU += delta * .65f;
                        else visU -= delta * .75f;
                        visU = MathUtils.clamp(visU, 0, 1);
                }


                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
                        if (fogState == FogState.MagicMapped) {
                                Color color = colorAttribute.color;
                                color.r = MathUtils.lerp(color.r, visU * .7f, delta);
                                color.g = MathUtils.lerp(color.g, visU * .8f, delta);
                                color.b = visU;
                                color.a = visU;
                        } else {
                                colorAttribute.color.set(visU, visU, visU, 1);
                        }
                }


                if (animController != null)
                        animController.update(delta);


        }

        @Override
        public void render(float delta) {
                if (visU <= 0) {
                        return;
                }

                if (mode == 3) {
                        // the projectile actually spawns at the beginning of the attack animation leaving about
                        // a half second of time between projectile spawna nd when it should show up
                        // we check the effective projectile u to make sure it is positive to make sure that the
                        // projectile should be visible
                        if (attackerToken == null || attackerToken.attack.getEffectiveProjectileU() < 0 || attackerToken.damage.isDead()) {
                                return;
                        }
                }

                modelInstance.transform.set(
                        translation.x, translation.y, translation.z,
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
                duration = 0;
        }

        public boolean isActive() {
                return mode != 0;
        }

        public Token getAttackerToken(){

                return attackerToken;
        }

        @Override
        public AbstractTokenSpatial getTokenSpatial() {
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
