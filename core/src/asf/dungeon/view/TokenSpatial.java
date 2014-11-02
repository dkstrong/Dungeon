package asf.dungeon.view;

import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.CrateToken;
import asf.dungeon.board.FogMap;
import asf.dungeon.board.FogState;
import asf.dungeon.board.LootToken;
import asf.dungeon.board.Token;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import asf.dungeon.view.shape.Shape;

/**
 * Created by danny on 10/20/14.
 */
public class TokenSpatial implements Spatial {

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
        protected TokenControl tokenControl;
        protected float visU = 0; // how visible this object is, 0 = not drawn, 1 = fully visible, inbetween for partially visible

        public TokenSpatial(DungeonWorld world, Token token, ModelInstance modelInstance, Shape shape, Environment environment) {
                this.world = world;
                this.token = token;
                this.modelInstance = modelInstance;
                this.assetLocation = null;
                this.shape = shape;
                this.environment = environment;
                postConstruct();
        }

        public TokenSpatial(DungeonWorld world, Token token, String assetLocation, Shape shape, Environment environment) {
                this.world = world;
                this.token = token;
                this.assetLocation = assetLocation;
                this.shape = shape;
                this.environment = environment;
                postConstruct();
        }

        private void postConstruct(){
                if(token instanceof CharacterToken){
                        tokenControl = new CharacterTokenControl(world, (CharacterToken)token);
                }else if(token instanceof CrateToken){
                        tokenControl = new CrateTokenControl(world, (CrateToken)token);
                }else if(token instanceof LootToken){
                        tokenControl = new LootTokenControl(world, (LootToken)token);
                }else{
                        throw new AssertionError(token);
                }
        }

        public TokenControl getControl(){
                return tokenControl;
        }

        public void preload(DungeonWorld world){
                if(assetLocation != null)
                        world.assetManager.load(assetLocation, Model.class);
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

                if (tokenControl != null)
                        tokenControl.start(this);
        }


        public void update(final float delta) {

                // this token is only visible if on the same floor
                // as the local player, and if fog mapping is enabled
                if(tokenControl.getToken().getFloorMap() == world.getLocalPlayerToken().getFloorMap() && world.getLocalPlayerToken().isFogMappingEnabled()){
                        FogMap fogMap = world.getLocalPlayerToken().getFogMap(world.getLocalPlayerToken().getFloorMap());
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
                        // TODO: maybe i should do visU = 1 if fogmapping is turned off to force all tokens to be rendered?
                        visU = 0;
                }




                for (Material material : modelInstance.materials) {
                        ColorAttribute colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);
                        colorAttribute.color.a = visU;
                }


                tokenControl.update(delta);

                if (animController != null) {
                        animController.update(delta);
                }

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
                if(world.getLocalPlayerToken().isFogMappingEnabled()){
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
                initialized = false;
        }

        public boolean isInitialized(){
                return initialized;
        }

}
