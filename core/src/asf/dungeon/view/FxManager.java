package asf.dungeon.view;

import asf.dungeon.model.FxId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Controls the creation and detruction of special effects like particle emitters and projectiles. will effeciently reuse them when possible
 * Created by Danny on 11/21/2014.
 */
public class FxManager implements Disposable {

        private DungeonWorld world;

        //particle effect system
        private final Array<ParticleBatch<?>> particleBatches;
        private final ModelBatch particlesModelBatch;

        // Fx mappings / pools
        private final FxMapping[] fxMappings;

        // preloaded assets
        private final Model[] loaded3dModels;
        private final Animation[] loadedDecalAnimations;
        private final ParticleEffect[] loadedParticleEffects;

        public FxManager(DungeonWorld world) {
                this.world = world;
                // init variables for particle system
                {
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
                }



                // Preload assets


                // 3d models
                loaded3dModels = new Model[1];
                world.assetManager.load("Models/Projectiles/Arrow.g3db", Model.class);

                // animated decals
                loadedDecalAnimations = new Animation[1];
                world.assetManager.load("Textures/SpriteSheets/shock.png", Texture.class);

                // particle effects
                ParticleEffectLoader.ParticleEffectLoadParameter loadParam =
                        new ParticleEffectLoader.ParticleEffectLoadParameter(particleBatches);

                loadedParticleEffects = new ParticleEffect[2];
                world.assetManager.load("ParticleEffects/ConsumeHealth.pfx", ParticleEffect.class, loadParam);
                world.assetManager.load("ParticleEffects/PlasmaBall.pfx", ParticleEffect.class, loadParam);

                fxMappings = new FxMapping[loaded3dModels.length+loadedDecalAnimations.length+loadedParticleEffects.length];
        }


