package asf.dungeon.view;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.Dungeon;
import asf.dungeon.utility.MoreMath;

/**
 * Created by danny on 10/20/14.
 */
public class CharacterTokenControl implements TokenControl {
        public TokenSpatial actorSpatial;
        private DungeonWorld world;
        private Dungeon dungeon;
        public final CharacterToken token;
        private Animation idle, walk, attack, hit, die;


        public CharacterTokenControl(DungeonWorld world, CharacterToken token) {
                this.world = world;
                this.dungeon = world.dungeon;
                this.token = token;

        }

        @Override
        public CharacterToken getToken(){
                return token;
        }

        @Override
        public void start(TokenSpatial actorSpatial) {
                this.actorSpatial = actorSpatial;

                float scale = .45f;
                actorSpatial.scale.set(scale, scale, scale);
                actorSpatial.translationBase.set(0, (actorSpatial.shape.getDimensions().y / 2f)+1.45f , 0);
                actorSpatial.translation.set(0, 0, 0);

                for (Animation animation : actorSpatial.modelInstance.model.animations) {
                        if (animation.id.equals("Walk")) {
                                walk = animation;
                        } else if (animation.id.equals("Idle")) {
                                idle = animation;
                        } else if (animation.id.equals("Attack")) {
                                attack = animation;
                        } else if (animation.id.equals("Hit")) {
                                hit = animation;
                        } else if (animation.id.equals("Damaged")) {
                                hit = animation;
                        } else if (animation.id.equals("Die")) {
                                die = animation;
                        }

                }
        }

        public void end(){

        }

        private Animation current;

        @Override
        public void update(float delta) {

                Animation anim = token.isMoving() ? walk : token.isAttacking() ? attack : token.isHit() ? hit : token.isDead() ? die : idle;

                if(current != anim){
                        if(token.isDead()){
                                actorSpatial.animController.animate(die.id, 1, die.duration / token.getDeathDuration(),null,.2f);
                        }else if (token.isHit()) {
                                if(hit == null){
                                        throw new Error(token.getName());
                                }
                                actorSpatial.animController.animate(hit.id, 1, hit.duration/token.getHitDuration(), null, .2f);
                        } else if (token.isAttacking()) {
                                actorSpatial.animController.animate(attack.id, 1, attack.duration/token.getAttackDuration(), null, .2f);
                        } else if (token.isMoving()) {
                                float v = MoreMath.scalarLimitsInterpolation(token.getMoveSpeed(), 1, 10, 0.25f, 1f);
                                actorSpatial.animController.animate(walk.id, -1, v, null, .2f);
                        } else {
                                actorSpatial.animController.animate(idle.id, -1, .25f, null, .2f);
                        }
                }



                world.getWorldCoords(token.getLocationFloatX(), token.getLocationFloatY(), actorSpatial.translation);
                float rotSpeed = delta * (MoreMath.largest(token.getMoveSpeed(), 7) + 0.5f);
                actorSpatial.rotation.slerp(token.getDirection().quaternion, rotSpeed);

        }


}
