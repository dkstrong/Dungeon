package asf.dungeon.view.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.SpikeTrap;
import asf.dungeon.model.token.StatusEffect;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.DungeonWorld;
import asf.dungeon.view.Spatial;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by danny on 10/20/14.
 */
public class CharacterTokenSpatial extends AbstractTokenSpatial implements Spatial, DungeonWorld.LoadedNotifyable {

        private boolean initialized = false;
        private ModelInstance modelInstance;
        private AnimationController animController;
        private Decal shadowDecal;
        private final Vector3 translationBase = new Vector3();
        private final Vector3 scale = new Vector3(1, 1, 1);

        private WeaponItem loadedWeaponItem;
        private ModelInstance weaponModelInstance;
        private AnimationController weaponAnimController;
        private Animation weaponAttackAnim;
        private Node weaponAttachmentNode;
        private Node projectileSpawnNode;
        private ModelInstance offhandModelInstance;
        private Node offhandAttachmentNode;


        public CharacterTokenSpatial(DungeonWorld world, Token token) {
                super(world, token);
        }

        public void preload(DungeonWorld world) {


                ModelLoader.ModelParameters params = new ModelLoader.ModelParameters();
                params.textureParameter.magFilter = Texture.TextureFilter.Nearest;
                params.textureParameter.minFilter = Texture.TextureFilter.Nearest;
                world.assetManager.load(world.assetMappings.getAssetLocation(token.modelId), Model.class,params);
                refreshWeaponAttachment();

                // check to see if the token spawned with a projectile, spawn the projectile with it if thats the case
                if (token.attack != null && token.attack.hasProjectile()) {
                        world.fxManager.shootProjectile(token.attack.getWeapon().projectileFx, token, token.attack.getProjectileAttackTarget(), token.attack.getProjectileAttackCoord());
                }

        }

        public boolean isUsing3dModel(String assetLocation3dModel){
                return super.isUsing3dModel(assetLocation3dModel) || (loadedWeaponItem!= null && world.assetMappings.getAssetLocation(loadedWeaponItem.modelId).equals(assetLocation3dModel));
        }

