package asf.dungeon.model.floorgen.cave;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.UtFloorGen;
import asf.dungeon.model.token.Stairs;
import asf.dungeon.model.token.Token;


/**
 * Created by Danny on 11/4/2014.
 */
public class MazeGen implements FloorMapGenerator {

        private int width;
        private int height;

        private Dungeon dungeon;

        public MazeGen(int width, int height) {
                this.width = width;
                this.height = height;
        }

        public FloorMap generate(Dungeon dungeon, FloorType floorType, int floorIndex){
                this.dungeon = dungeon;
                int w = Math.round(width/2f);
                int h = Math.round(height/2f);
                Tile[][] tiles = new Tile[w * 2 + 1][h * 2 + 1];
                boolean[][] visited = new boolean[w + 2][h + 2];

                for(int i = 0; i < tiles.length; i++)
                        for(int j = 0; j < tiles[0].length; j++)
                                tiles[i][j] = Tile.makeWall();
                for(int i = 0; i < visited.length; i++)
                        for(int j = 0; j < visited[0].length; j++)
                                if(i == 0 || i == visited.length - 1 || j == 0 || j == visited[0].length - 1)
                                        visited[i][j] = true;

                gen(tiles, 0, 0,visited);


                FloorMap floorMap = new FloorMap(floorType, floorIndex,tiles);

                // upper stairs is on bottom left
                outerloop:
                for(int x=0; x<tiles.length; x++){
                        for(int y=0; y<tiles[x].length; y++){
                                if(!tiles[x][y].blockMovement && !tiles[x][y].blockVision){
                                        Token stairsToken = new Token(dungeon, "Stairs", null);
                                        stairsToken.add(new Stairs(stairsToken, floorMap.index - 1));
                                        dungeon.addToken(stairsToken, floorMap, x, y);
                                        break outerloop;
                                }
                        }
                }


                // lower stairs is on top right
                outerloop:
                for (int x = tiles.length - 1; x >= 0; x--) {
                        for (int y = tiles[x].length - 1; y >= 0; y--) {
                                if(!tiles[x][y].blockMovement && !tiles[x][y].blockVision){
                                        Token stairsToken = new Token(dungeon, "Stairs", null);
                                        stairsToken.add(new Stairs(stairsToken, floorMap.index + 1));
                                        dungeon.addToken(stairsToken, floorMap, x, y);
                                        break outerloop;
                                }
                        }
                }
                UtFloorGen.spawnCharacters(dungeon, floorMap);
                return floorMap;
        }

        private void gen(Tile[][] tiles, int xPos, int yPos, boolean[][] visited) {
                int x = xPos * 2 + 1;
                int y = yPos * 2 + 1;
                tiles[x][y] = Tile.makeFloor();
                visited[xPos + 1][yPos + 1] = true;
                while(!visited[xPos][yPos + 1] || !visited[xPos + 2][yPos + 1] || !visited[xPos + 1][yPos + 2] || !visited[xPos + 1][yPos]) {
                        float num = dungeon.rand.random.nextFloat();
                        if(num < 0.25F && !visited[xPos][yPos + 1]) {
                                tiles[x - 1][y] = Tile.makeFloor();
                                gen(tiles,xPos - 1, yPos,visited);
                        }
                        else if(num < 0.5F && num >= 0.25F && !visited[xPos + 2][yPos + 1]) {
                                tiles[x + 1][y] = Tile.makeFloor();
                                gen(tiles,xPos + 1, yPos,visited);
                        }
                        else if(num >= 0.5F && num < 0.75F && !visited[xPos + 1][yPos + 2]) {
                                tiles[x][y + 1] = Tile.makeFloor();
                                gen(tiles,xPos, yPos + 1,visited);
                        }
                        else if(!visited[xPos + 1][yPos]) {
                                tiles[x][y - 1] = Tile.makeFloor();
                                gen(tiles,xPos, yPos - 1,visited);
                        }
                }
        }


}
