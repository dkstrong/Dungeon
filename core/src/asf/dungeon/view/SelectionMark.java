package asf.dungeon.view;

import asf.dungeon.board.Pair;

/**
 * Created by danny on 10/20/14.
 */
public class SelectionMark implements ActorControl {
        private ActorSpatial actorSpatial;
        private float count = 0;
        private DungeonWorld world;

        public SelectionMark(DungeonWorld world) {
                this.world = world;
        }

        @Override
        public void start(ActorSpatial actorSpatial) {
                actorSpatial.cullType = CullType.Always;
                this.actorSpatial = actorSpatial;

        }

        @Override
        public void update(float delta) {
                if(count <=0){
                        actorSpatial.cullType = CullType.Always;
                        count = Float.NaN;
                }else{
                        count-=delta;
                }
        }

        public void mark(Pair loc){
                if(loc == null){
                        actorSpatial.cullType = CullType.Always;
                        count = Float.NaN;
                        return;
                }
                actorSpatial.cullType = CullType.Dynamic;
                count = 2;
                world.getWorldCoords(loc, actorSpatial.translation);

        }
}
