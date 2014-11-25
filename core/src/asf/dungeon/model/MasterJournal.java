package asf.dungeon.model;

import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import com.badlogic.gdx.Gdx;

/**
 * On each new game potions, scrolls, and books are represented by different markings.
 * The master journal is used to look up these mappings
 *
 * Contains Type->Symbol mappings for potions, scrolls, and books.
 *
 * Created by Danny on 11/5/2014.
 */
public class MasterJournal  {


        private final PotionItem.Type[] potions;
        private final ScrollItem.Type[] scrolls;
        private final BookItem.Type[] books;

        public MasterJournal(PotionItem.Type[] potions, ScrollItem.Type[] scrolls, BookItem.Type[] books) {
                this.potions = potions;
                this.scrolls = scrolls;
                this.books = books;
        }

        // TODO: fix wasteful creation of arrays through values()

        public PotionItem.Color getPotionColor(PotionItem.Type type){
                for (int i = 0; i < potions.length; i++) {
                        PotionItem.Type potionType = potions[i];
                        if(potionType == type){
                                return PotionItem.Color.values()[i];
                        }
                }
                return null;
        }

        public PotionItem.Type getPotionType(PotionItem.Color color){
                return potions[color.ordinal()];
        }

        public ScrollItem.Symbol getScrollSymbol(ScrollItem.Type type){
                for (int i = 0; i < scrolls.length; i++) {
                        ScrollItem.Type scrollType = scrolls[i];
                        if(scrollType == type){
                                return ScrollItem.Symbol.values()[i];
                        }
                }
                return null;
        }

        public ScrollItem.Type getScrollType(ScrollItem.Symbol symbol){return scrolls[symbol.ordinal()];}

        public BookItem.Symbol getBookSymbol(BookItem.Type type){
                Gdx.app.log("master journal","getting symbol for book type: "+type);
                for (int i = 0; i < books.length; i++) {
                        BookItem.Type bookType = books[i];
                        if(bookType == type){
                                Gdx.app.log("master journal","found symbol: "+(BookItem.Symbol.values()[i]));
                                return BookItem.Symbol.values()[i];
                        }
                }
                Gdx.app.log("master journal","found no symbol");
                return null;
        }

        public BookItem.Type getBookType(BookItem.Symbol symbol){return books[symbol.ordinal()];}
}
