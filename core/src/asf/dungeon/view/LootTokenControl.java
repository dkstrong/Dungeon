package asf.dungeon.view;

import asf.dungeon.model.PotionItem;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.LootToken;

import javax.swing.text.AttributeSet;

/**
 * Created by danny on 10/20/14.
 */
public class LootTokenControl implements TokenControl{
        public TokenSpatial actorSpatial;
        private DungeonWorld world;
        private Dungeon dungeon;
        public final LootToken token;


        public LootTokenControl(DungeonWorld world, LootToken token) {
                this.world = world;
                this.dungeon = world.dungeon;
                this.token = token;

        }

        @Override
        public LootToken getToken(){
                return token;
        }

        @Override
        public void start(TokenSpatial actorSpatial) {
                this.actorSpatial = actorSpatial;

                float scale = 10f;
                actorSpatial.scale.set(scale, scale, scale);
                //spatial.translationBase.set(0, spatial.shape.getDimensions().y / 2f * scale + 1.35f * scale, 0);
                actorSpatial.translation.set(0, 0, 0);

                for (Animation animation : actorSpatial.modelInstance.model.animations) {
                        //System.out.println(animation.id);

                }

                //spatial.animController.setAnimation("Default Take",-1);

                if(token.getItem() instanceof PotionItem){
                        PotionItem potion = (PotionItem) token.getItem();
                        for (Material mat : actorSpatial.modelInstance.materials) {
                                ColorAttribute colorAttribute = (ColorAttribute)mat.get(ColorAttribute.Diffuse);
                                colorAttribute.color.set(potion.getColor().color);
                        }
                }

        }

        @Override
        public void update(float delta) {

                world.getWorldCoords(token.getLocationFloatX(), token.getLocationFloatY(), actorSpatial.translation);
                float rotSpeed = delta * (7 + 0.5f);
                actorSpatial.rotation.slerp(token.getDirection().quaternion, rotSpeed);


        }

        public void end(){

        }


        @Override
        public void dispose() {

        }
}
