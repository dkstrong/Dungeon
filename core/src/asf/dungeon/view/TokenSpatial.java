package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.SfxId;
import asf.dungeon.model.Tile;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.token.Attack;
import asf.dungeon.model.token.CharacterInventory;
import asf.dungeon.model.token.Fountain;
import asf.dungeon.model.token.Loot;
import asf.dungeon.model.token.StatusEffect;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;
import asf.dungeon.model.token.logic.fsm.State;
import asf.dungeon.model.token.quest.Dialouge;
import asf.dungeon.model.token.quest.Quest;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.UBJsonReader;

/**
 * Created by danny on 10/20/14.
 */
public class TokenSpatial implements Spatial, Token.Listener {

        private boolean initialized = false;
        private ModelInstance modelInstance;
        private AnimationController animController;
        private Decal shadowDecal;
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

                if(token.getModelId() != ModelId.UserMonster){
                        world.assetManager.load(world.assetMappings.getAssetLocation(token.getModelId()), Model.class);
                }

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
                        Fountain fountain = token.get(Fountain.class);
                        world.assetManager.load(world.assetMappings.getFountainTextureAssetLocation(fountain), Texture.class);
                        world.assetManager.load(world.assetMappings.getEmptyFountainTextureAssetLocation(fountain), Texture.class);
                }

                // check to see if the token spawned with a projectile, spawn the projectile with it if thats the case
                if(token.getAttack() != null && token.getAttack().hasProjectile()){
                        world.fxManager.shootProjectile(token.getAttack().getWeapon().getProjectileFx(), token, token.getAttack().getProjectileAttackTarget(), token.getAttack().getProjectileAttackCoord());
                }

        }

        public void init(AssetManager assetManager) {
                initialized = true;

                if(token.getModelId() != ModelId.UserMonster){
                        Model model = assetManager.get(world.assetMappings.getAssetLocation(token.getModelId()));
                        modelInstance = new ModelInstance(model);
                }else{
                        FileHandle fileHandle = AssetMappings.getUserMonsterLocation();
                        ModelLoader loader = new G3dModelLoader(new UBJsonReader());
                        Model model = loader.loadModel(fileHandle);
                        modelInstance = new ModelInstance(model);
                }

                shadowDecal = Decal.newDecal(
                        world.floorSpatial.tileDimensions.x,
                        world.floorSpatial.tileDimensions.z,
                        world.pack.findRegion("Textures/TokenShadow"),
                        GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shadowDecal.rotateX(-90);
                shadowDecal.setColor(1,1,1,0.5f);


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
                                Texture fountainTex = world.assetManager.get(world.assetMappings.getEmptyFountainTextureAssetLocation(fountain), Texture.class);
                                Material mat = modelInstance.materials.get(0);
                                mat.set(TextureAttribute.createDiffuse(fountainTex));
                        }else{
                                Texture fountainTex = world.assetManager.get(world.assetMappings.getFountainTextureAssetLocation(fountain), Texture.class);
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

                // check to see if token spawned with status effects already on, if so then shot their Fx and hud information
                if(token.getStatusEffects()!= null){
                        for (StatusEffect effect : StatusEffects.effectValues) {
                                if(token.getStatusEffects().has(effect)){
                                        float duration = token.getStatusEffects().getDuration(effect);
                                        onStatusEffectChange(effect, duration);
                                }
                        }
                }


        }

        private Animation current, idle, walk, attack, hit, die;


        public void update(final float delta) {

                float minVisU = 0;
                float maxVisU = 1;
                // if fogmapping is enabled, change its visU value based on the fogstate of the tile its on.
                FogState fogState;
                if(world.getLocalPlayerToken() != null && world.getLocalPlayerToken().getFogMapping() != null){
                        fogState = world.getLocalPlayerToken().getFogMapping().getCurrentFogMap().getFogState(token.getLocation().x, token.getLocation().y);
                }else{
                        fogState = FogState.Visible;
                }

                // TODO: i should have a targetVisuU and just lerp to that instead of how i have it here
                // as is if you are fully visible, then get the invisibility status effct you'll "snap" to visuU = 0.5f
                if(fogState == FogState.Visible){
                        visU += delta * .65f;
                        if(token.getStatusEffects() != null && token.getStatusEffects().has(StatusEffect.Invisibility)){
                                maxVisU = .5f;
                        }
                }else{
                        visU -= delta * .75f;
                        // crates can be seen in visited fog and magic mapped fog
                        // loot can only be seen in visited fog
                        if (token.getCrateInventory() != null && (fogState == FogState.Visited || fogState == FogState.MagicMapped)) {
                                minVisU = .3f;
                        }else if(token.getMove() == null && fogState == FogState.Visited){
                                minVisU = .3f;
                        }
                }
                visU = MathUtils.clamp(visU, minVisU, maxVisU);


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
                        world.getWorldCoords(token.getMove().getFloatLocation(), translation);

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
                                        Texture fountainTex = world.assetManager.get(world.assetMappings.getEmptyFountainTextureAssetLocation(fountain), Texture.class);
                                        Material mat = modelInstance.materials.get(0);
                                        mat.set(TextureAttribute.createDiffuse(fountainTex));
                                }else{
                                        Texture fountainTex = world.assetManager.get(world.assetMappings.getFountainTextureAssetLocation(fountain), Texture.class);
                                        Material mat = modelInstance.materials.get(0);
                                        mat.set(TextureAttribute.createDiffuse(fountainTex));
                                }
                        }


                }

                if (token.getDamage() != null && token.getDamage().isDead()) {
                        if (current != die) {
                                animController.animate(die.id, 1, die.duration / token.getDamage().getDeathDuration(), null, .2f);
                                current = die;
                                world.sounds.play(token.getDamage().getDeathSfx());
                        }

                } else if (token.getAttack() != null && token.getAttack().isAttacking()) {
                        if (current != attack) {
                                animController.animate(attack.id, 1, attack.duration / token.getAttack().getWeapon().getAttackDuration(), null, .2f);

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
                                world.sounds.play(token.getDamage().getHitSfx());
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



                if(token.getDamage() != null && !token.getDamage().isDead() && token.getAttack()!=null &&token.getAttack().isAttackingRanged()){
                        float rotMoveSpeed = token.getMove() == null ? 7 : UtMath.largest(token.getMove().getMoveSpeed(), 7f);
                        float rotSpeed = delta * (rotMoveSpeed + 0.5f);
                        Direction dir = token.getLocation().direction(token.getAttack().getAttackTarget().getLocation());
                        Quaternion tokenDirRot = world.assetMappings.getRotation(dir);
                        rotation.slerp(tokenDirRot, rotSpeed);
                }else{
                        float rotMoveSpeed = token.getMove() == null ? 7 : UtMath.largest(token.getMove().getMoveSpeed(), 7f);
                        float rotSpeed = delta * (rotMoveSpeed + 0.5f);
                        Quaternion tokenDirRot = world.assetMappings.getRotation(token.getDirection());
                        rotation.slerp(tokenDirRot, rotSpeed);
                }


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

                                        TokenSpatial targetTokenSpatial = world.getTokenSpatial(out.targetToken);

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

        public void render(float delta) {

                modelInstance.transform.set(
                        translation.x + translationBase.x, translation.y + translationBase.y, translation.z + translationBase.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        scale.x, scale.y, scale.z
                );



                if (visU > 0 && world.floorSpatial.tileSphere.isVisible(modelInstance.transform, world.cam)){
                        world.modelBatch.render(modelInstance, world.environment);
                        shadowDecal.setPosition(translation);
                        shadowDecal.translateY(0.1f);
                        world.decalBatch.add(shadowDecal);
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
                return world.floorSpatial.tileBox.intersects(modelInstance.transform, ray);
        }

        @Override
        public void dispose() {
                if (this.token != null)
                        this.token.setListener(null);

                if(this.token.getModelId() == ModelId.UserMonster){
                        modelInstance.model.dispose();
                }
                initialized = false;
        }

        public boolean isInitialized() {
                return initialized;
        }

}
