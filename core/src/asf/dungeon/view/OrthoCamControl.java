package asf.dungeon.view;

import asf.dungeon.utility.UtMath;
import asf.dungeon.view.token.AbstractTokenSpatial;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Daniel Strong on 12/30/14.
 */
public class OrthoCamControl implements CamControl {
        private final OrthographicCamera cam;

        private AbstractTokenSpatial chaseTarget;
        private final Vector3 chaseCamOffset = new Vector3();

        private float zoom = 1;
        private float targetZoom = 1;

        private boolean chasing = true;
        private float chasingTimer = 0;
        private int dragX = -1, dragY;

        public OrthoCamControl() {
                cam = new OrthographicCamera(20, 12);
                //cam.direction.set(0.45176828f, -0.77861917f, -0.43549702f).nor();
                cam.direction.set(0.48007333f, -0.7591688f, -0.43953663f).nor();
                //cam.direction.set(1f, -1f, -1f).nor();
                cam.near = .1f;
                cam.far = 300f;
                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                //chaseCamOffset.set(-40,40,40);
        }

        public void resize(int width, int height){
                cam.viewportWidth = 80;
                cam.viewportHeight = cam.viewportWidth * (float)height/(float)width;
                cam.update();
                chaseCamOffset.set(-cam.viewportWidth*cam.direction.x,-cam.viewportWidth*cam.direction.y,-cam.viewportWidth*cam.direction.z);
        }

        public void update(float delta){
//                if(Gdx.input.isKeyPressed(Input.Keys.X)){
//                        cam.direction.x += .01f;
//                }else if(Gdx.input.isKeyPressed(Input.Keys.Z)){
//                        cam.direction.x -= .01f;
//                }else if(Gdx.input.isKeyPressed(Input.Keys.C)){
//                        cam.direction.y += .01f;
//                }else if(Gdx.input.isKeyPressed(Input.Keys.D)){
//                        cam.direction.y -= .01f;
//                }else if(Gdx.input.isKeyPressed(Input.Keys.S)){
//                        cam.direction.z += .01f;
//                }else if(Gdx.input.isKeyPressed(Input.Keys.A)){
//                        cam.direction.z -= .01f;
//                }else if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
//                        Gdx.app.log("direction", cam.direction.toString());
//                }else if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
//                        cam.direction.set(1,-1,-1).nor();
//                }else if(Gdx.input.isKeyJustPressed(Input.Keys.T)){
//                        cam.direction.set(0.47620738f,-0.7265038f,-0.49539757f).nor();
//                }
//                cam.direction.nor();
//                chaseCamOffset.set(-cam.viewportWidth*cam.direction.x,-cam.viewportWidth*cam.direction.y,-cam.viewportWidth*cam.direction.z);

                if(zoom != targetZoom){
                        if(targetZoom > zoom){
                                zoom+=delta*1.5f;
                                if(zoom > targetZoom) zoom = targetZoom;
                        }else{
                                zoom -= delta*1.5f;
                                if(zoom<targetZoom) zoom = targetZoom;
                        }
                        cam.zoom = UtMath.scalarLimitsExtrapolation(zoom, 0, 1, 2, 1);

                }
                if (chasing && chaseTarget != null) {

                        cam.position.x =  chaseTarget.translation.x + chaseCamOffset.x;
                        cam.position.y =  chaseCamOffset.y;
                        cam.position.z =  chaseTarget.translation.z + chaseCamOffset.z;

                        if(targetZoom != 1){
                                chasingTimer+=delta;
                                if(chasingTimer > 1f && chaseTarget.getToken().getMove().isMoving()){
                                        targetZoom =1;
                                }
                        }
                }else{

                        //cam.position.y = chaseCamOffset.y;

                }
                cam.update();
        }

        public void setChaseTarget(AbstractTokenSpatial tokenSpatial){
                chaseTarget = tokenSpatial;
        }

        @Override
        public AbstractTokenSpatial getChaseTarget() {
                return chaseTarget;
        }

        public float getZoom(){
                return targetZoom;
        }

        public void setZoom(float amount){
                this.targetZoom = UtMath.clamp(amount, 0, 1f);
        }

        public boolean isChasing() {
                return chasing;
        }

        public void setChasing(boolean chasing) {
                this.chasing = chasing;
                if(chasing){
                        chasingTimer = 0;
                }else{
                        dragX= -1;
                }
        }

        public void drag(int screenX, int screenY){
                if(screenX == -1){
                        dragX = -1;
                }else if(dragX != -1){
                        int deltaX = dragX - screenX;
                        int deltaY = dragY - screenY;
                        cam.position.x += deltaX/4f*cam.direction.x;
                        cam.position.z +=deltaY/4f*cam.direction.z;
                }
                dragX = screenX;
                dragY = screenY;
        }

        @Override
        public Camera getCamera() {
                return cam;
        }
}
