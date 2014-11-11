package asf.dungeon.view;

import asf.dungeon.model.Pair;

/**
 * Created by danny on 10/20/14.
 */
public class SelectionMark implements GenericControl {
        private GenericSpatial genericSpatial;
        private float count = 0;
        private DungeonWorld world;

        public SelectionMark(DungeonWorld world) {
                this.world = world;
        }

        @Override
        public void start(GenericSpatial genericSpatial) {
                genericSpatial.cullType = GenericSpatial.CullType.Always;
                this.genericSpatial = genericSpatial;

        }

        @Override
        public void update(float delta) {
                if(count <=0){
                        genericSpatial.cullType = GenericSpatial.CullType.Always;
                        count = Float.NaN;
                }else{
                        count-=delta;
                }
        }

        public void mark(Pair loc){
                if(loc == null){
                        genericSpatial.cullType = GenericSpatial.CullType.Always;
                        count = Float.NaN;
                        return;
                }
                genericSpatial.cullType = GenericSpatial.CullType.Dynamic;
                count = 2;
                world.getWorldCoords(loc, genericSpatial.translation);

        }
}
