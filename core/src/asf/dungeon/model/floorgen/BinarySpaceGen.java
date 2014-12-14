package asf.dungeon.model.floorgen;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.room.Room;
import asf.dungeon.model.floorgen.room.UtRoomSpawn;
import asf.dungeon.model.floorgen.room.UtRoomCarve;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 *
 * floor is made by repeatidaly dividing the floor into 2 smaller parts
 *
 * http://www.roguebasin.com/index.php?title=Basic_BSP_Dungeon_generation
 *
 * TODO: when splitting and creating inner rooms, need to make sure that a minimum size can be
 * maintained to support fitting in stairs and other items.
 *
 * Created by Danny on 11/4/2014.
 */
public class BinarySpaceGen implements FloorMapGenerator{


        private int minFloorWidth = 30;
        private int maxFloorWidth = 45;
        private int minFloorHeight = 30;
        private int maxFloorHeight = 45;


        private int numberOfSubdivisions = 4;
        private Dungeon dungeon;


        @Override
        public FloorMap generate(Dungeon dungeon, int floorIndex) {
                this.dungeon = dungeon;
                int floorWidth = dungeon.rand.range(minFloorWidth, maxFloorWidth);
                int floorHeight = dungeon.rand.range(minFloorHeight, maxFloorHeight);

                Tile[][] tiles = new Tile[floorWidth][floorHeight];
                RoomCell baseRoomCell = new RoomCell(0,0,floorWidth-1, floorHeight-1);
                for(int k=0;k< numberOfSubdivisions; k++) baseRoomCell.split();

                Array<Room> rooms = new Array<Room>(true, UtMath.pow(2, numberOfSubdivisions), Room.class);
                addToArray(baseRoomCell, rooms);

                UtRoomCarve.fillRooms(tiles, rooms);
                fillTunnels(tiles, baseRoomCell.childCell1, baseRoomCell.childCell2);
                UtRoomCarve.carveDoors(dungeon, floorIndex, tiles, rooms);
                UtRoomCarve.carveStairs(dungeon, floorIndex, tiles, rooms);


                FloorMap floorMap = new FloorMap(floorIndex, tiles);
                UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap);
                boolean valid = UtRoomSpawn.spawnKeys(dungeon, floorMap, rooms);
                if(!valid) throw new Error("could not generate valid key locations, need to regenrate");
                return floorMap;
        }

        private void addToArray(RoomCell roomCell, Array<Room> rooms){
                if(roomCell.isLeaf()){
                        rooms.add(roomCell.getInnerRoom());
                }else{
                        addToArray(roomCell.childCell1, rooms);
                        addToArray(roomCell.childCell2, rooms);
                }
        }

        private void fillTunnels(Tile[][] tiles, RoomCell cell1, RoomCell cell2){

                UtRoomCarve.fillTunnel(dungeon, tiles, cell1.getEndLeaf().getInnerRoom(), cell2.getEndLeaf().getInnerRoom(), false);
                if(cell1.childCell1 != null)
                        fillTunnels(tiles, cell1.childCell1, cell1.childCell2);
                if(cell2.childCell1 != null)
                        fillTunnels(tiles, cell2.childCell1, cell2.childCell2);


        }



        private class RoomCell {

                private RoomCell parentCell;
                private RoomCell childCell1;
                private RoomCell childCell2;
                private Room innerRoom;

                private int x1, y1, x2, y2;
                private boolean vertSplit;

                private RoomCell(int x1, int y1, int x2, int y2) {
                        this.x1 = x1;
                        this.y1 = y1;
                        this.x2 = x2;
                        this.y2 = y2;
                }

                private boolean isLeaf(){
                        return childCell1 == null && childCell2 == null;
                }

                private RoomCell getEndLeaf(){
                        if(isLeaf())
                                return this;
                        return childCell1.getEndLeaf();
                }

                private void split(){
                        if(!isLeaf()){
                                childCell1.split();
                                childCell2.split();
                                return;
                        }
                        // look at how the parent was split to encourage its chldren to split in the other direction
                        // this should create layouts with less "squished" rooms that represent closets more than rooms
                        //vertSplit = MathUtils.randomBoolean(parentCell != null && parentCell.vertSplit ? .05f :.95f); //false; // MathUtils.randomBoolean()
                        vertSplit  = parentCell!= null && parentCell.vertSplit ? false : true;
                        float splitRatio = dungeon.rand.range(.35f, .65f);
                        if(vertSplit){
                                int newX2 = Math.round(MathUtils.lerp(x1, x2, splitRatio));
                                childCell1= new RoomCell(x1, y1, newX2, y2);
                                childCell1.parentCell =this;

                                childCell2 = new RoomCell(newX2, y1, x2, y2);
                                childCell2.parentCell = this;

                        }else{
                                int newY2 = Math.round(MathUtils.lerp(y1,y2,splitRatio));
                                childCell1= new RoomCell(x1, y1, x2, newY2);
                                childCell1.parentCell =this;

                                childCell2 = new RoomCell(x1, newY2, x2, y2);
                                childCell2.parentCell = this;

                        }

                }

                private Room getInnerRoom(){
                        if(innerRoom != null)
                                return innerRoom;

                        int innerX1= dungeon.rand.range(x1, Math.round(MathUtils.lerp(x1, x2, 0.2f)));
                        int innerX2 = dungeon.rand.range(Math.round(MathUtils.lerp(x1, x2, .75f)), x2);

                        int innerY1= dungeon.rand.range(y1, Math.round(MathUtils.lerp(y1, y2, 0.25f)));
                        int innerY2 = dungeon.rand.range(Math.round(MathUtils.lerp(y1, y2, .75f)), y2);

                        innerRoom = new Room(innerX1, innerY1, innerX2, innerY2);
                        return innerRoom;
                }
        }

}
