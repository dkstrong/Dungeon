package asf.dungeon.view;

import asf.dungeon.model.CharacterToken;
import asf.dungeon.model.DamageableToken;
import asf.dungeon.model.Item;
import asf.dungeon.model.StatusEffect;
import asf.dungeon.utility.MoreMath;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by danny on 10/20/14.
 */
public class CharacterTokenControl implements TokenControl, CharacterToken.Listener {
        public TokenSpatial spatial;
        private DungeonWorld world;

        public final CharacterToken token;
        private Animation idle, walk, attack, hit, die;


        public CharacterTokenControl(DungeonWorld world, CharacterToken token) {
                this.world = world;
                this.token = token;
                this.token.setListener(this);

        }

        @Override
        public CharacterToken getToken() {
                return token;
        }

        @Override
        public void start(TokenSpatial actorSpatial) {
                this.spatial = actorSpatial;

                float scale = .45f;
                actorSpatial.scale.set(scale, scale, scale);
                actorSpatial.translationBase.set(0, (actorSpatial.shape.getDimensions().y / 2f) + 1.45f, 0);
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

        public void end() {

        }

        private Animation current;
        private Item.Consumable currentItemConsume;
        private static final Vector3 temp = new Vector3();
        private final Quaternion tempTargetRot = new Quaternion();

        @Override
        public void update(float delta) {
                world.getWorldCoords(token.getLocationFloatX(), token.getLocationFloatY(), spatial.translation);

                if (currentItemConsume != null) {
                        Gdx.app.log("CharacterTokenControl", "bloob bloob bloob " + currentItemConsume + "!");
                        currentItemConsume = null;
                }

                Animation anim = token.isMoving() ? walk : token.isAttacking() ? attack : token.isHit() ? hit : token.isDead() ? die : idle;

                if (token.isDead()) {
                        if(current != die){
                                spatial.animController.animate(die.id, 1, die.duration / token.getDeathDuration(), null, .2f);
                                current = die;
                        }

                } else if (token.isHit()) {
                        if(current != hit){
                                if (world.getHud().localPlayerToken == token)
                                        world.getHud().closeAllWindows();

                                if (hit == null) {
                                        throw new Error(token.getName());
                                }
                                spatial.animController.animate(hit.id, 1, hit.duration / token.getHitDuration(), null, .2f);
                                current = hit;
                        }
                } else if (token.isAttacking()) {
                        if (current != attack) {
                                spatial.animController.animate(attack.id, 1, attack.duration / token.getAttackDuration(), null, .2f);

                                current = attack;
                        }
                } else if (token.isMoving()) {
                        if(current != walk){
                                float v = MoreMath.scalarLimitsInterpolation(token.getMoveSpeed(), 1, 10, 0.25f, 1f);
                                spatial.animController.animate(walk.id, -1, v, null, .2f);
                                current = walk;
                        }
                } else {
                        if(current != idle){
                                spatial.animController.animate(idle.id, -1, .25f, null, .2f);
                                current = idle;
                        }
                }




                if (token.isAttackingRanged()) {
                        Vector3 rotDir = temp;
                        world.getWorldCoords(token.getAttackTarget().getLocationFloatX(), token.getAttackTarget().getLocationFloatY(), rotDir);
                        rotDir.sub(spatial.translation);
                        MoreMath.normalize(rotDir);
                        tempTargetRot.setFromCross(Vector3.Z, rotDir);

                        float rotSpeed = delta * (MoreMath.largest(token.getMoveSpeed(), 7) + 0.5f);
                        spatial.rotation.slerp(tempTargetRot, rotSpeed);
                } else if(token.hasProjectile()){
                        float rotSpeed = delta * (MoreMath.largest(token.getMoveSpeed(), 7) + 0.5f)*.05f;
                        spatial.rotation.slerp(token.getDirection().quaternion, rotSpeed);
                } else{
                        float rotSpeed = delta * (MoreMath.largest(token.getMoveSpeed(), 7) + 0.5f);
                        spatial.rotation.slerp(token.getDirection().quaternion, rotSpeed);
                }


        }

        @Override
        public void onAttack(DamageableToken target, boolean ranged) {
                if(ranged){
                        world.shootProjectile(token, token.getAttackTarget());
                }

                if (world.getHud().localPlayerToken == token)
                        world.getHud().onAttack(target, ranged);
        }

        @Override
        public void onAttacked(CharacterToken attacker, CharacterToken target, int damage, boolean dodge) {
                // always foward this, let the Hud decide if the information is worth showing
                world.getHud().onAttacked(attacker, target,damage, dodge);
        }

        @Override
        public void onInventoryAdd(Item item) {
                if (world.getHud().localPlayerToken == token)
                        world.getHud().onInventoryAdd(item);

        }

        @Override
        public void onInventoryRemove(Item item) {
                if (world.getHud().localPlayerToken == token)
                        world.getHud().onInventoryRemove(item);
        }

        @Override
        public void onConsumeItem(Item.Consumable item) {
                currentItemConsume = item;
                if (world.getHud().localPlayerToken == token)
                        world.getHud().onConsumeItem(item);
        }

        @Override
        public void onStatusEffectChange(StatusEffect effect, float duration) {
                if (world.getHud().localPlayerToken == token)
                        world.getHud().onStatusEffectChange(effect, duration);
        }

        @Override
        public void dispose() {
                if (this.token != null)
                        this.token.setListener(null);
        }
}
