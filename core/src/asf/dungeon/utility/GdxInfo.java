package asf.dungeon.utility;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.utils.Array;

/**
 * Created by danny on 10/20/14.
 */
public class GdxInfo {

        private static void print(Array<String> out){
                boolean first = true;
                for(String string : out){
                        if(first){
                                System.out.println("-------------"+string+"---------------");
                                first = false;
                        }else{
                                System.out.println(string);
                        }

                }
        }

        public static void model(Model model){

                Array<String> out = new Array<String>();
                out.add("Analying Model: "+model);
                out.add("Analyzing  Animations ");
                for(Animation animation : model.animations){
                        out.add(animation.id +" : "+animation.duration);
                }

                out.add("Analyzing  Nodes");
                for (Node node : model.nodes) {
                        nodes(node, out);
                }

                print(out);
        }

        private static void nodes(Node node, Array<String> out){
                out.add(node.id);
                for (Node child : node.children) {
                        nodes(child, out);
                }
        }

        public static void material(Material material){

                Array<String> out = new Array<String>();
                out.add("Analying Material: "+material);

                out.add("Analying  Attributes ");
                for (Attribute attribute : material) {
                        out.add(attribute.toString());

                }



                print(out);
        }
}
