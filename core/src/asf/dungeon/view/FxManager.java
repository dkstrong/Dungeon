package asf.dungeon.view;

import asf.dungeon.model.Pair;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.reflect.ArrayReflection;

import java.util.Iterator;

/**
 * Controls the creation and detruction of special effects like particle emitters and projectiles. will effeciently reuse them when possible
 * Created by Danny on 11/21/2014.
 */
public class FxManager implements Disposable{


        public enum EffectType{
                Lightning("ParticleEffects/Flame.pfx"),
                Flame("ParticleEffects/Flame.pfx"),
                Heal("ParticleEffects/ConsumeHealth.pfx");
                private final String assetLocation; // TODO: transient?

                EffectType(String assetLocation) {
                        this.assetLocation = assetLocation;
                }
        }


        private DungeonWorld world;

        // projectiles
        private final Array<ProjectileSpatial> projectileSpatialPool;

        // animated decals
        private final Array<AnimatedDecalSpatial> animatedDecalSpatialPool;
        private final Animation[] decalAnimations;

        //particle effects
        private final Array<ParticleBatch<?>> particleBatches;
        private final ModelBatch particlesModelBatch;
        private final Array<ParticleEffectSpatial>[] particleSpatials;
        private final ParticleEffect[] loadedParticleEffects;

        public FxManager(DungeonWorld world) {
                this.world = world;

                projectileSpatialPool = new Array<ProjectileSpatial>(false, 16, ProjectileSpatial.class);
                animatedDecalSpatialPool = new Array<AnimatedDecalSpatial>(false, 16, AnimatedDecalSpatial.class);
                decalAnimations = new Animation[1];
                world.assetManager.load("Textures/SpriteSheets/shock.png", Texture.class);

                particlesModelBatch = new ModelBatch();
                particleBatches = new Array<ParticleBatch<?>>();
                PointSpriteParticleBatch pointSpriteBatch = new PointSpriteParticleBatch();
                pointSpriteBatch.setCamera(world.cam);
                particleBatches.add(pointSpriteBatch);
                BillboardParticleBatch billboardParticleBatch = new BillboardParticleBatch();
                billboardParticleBatch.setCamera(world.cam);
                particleBatches.add(billboardParticleBatch);

                ParticleEffectLoader pfxLoader = new ParticleEffectLoader(new InternalFileHandleResolver());
                world.assetManager.setLoader(ParticleEffect.class, pfxLoader);

                EffectType[] effectTypeValues = EffectType.values();

                loadedParticleEffects = new ParticleEffect[effectTypeValues.length];
                particleSpatials = (Array<ParticleEffectSpatial>[]) ArrayReflection.newInstance(Array.class, effectTypeValues.length);


                ParticleEffectLoader.ParticleEffectLoadParameter loadParam =
                        new ParticleEffectLoader.ParticleEffectLoadParameter(particleBatches);

                for (EffectType effectType : effectTypeValues) {
                        world.assetManager.load(effectType.assetLocation, ParticleEffect.class, loadParam);
                }
        }

        public void init() {

                Texture shockTex = world.assetManager.get("Textures/SpriteSheets/shock.png", Texture.class);
                TextureRegion[][] shockTexRegions = TextureRegion.split(shockTex,shockTex.getWidth()/4,shockTex.getHeight()/4);
                TextureRegion[] shockFrames = new TextureRegion[4*4];
                int index = 0;
                for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                                shockFrames[index++] = shockTexRegions[i][j];
                        }
                }
                decalAnimations[0] = new Animation(.125f, shockFrames);



                EffectType[] effectTypeValues = EffectType.values();

                for (EffectType effectType : effectTypeValues) {
                        loadedParticleEffects[effectType.ordinal()] = world.assetManager.get(effectType.assetLocation, ParticleEffect.class);
                        particleSpatials[effectType.ordinal()] = new Array<ParticleEffectSpatial>(false, 16, ParticleEffectSpatial.class);
                }
        }




        public void spawnEffect(EffectType effectType, TokenSpatial tokenSpatial, float duration){
                Array<ParticleEffectSpatial> pool = particleSpatials[effectType.ordinal()];
                // first try to add duration to any existing effect already on this token spatial
                for (ParticleEffectSpatial thing : pool) {
                        if(thing.tokenSpatial == tokenSpatial && !Float.isNaN(thing.duration)){
                                thing.set(tokenSpatial, duration);
                                return;
                        }
                }

                // if no effect of this type already exists, then use an inactive one from the pool
                for (ParticleEffectSpatial thing : pool) {
                        if(!thing.isActive()){
                                thing.set(tokenSpatial, duration);
                                return;
                        }
                }

                // if there is no available particle effect in the pool, then create a new one
                ParticleEffect effectInstance = loadedParticleEffects[effectType.ordinal()].copy();
                ParticleEffectSpatial thing = new ParticleEffectSpatial(effectInstance);
                pool.add(thing);
                world.addSpatial(thing);
                thing.set(tokenSpatial, duration);

        }

        protected void spawnAnimatedDecalEffect(EffectType effectType, TokenSpatial tokenSpatial, float duration){

                for (AnimatedDecalSpatial thing : animatedDecalSpatialPool) {
                        if(thing.tokenSpatial == tokenSpatial && !Float.isNaN(thing.duration)){
                                thing.set(tokenSpatial, decalAnimations[0], duration);
                                return;
                        }
                }


                // if no effect of this type already exists, then use an inactive one from the pool
                for (AnimatedDecalSpatial thing : animatedDecalSpatialPool) {
                        if(!thing.isActive()){
                                thing.set(tokenSpatial,decalAnimations[0],  duration);
                                return;
                        }
                }

                // if there is no available particle effect in the pool, then create a new one

                AnimatedDecalSpatial thing = new AnimatedDecalSpatial();
                animatedDecalSpatialPool.add(thing);
                world.addSpatial(thing);
                thing.set(tokenSpatial, decalAnimations[0],duration);

        }

        protected void shootProjectile(Token source, Token target, Pair destLoc){
                for (ProjectileSpatial projectileSpatial : projectileSpatialPool) {
                        if(!projectileSpatial.isActive()){
                                projectileSpatial.shootProjectile(source, target,destLoc);
                                return;
                        }
                }

                ProjectileSpatial projectileSpatial = new ProjectileSpatial(world, world.environment);
                projectileSpatialPool.add(projectileSpatial);
                world.addSpatial(projectileSpatial);
                projectileSpatial.shootProjectile(source, target, destLoc);
        }

        protected void clearAll(){
                Iterator<ProjectileSpatial> i = projectileSpatialPool.iterator();
                while(i.hasNext()){
                        ProjectileSpatial next = i.next();
                        world.removeSpatial(next);
                        i.remove();;
                }
        }

        public void beginRender(){
                particlesModelBatch.begin(world.cam);
                for(ParticleBatch<?> batch : particleBatches)
                        batch.begin();
        }

        public void endRender(){
                for(ParticleBatch<?> batch : particleBatches)
                        batch.end();
                for (ParticleBatch<?> batch : particleBatches) {
                        particlesModelBatch.render(batch);
                }
                particlesModelBatch.end();
        }

        @Override
        public void dispose() {
                particlesModelBatch.dispose();
        }


        public interface PoolableFx{
                public void deactivate();
                public boolean isActive();
                public TokenSpatial getTokenSpatial();
        }

}
