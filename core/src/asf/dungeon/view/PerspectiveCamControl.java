package asf.dungeon.view;

import asf.dungeon.utility.UtMath;
import asf.dungeon.view.token.AbstractTokenSpatial;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Daniel Strong on 11/27/2014.
 */
public class PerspectiveCamControl implements CamControl{
        private final PerspectiveCamera cam;

        private AbstractTokenSpatial chaseTarget;
        private final Vector3 chaseCamOffset = new Vector3();

        private float zoom = 1;
        private float targetZoom = 1;

        private boolean chasing = true;
        private float chasingTimer = 0;
        private int dragX = -1, dragY;

        public PerspectiveCamControl() {
                cam = new PerspectiveCamera(50, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // 67
                cam.position.set(-15f, 35f, 15f);  //cam.position.set(0f, 25f, 15f);
                cam.lookAt(0, 0, 0);
                cam.near = 1f;
                cam.far = 100f;
                cam.update();
                chaseCamOffset.set(cam.position);
        }

        public void resize(int width, int height){
                cam.viewportWidth = width;
                cam.viewportHeight = height;
                cam.update();
        }

        public void update(float delta){

                if(zoom != targetZoom){
                        if(targetZoom > zoom){
                                zoom+=delta*1.5f;
                                if(zoom > targetZoom) zoom = targetZoom;
                        }else{
                                zoom -= delta*1.5f;
                                if(zoom<targetZoom) zoom = targetZoom;
                        }
                        chaseCamOffset.y = UtMath.scalarLimitsExtrapolation(zoom, 0, 1, 100, 35f);
                }
                if (chasing && chaseTarget != null) {

                        cam.position.x = chaseTarget.translation.x + chaseCamOffset.x;
                        cam.position.y = chaseCamOffset.y;
                        cam.position.z = chaseTarget.translation.z + chaseCamOffset.z;

                        if(targetZoom != 1){
                                chasingTimer+=delta;
                                if(chasingTimer > 1f && chaseTarget.getToken().move.isMoving()){
                                        targetZoom =1;
                                }
                        }
                }else{

                        cam.position.y = chaseCamOffset.y;

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
                        Vector2 v = new Vector2(dragX - screenX, dragY - screenY);
                        v.rotate(45);
                        cam.position.x += v.x/4f;
                        cam.position.z +=v.y/4f;
                }
                dragX = screenX;
                dragY = screenY;
        }

        @Override
        public Camera getCamera() {
                return cam;
        }
}
