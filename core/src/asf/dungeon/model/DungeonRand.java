package asf.dungeon.model;


import asf.dungeon.model.item.PotionItem;

import java.util.Random;

/**
 * Created by Danny on 11/17/2014.
 */
public class DungeonRand {
        public final Random random;

        public DungeonRand(Random random) {
                if(random == null){
                        this.random = new Random();
                }else{
                        this.random = random;
                }
        }

        public PotionItem.Type potionType(){
                int i = random.nextInt(10);
                if(i== 0){
                        return PotionItem.Type.Blindness;
                }else if(i==1){
                        return PotionItem.Type.Hallucination;
                }else if(i==2){
                        return PotionItem.Type.Health;
                }else if(i==3){
                        return PotionItem.Type.Invisibility;
                }else if(i==4){
                        return PotionItem.Type.Might;
                }else if(i==5){
                        return PotionItem.Type.MindVision;
                }else if(i==6){
                        return PotionItem.Type.Paralyze;
                }else if(i==7){
                        return PotionItem.Type.Poison;
                }else if(i==8){
                        return PotionItem.Type.Poison;
                }else if(i==9){
                        return PotionItem.Type.Speed;
                }
                throw new AssertionError(i);
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

        public Direction direction8Axis(){
                int i = random.nextInt(8);
                if (i == 0)
                        return Direction.North;
                else if (i == 1)
                        return Direction.South;
                else if (i == 2)
                        return Direction.East;
                else if( i == 3)
                        return Direction.West;
                else if( i == 4)
                        return Direction.NorthEast;
                else if( i == 5)
                        return Direction.NorthWest;
                else if( i == 6)
                        return Direction.SouthEast;
                else
                        return Direction.SouthWest;
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
