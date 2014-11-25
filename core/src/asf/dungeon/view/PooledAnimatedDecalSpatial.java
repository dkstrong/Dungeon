package asf.dungeon.view;

import asf.dungeon.model.FxId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.shape.Sphere;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Danny on 11/21/2014.
 */
public class PooledAnimatedDecalSpatial implements Spatial , FxManager.PooledFx {

        private DungeonWorld world;
        private Decal decal;
        private Sphere sphere;

        protected float visU = 0; // how visible this object is, 0 = not drawn, 1 = fully visible, inbetween for partially visible


        // current active
        private FxId fxId;
        private int mode;
        private Animation animation;
        private float time;
        protected TokenSpatial tokenSpatial;

        protected float duration;

        private Token attackerToken;
        private final Pair destLoc = new Pair();
        private final Vector3 worldMoveDir = new Vector3(), worldStartLoc = new Vector3(), worldDestLoc = new Vector3();

        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                decal = new Decal();

                float size = 8;

                decal.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                decal.setDimensions(size,size);
                decal.setColor(1, 1, 1, 1);


                sphere = new Sphere(size);

                //TextureRegion region = decal.getTextureRegion();
                //decal.setDimensions(region.getRegionWidth(), region.getRegionHeight());
                //

        }

        @Override
        public void init(AssetManager assetManager) {

        }


        private void setAnimation(){
                float decalSize = 8;
                time = 0;
                this.animation = world.getFxManager().getAnimation(fxId);
                decal.setTextureRegion(animation.getKeyFrame(0));
                decal.setDimensions(decalSize,decalSize);
                sphere.set(decalSize);
        }
        @Override
        public void set(FxId fxId, Pair location, float duration) {
                this.fxId = fxId;
                setAnimation();
                mode=  1;
                destLoc.set(location);
                tokenSpatial = null;
                this.duration = duration;


                world.getWorldCoords(location, decal.getPosition());
                decal.setPosition(decal.getPosition());
        }
        @Override
        public void set(FxId fxId, TokenSpatial followTokenSpatial, float duration) {
                this.fxId = fxId;
                setAnimation();
                mode = 2;
                tokenSpatial = followTokenSpatial;
                this.duration = duration;
                Gdx.app.log("DecalSpatial","set Decal Spatial- follow token: "+followTokenSpatial);


        }
        @Override
        public void set(FxId fxId, Token attacker, Token target, Pair destLoc) {
                this.fxId = fxId;
                setAnimation();
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
                //rotation.setFromCross(Vector3.Z, worldMoveDir); // TODO: decals dont really use rotation
                decal.setPosition(worldStartLoc);

        }



        @Override
        public void deactivate() {
                mode = 0;
                tokenSpatial = null;
                duration = 0;
                visU =0;
        }

        @Override
        public boolean isActive() {
                return mode > 0;
        }


        @Override
        public void update(float delta) {

                if(mode == 1 || mode == 2){
                        duration -= delta;
                        if(duration <=0){
                                deactivate();
                                return;
                        }
                        if(tokenSpatial != null)
                                decal.setPosition(tokenSpatial.translation.x,
                                        tokenSpatial.translation.y+sphere.getRadius(),
                                        tokenSpatial.translation.z);
                }else if(mode == 3){
                        if (attackerToken == null || !attackerToken.getAttack().hasProjectile()) {
                                deactivate();
                                return;
                        } else if (attackerToken.getAttack().getEffectiveProjectileU() < 0) {
                                return;
                        }

                        UtMath.interpolate(
                                Interpolation.pow3,
                                attackerToken.getAttack().getEffectiveProjectileU(),
                                worldStartLoc,
                                worldDestLoc,
                                decal.getPosition());
                        decal.setPosition(decal.getPosition().x,
                                decal.getPosition().y+sphere.getRadius(),
                                decal.getPosition().z);
                }


                //
                // Common update code (visibility of material, animation update, rotation of decal)

                if (tokenSpatial != null)
                        visU = tokenSpatial.visU;
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

                decal.setColor(1,1,1,visU);

                //decal.lookAt(world.cam.position, world.cam.up);
                decal.setTextureRegion(animation.getKeyFrame(time += delta));
                if(time > animation.getAnimationDuration()) time = 0;


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

                if(!sphere.isVisible(decal.getPosition(), world.cam)) // TODO: this visibility check might not be needed
                        return;

                world.decalBatch.add(decal);
        }

        @Override
        public boolean isInitialized() {
                return isActive();
        }

        @Override
        public void dispose() {

        }

        @Override
        public TokenSpatial getTokenSpatial() {
                return tokenSpatial;
        }

        @Override
        public int getMode() {
                return mode;
        }

        @Override
        public FxId getFxId() {
                return fxId;
        }
}
