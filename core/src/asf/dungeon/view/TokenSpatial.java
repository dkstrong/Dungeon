package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.token.Attack;
import asf.dungeon.model.token.Fountain;
import asf.dungeon.model.token.Inventory;
import asf.dungeon.model.token.Loot;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.quest.Dialouge;
import asf.dungeon.model.token.quest.Quest;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
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
        private ModelInstance modelInstance;
        private AnimationController animController;
        private final Vector3 translationBase = new Vector3();
        protected final Vector3 translation = new Vector3();
        protected final Quaternion rotation = new Quaternion();
        private final Vector3 scale = new Vector3(1, 1, 1);
        private DungeonWorld world;
        private Token token;
        protected float visU = 0; // how visible this object is, 0 = not drawn, 1 = fully visible, inbetween for partially visible
        private boolean texToggle = false;

        public TokenSpatial(DungeonWorld world, Token token) {
                this.world = world;
                this.token = token;
                token.setListener(this);
        }

        public void preload(DungeonWorld world) {


                world.assetManager.load(world.assetMappings.getAssetLocation(token.getModelId()), Model.class);

                Loot loot = token.get(Loot.class);
                if (loot != null) {
                        if (loot.getItem() instanceof PotionItem) {
                                PotionItem potion = (PotionItem) loot.getItem();
                                world.assetManager.load(world.assetMappings.getPotionTextureAssetLocation(potion), Texture.class);
                        } else if (loot.getItem() instanceof ScrollItem) {
                                ScrollItem scroll = (ScrollItem) loot.getItem();
                                world.assetManager.load(world.assetMappings.getScrollTextureAssetLocation(scroll), Texture.class);
                        } else if (loot.getItem() instanceof BookItem) {
                                BookItem book = (BookItem) loot.getItem();
                                String assetLocation = world.assetMappings.getBookTextureAssetLocation(book);
                                world.assetManager.load(assetLocation, Texture.class);
                        }
                } else {

                }

                if(token.getModelId() == ModelId.Fountain){
                        world.assetManager.load(world.assetMappings.getFountainTextureAssetLocation(token), Texture.class);
                        world.assetManager.load(world.assetMappings.getEmptyFountainTextureAssetLocation(token), Texture.class);
                }


        }

        public void init(AssetManager assetManager) {
                initialized = true;

                if (!assetManager.isLoaded(world.assetMappings.getAssetLocation(token.getModelId()), Model.class))
                        throw new Error("asset not loaded");

                Model model = assetManager.get(world.assetMappings.getAssetLocation(token.getModelId()));
                modelInstance = new ModelInstance(model);

                //if (shape != null)
                //        shape.setFromModelInstance(modelInstance);

                if (modelInstance.animations.size > 0)
                        animController = new AnimationController(modelInstance);

                //for (Material material : modelInstance.materials) {
                        //material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                //}

                Loot loot = token.get(Loot.class);
                if (loot != null) {
                        if (loot.getItem() instanceof PotionItem) {
                                PotionItem potion = (PotionItem) loot.getItem();
                                Texture potionTex = assetManager.get(world.assetMappings.getPotionTextureAssetLocation(potion), Texture.class);
                                Material mat = modelInstance.materials.get(0);
                                mat.set(TextureAttribute.createDiffuse(potionTex));
                                //ColorAttribute colorAttribute = (ColorAttribute)mat.get(ColorAttribute.Diffuse);
                                //colorAttribute.color.set(potion.getColor().color);
                        } else if (loot.getItem() instanceof ScrollItem) {
                                ScrollItem scroll = (ScrollItem) loot.getItem();
                                Texture scrollTex = assetManager.get(world.assetMappings.getScrollTextureAssetLocation(scroll), Texture.class);
                                Material mat = modelInstance.materials.get(0);
                                mat.set(TextureAttribute.createDiffuse(scrollTex));

                        } else if (loot.getItem() instanceof BookItem) {
                                BookItem book = (BookItem) loot.getItem();
                                Texture bookTex = assetManager.get(world.assetMappings.getBookTextureAssetLocation(book), Texture.class);
                                Material mat = modelInstance.materials.get(0);
                                mat.set(TextureAttribute.createDiffuse(bookTex));

                        }
                }

                if(token.getModelId() == ModelId.Fountain ){
                        Fountain fountain = token.get(Fountain.class);
                        texToggle = fountain.isConsumed();
                        if(fountain.isConsumed()){
                                Texture fountainTex = world.assetManager.get(world.assetMappings.getEmptyFountainTextureAssetLocation(token), Texture.class);
                                Material mat = modelInstance.materials.get(0);
                                mat.set(TextureAttribute.createDiffuse(fountainTex));
                        }else{
                                Texture fountainTex = world.assetManager.get(world.assetMappings.getFountainTextureAssetLocation(token), Texture.class);
                                Material mat = modelInstance.materials.get(0);
                                mat.set(TextureAttribute.createDiffuse(fountainTex));
                        }

                }

                if (world.assetMappings.getAssetLocation(token.getModelId()).contains("Characters")) {
                        if(token.getModelId() != ModelId.Skeleton){
                                float s = .45f;
                                scale.set(s, s, s);

                                translationBase.set(0, (world.floorSpatial.tileBox.getDimensions().y / 2f) + 1.45f, 0);
                        }
                }

                if (token.getModelId() == ModelId.Diablous || token.getModelId() == ModelId.Berzerker || token.getModelId() == ModelId.Priest || token.getModelId() == ModelId.Scroll) {
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
        private static final Vector3 temp = new Vector3();
        private final Quaternion tempTargetRot = new Quaternion();


        public void update(final float delta) {

                float minVisU = 0;
                // if fogmapping is enabled, change its visU value based on the fogstate of the tile its on.
                FogState fogState;
                if(world.getLocalPlayerToken() != null && world.getLocalPlayerToken().getFogMapping() != null){
                        fogState = world.getLocalPlayerToken().getFogMapping().getCurrentFogMap().getFogState(token.getLocation().x, token.getLocation().y);
                }else{
                        fogState = FogState.Visible;
                }


                if(fogState == FogState.Visible){
                        visU += delta * .65f;
                }else{
                        visU -= delta * .75f;
                        // crates can be seen in visited fog and magic mapped fog
                        // loot can only be seen in visited fog
                        if (token.getSimpleInventory() != null && (fogState == FogState.Visited || fogState == FogState.MagicMapped)) {
                                minVisU = .3f;
                        }else if(token.getMove() == null && fogState == FogState.Visited){
                                minVisU = .3f;
                        }
                }
                visU = MathUtils.clamp(visU, minVisU, 1);


                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
                        //colorAttribute.color.a = visU;
                        if(fogState == FogState.MagicMapped){
                                colorAttribute.color.set(visU*0.7f,visU*.8f,visU,1);
                        }else{
                                colorAttribute.color.set(visU,visU,visU,1);
                        }
                }

                if (token.getMove() == null)
                        world.getWorldCoords(token.getLocation().x, token.getLocation().y, translation);
                else
                        world.getWorldCoords(token.getMove().getLocationFloatX(), token.getMove().getLocationFloatY(), translation);

                // changing animations and rotations is not allowed for
                // objects that modify the minVisU (eg these items are in the fog of war but still visible)
                if (minVisU == 0 || visU != minVisU)
                        updateIfNotFogBlocked(delta);


                if (animController != null) {
                        animController.update(delta);
                }

        }

        private void updateIfNotFogBlocked(float delta) {

                if(token.getModelId() == ModelId.Fountain ){
                        Fountain fountain = token.get(Fountain.class);
                        if(texToggle != fountain.isConsumed()){
                                texToggle= fountain.isConsumed();
                                if(fountain.isConsumed()){
                                        Texture fountainTex = world.assetManager.get(world.assetMappings.getEmptyFountainTextureAssetLocation(token), Texture.class);
                                        Material mat = modelInstance.materials.get(0);
                                        mat.set(TextureAttribute.createDiffuse(fountainTex));
                                }else{
                                        Texture fountainTex = world.assetManager.get(world.assetMappings.getFountainTextureAssetLocation(token), Texture.class);
                                        Material mat = modelInstance.materials.get(0);
                                        mat.set(TextureAttribute.createDiffuse(fountainTex));
                                }
                        }


                }

                if (token.getDamage() != null && token.getDamage().isDead()) {
                        if (current != die) {
                                animController.animate(die.id, 1, die.duration / token.getDamage().getDeathDuration(), null, .2f);
                                current = die;
                        }

                } else if (token.getAttack() != null && token.getAttack().isAttacking()) {
                        if (current != attack) {
                                animController.animate(attack.id, 1, attack.duration / token.getAttack().getAttackDuration(), null, .2f);

                                current = attack;
                        }
                } else if (token.getDamage() != null && token.getDamage().isHit()) {
                        if (current != hit) {
                                if (world.hudSpatial.localPlayerToken == token){
                                        world.hudSpatial.setMapViewMode(false); // if being attacked, force out of map view mode to make it easier to respond
                                }

                                if (hit == null) {
                                        throw new Error(token.getName());
                                }
                                animController.animate(hit.id, 1, hit.duration / token.getDamage().getHitDuration(), null, .2f);
                                current = hit;
                        }
                } else if (token.getMove() != null && token.getMove().isMoving() && !(token.getAttack() != null && token.getAttack().isInRangeOfAttackTarget())) {
                        if (current != walk) {
                                float v = UtMath.scalarLimitsInterpolation(token.getMove().getMoveSpeed(), 1, 10, 0.25f, 1f);
                                animController.animate(walk.id, -1, v, null, .2f);
                                current = walk;
                        }
                } else {
                        if (current != idle) {
                                animController.animate(idle.id, -1, .25f, null, .2f);
                                current = idle;
                        }
                }

                if (token.getAttack() != null && token.getAttack().isAttackingRanged()) {
                        Vector3 rotDir = temp;
                        Token attackTarget = token.getAttack().getAttackTarget();
                        if (attackTarget.getMove() == null) {
                                world.getWorldCoords(
                                        attackTarget.getLocation().x,
                                        attackTarget.getLocation().y, rotDir);
                        } else {
                                world.getWorldCoords(
                                        attackTarget.getMove().getLocationFloatX(),
                                        attackTarget.getMove().getLocationFloatY(), rotDir);
                        }

                        rotDir.sub(translation);
                        UtMath.normalize(rotDir);

                        if (rotDir.z != -1) {
                                tempTargetRot.setFromCross(Vector3.Z, rotDir);
                        } else {
                                // TODO: this hackaround helps prevent some instances of where the
                                // rotation "loops over" and cases looking south when should really be north
                                // however it still happens in some instances
                                tempTargetRot.set(world.assetMappings.getRotation(Direction.North));
                        }


                        float rotSpeed = delta * (UtMath.largest(token.getMove().getMoveSpeed(), 7) + 0.5f);
                        rotation.slerp(tempTargetRot, rotSpeed);
                } else if (token.getAttack() != null && token.getAttack().hasProjectile()) {
                        float rotSpeed = delta * (UtMath.largest(token.getMove().getMoveSpeed(), 7) + 0.5f) * .05f;

                        Quaternion tokenDirRot = world.assetMappings.getRotation(token.getDirection());
                        rotation.slerp(tokenDirRot, rotSpeed);
                } else {
                        float rotMoveSpeed = token.getMove() == null ? 7 : UtMath.largest(token.getMove().getMoveSpeed(), 7f);
                        float rotSpeed = delta * (rotMoveSpeed + 0.5f);
                        Quaternion tokenDirRot = world.assetMappings.getRotation(token.getDirection());
                        rotation.slerp(tokenDirRot, rotSpeed);
                }
        }


        @Override
        public void onPathBlocked(Pair nextLocation, Tile nextTile) {
                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onPathBlocked(nextLocation, nextTile);
        }

        @Override
        public void onAttack(Token target, Pair targetLocation, boolean ranged) {
                if (ranged) {

                        world.fxManager.shootProjectile(token.getInventory().getWeaponSlot().getProjectileFx(), token, target, targetLocation);
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
        public void onUseItem(Item item, Inventory.Character.UseItemOutcome out) {
                if(out.didSomething){
                        if(item instanceof ScrollItem){
                                ScrollItem scroll = (ScrollItem ) item;
                                if(scroll.getType() == ScrollItem.Type.Lightning){

                                        TokenSpatial targetTokenSpatial = world.getTokenSpatial(out.targetToken);

                                        world.fxManager.spawnEffect(FxId.Lightning, targetTokenSpatial, 3);
                                }
                        }
                }


                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onUseItem(item, out);
        }


        @Override
        public void onStatusEffectChange(StatusEffects.Effect effect, float duration) {

                world.fxManager.spawnEffect(world.assetMappings.getStatusEffectFxId(effect), this, duration);

                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onStatusEffectChange(effect, duration);
        }

        @Override
        public void onLearned(Item journalObject, boolean study) {
                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onLearned(journalObject, false);
        }

        @Override
        public void onInteract(Quest quest, Dialouge dialouge) {
                if (world.hudSpatial.localPlayerToken == token)
                        world.hudSpatial.onInteract(quest, dialouge);
        }

        public void render(float delta) {

                modelInstance.transform.set(
                        translation.x + translationBase.x, translation.y + translationBase.y, translation.z + translationBase.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        scale.x, scale.y, scale.z
                );

                if (visU > 0 && world.floorSpatial.tileSphere.isVisible(modelInstance.transform, world.cam))
                        world.modelBatch.render(modelInstance, world.environment);

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
                return world.floorSpatial.tileBox.intersects(modelInstance.transform, ray);
        }

        @Override
        public void dispose() {
                if (this.token != null)
                        this.token.setListener(null);
                initialized = false;
        }

        public boolean isInitialized() {
                return initialized;
        }

}