        public void init(AssetManager assetManager) {
                initialized = true;

                Model model = assetManager.get(world.assetMappings.getAssetLocation(token.modelId));
                modelInstance = new ModelInstance(model);

                //if (shape != null)
                //        shape.setFromModelInstance(modelInstance);

                if (modelInstance.animations.size > 0)
                        animController = new AnimationController(modelInstance);

//                Gdx.app.log("CharacterTokenSpatial",token.name);
//                for (Material material : modelInstance.materials) {
//                        //material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
//                        TextureDescriptor<Texture> texDesc = material.get(TextureAttribute.class, TextureAttribute.Diffuse).textureDescription;
//                        Gdx.app.log("Mag",texDesc.magFilter.name());
//                        Gdx.app.log("Min",texDesc.minFilter.name());
//                }

                if (
                        token.modelId != ModelId.Knight &&
                                token.modelId != ModelId.Archer &&
                                token.modelId != ModelId.Mage &&
                                token.modelId != ModelId.Goblin &&
                                token.modelId != ModelId.Rat &&
                                token.modelId != ModelId.RockMonster &&
                                token.modelId != ModelId.Skeleton) {
                        float s = .45f;
                        scale.set(s, s, s);
                        translationBase.set(0, (world.floorSpatial.tileBox.getDimensions().y / 2f) + 2.65f, 0);
                }

                if (token.modelId == ModelId.Diablous || token.modelId == ModelId.Berzerker || token.modelId == ModelId.Priest
                        || token.modelId == ModelId.Scroll || token.modelId == ModelId.Torch) {
                        for (Material mat : modelInstance.materials) {
                                //GdxInfo.material(mat);
                                mat.set(new IntAttribute(IntAttribute.CullFace, 0));
                        }
                }


                for (Animation animation : modelInstance.animations) {
                        String animId = animation.id.toLowerCase();
                        if (animId.contains("walk") && !animId.contains("old")) walk = animation;
                        else if (animId.contains("pile_of_rocks")) monsterTrap = animation;
                        else if (animId.contains("sprint")) sprint = animation;
                        else if (animId.contains("rockpush")) rockPush = animation;
                        else if (animId.contains("keyturn")) keyTurn = animation;
                        else if (animId.contains("dazed")) dazed = animation;
                        else if (animId.contains("idle")) idle = animation;
                        else if (animId.contains("attacksword")) attackSword = animation;
                        else if (animId.contains("attackbow")) attackBow = animation;
                        else if (animId.contains("attackstaff")) attackStaff = animation;
                        else if (animId.contains("attack")) attack = animation;
                        else if (animId.contains("hit") || animId.contains("damage")) hit = animation;
                        else if (animId.contains("die")) die = animation;


                }
                if (attackUnarmed == null) attackUnarmed = attack;
                if (attackSword == null) attackSword = attack;
                if (attackBow == null) attackBow = attack;
                if (attackStaff == null) attackStaff = attack;
                if (walk == null) walk = idle;
                if (sprint == null) sprint = walk;
                if (rockPush == null) rockPush = walk;
                if (keyTurn == null) keyTurn = walk;
                if (dazed == null) dazed = idle;
                if (monsterTrap == null) monsterTrap = idle;

                if(token.monsterTrap != null)
                        animController.animate(monsterTrap.id, -1, 0f, null, 0.3f);

                // check to see if token spawned with status effects already on, if so then shot their Fx and hud information
                if (token.statusEffects != null) {
                        for (StatusEffect effect : StatusEffects.effectValues) {
                                if (token.statusEffects.has(effect)) {
                                        float duration = token.statusEffects.getDuration(effect);
                                        onStatusEffectChange(effect, duration);
                                }
                        }
                }

                shadowDecal = Decal.newDecal(
                        world.floorSpatial.tileDimensions.x,
                        world.floorSpatial.tileDimensions.z,
                        world.pack.findRegion("Textures/TokenShadow"),
                        GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shadowDecal.rotateX(-90);
                shadowDecal.setColor(1, 1, 1, 0.5f);

        }

        private Animation current,
                idle, walk, monsterTrap, sprint, rockPush, keyTurn, dazed, attack, attackUnarmed, attackSword, attackBow, attackStaff, hit, die;

        private void refreshWeaponAttachment() {
                WeaponItem weaponSlot = token.inventory.getWeaponSlot();
                if (weaponSlot != loadedWeaponItem) {
                        if (weaponSlot == null) {
                                unloadWeaponAttachment();
                        } else {
                                loadedWeaponItem = weaponSlot;
                                String weaponAssetLocation = world.assetMappings.getAssetLocation(token.inventory.getWeaponSlot().getModelId());
                                world.assetManager.load(weaponAssetLocation, Model.class);
                                if (!loadedWeaponItem.isRanged())
                                        world.assetManager.load("Models/Loot/Sword/Shield.g3db", Model.class);
                                world.notifyOnLoaded(this);
                        }
                }
        }

        private void unloadWeaponAttachment() {
                loadedWeaponItem = null;
                weaponModelInstance = null;
                weaponAnimController = null;
                weaponAttackAnim = null;
                weaponAttachmentNode = null;
                offhandModelInstance = null;
                offhandAttachmentNode = null;
                attack = attackUnarmed;
        }

        @Override
        public boolean onLoaded() {
                if (modelInstance == null)
                        return false;
                WeaponItem weaponSlot = token.inventory.getWeaponSlot();
                if (weaponSlot == null) {
                        unloadWeaponAttachment();
                        return true;
                }
                boolean offhandIsPrimary = weaponSlot.getModelId().name().contains("Bow");

                weaponAttachmentNode = offhandIsPrimary ? modelInstance.getNode("attach_l", true, true) : modelInstance.getNode("attach_r", true, true);
                if (weaponAttachmentNode == null) {
                        unloadWeaponAttachment();
                        loadedWeaponItem = weaponSlot; // to preferent spamming the loaded notifyable
                        return true;
                }

                //GdxInfo.model(modelInstance.model);

                String weaponAssetLocation = world.assetMappings.getAssetLocation(weaponSlot.getModelId());
                if (!world.assetManager.isLoaded(weaponAssetLocation, Model.class))
                        return false;

                Model weaponModel = world.assetManager.get(weaponAssetLocation, Model.class);
                weaponModelInstance = new ModelInstance(weaponModel);
                if (weaponModelInstance.animations.size > 0) {
                        weaponAnimController = new AnimationController(weaponModelInstance);
                        weaponAnimController.allowSameAnimation = true;
                        weaponAttackAnim = weaponModelInstance.animations.get(0);
                }

                if(!offhandIsPrimary && weaponSlot.isRanged() && weaponModelInstance.nodes.size > 1){
                        projectileSpawnNode = weaponModelInstance.nodes.get(1);
                }else{
                        projectileSpawnNode = null;
                }


                if (weaponSlot.isRanged()) attack = offhandIsPrimary ? attackBow : attackStaff;
                else attack = attackSword;

                if (token.modelId == ModelId.Knight && !weaponSlot.isRanged()) {
                        Model weaponOffhandModel = world.assetManager.get("Models/Loot/Sword/Shield.g3db", Model.class);
                        offhandModelInstance = new ModelInstance(weaponOffhandModel);
                        offhandAttachmentNode = modelInstance.getNode("shield", true, true);
                } else {
                        offhandModelInstance = null;
                        offhandAttachmentNode = null;
                }

                return true;
        }


        public void update(final float delta) {
                FogState fogState = world.floorSpatial.fogMap == null ? FogState.Visible : world.floorSpatial.fogMap.getFogState(token.location.x, token.location.y);
                float minVisU = 0;
                float maxVisU = 1;

                // TODO: i should have a targetVisuU and just lerp to that instead of how i have it here
                // as is if you are fully visible, then get the invisibility status effct you'll "snap" to visuU = 0.5f
                if (fogState == FogState.Visible) {
                        visU += delta * .65f;

                        if (token.statusEffects.has(StatusEffect.Invisibility)) {
                                if (token == world.hudSpatial.localPlayerToken)
                                        maxVisU = .5f;
                                else
                                        maxVisU = .45f;
                        }
                } else {
                        visU -= delta * .75f;
                }

                visU = MathUtils.clamp(visU, minVisU, maxVisU);


                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
                        //colorAttribute.color.a = visU;
                        if (fogState == FogState.MagicMapped) {
                                colorAttribute.color.set(visU * 0.7f, visU * .8f, visU, 1);
                        } else {
                                colorAttribute.color.set(visU, visU, visU, 1);
                        }
                }

                world.getWorldCoords(token.move.getFloatLocation(), translation);

                // changing animations and rotations is not allowed for
                // objects that modify the minVisU (eg these items are in the fog of war but still visible)
                if (minVisU == 0 || visU != minVisU)
                        updateIfNotFogBlocked(delta);

                if (animController != null) {
                        animController.update(delta);

                        if (weaponAnimController != null)
                                weaponAnimController.update(delta);
                }

        }

