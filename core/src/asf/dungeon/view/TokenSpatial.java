package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.Item;
import asf.dungeon.model.PotionItem;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.Loot;
import asf.dungeon.utility.GdxInfo;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import asf.dungeon.view.shape.Shape;

/**
 * Created by danny on 10/20/14.
 */
public class TokenSpatial implements Spatial, Token.Listener {

        private boolean initialized = false;
        public final String assetLocation;
        public Environment environment;
        public ModelInstance modelInstance;
        public Shape shape;
        public AnimationController animController;
        public final Vector3 translationBase = new Vector3();
        public final Vector3 translation = new Vector3();
        public final Quaternion rotation = new Quaternion();
        public final Vector3 scale = new Vector3(1, 1, 1);
        protected DungeonWorld world;
        protected Token token;
        protected float visU = 0; // how visible this object is, 0 = not drawn, 1 = fully visible, inbetween for partially visible

        public TokenSpatial(DungeonWorld world, Token token, Shape shape, Environment environment) {
                this.world = world;
                this.token = token;
                this.assetLocation = token.getModelId().assetLocation;
                this.shape = shape;
                this.environment = environment;
                token.setListener(this);
        }




        public void preload(DungeonWorld world){

                world.assetManager.load(assetLocation, Model.class);

                Loot loot = token.get(Loot.class);
                if(loot != null){
                        if(loot.getItem() instanceof PotionItem){
                                world.assetManager.load(((PotionItem) loot.getItem()).getColor().textureAssetLocation, Texture.class);
                        }
                }


        }

        public void init(AssetManager assetManager) {

                if (assetLocation != null) {
                        if (!assetManager.isLoaded(assetLocation, Model.class))
                                throw new Error("asset not loaded");

                        Model model = assetManager.get(assetLocation);
                        modelInstance = new ModelInstance(model);
                }


                initialized = true;

                if (shape != null) {
                        shape.setFromModelInstance(modelInstance);
                }

                if (modelInstance.animations.size > 0) {
                        animController = new AnimationController(modelInstance);
                        //GdxInfo.model(modelInstance.model);
                        //animController.setAnimation(modelInstance.animations.get(0).id, 100);
                }

                for (Material material : modelInstance.materials) {
                        material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                }

                Loot loot = token.get(Loot.class);
                if(loot != null){
                        if(loot.getItem() instanceof PotionItem){
                                PotionItem potion = (PotionItem) loot.getItem();
                                Texture potionTex = assetManager.get(potion.getColor().textureAssetLocation, Texture.class);
                                Material mat = modelInstance.materials.get(0);
                                mat.set(TextureAttribute.createDiffuse(potionTex));
                                //ColorAttribute colorAttribute = (ColorAttribute)mat.get(ColorAttribute.Diffuse);
                                //colorAttribute.color.set(potion.getColor().color);
                        }
                }

                if(token.getModelId().assetLocation.contains("Characters")){
                        float s = .45f;
                        scale.set(s, s, s);
                        translationBase.set(0, (shape.getDimensions().y / 2f) + 1.45f, 0);
                        translation.set(0, 0, 0);



                }

                if(token.getModelId() == ModelId.Diablous || token.getModelId() == ModelId.Berzerker){
                        for (Material mat : modelInstance.materials) {
                                GdxInfo.material(mat);
                                mat.set(new IntAttribute(IntAttribute.CullFace, 0));
                        }
                }

                for (Animation animation : modelInstance.model.animations) {
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

        private Animation current, idle, walk, attack, hit, die;
        private Item.Consumable currentItemConsume;
        private static final Vector3 temp = new Vector3();
        private final Quaternion tempTargetRot = new Quaternion();


        public void update(final float delta) {

                // this token is only visible if on the same floor
                // as the local player, and if fog mapping is enabled
                if( world.getLocalPlayerToken() != null && world.getLocalPlayerToken().getFogMapping() != null){

                        FogMap fogMap = world.getLocalPlayerToken().getFogMapping().getFogMap(world.getLocalPlayerToken().getFloorMap());
                        if(fogMap != null){
                                FogState fogState = fogMap.getFogState(token.getLocation().x, token.getLocation().y);
                                if(fogState == FogState.Visible){
                                        visU+=delta*.5f;
                                }else{
                                        visU-=delta*.75f;
                                }
                                visU = MathUtils.clamp(visU,0,1);
                        }
                }else{
                        visU = 1;
                }




                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);
                        colorAttribute.color.a = visU;
                }

                if(token.getMove() == null){
                        world.getWorldCoords(token.getLocation().x, token.getLocation().y, translation);
                }else{
                        world.getWorldCoords(token.getMove().getLocationFloatX(), token.getMove().getLocationFloatY(), translation);
                }


                if (currentItemConsume != null) {
                        Gdx.app.log("CharacterTokenControl", "bloob bloob bloob " + currentItemConsume + "!");
                        currentItemConsume = null;
                }

                if (token.getDamage() != null && token.getDamage().isDead()) {
                        if(current != die){
                                animController.animate(die.id, 1, die.duration / token.getDamage().getDeathDuration(), null, .2f);
                                current = die;
                        }

                } else if (token.getAttack() != null && token.getAttack().isAttacking()) {
                        if (current != attack) {
                                animController.animate(attack.id, 1, attack.duration / token.getAttack().getAttackDuration(), null, .2f);

                                current = attack;
                        }
                } else if (token.getDamage() != null && token.getDamage().isHit()) {
                        if(current != hit){
                                if (world.getHud().localPlayerToken == token)
                                        world.getHud().closeAllWindows();

                                if (hit == null) {
                                        throw new Error(token.getName());
                                }
                                animController.animate(hit.id, 1, hit.duration / token.getDamage().getHitDuration(), null, .2f);
                                current = hit;
                        }
                } else if (token.getMove() != null && token.getMove().isMoving()  && !(token.getAttack() != null && token.getAttack().isInRangeOfAttackTarget())) {
                        if(current != walk){
                                float v = UtMath.scalarLimitsInterpolation(token.getMove().getMoveSpeed(), 1, 10, 0.25f, 1f);
                                animController.animate(walk.id, -1, v, null, .2f);
                                current = walk;
                        }
                } else {
                        if(current != idle){
                                animController.animate(idle.id, -1, .25f, null, .2f);
                                current = idle;
                        }
                }

                if (token.getAttack() != null && token.getAttack().isAttackingRanged()) {
                        Vector3 rotDir = temp;
                        Token attackTarget = token.getAttack().getAttackTarget();
                        if(attackTarget.getMove() == null){
                                world.getWorldCoords(
                                        attackTarget.getLocation().x,
                                        attackTarget.getLocation().y, rotDir);
                        }else{
                                world.getWorldCoords(
                                        attackTarget.getMove().getLocationFloatX(),
                                        attackTarget.getMove().getLocationFloatY(), rotDir);
                        }

                        rotDir.sub(translation);
                        UtMath.normalize(rotDir);
                        if(rotDir.z != -1){
                                tempTargetRot.setFromCross(Vector3.Z, rotDir);
                        }else{
                                tempTargetRot.set(Direction.North.quaternion);
                        }


                        float rotSpeed = delta * (UtMath.largest(token.getMove().getMoveSpeed(), 7) + 0.5f);
                        rotation.slerp(tempTargetRot, rotSpeed);
                } else if(token.getAttack() != null && token.getAttack().hasProjectile()){
                        float rotSpeed = delta * (UtMath.largest(token.getMove().getMoveSpeed(), 7) + 0.5f)*.05f;
                        rotation.slerp(token.getDirection().quaternion, rotSpeed);
                } else{
                        int rotMoveSpped = token.getMove() == null ? 7 : UtMath.largest(token.getMove().getMoveSpeed(), 7);
                        float rotSpeed = delta * (rotMoveSpped + 0.5f);
                        rotation.slerp(token.getDirection().quaternion, rotSpeed);
                }

                if (animController != null) {
                        animController.update(delta);
                }

        }

