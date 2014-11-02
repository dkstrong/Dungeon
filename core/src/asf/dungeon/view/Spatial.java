package asf.dungeon.view;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by danny on 10/21/14.
 */
public interface Spatial extends Disposable{

        public void preload(DungeonWorld world);

        public void init(AssetManager assetManager);

        public void update(float delta);

        public void render(float delta);

        public boolean isInitialized();


}
