/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.zrp200.lustrouspixeldungeon.items.spells;

import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.hero.Hero;
import com.zrp200.lustrouspixeldungeon.items.Item;
import com.zrp200.lustrouspixeldungeon.items.MerchantsBeacon;
import com.zrp200.lustrouspixeldungeon.items.scrolls.ScrollOfIdentify;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.sprites.ItemSpriteSheet;
import com.zrp200.lustrouspixeldungeon.utils.GLog;
import com.zrp200.lustrouspixeldungeon.windows.WndBag;

import java.util.ArrayList;

public class MagicalPorter extends InventorySpell {
	
	{
		image = ItemSpriteSheet.MAGIC_PORTER;
		mode = WndBag.Mode.NOT_EQUIPPED;
		value = (5 + 40) / 8f;	//prices of ingredients, divided by output quantity
    }
	
	@Override
	protected void onCast(Hero hero) {
		if (Dungeon.depth >= 25){
			GLog.w(Messages.get(this, "nowhere"));
		} else {
			super.onCast(hero);
		}
	}
	
	@Override
	protected void onItemSelected(Item item) {
		
		Item result = item.detachAll(curUser.belongings.backpack);
		int portDepth = 5 * (1 + Dungeon.depth/5);
		ArrayList<Item> ported = Dungeon.portedItems.get(portDepth);
		if (ported == null) {
			Dungeon.portedItems.put(portDepth, ported = new ArrayList<>());
		}
		ported.add(result);
		
	}
	
	public static class Recipe extends com.zrp200.lustrouspixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{MerchantsBeacon.class, ArcaneCatalyst.class};
			inQuantity = new int[]{1, 1};
			
			cost = 4;
			
			output = MagicalPorter.class;
			outQuantity = 8;
		}
		
	}
}
