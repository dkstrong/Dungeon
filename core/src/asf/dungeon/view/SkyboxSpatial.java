package asf.dungeon.view;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

/**
 * Created by danny on 10/20/14.
 */
public class SkyboxSpatial implements Spatial {

        private DungeonWorld world;
        public ModelInstance modelInstance;

        public void preload(DungeonWorld world){
                this.world  = world;
                world.assetManager.load("Models/skydome.g3db", Model.class);
        }

        public void init(AssetManager assetManager) {
                Model model = assetManager.get("Models/skydome.g3db");
                modelInstance = new ModelInstance(model);
        }

        public void update(final float delta) {

        }

        public void render(float delta) {
                world.modelBatch.render(modelInstance);
        }

        @Override
        public void dispose() {
                modelInstance = null;
        }

        public boolean isInitialized(){
                return modelInstance!=null;
        }

}
