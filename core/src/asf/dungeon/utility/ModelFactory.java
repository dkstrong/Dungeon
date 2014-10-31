package asf.dungeon.utility;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 * Created by danny on 10/20/14.
 */
public class ModelFactory {

        public static Model box(float x, float y, float z, Color color){
                ModelBuilder modelBuilder = new ModelBuilder();
                Material mat = new Material(ColorAttribute.createDiffuse(color));
                Model model = modelBuilder.createBox(x, y, z,mat, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
                //model.manageDisposable(mat);
                return model;
        }
}