        private void updateIfNotFogBlocked(float delta) {

                if (token.damage.isDead()) {
                        if (current != die) {
                                animController.animate(die.id, 1, 1, null, .2f); // die.duration / token.damage.getDeathDuration()
                                current = die;
                                world.sounds.play(token.damage.getDeathSfx());
                        }

                } else if (token.attack != null && token.attack.isAttacking()) {
                        if (current != attack) {
                                animController.animate(attack.id, 1, attack.duration / token.attack.getWeapon().attackDuration, null, .2f);
                                if (weaponAnimController != null)
                                        weaponAnimController.animate(weaponAttackAnim.id, 1, weaponAttackAnim.duration / token.attack.getWeapon().attackDuration, null, .2f);
                                if(token.attack.getWeapon().projectileFx != null){
                                        world.fxManager.spawnProjectile(token.attack.getWeapon().projectileFx, token, token.attack.getAttackTarget());
                                }

                                current = attack;
                        }
                } else if (token.damage.isHit()) {
                        if (current != hit) {
                                if (world.hudSpatial.localPlayerToken == token) {
                                        world.hudSpatial.setMapViewMode(false); // if being attacked, force out of map view mode to make it easier to respond
                                }

                                if(token.damage.getHitSource().get(SpikeTrap.class) != null ){
                                        animController.animate(hit.id, .75f, -1,1, hit.duration/ (token.damage.getHitDuration()+0.75f),null,0.2f);
                                }else{
                                        animController.animate(hit.id, 1, hit.duration / token.damage.getHitDuration(), null, .2f);
                                }
                                current = hit;
                                world.sounds.play(token.damage.getHitSfx());
                        }
                } else if (token.inventory.isKeyTurn()) {
                        if (current != keyTurn) {
                                animController.animate(keyTurn.id, 1, 1f, null, 0.3f);
                                current = keyTurn;
                        }
                } else if (token.move.isPushingBoulder()) {
                        if (current != rockPush) {
                                animController.animate(rockPush.id, 1, null, 0.3f);
                                current = rockPush;
                        }
                }else if (token.statusEffects.has(StatusEffect.Paralyze)) {
                        if (current != dazed) {
                                animController.animate(dazed.id, -1, 1f, null, 0.3f);
                                current = dazed;
                        }
                }else  if (token.statusEffects.has(StatusEffect.Frozen)) {
                        if (current != dazed) {
                                animController.animate(dazed.id, -1, 0, null, 0.3f);
                                current = dazed;
                        }
                } else if(token.move.isMoving() && (token.attack == null || !token.attack.isInRangeOfAttackTarget())){
                        if (token.statusEffects.has(StatusEffect.Speed)) {
                                if (current != sprint) {
                                        float v = UtMath.scalarLimitsInterpolation(token.move.getMoveSpeed(), 1, 10, .91f, 1.25f);
                                        animController.animate(sprint.id, -1, v, null, 0.3f);
                                        current = sprint;
                                }
                        } else {
                                if (current != walk) {
                                        float v = UtMath.scalarLimitsInterpolation(token.move.getMoveSpeed(), 1, 10, 1f, 1.5f);
                                        animController.animate(walk.id, -1, v, null, 0.3f);
                                        current = walk;
                                }
                        }
                }else{
                        if(token.monsterTrap == null || token.monsterTrap.isAwake()){
                                if (current != idle) {
                                        animController.animate(idle.id, -1, 1f, null, 0.3f);
                                        current = idle;
                                }
                        }else{
                                if(token.monsterTrap.isTriggered() && current != monsterTrap){
                                        animController.animate(monsterTrap.id, 1, 1f, null, 0.3f);
                                        animController.queue(idle.id, -1, 1f, null, 03f);
                                        current = monsterTrap;
                                }
                        }
                }





                if (token.damage != null && !token.damage.isDead() && token.attack != null && token.attack.isAttackingRanged()) {
                        float rotMoveSpeed = token.move == null ? 7 : UtMath.largest(token.move.getMoveSpeed(), 7f);
                        float rotSpeed = delta * (rotMoveSpeed + 0.5f);
                        Direction dir = token.location.direction(token.attack.getAttackTarget().location);
                        Quaternion tokenDirRot = world.assetMappings.getRotation(dir);
                        rotation.slerp(tokenDirRot, rotSpeed);
                } else {
                        float rotMoveSpeed = UtMath.largest(token.move.getMoveSpeed(), 7f);
                        float rotSpeed = delta * (rotMoveSpeed + 0.5f);
                        Quaternion tokenDirRot = world.assetMappings.getRotation(token.direction);
                        rotation.slerp(tokenDirRot, rotSpeed);
                }


        }


