package asf.dungeon.utility;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;

/**
 * Created by Daniel Strong on 1/11/2015.
 */
public class BillboardParticleBatchBlendable extends BillboardParticleBatch {
        @Override
        protected Renderable allocRenderable() {
/*
                renderable.material = new Material(	new BlendingAttribute(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
                        new DepthTestAttribute(GL20.GL_LEQUAL, false),
                        TextureAttribute.createDiffuse(texture));
                        */

                Renderable renderable = super.allocRenderable();
                //renderable.material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                renderable.material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                return renderable;
        }
}
