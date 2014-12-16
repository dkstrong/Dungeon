package asf.dungeon.utility;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Coded animations that can be added to model instances.
 * <p/>
 * Mainly useful to give all loot the same "dropped" animation.
 * Created by Daniel Strong on 12/12/2014.
 */
public class AnimFactory {

        public static void createIdleAnim(BetterModelInstance target) {
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

                float numFrames = 60;
                for (int i = 0; i < numFrames; i++) {
                        NodeKeyframe keyframe = new NodeKeyframe();
                        keyframe.keytime = i / numFrames * anim.duration;
                        keyframe.scale.x = Interpolation.linear.apply(0.25f, 1, i / numFrames);
                        keyframe.scale.y = keyframe.scale.x;
                        keyframe.scale.z = keyframe.scale.x;
                        UtMath.interpolateLinear(i / numFrames * 8f, start, end, keyframe.rotation);
                        //keyframe.rotation.setFromAxis(1,0,0, Interpolation.linear.apply(680, -90, i/numFrames));
                        curve.valueAt(keyframe.translation, i / numFrames);

                        na.keyframes.add(keyframe);
                }

                //NodeKeyframe lastFrame = new NodeKeyframe();
                //lastFrame.rotation.setFromAxis(1,0,0,-90);
                //na.keyframes.add(lastFrame);


                anim.nodeAnimations.add(na);


                //target.animations.add(anim);


                return dropped;
        }

        public static void createAnim(Animation anim, BetterModelInstance target) {
                Animation cloneAnim = new Animation();
                cloneAnim.id = anim.id;
                cloneAnim.duration = anim.duration;
                for (final NodeAnimation na : anim.nodeAnimations) {
                        if (na.keyframes.size <= 0)
                                continue;
                        NodeAnimation cloneNa = new NodeAnimation();
                        cloneNa.node = target.nodes.get(0);
                        if (cloneNa.node == null) {
                                throw new IllegalArgumentException("target does not have any nodes");
                        }
                        for (final NodeKeyframe kf : na.keyframes) {
//                                NodeKeyframe cloneKeyframe = new NodeKeyframe();
//                                cloneKeyframe.keytime = kf.keytime;
//                                cloneKeyframe.rotation.set(kf.rotation);
//                                cloneKeyframe.scale.set(kf.scale);
//                                cloneKeyframe.translation.set(kf.translation);
                                cloneNa.keyframes.add(kf);
                        }
                        cloneAnim.nodeAnimations.add(cloneNa);
                }
                target.animations.add(cloneAnim);
        }

}
