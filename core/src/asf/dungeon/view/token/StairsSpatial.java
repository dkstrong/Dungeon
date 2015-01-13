package asf.dungeon.view.token;

import asf.dungeon.model.token.Token;
import asf.dungeon.utility.BetterModelInstance;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Daniel Strong on 12/20/2014.
 */
public class StairsSpatial extends AbstractTokenSpatial{
        private boolean initialized = false;
        private BetterModelInstance modelInstance;
        private ColorAttribute colorAttribute;
        private Color floorDecalColor;

        public StairsSpatial(DungeonWorld world, Token token) {
                super(world, token);
        }


        @Override
        public void preload(DungeonWorld world) {
                if(token.stairs.isStairsUp()){
                        world.assetManager.load("Models/Stairs/StairsUp.g3db", Model.class);
                }else{
                        world.assetManager.load("Models/Stairs/StairsDown.g3db", Model.class);
                }

        }

        @Override
        public void init(AssetManager assetManager) {
                initialized = true;

                if(token.stairs.isStairsUp()){
                        Model model = assetManager.get("Models/Stairs/StairsUp.g3db", Model.class);
                        modelInstance = new BetterModelInstance(model);
                }else{
                        Model model = assetManager.get("Models/Stairs/StairsDown.g3db", Model.class);
                        modelInstance = new BetterModelInstance(model);
                }
                Material material = modelInstance.materials.get(0);
                colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);

                world.getWorldCoords(token.location, translation);

                rotation.set(world.assetMappings.getRotation(token.direction));

                modelInstance.transform.set(
                        translation.x , translation.y , translation.z ,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        1, 1, 1
                );

                floorDecalColor = world.floorSpatial.getDecalNodeColorAt(token.location.x, token.location.y);
        }

        @Override
        public void update(float delta) {
                visU = floorDecalColor.g;
        }

        @Override
        public void render(float delta) {
                if (visU <= 0) return;
                if (world.hudSpatial.isMapViewMode()){
                        if (!world.cam.frustum.sphereInFrustumWithoutNearFar(translation, 5)) return;
                }else if (world.hudSpatial.localPlayerToken != null && world.hudSpatial.localPlayerToken.location.distance(token.location) > 16) return;


                colorAttribute.color.r = UtMath.clamp(floorDecalColor.r + .1f, 0f, 1f);
                colorAttribute.color.g = UtMath.clamp(floorDecalColor.g+.1f, 0f,1f);
                colorAttribute.color.b = UtMath.clamp(floorDecalColor.b+.1f, 0f,1f);
                world.modelBatch.render(modelInstance, world.environment);
        }

        @Override
        public boolean isInitialized() {
                return initialized;
        }
        @Override
        public float intersects(Ray ray) {
                return world.floorSpatial.tileBox.intersects(modelInstance.transform, ray);
        }

        @Override
        public void dispose() {
                super.dispose();
                initialized = false;
        }
}
