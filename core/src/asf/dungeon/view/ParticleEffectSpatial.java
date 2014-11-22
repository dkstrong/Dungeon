package asf.dungeon.view;

import asf.dungeon.view.shape.Shape;
import asf.dungeon.view.shape.Sphere;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Danny on 11/21/2014.
 */
public class ParticleEffectSpatial implements Spatial, FxManager.PoolableFx {
        private DungeonWorld world;
        private final ParticleEffect effect;
        private final RegularEmitter reg;


        protected TokenSpatial tokenSpatial;
        private final Matrix4 transformMatrix = new Matrix4();
        protected float duration = 0;

        private boolean active = false;

        private Shape shape;

        public ParticleEffectSpatial(ParticleEffect effect) {
                shape = new Sphere(5);

                this.effect = effect;
                effect.init();
                reg = (RegularEmitter)effect.getControllers().first().emitter;
                float delta = effect.getControllers().first().deltaTime;

                System.out.println(delta);

        }

        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
        }

        /**
         * @param followTokenSpatial TokenSpatial for the particle effect to follow
         * @param duration duration of the particle effect, use Float.NaN to play the particle effect once then stop
         */
        public void set( TokenSpatial followTokenSpatial, float duration){
                this.tokenSpatial =followTokenSpatial;
                this.duration = duration;
                commonSet();
        }

        /**
         * @param location static location to play the particle effect at
         * @param duration duration of the particle effect, use Float.NaN to play the particle effect once then stop
         */
        public void set(Vector3 location, float duration){
                tokenSpatial = null;
                //transformMatrix.setToTranslation(location);
                transformMatrix.idt();
                transformMatrix.translate(location);
                this.duration = duration;
                commonSet();
        }

        private void commonSet(){
                reg.setContinuous(!Float.isNaN(duration));
                reg.setEmissionMode(RegularEmitter.EmissionMode.Enabled);
                effect.start();
                active = true;
        }

        /**
         * forces isActive() == false
         */
        public void deactivate(){
                if(active){
                        effect.reset();
                        active= false;
                        tokenSpatial = null;
                }

        }

        public boolean isActive() {
                return active;
        }

        @Override
        public TokenSpatial getTokenSpatial() {
                return tokenSpatial;
        }


        @Override
        public void update(float delta){
                if(reg.isComplete()){
                        System.out.println("complete");
                        deactivate();
                        return;
                }

                if(duration <=0){
                        reg.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd);
                }


                if(tokenSpatial != null){
                        transformMatrix.idt();
                        transformMatrix.translate(tokenSpatial.translation);
                }
                effect.setTransform(transformMatrix);

                duration-=delta;

                effect.update();


        }

        @Override
        public void render(float delta) {
                if(!shape.isVisible(transformMatrix, world.cam))
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
