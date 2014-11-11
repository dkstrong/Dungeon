package asf.dungeon.view;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import asf.dungeon.model.CrateToken;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.Token;

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


                //float scale = 20f;
                //spatial.scale.set(scale, scale, scale);
                //spatial.translationBase.set(0, spatial.shape.getDimensions().y / 2f * scale + 1.35f * scale, 0);
                //spatial.translation.set(0, 0, 0);

                for (Animation animation : actorSpatial.modelInstance.model.animations) {
                        //System.out.println(animation.id);

                }

                //spatial.animController.setAnimation("Default Take",-1);


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


        @Override
        public void dispose() {

        }
}
