package com.zrp200.lustrouspixeldungeon.items.armor.curses;

import com.watabou.utils.Random;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.actors.hero.Hero;
import com.zrp200.lustrouspixeldungeon.items.armor.Armor;
import com.zrp200.lustrouspixeldungeon.items.bombs.Bomb;
import com.zrp200.lustrouspixeldungeon.sprites.ItemSprite;


public class Volatility extends Armor.Glyph {
    private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );
    @Override
    public int proc(Armor armor, Char attacker, Char defender, int damage) {
        if(Random.Int(20)==0) {
            new Bomb().explode(defender.pos);
            if (armor != null && armor.isDestroyable() && defender instanceof Hero && ((Hero)defender).belongings.armor == armor)
                ((Hero)defender).belongings.armor = null; // DESTROY!!!!
        }
        return damage;
    }

    @Override
    public boolean curse() {
        return true;
    }
    public ItemSprite.Glowing glowing() {return BLACK;}
}