        public void render(float delta) {
                if (visU <= 0) return;
                if (world.hudSpatial.isMapViewMode()) {
                        if (!world.cam.frustum.sphereInFrustumWithoutNearFar(translation, 5)) return;
                } else if (world.hudSpatial.localPlayerToken != null && world.hudSpatial.localPlayerToken.location.distance(token.location) > 16) return;


                modelInstance.transform.set(
                        translation.x + translationBase.x, translation.y + translationBase.y, translation.z + translationBase.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        scale.x, scale.y, scale.z
                );
                world.modelBatch.render(modelInstance, world.environment);

                if (weaponModelInstance != null) {
                        weaponModelInstance.transform.set(modelInstance.transform).mul(weaponAttachmentNode.globalTransform);
                        weaponModelInstance.transform.rotate(Vector3.Z, -90);
                        world.modelBatch.render(weaponModelInstance, world.environment);
                        if (offhandModelInstance != null) {
                                offhandModelInstance.transform.set(modelInstance.transform).mul(offhandAttachmentNode.globalTransform);
                                offhandModelInstance.transform.rotate(Vector3.Z, -90);
                                world.modelBatch.render(offhandModelInstance, world.environment);
                        }
                }


                if(!token.damage.isDead()){
                        shadowDecal.setPosition(translation);
                        shadowDecal.translateY(0.1f);
                        world.decalBatch.add(shadowDecal);
                }

        }

        @Override
        public void onInventoryChanged() {
                super.onInventoryChanged();
                refreshWeaponAttachment();
        }

        /**
         * the current world translation of the weapon attachment bone
         * if this model does not have a weapon bone bone then it will return the
         * models translation.
         * <p/>
         * this is primarily used for spawning Fx
         *
         * @param store
         */
        public void getWeaponAttachmentTranslation(Vector3 store) {
                if (weaponModelInstance == null){
                        store.set(translation).y = 4;
                }else if(projectileSpawnNode != null){
                        weaponModelInstance.transform.cpy().mul(projectileSpawnNode.globalTransform).getTranslation(store);
                }
                else {
                        weaponModelInstance.transform.getTranslation(store);
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
                // TODO: character tokens actually need a taller box for accurate collision detection
                // NOTE: model instance transofrm is only updated if it is not culled this might lead to invalid intersections
                return Intersector.intersectRayBoundsFast(ray, translation, world.floorSpatial.tileDimensions) ? 1 : -1;
                //return world.floorSpatial.tileBox.intersects(modelInstance.transform, ray);
        }

        @Override
        public void dispose() {
                super.dispose();

                initialized = false;
        }

        public boolean isInitialized() {
                return initialized;
        }

}
