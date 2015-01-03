package asf.dungeon.view.token;

import asf.dungeon.model.ModelId;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.token.Loot;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.AnimFactory;
import asf.dungeon.utility.BetterAnimationController;
import asf.dungeon.utility.BetterModelInstance;
import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Daniel Strong on 12/20/2014.
 */
public class LootTokenSpatial extends AbstractTokenSpatial {
        private boolean initialized = false;
        private BetterModelInstance modelInstance;
        private BetterAnimationController animController;
        private Decal shadowDecal;
        private Loot loot;

        public LootTokenSpatial(DungeonWorld world, Token token) {
                super(world, token);
        }

        public void preload(DungeonWorld world) {

                world.assetManager.load(world.assetMappings.getAssetLocation(token.getModelId()), Model.class);

                loot = token.getLoot();
                if (loot.getItem() instanceof PotionItem) {
                        PotionItem potion = (PotionItem) loot.getItem();
                        world.assetManager.load(world.assetMappings.getPotionTextureAssetLocation(potion), Texture.class);
                } else if (loot.getItem() instanceof ScrollItem) {
                        ScrollItem scroll = (ScrollItem) loot.getItem();
                        world.assetManager.load(world.assetMappings.getScrollTextureAssetLocation(scroll), Texture.class);
                } else if (loot.getItem() instanceof BookItem) {
                        BookItem book = (BookItem) loot.getItem();
                        String assetLocation = world.assetMappings.getBookTextureAssetLocation(book);
                        world.assetManager.load(assetLocation, Texture.class);
                }
        }

        public void init(AssetManager assetManager) {
                initialized = true;
                Model model = assetManager.get(world.assetMappings.getAssetLocation(token.getModelId()));
                modelInstance = new BetterModelInstance(model);

                AnimFactory.createAnim(AnimFactory.dropped(), modelInstance);
                AnimFactory.createIdleAnim(modelInstance);
                animController = new BetterAnimationController(modelInstance);

                if (loot.getItem() instanceof PotionItem) {
                        PotionItem potion = (PotionItem) loot.getItem();
                        Texture potionTex = assetManager.get(world.assetMappings.getPotionTextureAssetLocation(potion), Texture.class);
                        Material mat = modelInstance.materials.get(0);
                        mat.set(TextureAttribute.createDiffuse(potionTex));
                        //ColorAttribute colorAttribute = (ColorAttribute)mat.get(ColorAttribute.Diffuse);
                        //colorAttribute.color.set(potion.getColor().color);
                } else if (loot.getItem() instanceof ScrollItem) {
                        ScrollItem scroll = (ScrollItem) loot.getItem();
                        Texture scrollTex = assetManager.get(world.assetMappings.getScrollTextureAssetLocation(scroll), Texture.class);
                        Material mat = modelInstance.materials.get(0);
                        mat.set(TextureAttribute.createDiffuse(scrollTex));

                } else if (loot.getItem() instanceof BookItem) {
                        BookItem book = (BookItem) loot.getItem();
                        Texture bookTex = assetManager.get(world.assetMappings.getBookTextureAssetLocation(book), Texture.class);
                        Material mat = modelInstance.materials.get(0);
                        mat.set(TextureAttribute.createDiffuse(bookTex));

                }

                if (token.getModelId() == ModelId.Scroll) {
                        for (Material mat : modelInstance.materials) {
                                //GdxInfo.material(mat);
                                mat.set(new IntAttribute(IntAttribute.CullFace, 0));
                        }
                }

                for (Animation animation : modelInstance.animations) {
                        if (animation.id.equals("Idle")) idle = animation;
                        else if (animation.id.equals("Dropped")) dropped = animation;
                }

                shadowDecal = Decal.newDecal(
                        world.floorSpatial.tileDimensions.x,
                        world.floorSpatial.tileDimensions.z,
                        world.pack.findRegion("Textures/TokenShadow"),
                        GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shadowDecal.rotateX(-90);
                shadowDecal.setColor(1,1,1,0.5f);

        }

        private Animation current, idle, dropped;

        public void update(final float delta) {

                FogState fogState = world.floorSpatial.fogMap == null ? FogState.Visible : world.floorSpatial.fogMap.getFogState(token.location.x, token.location.y);
                float minVisU = 0;
                float maxVisU = 1;
                if (fogState == FogState.Visible) {
                        visU += delta * .65f;
                } else {
                        visU -= delta * .75f;
                        // loot can be seen through fog if it is visited, but not if it is magicmapped
                        if (fogState == FogState.Visited ) {
                                minVisU = .3f;
                        }
                }

                visU = MathUtils.clamp(visU, minVisU, maxVisU);

                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
                        //colorAttribute.color.a = visU;
                        if (fogState == FogState.MagicMapped) {
                                colorAttribute.color.set(visU * 0.7f, visU * .8f, visU, 1);
                        } else {
                                colorAttribute.color.set(visU, visU, visU, 1);
                        }
                }

                world.getWorldCoords(token.getLoot().getFloatLocationX(), token.getLoot().getFloatLocationY(), translation);
                rotation.set(world.assetMappings.getRotation(token.getDirection()));



                if (minVisU == 0 || visU != minVisU) {
                        // if not fog blocked

                }
                if(token.getLoot().isBeingThrown()){
                        if(current != dropped){
                                animController.animate(idle.id, -1, 1, null, 0);
                                current = dropped;
                                visU = maxVisU;
                        }
                }else if(current!= dropped){
                        animController.animate(dropped.id, 1, 1, null, 0);
                        animController.queue(idle.id,-1,1,null,.015f*dropped.duration);
                        current = dropped;
                }

                if (animController != null) {
                        animController.update(delta);
                }

        }

        @Override
        public void render(float delta) {
                if (visU <= 0) return;
                if (world.hudSpatial.isMapViewMode()){
                        if (!world.cam.frustum.sphereInFrustumWithoutNearFar(translation, 5)) return;
                }else if (world.hudSpatial.localPlayerToken != null && world.hudSpatial.localPlayerToken.getLocation().distance(token.getLocation()) > 16) return;

                modelInstance.transform.set(
                        translation.x, translation.y, translation.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        1, 1, 1
                );

                world.modelBatch.render(modelInstance, world.environment);

                shadowDecal.setPosition(translation);
                shadowDecal.translateY(0.1f);
                world.decalBatch.add(shadowDecal);
        }

        @Override
        public float intersects(Ray ray) {
                return world.floorSpatial.tileBox.intersects(modelInstance.transform, ray);
        }

        @Override
        public boolean isInitialized() {
                return initialized;
        }

        @Override
        public void dispose() {
                super.dispose();

                initialized = false;
        }
}
