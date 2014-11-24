package asf.dungeon.view;

import asf.dungeon.model.FxId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.shape.Shape;
import asf.dungeon.view.shape.Sphere;
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
        protected TokenSpatial tokenSpatial;
        private final Matrix4 transformMatrix = new Matrix4();
        private final Vector3 translationBase = new Vector3();
        private final Vector3 translation = new Vector3();
        private final Quaternion rotation = new Quaternion();
        private float visU;

        protected float duration = 0;

        private Token attackerToken;
        private final Pair destLoc = new Pair();
        private final Vector3 worldMoveDir = new Vector3(), worldStartLoc = new Vector3(), worldDestLoc = new Vector3();

        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
        }

        private void setEffect(){
                if(effect != null)
                        return;

                shape = new Sphere(5);

                ParticleEffect srcEffect = world.getFxManager().getParticleEffect(fxId);

                effect = srcEffect.copy();
                effect.init();
                reg = (RegularEmitter)effect.getControllers().first().emitter;
                float delta = effect.getControllers().first().deltaTime;

                System.out.println(delta);
        }

        @Override
        public void set(FxId fxId, Pair location, float duration){
                this.fxId = fxId;
                setEffect();
                mode = 1;
                destLoc.set(location);
                tokenSpatial = null;
                this.duration = duration;

                world.getWorldCoords(destLoc, translation);
                rotation.idt();

                commonSet();
        }

        @Override
        public void set(FxId fxId, TokenSpatial followTokenSpatial, float duration){
                this.fxId = fxId;
                setEffect();
                mode = 2;
                this.tokenSpatial =followTokenSpatial;
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
                this.destLoc.set(destLoc);
                world.getWorldCoords(attacker.getMove().getLocationFloatX(), attacker.getMove().getLocationFloatY(), worldStartLoc);
                if (target == null) {
                        tokenSpatial = null;
                        world.getWorldCoords(destLoc.x, destLoc.y, worldDestLoc);
                } else {
                        tokenSpatial = world.getTokenSpatial(target);
                        if (target.getMove() == null) world.getWorldCoords(target.getLocation().x, target.getLocation().y, worldDestLoc);
                        else world.getWorldCoords(target.getMove().getLocationFloatX(), target.getMove().getLocationFloatY(), worldDestLoc);

                }
                worldMoveDir.set(worldDestLoc).sub(worldStartLoc);
                UtMath.normalize(worldMoveDir);

                transformMatrix.idt();
                //Quaternion q = new Quaternion().setFromCross(Vector3.Z, worldMoveDir);
                //transformMatrix.rotate(q);

                translation.set(worldStartLoc);
                rotation.setFromCross(Vector3.Z, worldMoveDir);
                commonSet();

        }

        private void commonSet(){
                //reg.setContinuous(!Float.isNaN(duration));
                reg.setEmissionMode(RegularEmitter.EmissionMode.Enabled);
                effect.start();
        }

        /**
         * forces isActive() == false
         */
        public void deactivate(){
                if(mode > 0){
                        effect.reset();
                        mode = 0;
                        tokenSpatial = null;
                        visU=0;
                }

        }

        public boolean isActive() {
                return mode > 0;
        }

        @Override
        public TokenSpatial getTokenSpatial() {
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


        @Override
        public void update(float delta){
                if(reg.isComplete()){
                        //System.out.println("complete");
                        deactivate();
                        return;
                }

                if(mode == 1 || mode == 2){
                        if(duration <=0){
                                reg.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd);
                        }

                        if(tokenSpatial != null){
                                translation.set(tokenSpatial.translation);
                        }

                        duration-=delta;
                }else if(mode == 3){
                        if (attackerToken == null || !attackerToken.getAttack().hasProjectile()) {
                                //Gdx.app.log("Pooled PE","trigger end fx");
                                reg.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd);
                        } else if (attackerToken.getAttack().getEffectiveProjectileU() < 0) {
                                return;
                        }

                        UtMath.interpolate(
                                Interpolation.pow3,
                                attackerToken.getAttack().getEffectiveProjectileU(),
                                worldStartLoc,
                                worldDestLoc,
                                translation);
                }

                //
                // Common update code (apply transform, visibility of material, animation update)

                if (tokenSpatial != null)
                        visU = tokenSpatial.visU;
                else {
                        if (attackerToken.getFloorMap() == world.getLocalPlayerToken().getFloorMap() && world.getLocalPlayerToken().getFogMapping() != null) {
                                FogMap fogMap = world.getLocalPlayerToken().getFogMapping().getCurrentFogMap();

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
                //visU= 1;

                // TODO: can visU value be applied to particle effects?

                transformMatrix.set(
                        translation.x + translationBase.x, translation.y + translationBase.y, translation.z + translationBase.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        1, 1, 1
                );
                effect.setTransform(transformMatrix);

                effect.update();


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

                if(!shape.isVisible(transformMatrix, world.cam)) // TODO: this visibility check might not be needed
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
