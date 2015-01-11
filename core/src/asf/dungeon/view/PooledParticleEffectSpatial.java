package asf.dungeon.view;

import asf.dungeon.model.FxId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.shape.Shape;
import asf.dungeon.view.shape.Sphere;
import asf.dungeon.view.token.AbstractTokenSpatial;
import asf.dungeon.view.token.CharacterTokenSpatial;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Danny on 11/21/2014.
 */
public class PooledParticleEffectSpatial implements Spatial, FxManager.PooledFx {
        private DungeonWorld world;
        private ParticleEffect effect;
        private RegularEmitter reg;

        private Shape shape;

        //current active
        private FxId fxId;
        private int mode;
        protected AbstractTokenSpatial tokenSpatial;
        private final Matrix4 transformMatrix = new Matrix4();
        private final Vector3 translation = new Vector3();
        private final Quaternion rotation = new Quaternion();
        private float visU;

        protected float duration = 0;

        private Token attackerToken;
        private CharacterTokenSpatial attackerTokenSpatial;
        private final Pair destLoc = new Pair();
        private final Vector3 worldMoveDir = new Vector3(), worldStartLoc = new Vector3(), worldDestLoc = new Vector3();

        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
        }

        private void setEffect() {
                if (effect != null)
                        return;

                shape = new Sphere(5);

                ParticleEffect srcEffect = world.fxManager.getParticleEffect(fxId);

                effect = srcEffect.copy();
                effect.init();
                reg = (RegularEmitter) effect.getControllers().first().emitter;
                float delta = effect.getControllers().first().deltaTime;

                //System.out.println(delta);
        }

        @Override
        public void set(FxId fxId, float x, float y, float z, float duration) {
                this.fxId = fxId;
                setEffect();
                mode = 1;
                world.getMapCoords(x,y,z,destLoc);
                tokenSpatial = null;
                this.duration = duration;

                translation.set(x,y,z);
                rotation.idt();

                commonSet();
        }

        @Override
        public void set(FxId fxId, AbstractTokenSpatial followTokenSpatial, float duration) {
                this.fxId = fxId;
                setEffect();
                mode = 2;
                this.tokenSpatial = followTokenSpatial;
                this.duration = duration;
                rotation.idt();
                commonSet();
        }

        @Override
        public void set(FxId fxId, Token attacker, Token target, Pair destLoc) {
                this.fxId = fxId;
                setEffect();
                this.mode = 3;
                this.attackerToken = attacker;
                attackerTokenSpatial = (CharacterTokenSpatial)world.getTokenSpatial(attackerToken);
                this.destLoc.set(destLoc);
                attackerTokenSpatial.getWeaponAttachmentTranslation(worldStartLoc);
                if(worldStartLoc.y == 0)
                        worldStartLoc.y = 4;
                if (target == null) {
                        tokenSpatial = null;
                        world.getWorldCoords(destLoc.x, destLoc.y, worldDestLoc);
                } else {
                        tokenSpatial = world.getTokenSpatial(target);
                        if (target.move == null) world.getWorldCoords(target.location.x, target.location.y, worldDestLoc);
                        else world.getWorldCoords(target.move.getFloatLocation(), worldDestLoc);
                }
                worldDestLoc.y = worldStartLoc.y;
                worldMoveDir.set(worldDestLoc).sub(worldStartLoc).nor();

                transformMatrix.idt();
                //Quaternion q = new Quaternion().setFromCross(Vector3.Z, worldMoveDir);
                //transformMatrix.rotate(q);

                translation.set(worldStartLoc);
                rotation.setFromCross(Vector3.Z, worldMoveDir);
                commonSet();

        }

        private void commonSet() {
                //reg.setContinuous(!Float.isNaN(duration));
                reg.setEmissionMode(RegularEmitter.EmissionMode.Enabled);
                effect.start();
        }

        /**
         * forces isActive() == false
         */
        public void deactivate() {
                if (mode > 0) {
                        effect.reset();
                        mode = 0;
                        tokenSpatial = null;
                        visU = 0;
                }

        }

        public boolean isActive() {
                return mode > 0;
        }

        @Override
        public AbstractTokenSpatial getTokenSpatial() {
                return tokenSpatial;
        }

        @Override
        public FxId getFxId() {
                return fxId;
        }

        @Override
        public int getMode() {
                return mode;
        }


        private float deltaAccumulate;

        @Override
        public void update(float delta) {
                if (reg.isComplete()) {
                        //System.out.println("complete");
                        deactivate();
                        return;
                }


                if (mode == 1 || mode == 2) {
                        duration -= delta;
                        if (duration <= 0) {
                                reg.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd);
                        }

                        if (tokenSpatial != null) {
                                translation.set(tokenSpatial.translation);

                                if(tokenSpatial.getToken().damage != null && tokenSpatial.getToken().damage.isDead()){
                                        duration = 0;
                                }
                        }


                } else if (mode == 3) {
                        if (attackerToken == null || !attackerToken.attack.hasProjectile() || attackerToken.damage.isDead()) {
                                //Gdx.app.log("Pooled PE","trigger end fx");
                                reg.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd);
                        } else if (attackerToken.attack.getEffectiveProjectileU() < 0) {
                                return;
                        }

                        UtMath.interpolate(
                                Interpolation.pow3,
                                attackerToken.attack.getEffectiveProjectileU(),
                                worldStartLoc,
                                worldDestLoc,
                                translation);
                }

                //
                // Common update code (apply transform, visibility of material, animation update)

                FogState fogState;
                if (tokenSpatial != null) {
                        if (world.getLocalPlayerToken() != null && world.getLocalPlayerToken().fogMapping != null) {
                                fogState = world.getLocalPlayerToken().fogMapping.getCurrentFogMap().getFogState(tokenSpatial.getToken().location.x, tokenSpatial.getToken().location.y);
                        } else {
                                fogState = FogState.Visible;
                        }
                        visU = tokenSpatial.visU;

                } else {
                        if (world.getLocalPlayerToken() != null && world.getLocalPlayerToken().fogMapping != null) {
                                fogState = world.getLocalPlayerToken().fogMapping.getCurrentFogMap().getFogState(destLoc.x, destLoc.y);
                        } else {
                                fogState = FogState.Visible;
                        }

                        if(fogState == FogState.Visible || fogState == FogState.MagicMapped)visU += delta * .65f;
                        else visU -= delta * .75f;
                        visU = MathUtils.clamp(visU, 0, 1);
                }

                // TODO: can visU value be applied to particle effects?

                transformMatrix.set(
                        translation.x , translation.y , translation.z ,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        1, 1, 1
                );
                effect.setTransform(transformMatrix);

                // for god knows why the particle effect system is on a fixed timestep of 60 fps.
                // this hack should help limit its update rate to 60 fps
                deltaAccumulate += delta;
                if (deltaAccumulate > 0.01666f) {
                        effect.update();
                        deltaAccumulate -= 0.01666f;
                }


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

                if (!shape.isVisible(transformMatrix, world.cam)) // TODO: this visibility check might not be needed
                        return;


                effect.draw();
        }

        @Override
        public void init(AssetManager assetManager) {

        }

        @Override
        public boolean isInitialized() {
                return isActive(); // note that doing this will cause init() to be called often wehn it is not needed, but since init() is empty this doesnt matter
        }


        @Override
        public void dispose() {

        }
}
