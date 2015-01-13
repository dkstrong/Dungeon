package asf.dungeon.view;

import asf.dungeon.model.FxId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.token.Token;
import asf.dungeon.view.token.AbstractTokenSpatial;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Controls the creation and destruction of special effects like particle emitters and projectiles. will efficiently reuse them when possible
 * <p/>
 * Created by Daniel Strong on 11/21/2014.
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
                        PointSpriteParticleBatch pointSpriteBatch = new PointSpriteParticleBatch(500);
                        pointSpriteBatch.setCamera(world.cam);
                        particleBatches.add(pointSpriteBatch);
                        BillboardParticleBatch billboardParticleBatch = new BillboardParticleBatch(ParticleShader.AlignMode.Screen, true, 100);
                        billboardParticleBatch.setCamera(world.cam);
                        particleBatches.add(billboardParticleBatch);
                        ParticleEffectLoader pfxLoader = new ParticleEffectLoader(new InternalFileHandleResolver());
                        world.assetManager.setLoader(ParticleEffect.class, pfxLoader);
                }


                // 3d models
                loaded3dModels = new Model[1];
                world.assetManager.load("Models/Arrow/Arrow.g3db", Model.class);

                // animated decals
                loadedDecalAnimations = new Animation[3];
                world.assetManager.load("Textures/SpriteSheets/shock.png", Texture.class);
                world.assetManager.load("Textures/SpriteSheets/sparkles.png", Texture.class);
                world.assetManager.load("Textures/SpriteSheets/explosion.png", Texture.class);

                // particle effects
                world.assetManager.load("ParticleEffects/Particle.png", Texture.class); // particle batch texture
                ParticleEffectLoader.ParticleEffectLoadParameter loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(particleBatches);

                loadedParticleEffects = new ParticleEffect[4];
                world.assetManager.load("ParticleEffects/ConsumeHealth.pfx", ParticleEffect.class, loadParam);
                world.assetManager.load("ParticleEffects/PlasmaBall.pfx", ParticleEffect.class, loadParam);
                world.assetManager.load("ParticleEffects/Burning.pfx", ParticleEffect.class, loadParam);
                world.assetManager.load("ParticleEffects/CrateWoodExplode.pfx", ParticleEffect.class, loadParam);


                fxMappings = new FxMapping[loaded3dModels.length + loadedDecalAnimations.length + loadedParticleEffects.length];
        }

        public void init() {

                // 3d models
                init3dModel(FxId.Arrow, "Models/Arrow/Arrow.g3db");

                // animated decals
                Array<PooledFx> decalsPool = new Array<PooledFx>(false, 16, PooledFx.class); // animated decals can all share the same pool, 3d models and particle effects require that each Fx has its own pool
                initAnimatedDecal(FxId.Lightning, "Textures/SpriteSheets/shock.png", 4, 4, .125f, decalsPool);
                initAnimatedDecal(FxId.Sparkles, "Textures/SpriteSheets/sparkles.png", 6, 1, .1f, decalsPool);
                initAnimatedDecal(FxId.Explosion, "Textures/SpriteSheets/explosion.png", 3, 4, .125f, decalsPool);

                // particle effects
                ((BillboardParticleBatch) particleBatches.get(1)).setTexture(world.assetManager.get("ParticleEffects/Particle.png", Texture.class));

                initParticleEffect(FxId.HealAura, "ParticleEffects/ConsumeHealth.pfx");
                initParticleEffect(FxId.PlasmaBall, "ParticleEffects/PlasmaBall.pfx");
                initParticleEffect(FxId.Burning, "ParticleEffects/Burning.pfx");
                initParticleEffect(FxId.CrateWoodExplosion, "ParticleEffects/CrateWoodExplode.pfx");


        }

        private void init3dModel(FxId fxId, String fileName) {
                int fxIndex = 0;
                while (loaded3dModels[fxIndex] != null)
                        ++fxIndex;

                loaded3dModels[fxIndex] = world.assetManager.get(fileName, Model.class);
                fxMappings[fxId.ordinal()] = new FxMapping((byte) 0, fxIndex, new Array<PooledFx>(false, 8, PooledFx.class));
        }

        private void initAnimatedDecal(FxId fxId, String fileName, int cols, int rows, float frameDuration, Array<PooledFx> sharedFxPool) {
                Texture tex = world.assetManager.get(fileName, Texture.class);
                TextureRegion[][] texRegions = TextureRegion.split(tex, tex.getWidth() / cols, tex.getHeight() / rows);
                TextureRegion[] frames = new TextureRegion[cols * rows];
                int index = 0;
                for (int j = 0; j < rows; j++) {
                        for (int i = 0; i < cols; i++) {
                                frames[index++] = texRegions[j][i];
                        }
                }

                int fxIndex = 0;
                while (loadedDecalAnimations[fxIndex] != null)
                        ++fxIndex;

                loadedDecalAnimations[fxIndex] = new Animation(frameDuration, frames);
                fxMappings[fxId.ordinal()] = new FxMapping((byte) 1, fxIndex, sharedFxPool);
        }

        private void initParticleEffect(FxId fxId, String fileName) {
                int fxIndex = 0;
                while (loadedParticleEffects[fxIndex] != null)
                        ++fxIndex;

                loadedParticleEffects[fxIndex] = world.assetManager.get(fileName, ParticleEffect.class);
                fxMappings[fxId.ordinal()] = new FxMapping((byte) 2, fxIndex, new Array<PooledFx>(false, 8, PooledFx.class));

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
                byte b = fxMappings[fxId.ordinal()].fxClass;
                if (b == 0) return new Pooled3dModelSpatial();
                else if (b == 1) return new PooledAnimatedDecalSpatial();
                else if (b == 2) return new PooledParticleEffectSpatial();
                throw new AssertionError(fxId);
        }

        public void spawnEffect(FxId fxId, float x, float y, float z, float duration) {
                Array<PooledFx> pool = fxMappings[fxId.ordinal()].fxPool;

                for (PooledFx pooledFx : pool) {
                        if (!pooledFx.isActive()) {
                                pooledFx.set(fxId, x,y,z, duration);
                                return;
                        }
                }

                // if there is no available particle effect in the pool, then create a new one
                PooledFx pooledFx = makeFx(fxId);
                pool.add(pooledFx);
                world.addSpatial(pooledFx);
                pooledFx.set(fxId, x,y,z, duration);

        }
        public void spawnEffect(FxId fxId, AbstractTokenSpatial tokenSpatial, float duration) {
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

        public void shootProjectile(FxId fxId, Token source, Token target, Pair destLoc) {
                Array<PooledFx> pool = fxMappings[fxId.ordinal()].fxPool;

                // first try to add duration to any existing effect already on this token spatial
                for (PooledFx pooledFx : pool) {
                        if (pooledFx.getFxId() == fxId && pooledFx.getAttackerToken() == source &&  pooledFx.getTokenSpatial() != null && pooledFx.getTokenSpatial().getToken() == target && pooledFx.getMode() == 4) {
                                pooledFx.set(fxId, source, target, destLoc);
                                return;
                        }
                }

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

        public void spawnProjectile(FxId fxId, Token source, Token target) {
                Array<PooledFx> pool = fxMappings[fxId.ordinal()].fxPool;

                // first try to add duration to any existing effect already on this token spatial
                for (PooledFx pooledFx : pool) {
                        if (pooledFx.getFxId() == fxId && pooledFx.getAttackerToken() == source &&  pooledFx.getTokenSpatial() != null && pooledFx.getTokenSpatial().getToken() == target && pooledFx.getMode() == 4) {
                                pooledFx.set(fxId, source, target);
                                return;
                        }
                }

                // if no effect of this type already exists, then use an inactive one from the pool
                for (PooledFx pooledFx : pool) {
                        if (!pooledFx.isActive()) {
                                pooledFx.set(fxId, source, target);
                                return;
                        }
                }

                // if there is no available particle effect in the pool, then create a new one
                PooledFx pooledFx = makeFx(fxId);
                pool.add(pooledFx);
                world.addSpatial(pooledFx);
                pooledFx.set(fxId, source, target);
        }

        protected void clearAll() {
                if(fxMappings[0] == null) return;
                for (FxMapping fxMapping : fxMappings) {
                        for (PooledFx pooledFx : fxMapping.fxPool) {
                                pooledFx.deactivate();
                        }
                }


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
                byte fxClass;
                /**
                 * index of the resource from the loaded resources array
                 */
                int resourceId;

                /**
                 * pool to store and obtain Fx Spatials from
                 */
                Array<PooledFx> fxPool;

                public FxMapping(byte fxClass, int resourceId, Array<PooledFx> fxPool) {
                        this.fxClass = fxClass;
                        this.resourceId = resourceId;
                        this.fxPool = fxPool;
                }
        }

        public interface PooledFx extends Spatial {
                /**
                 * @param fxId
                 * @param x,y,z static location to play the particle effect at
                 * @param duration duration of the particle effect
                 */
                public void set(FxId fxId, float x, float y, float z, float duration);

                /**
                 * @param fxId
                 * @param followTokenSpatial TokenSpatial for the particle effect to follow
                 * @param duration           duration of the particle effect
                 */
                public void set(FxId fxId, AbstractTokenSpatial followTokenSpatial, float duration);

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
                 * spawns a projectile to get it ready to shoot
                 *
                 * @param fxId
                 * @param attacker token shooting the projectile
                 * @param target   target being shot at (may be null)
                 */
                public void set(FxId fxId, Token attacker, Token target);

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
                 * the token that is "attacking" - only valid for modes 3 and 4
                 * @return
                 */
                public Token getAttackerToken();
                /**
                 * the token spatial that the projectile is following, may be null if it does not apply
                 *
                 * @return
                 */
                public AbstractTokenSpatial getTokenSpatial();

                /**
                 * 1 = static location on worldDestLoc, 2 =  follow targetTokenSpatial, 3 = projectile, 4 = spawned projectile ready to shoot
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
