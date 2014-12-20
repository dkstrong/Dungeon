package asf.dungeon.view.token;

import asf.dungeon.model.FxId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.SfxId;
import asf.dungeon.model.Tile;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.token.Attack;
import asf.dungeon.model.token.CharacterInventory;
import asf.dungeon.model.token.StatusEffect;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;
import asf.dungeon.model.token.logic.fsm.State;
import asf.dungeon.model.token.quest.Dialouge;
import asf.dungeon.model.token.quest.Quest;
import asf.dungeon.view.DungeonWorld;
import asf.dungeon.view.Spatial;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Daniel Strong on 12/19/2014.
 */
public abstract class AbstractTokenSpatial implements Spatial, Token.Listener{
        protected final DungeonWorld world;
        protected final Token token;

        public final Vector3 translation = new Vector3();
        protected final Quaternion rotation = new Quaternion();
        public float visU = 0; // how visible this object is, 0 = not drawn, 1 = fully visible, inbetween for partially visible

        public AbstractTokenSpatial(DungeonWorld world, Token token) {
                this.world = world;
                this.token = token;
                this.token.setListener(this);
        }

        /**
         * The token that this spatial is a view for
         * @return
         */
        public Token getToken(){
                return token;
        }
        /**
         * @return -1 on no intersection,
         * or when there is an intersection: the squared distance between the center of this
         * object and the point on the ray closest to this object when there is intersection.
         */
        public abstract float intersects(Ray ray);

        @Override
        public void dispose() {
                if(token != null)
                        token.setListener(null);
        }


        @Override
        public void onFsmStateChange(FsmLogic fsm, State oldState, State newState) {
                if(oldState == Monster.Sleep && newState == Monster.Chase){
                        world.sounds.play(SfxId.AlertMonster);
                }
        }

        @Override
        public void onStatusEffectChange(StatusEffect effect, float duration) {

                world.fxManager.spawnEffect(world.assetMappings.getStatusEffectFxId(effect), this, duration);

                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onStatusEffectChange(effect, duration);
        }

        @Override
        public void onPathBlocked(Pair nextLocation, Tile nextTile) {
                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onPathBlocked(nextLocation, nextTile);
        }

        @Override
        public void onAttack(Token target, Pair targetLocation, boolean ranged) {

                if (ranged) {
                        world.fxManager.shootProjectile(token.getAttack().getWeapon().getProjectileFx(), token, target, targetLocation);
                }

                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onAttack(target, targetLocation, ranged);
        }

        @Override
        public void onAttacked(Token attacker, Token target, Attack.AttackOutcome attackOutcome) {
                // always foward this, let the Hud decide if the information is worth showing
                world.hudSpatial.onAttacked(attacker, target, attackOutcome);

        }

        @Override
        public void onInventoryChanged() {
                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onInventoryChanged();

        }

        @Override
        public void onUseItem(Item item, CharacterInventory.UseItemOutcome out) {
                if(out.didSomething){
                        if(item instanceof ScrollItem){
                                ScrollItem scroll = (ScrollItem ) item;
                                if(scroll.getType() == ScrollItem.Type.Lightning){

                                        AbstractTokenSpatial targetTokenSpatial = world.getTokenSpatial(out.targetToken);

                                        world.fxManager.spawnEffect(FxId.Lightning, targetTokenSpatial, 3);
                                }
                        }
                }


                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onUseItem(item, out);
        }




        @Override
        public void onLearned(Object journalObject, boolean study) {
                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onLearned(journalObject, false);
        }

        @Override
        public void onInteract(Quest quest, Dialouge dialouge) {
                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onInteract(quest, dialouge);
        }
}
