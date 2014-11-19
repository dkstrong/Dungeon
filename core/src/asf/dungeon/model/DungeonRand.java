package asf.dungeon.model;


import java.util.Random;

/**
 * Created by Danny on 11/17/2014.
 */
public class DungeonRand {
        public final Random random;

        public DungeonRand() {
                random = new Random();
        }

        public DungeonRand(Random random) {
                if(random == null){
                        this.random = new Random();
                }else{
                        this.random = random;
                }
        }

        public Direction direction(){
                int i = random.nextInt(4);
                if (i == 0)
                        return Direction.North;
                else if (i == 1)
                        return Direction.South;
                else if (i == 2)
                        return Direction.East;
                else
                        return Direction.West;
        }

        public boolean bool(float chance){
                return random.nextFloat() < chance;
        }

        /** Returns a rand number between start (inclusive) and end (inclusive). */
        public int range(int start, int end) {
                return start + random.nextInt(end - start + 1);
        }

        /** Returns a rand number between start (inclusive) and end (exclusive). */
        public float range(float start, float end){
                return start + random.nextFloat() * (end - start);
        }

        public int sign(){ return 1 | (random.nextInt() >> 31); }
}
