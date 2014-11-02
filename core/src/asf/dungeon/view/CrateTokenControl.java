package asf.dungeon.view;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import asf.dungeon.board.CrateToken;
import asf.dungeon.board.Dungeon;
import asf.dungeon.board.Token;

/**
 * Created by danny on 10/20/14.
 */
public class CrateTokenControl implements TokenControl{
        public TokenSpatial actorSpatial;
        private DungeonWorld world;
        private Dungeon dungeon;
        public final CrateToken token;


        public CrateTokenControl(DungeonWorld world, CrateToken token) {
                this.world = world;
                this.dungeon = world.dungeon;
                this.token = token;

        }

        @Override
        public Token getToken(){
                return token;
        }

        @Override
        public void start(TokenSpatial actorSpatial) {
                this.actorSpatial = actorSpatial;

                // TODO: i need to scale the Ceramic Pitcher up 20 times in blender and apply scale
                // also i need ot figure out why im having so much issue changing the texture name, its mysteriously somehow keeping the diffuse texture reference
                //float scale = 20f;
                //actorSpatial.scale.set(scale, scale, scale);
                //actorSpatial.translationBase.set(0, actorSpatial.shape.getDimensions().y / 2f * scale + 1.35f * scale, 0);
                //actorSpatial.translation.set(0, 0, 0);

                for (Animation animation : actorSpatial.modelInstance.model.animations) {
                        //System.out.println(animation.id);

                }

                //actorSpatial.animController.setAnimation("Default Take",-1);


        }

        public void end(){

        }

        private float dead = 0;

        @Override
        public void update(float delta) {

                world.getWorldCoords(token.getLocationFloatX(), token.getLocationFloatY(), actorSpatial.translation);
                float rotSpeed = delta * (7 + 0.5f);
                actorSpatial.rotation.slerp(token.getDirection().quaternion, rotSpeed);

                if(token.isDead()){
                        if(dead == 0){
                                for (Material mat : actorSpatial.modelInstance.materials) {
                                        ColorAttribute colorAttribute = (ColorAttribute)mat.get(ColorAttribute.Diffuse);
                                        colorAttribute.color.r = .1f;
                                        colorAttribute.color.g = .1f;
                                        colorAttribute.color.b = .1f;
                                }
                        }
                        dead+=delta;
                }

        }


}