        @Override
        public void onAttack(Token target, boolean ranged) {
                if(ranged){
                        world.shootProjectile(token, target);
                }

                if (world.getHud().localPlayerToken == token)
                        world.getHud().onAttack(target, ranged);
        }

        @Override
        public void onAttacked(Token attacker, Token target, int damage, boolean dodge) {
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
        public void onStatusEffectChange(StatusEffects.Effect effect, float duration) {
                if (world.getHud().localPlayerToken == token)
                        world.getHud().onStatusEffectChange(effect, duration);
        }

        public void render(float delta) {

                modelInstance.transform.set(
                        translation.x + translationBase.x, translation.y + translationBase.y, translation.z + translationBase.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        scale.x, scale.y, scale.z
                );

                if (isVisible(world.modelBatch.getCamera()))
                        world.modelBatch.render(modelInstance, environment);

        }

        private boolean isVisible(Camera cam) {
                if(world.getLocalPlayerToken()!=null && world.getLocalPlayerToken().getFogMapping() != null){
                        return visU >0;
                }else{
                        return shape.isVisible(modelInstance.transform, cam);
                }

        }

        /**
         * @return -1 on no intersection,
         * or when there is an intersection: the squared distance between the center of this
         * object and the point on the ray closest to this object when there is intersection.
         */
        public float intersects(Ray ray) {
                return shape == null ? -1f : shape.intersects(modelInstance.transform, ray);
        }

        @Override
        public void dispose() {
                // if modelIntance isnt loaded from the AssetManager
                // then we need to dispose the model ourseleves
                if (assetLocation == null)
                        if (modelInstance != null)
                                modelInstance.model.dispose();

                if (this.token != null)
                        this.token.setListener(null);
                initialized = false;
        }

        public boolean isInitialized(){
                return initialized;
        }

}