        public void init() {


                // 3d models
                loaded3dModels[0] = world.assetManager.get("Models/Projectiles/Arrow.g3db", Model.class);

                // animated decals
                Texture shockTex = world.assetManager.get("Textures/SpriteSheets/shock.png", Texture.class);
                TextureRegion[][] shockTexRegions = TextureRegion.split(shockTex, shockTex.getWidth() / 4, shockTex.getHeight() / 4);
                TextureRegion[] shockFrames = new TextureRegion[4 * 4];
                int index = 0;
                for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                                shockFrames[index++] = shockTexRegions[i][j];
                        }
                }
                loadedDecalAnimations[0] = new Animation(.125f, shockFrames); // lightning

                // particle effects
                loadedParticleEffects[0] = world.assetManager.get("ParticleEffects/ConsumeHealth.pfx", ParticleEffect.class);
                loadedParticleEffects[1] = world.assetManager.get("ParticleEffects/PlasmaBall.pfx", ParticleEffect.class);

                // Fx Pools


                fxMappings[FxId.Arrow.ordinal()] = new FxMapping(Pooled3dModelSpatial.class, 0);
                fxMappings[FxId.Lightning.ordinal()] = new FxMapping(PooledAnimatedDecalSpatial.class, 0);
                fxMappings[FxId.HealAura.ordinal()] = new FxMapping(PooledParticleEffectSpatial.class, 0);
                fxMappings[FxId.PlasmaBall.ordinal()] = new FxMapping(PooledParticleEffectSpatial.class, 1);

                // all animated decal Fx can share the same pool, all other Fx need their own pools
                Array<PooledFx> decalsPool = new Array<PooledFx>(false, 16, PooledFx.class);
                for (FxMapping fxMapping : fxMappings) {
                        if (fxMapping.fxClass == Pooled3dModelSpatial.class) {
                                fxMapping.fxPool = new Array<PooledFx>(false, 8, PooledFx.class);
                        } else if (fxMapping.fxClass == PooledAnimatedDecalSpatial.class) {
                                fxMapping.fxPool = decalsPool;
                        } else if (fxMapping.fxClass == PooledParticleEffectSpatial.class) {
                                fxMapping.fxPool = new Array<PooledFx>(false, 8, PooledFx.class);
                        } else {
                                throw new AssertionError(fxMapping.fxClass);
                        }
                }

        }

        protected Model getModel(FxId fxId) {
                return loaded3dModels[fxMappings[fxId.ordinal()].resourceId];
        }

        protected Animation getAnimation(FxId fxId) {
                return loadedDecalAnimations[fxMappings[fxId.ordinal()].resourceId];
        }

        protected ParticleEffect getParticleEffect(FxId fxId) {
                return loadedParticleEffects[fxMappings[fxId.ordinal()].resourceId];
        }

        private PooledFx makeFx(FxId fxId) {
                // ghetto stuff to avoid c.newInstnce()
                Class c = fxMappings[fxId.ordinal()].fxClass;
                if (c == Pooled3dModelSpatial.class) {
                        return new Pooled3dModelSpatial();
                } else if (c == PooledAnimatedDecalSpatial.class) {
                        return new PooledAnimatedDecalSpatial();
                } else if (c == PooledParticleEffectSpatial.class) {
                        return new PooledParticleEffectSpatial();
                }
                throw new AssertionError(fxId);
        }


        public void spawnEffect(FxId fxId, TokenSpatial tokenSpatial, float duration) {
                Array<PooledFx> pool = fxMappings[fxId.ordinal()].fxPool;
                // first try to add duration to any existing effect already on this token spatial
                for (PooledFx pooledFx : pool) {
                        if (pooledFx.getFxId() == fxId && pooledFx.getTokenSpatial() == tokenSpatial && pooledFx.getMode() == 2) {
                                pooledFx.set(fxId, tokenSpatial, duration);
                                return;
                        }
                }

                // if no effect of this type already exists, then use an inactive one from the pool
                for (PooledFx pooledFx : pool) {
                        if (!pooledFx.isActive()) {
                                pooledFx.set(fxId, tokenSpatial, duration);
                                return;
                        }
                }

                // if there is no available particle effect in the pool, then create a new one

                PooledFx pooledFx = makeFx(fxId);
                pool.add(pooledFx);
                world.addSpatial(pooledFx);
                pooledFx.set(fxId, tokenSpatial, duration);


        }

        protected void shootProjectile(FxId fxId, Token source, Token target, Pair destLoc) {
                Array<PooledFx> pool = fxMappings[fxId.ordinal()].fxPool;

                // if no effect of this type already exists, then use an inactive one from the pool
                for (PooledFx pooledFx : pool) {
                        if (!pooledFx.isActive()) {
                                pooledFx.set(fxId, source, target, destLoc);
                                return;
                        }
                }

                // if there is no available particle effect in the pool, then create a new one
                PooledFx pooledFx = makeFx(fxId);
                pool.add(pooledFx);
                world.addSpatial(pooledFx);
                pooledFx.set(fxId, source, target, destLoc);
        }

        protected void clearAll() {
                        // TODO: need to implement
        }

        public void beginRender() {
                particlesModelBatch.begin(world.cam);
                for (ParticleBatch<?> batch : particleBatches)
                        batch.begin();
        }

        public void endRender() {
                for (ParticleBatch<?> batch : particleBatches)
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

        private static class FxMapping {
                /**
                 * the type of fx spatial to use, 0 = 3d model, 1 = animated decal, 2= particle effect
                 */
                Class<? extends PooledFx> fxClass;
                /**
                 * index of the resource from the loaded resources array
                 */
                int resourceId;

                /**
                 * pool to store and obtain Fx Spatials from
                 */
                Array<PooledFx> fxPool;

                private FxMapping(Class<? extends PooledFx> fxClass, int resourceId) {
                        this.fxClass = fxClass;
                        this.resourceId = resourceId;
                }
        }

        public interface PooledFx extends Spatial {
                /**
                 * @param fxId
                 * @param location static location to play the particle effect at
                 * @param duration duration of the particle effect
                 */
                public void set(FxId fxId, Pair location, float duration);

                /**
                 * @param fxId
                 * @param followTokenSpatial TokenSpatial for the particle effect to follow
                 * @param duration           duration of the particle effect
                 */
                public void set(FxId fxId, TokenSpatial followTokenSpatial, float duration);

                /**
                 * shoots the effect as a projectile
                 *
                 * @param fxId
                 * @param attacker token shooting the projectile
                 * @param target   target being shot at (may be null)
                 * @param destLoc  target location of the projectile
                 */
                public void set(FxId fxId, Token attacker, Token target, Pair destLoc);

                /**
                 * fully deactivate the Fx so it will not update or render
                 */
                public void deactivate();

                /**
                 * if the Fx is "active" and can not be reused. note that it is still allowed to render and update
                 * inactive Fx, just note that they need to properly be reset on their own once set() is called
                 *
                 * @return
                 */
                public boolean isActive();

                /**
                 * the token spatial that the projectile is following, may be null if it does not apply
                 *
                 * @return
                 */
                public TokenSpatial getTokenSpatial();

                /**
                 * 1 = static location on worldDestLoc, 2 =  follow targetTokenSpatial, 3 = projectile
                 *
                 * @return
                 */
                public int getMode();

                /**
                 * the fxId that describes this PooledFx when active
                 *
                 * @return
                 */
                public FxId getFxId();
        }

}
