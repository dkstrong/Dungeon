package asf.dungeon.view;

import asf.dungeon.model.FxId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.shape.Sphere;
import asf.dungeon.view.token.AbstractTokenSpatial;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
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
        private int mode; // 1 = static location on worldDestLoc, 2 =  follow targetTokenSpatial, 3 = projectile, 4 = spawned projectile ready to shoot
        private Animation animation;
        private float time;
        protected AbstractTokenSpatial tokenSpatial;

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
                this.animation = world.fxManager.getAnimation(fxId);
                decal.setTextureRegion(animation.getKeyFrame(0));
                decal.setDimensions(decalSize,decalSize);
                sphere.set(decalSize);
        }
        @Override
        public void set(FxId fxId,float x, float y, float z, float duration) {
                this.fxId = fxId;
                setAnimation();
                mode=  1;
                world.getMapCoords(x,y,z,destLoc);
                tokenSpatial = null;
                this.duration = duration;


                //world.getWorldCoords(location, decal.getPosition());
                //decal.setPosition(decal.getPosition());
                decal.setPosition(x,y,z);
        }
        @Override
        public void set(FxId fxId, AbstractTokenSpatial followTokenSpatial, float duration) {
                this.fxId = fxId;
                setAnimation();
                mode = 2;
                tokenSpatial = followTokenSpatial;
                this.duration = duration;
                //Gdx.app.log("DecalSpatial","set Decal Spatial- follow token: "+followTokenSpatial);


        }
        @Override
        public void set(FxId fxId, Token attacker, Token target, Pair destLoc) {
                this.fxId = fxId;
                setAnimation();
                this.mode = 3;
                this.attackerToken = attacker;
                this.destLoc.set(destLoc);
                world.getWorldCoords(attacker.move.getFloatLocation(), worldStartLoc);
                if (target == null) {
                        tokenSpatial = null;
                        world.getWorldCoords(destLoc.x, destLoc.y, worldDestLoc);
                } else {
                        tokenSpatial = world.getTokenSpatial(target);
                        if (target.move == null) world.getWorldCoords(target.location.x, target.location.y, worldDestLoc);
                        else world.getWorldCoords(target.move.getFloatLocation(), worldDestLoc);

                }
                worldMoveDir.set(worldDestLoc).sub(worldStartLoc).nor();
                //rotation.setFromCross(Vector3.Z, worldMoveDir); // TODO: decals dont really use rotation
                decal.setPosition(worldStartLoc);

        }

        @Override
        public void set(FxId fxId, Token attacker, Token target) {
                this.fxId = fxId;
                setAnimation();
                mode = 4;
                attackerToken = attacker;
                tokenSpatial = world.getTokenSpatial(target);
                this.duration = Float.NaN;

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
                        if(tokenSpatial != null){

                        }

                }else if(mode == 4){
                        if(tokenSpatial.token.damage != null && tokenSpatial.token.damage.isDead()){
                                deactivate();
                                return;
                        }
                        if (tokenSpatial.token.attack == null || !tokenSpatial.token.attack.isAttacking()) {
                                deactivate();
                                return;
                        }

                        decal.setPosition(tokenSpatial.translation.x,
                                tokenSpatial.translation.y+sphere.getRadius(),
                                tokenSpatial.translation.z);


                }else if(mode == 3){
                        if (attackerToken == null || !attackerToken.attack.hasProjectile() || attackerToken.damage.isDead()) {
                                deactivate();
                                return;
                        } else if (attackerToken.attack.getEffectiveProjectileU() < 0) {
                                return;
                        }

                        if(attackerToken.attack.getEffectiveProjectileU() > 0.25f){
                                UtMath.interpolate(
                                        Interpolation.pow3,
                                        attackerToken.attack.getEffectiveProjectileU(),
                                        worldStartLoc,
                                        worldDestLoc,
                                        decal.getPosition());
                        }else{
                                decal.setPosition(worldStartLoc);
                        }


                        decal.setPosition(decal.getPosition().x,
                                decal.getPosition().y+sphere.getRadius(),
                                decal.getPosition().z);
                }


                //
                // Common update code (visibility of material, animation update, rotation of decal)
                FogState fogState;
                if (tokenSpatial != null) {
                        if (world.getLocalPlayerToken() != null && world.getLocalPlayerToken().fogMapping != null) {
                                fogState = world.getLocalPlayerToken().fogMapping.getCurrentFogMap().getFogState(tokenSpatial.token.location.x, tokenSpatial.token.location.y);
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


                if(fogState == FogState.MagicMapped){
                        Color color = decal.getColor();
                        color.r = MathUtils.lerp(color.r, visU*.7f, delta);
                        color.g = MathUtils.lerp(color.g, visU*.8f, delta);
                        color.b = visU;
                        color.a = visU;
                        decal.setColor(color);
                }else{
                        decal.setColor(1, 1, 1, visU);
                }




                decal.lookAt(world.cam.position, world.cam.up);
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
                        if (attackerToken == null || attackerToken.attack.getEffectiveProjectileU() < 0 || attackerToken.damage.isDead()) {
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

        public Token getAttackerToken(){

                return attackerToken;
        }

        @Override
        public AbstractTokenSpatial getTokenSpatial() {
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
