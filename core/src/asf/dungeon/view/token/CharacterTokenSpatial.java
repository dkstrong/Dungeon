package asf.dungeon.view.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.StatusEffect;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.BetterAnimationController;
import asf.dungeon.utility.BetterModelInstance;
import asf.dungeon.utility.GdxInfo;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.DungeonWorld;
import asf.dungeon.view.Spatial;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
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
        private BetterModelInstance modelInstance;
        private BetterAnimationController animController;
        private Decal shadowDecal;
        private final Vector3 translationBase = new Vector3();
        private final Vector3 scale = new Vector3(1, 1, 1);

        private WeaponItem loadedWeaponItem;
        private BetterModelInstance weaponModelInstance;
        private Node weaponAttachmentNode;
        private BetterModelInstance offhandModelInstance;
        private Node offhandAttachmentNode;

        public CharacterTokenSpatial(DungeonWorld world, Token token) {
                super(world, token);
        }

        public void preload(DungeonWorld world) {
                token.setListener(this);

                world.assetManager.load(world.assetMappings.getAssetLocation(token.getModelId()), Model.class);
                refreshWeaponAttachment();

                // check to see if the token spawned with a projectile, spawn the projectile with it if thats the case
                if (token.getAttack() != null && token.getAttack().hasProjectile()) {
                        world.fxManager.shootProjectile(token.getAttack().getWeapon().getProjectileFx(), token, token.getAttack().getProjectileAttackTarget(), token.getAttack().getProjectileAttackCoord());
                }

        }



        private void refreshWeaponAttachment(){
                WeaponItem weaponSlot = token.getInventory().getWeaponSlot();
                if(weaponSlot != loadedWeaponItem){
                        if(weaponSlot == null){
                                loadedWeaponItem = null;
                                weaponModelInstance = null;
                                weaponAttachmentNode = null;
                                offhandModelInstance = null;
                                offhandAttachmentNode = null;
                                attack = attackUnarmed;
                        }else{

                                loadedWeaponItem = weaponSlot;
                                String weaponAssetLocation = world.assetMappings.getAssetLocation(token.getInventory().getWeaponSlot().getModelId());
                                world.assetManager.load(weaponAssetLocation, Model.class);
                                if(!loadedWeaponItem.isRanged())
                                        world.assetManager.load("Models/Loot/Sword/shield_01.g3db", Model.class);
                                world.notifyOnLoaded(this);
                        }
                }
        }

        @Override
        public boolean onLoaded() {
                if(modelInstance == null)
                        return false;

                weaponAttachmentNode = modelInstance.getNode("palm_r", true, true);
                WeaponItem weaponSlot = token.getInventory().getWeaponSlot();
                if(weaponSlot == null || weaponAttachmentNode == null){
                        loadedWeaponItem = null;
                        weaponModelInstance = null;
                        weaponAttachmentNode = null;
                        offhandModelInstance = null;
                        offhandAttachmentNode = null;
                        attack = attackUnarmed;
                        return true;
                }

                String weaponAssetLocation = world.assetMappings.getAssetLocation(weaponSlot.getModelId());
                if(!world.assetManager.isLoaded(weaponAssetLocation, Model.class))
                        return false;

                Model swordModel = world.assetManager.get(weaponAssetLocation, Model.class);
                weaponModelInstance = new BetterModelInstance(swordModel);

                if(weaponSlot.isRanged()){
                        if(weaponSlot.getProjectileFx() == FxId.Arrow) attack = attackBow;
                        else attack = attackStaff;
                }else attack = attackSword;

                if(!weaponSlot.isRanged()){
                        Model weaponOffhandModel = world.assetManager.get("Models/Loot/Sword/shield_01.g3db", Model.class);
                        offhandModelInstance = new BetterModelInstance(weaponOffhandModel);
                        offhandAttachmentNode = modelInstance.getNode("palm_l", true, true);
                }

                // GdxInfo.model(modelInstance.model);
                return true;
        }

        public void init(AssetManager assetManager) {
                initialized = true;

                Model model = assetManager.get(world.assetMappings.getAssetLocation(token.getModelId()));
                modelInstance = new BetterModelInstance(model);

                //if (shape != null)
                //        shape.setFromModelInstance(modelInstance);

                if (modelInstance.animations.size > 0)
                        animController = new BetterAnimationController(modelInstance);


                //for (Material material : modelInstance.materials) {
                //material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                //}

                if (    token.getModelId() != ModelId.Knight &&
                        token.getModelId() != ModelId.Goblin &&
                        token.getModelId() != ModelId.Skeleton) {
                        float s = .45f;
                        scale.set(s, s, s);
                        translationBase.set(0, (world.floorSpatial.tileBox.getDimensions().y / 2f) + 1.45f, 0);
                }else if(token.getModelId() == ModelId.Knight){
                        float s = .55f;
                        scale.set(s, s, s);
                }

                if (token.getModelId() == ModelId.Diablous || token.getModelId() == ModelId.Berzerker || token.getModelId() == ModelId.Priest
                        || token.getModelId() == ModelId.Scroll || token.getModelId() == ModelId.Torch) {
                        for (Material mat : modelInstance.materials) {
                                //GdxInfo.material(mat);
                                mat.set(new IntAttribute(IntAttribute.CullFace, 0));
                        }
                }


                for (Animation animation : modelInstance.animations) {
                        if (animation.id.contains("Walk")) {
                                walk = animation;
                        } else if (animation.id.contains("Idle")) {
                                idle = animation;
                        } else if (animation.id.contains("Attack")) {
                                attack = animation;
                        } else if (animation.id.contains("AttackSword")) {
                                attackSword = animation;
                        } else if (animation.id.contains("bow_pull")) {
                                attackBow = animation;
                        } else if (animation.id.contains("AttackStaff")) {
                                attackStaff = animation;
                        } else if (animation.id.contains("Hit")) {
                                hit = animation;
                        } else if (animation.id.contains("Damaged")) {
                                hit = animation;
                        } else if (animation.id.contains("Die")) {
                                die = animation;
                        }

                }
                if(attackUnarmed == null) attackUnarmed = attack;
                if(attackSword == null) attackSword = attack;
                if(attackBow == null) attackBow = attack;
                if(attackStaff == null) attackStaff = attack;

                // check to see if token spawned with status effects already on, if so then shot their Fx and hud information
                if (token.getStatusEffects() != null) {
                        for (StatusEffect effect : StatusEffects.effectValues) {
                                if (token.getStatusEffects().has(effect)) {
                                        float duration = token.getStatusEffects().getDuration(effect);
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

                GdxInfo.model(modelInstance.model);


        }

        private Animation current, idle, walk, attack, attackUnarmed, attackSword, attackBow, attackStaff, hit, die;


        public void update(final float delta) {
                refreshWeaponAttachment();
                FogState fogState = world.floorSpatial.fogMap == null ? FogState.Visible : world.floorSpatial.fogMap.getFogState(token.location.x, token.location.y);
                float minVisU = 0;
                float maxVisU = 1;

                // TODO: i should have a targetVisuU and just lerp to that instead of how i have it here
                // as is if you are fully visible, then get the invisibility status effct you'll "snap" to visuU = 0.5f
                if (fogState == FogState.Visible) {
                        visU += delta * .65f;

                        if (token.getStatusEffects().has(StatusEffect.Invisibility)) {
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
                                if (world.hudSpatial.localPlayerToken == token) {
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


                if (token.getDamage() != null && !token.getDamage().isDead() && token.getAttack() != null && token.getAttack().isAttackingRanged()) {
                        float rotMoveSpeed = token.getMove() == null ? 7 : UtMath.largest(token.getMove().getMoveSpeed(), 7f);
                        float rotSpeed = delta * (rotMoveSpeed + 0.5f);
                        Direction dir = token.getLocation().direction(token.getAttack().getAttackTarget().getLocation());
                        Quaternion tokenDirRot = world.assetMappings.getRotation(dir);
                        rotation.slerp(tokenDirRot, rotSpeed);
                } else {
                        float rotMoveSpeed = UtMath.largest(token.getMove().getMoveSpeed(), 7f);
                        float rotSpeed = delta * (rotMoveSpeed + 0.5f);
                        Quaternion tokenDirRot = world.assetMappings.getRotation(token.getDirection());
                        rotation.slerp(tokenDirRot, rotSpeed);
                }


        }


        public void render(float delta) {
                if (visU <= 0) return;
                if (world.hudSpatial.localPlayerToken != null && world.hudSpatial.localPlayerToken.getLocation().distance(token.getLocation()) > 16) return;
                if (world.hudSpatial.isMapViewMode() && !world.cam.frustum.sphereInFrustumWithoutNearFar(translation, 5)) return;


                modelInstance.transform.set(
                        translation.x + translationBase.x, translation.y + translationBase.y, translation.z + translationBase.z,
                        rotation.x, rotation.y, rotation.z, rotation.w,
                        scale.x, scale.y, scale.z
                );
                world.modelBatch.render(modelInstance, world.environment);

                if(weaponModelInstance != null){
                        //Matrix4 boneWorldTransform = modelInstance.transform.cpy().mul(weaponAttachmentNode.globalTransform);
                        //boneWorldTransform.rotate(Vector3.Z, -90);
                        //weaponModelInstance.transform.set(boneWorldTransform);

                        weaponModelInstance.transform.set(modelInstance.transform).mul(weaponAttachmentNode.globalTransform);
                        world.modelBatch.render(weaponModelInstance, world.environment);

                        if(offhandModelInstance != null){
                                offhandModelInstance.transform.set(modelInstance.transform).mul(offhandAttachmentNode.globalTransform);
                                world.modelBatch.render(offhandModelInstance, world.environment);
                        }
                }



                shadowDecal.setPosition(translation);
                shadowDecal.translateY(0.1f);
                world.decalBatch.add(shadowDecal);


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
