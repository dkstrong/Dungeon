package asf.dungeon.view;

import asf.dungeon.view.shape.Sphere;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Danny on 11/21/2014.
 */
public class AnimatedDecalSpatial implements Spatial , FxManager.PoolableFx {

        private DungeonWorld world;
        private Decal decal;
        private Sphere sphere;

        private Animation animation;
        private float time;

        protected float duration;
        protected TokenSpatial tokenSpatial;

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

        }

        @Override
        public void init(AssetManager assetManager) {

        }


        public void set(TokenSpatial followTokenSpatial, Animation animation,float duration) {
                this.animation = animation;
                decal.setTextureRegion(animation.getKeyFrame(0));
                this.duration = duration;
                tokenSpatial = followTokenSpatial;

        }


        public void set(Vector3 location, Animation animation,float duration) {
                this.animation = animation;
                decal.setTextureRegion(animation.getKeyFrame(0));
                this.duration = duration;
                tokenSpatial = null;
                decal.setPosition(location);
        }

        @Override
        public void deactivate() {
                duration = 0;
        }

        @Override
        public boolean isActive() {
                return duration > 0;
        }


        @Override
        public void update(float delta) {
                if(tokenSpatial != null)
                        decal.setPosition(tokenSpatial.translation.x,
                                tokenSpatial.translation.y+sphere.getRadius(),
                                tokenSpatial.translation.z);

                decal.lookAt(world.cam.position, world.cam.up);


                decal.setTextureRegion(animation.getKeyFrame(time += delta));

                if(time > animation.getAnimationDuration())
                        time = 0;

                duration -= delta;
        }

        @Override
        public void render(float delta) {
                if(!sphere.isVisible(decal.getPosition(), world.cam))
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
}
