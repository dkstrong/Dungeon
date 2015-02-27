package asf.dungeon.utility;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Coded animations that can be added to model instances.
 * <p/>
 * Mainly useful to give all loot the same "dropped" animation.
 * Created by Daniel Strong on 12/12/2014.
 */
public class AnimFactory {

        public static void createIdleAnim(ModelInstance target) {
                Animation anim = new Animation();
                anim.id = "Idle";
                anim.duration = 1f;
                target.animations.add(anim);
        }

        private static Animation dropped;

        public static Animation dropped() {
                if (dropped != null) return dropped;

                Animation anim = new Animation();
                dropped = anim;
                anim.id = "Dropped";
                anim.duration = .75f;

                NodeAnimation na = new NodeAnimation();
                //na.node = target.nodes.get(0);


                Bezier<Vector3> curve = new Bezier<Vector3>(
                        new Vector3(-.3f, 5, 0),
                        new Vector3(-.25f, 15, 0),
                        new Vector3(-.15f, 10, 0),
                        new Vector3(0, 0, 0)

                );


                Quaternion start = new Quaternion().setFromAxis(new Vector3(-1, -4, 25).nor(), 300);
                Quaternion end = new Quaternion().setFromAxis(1, 0, 0, -90);

                final int iNumFrames = 60;
                final float numFrames = iNumFrames;
                na.scaling = new Array<NodeKeyframe<Vector3>>(iNumFrames);
                na.rotation = new Array<NodeKeyframe<Quaternion>>(iNumFrames);
                na.translation = new Array<NodeKeyframe<Vector3>>(iNumFrames);
                for (int i = 0; i < numFrames; i++) {
                        float keytime = i / numFrames * anim.duration;
                        float scale = Interpolation.linear.apply(0.25f, 1, i / numFrames);
                        NodeKeyframe<Vector3> scalekf = new NodeKeyframe<Vector3>(keytime, new Vector3(scale,scale,scale));

                        Quaternion rot =new Quaternion();
                        UtMath.interpolateLinear(i / numFrames * 8f, start, end, rot);
                        NodeKeyframe<Quaternion> rotkf = new NodeKeyframe<Quaternion>(keytime, rot);

                        Vector3 trans = curve.valueAt(new Vector3(), i / numFrames);
                        NodeKeyframe<Vector3> transkf = new NodeKeyframe<Vector3>(keytime, trans);


                        na.scaling.add(scalekf);
                        na.rotation.add(rotkf);
                        na.translation.add(transkf);
                }

                //NodeKeyframe lastFrame = new NodeKeyframe();
                //lastFrame.rotation.setFromAxis(1,0,0,-90);
                //na.keyframes.add(lastFrame);


                anim.nodeAnimations.add(na);


                //target.animations.add(anim);


                return dropped;
        }

        public static void createAnim(Animation anim, ModelInstance target) {
                Animation animation = new Animation();
                animation.id = anim.id;
                animation.duration = anim.duration;
                for (final NodeAnimation nanim : anim.nodeAnimations) {
                        NodeAnimation nodeAnim = new NodeAnimation();
                        nodeAnim.node = target.nodes.get(0);
                        nodeAnim.scaling = nanim.scaling;
                        nodeAnim.rotation = nanim.rotation;
                        nodeAnim.translation = nanim.translation;

                        if (nodeAnim.node == null) {
                                throw new IllegalArgumentException("target does not have any nodes");
                        }
                        if (nodeAnim.translation != null || nodeAnim.rotation != null || nodeAnim.scaling != null)
                                animation.nodeAnimations.add(nodeAnim);
                }
                target.animations.add(animation);
        }

}
