package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.item.ConsumableItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.token.Attack;
import asf.dungeon.model.token.Loot;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.shape.Shape;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
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

/**
 * Created by danny on 10/20/14.
 */
public class TokenSpatial implements Spatial, Token.Listener {

        private boolean initialized = false;
        private Environment environment;
        private ModelInstance modelInstance;
        private Shape shape;
        private AnimationController animController;
        private final Vector3 translationBase = new Vector3();
        protected final Vector3 translation = new Vector3();
        private final Quaternion rotation = new Quaternion();
        private final Vector3 scale = new Vector3(1, 1, 1);
        private DungeonWorld world;
        private Token token;
        protected float visU = 0; // how visible this object is, 0 = not drawn, 1 = fully visible, inbetween for partially visible

        public TokenSpatial(DungeonWorld world, Token token, Shape shape, Environment environment) {
                this.world = world;
                this.token = token;
                this.shape = shape;
                this.environment = environment;
                token.setListener(this);
        }

        public void preload(DungeonWorld world){

                world.assetManager.load(token.getModelId().assetLocation, Model.class);

                Loot loot = token.get(Loot.class);
                if(loot != null){
                        if(loot.getItem() instanceof PotionItem){
                                PotionItem potion = (PotionItem) loot.getItem();
                                world.assetManager.load(potion.getColor().textureAssetLocation, Texture.class);
                        }else if(loot.getItem() instanceof  KeyItem){

                        }
                }else{

                }


        }

        public void init(AssetManager assetManager) {
                initialized = true;

                if (!assetManager.isLoaded(token.getModelId().assetLocation, Model.class))
                        throw new Error("asset not loaded");

                Model model = assetManager.get(token.getModelId().assetLocation);
                modelInstance = new ModelInstance(model);

                if (shape != null)
                        shape.setFromModelInstance(modelInstance);

                if (modelInstance.animations.size > 0)
                        animController = new AnimationController(modelInstance);

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
                        }else if(loot.getItem() instanceof KeyItem){
                                //KeyItem key = (KeyItem) loot.get();

                        }
                }

                if(token.getModelId().assetLocation.contains("Characters")){
                        float s = .45f;
                        scale.set(s, s, s);
                        translationBase.set(0, (shape.getDimensions().y / 2f) + 1.45f, 0);
                }

                if(token.getModelId() == ModelId.Diablous || token.getModelId() == ModelId.Berzerker){
                        for (Material mat : modelInstance.materials) {
                                //GdxInfo.material(mat);
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
        private Item currentItemUse;
        private static final Vector3 temp = new Vector3();
        private final Quaternion tempTargetRot = new Quaternion();


        public void update(final float delta) {

                float minVisU = 0;
                // if fogmapping is enabled, change its visU value based on the fogstate of the tile its on.
                if( world.getLocalPlayerToken() != null && world.getLocalPlayerToken().getFogMapping() != null){
                        FogMap fogMap = world.getLocalPlayerToken().getFogMapping().getCurrentFogMap();
                        if(fogMap.isVisible(token.getLocation().x, token.getLocation().y)){
                                visU+=delta*.5f;
                        }else{
                                visU-=delta*.75f;
                                // if in a visited tile, "non move" tokens (eg crates)
                                // will still be seen through the fog
                                // also below checks should be done so that their animation
                                // state doesnt get updated while in the fog.
                                if(token.getMove() == null && fogMap.isVisited(token.getLocation().x, token.getLocation().y)){
                                        minVisU = .2f;
                                }
                        }
                        visU = MathUtils.clamp(visU,minVisU,1);
                }else{
                        visU = 1;
                }

                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);
                        colorAttribute.color.a = visU;
                }

                if(token.getMove() == null)
                        world.getWorldCoords(token.getLocation().x, token.getLocation().y, translation);
                else
                        world.getWorldCoords(token.getMove().getLocationFloatX(), token.getMove().getLocationFloatY(), translation);

                // changing animations and rotations is not allowed for
                // objects that modify the minVisU (eg these items are in the fog of war but still visible)
                if(minVisU == 0 || visU != minVisU)
                        updateIfNotFogBlocked(delta);


                if (animController != null) {
                        animController.update(delta);
                }

        }

        private void updateIfNotFogBlocked(float delta){
                if (currentItemUse != null) {
                        if(currentItemUse instanceof ConsumableItem){
                                Gdx.app.log("CharacterTokenControl", "bloob bloob bloob " + currentItemUse.getNameFromJournal(token) + "!");
                        }else if(currentItemUse instanceof KeyItem){

                        }

                        currentItemUse = null;
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
                        float rotMoveSpeed = token.getMove() == null ? 7 : UtMath.largest(token.getMove().getMoveSpeed(), 7f);
                        float rotSpeed = delta * (rotMoveSpeed + 0.5f);
                        rotation.slerp(token.getDirection().quaternion, rotSpeed);
                }
        }



        @Override
        public void onPathBlocked(Pair nextLocation, Tile nextTile) {
                if (world.getHud().localPlayerToken == token)
                        world.getHud().onPathBlocked(nextLocation, nextTile);
        }

        @Override
        public void onAttack(Token target, Pair targetLocation, boolean ranged) {
                if(ranged){
                        world.shootProjectile(token, target, targetLocation);
                }

                if (world.getHud().localPlayerToken == token)
                        world.getHud().onAttack(target, targetLocation, ranged);
        }

        @Override
        public void onAttacked(Token attacker, Token target, Attack.AttackOutcome attackOutcome) {
                // always foward this, let the Hud decide if the information is worth showing
                world.getHud().onAttacked(attacker, target,attackOutcome);
        }

        @Override
        public void onInventoryChanged() {
                if (world.getHud().localPlayerToken == token)
                        world.getHud().onInventoryChanged();

        }

        @Override
        public void onUseItem(Item item) {
                currentItemUse = item;
                if (world.getHud().localPlayerToken == token)
                        world.getHud().onUseItem(item);
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

        public Token getToken() {
                return token;
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
                if (this.token != null)
                        this.token.setListener(null);
                initialized = false;
        }

        public boolean isInitialized(){
                return initialized;
        }

}
