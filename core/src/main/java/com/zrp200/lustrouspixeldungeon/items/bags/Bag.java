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

package com.zrp200.lustrouspixeldungeon.items.bags;

import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.zrp200.lustrouspixeldungeon.Badges;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.actors.hero.Hero;
import com.zrp200.lustrouspixeldungeon.items.Heap;
import com.zrp200.lustrouspixeldungeon.items.Item;
import com.zrp200.lustrouspixeldungeon.scenes.GameScene;
import com.zrp200.lustrouspixeldungeon.windows.WndBag;

import java.util.ArrayList;
import java.util.Iterator;

public class Bag extends Item implements Iterable<Item> {

	private static final String AC_OPEN	= "OPEN";
	
	{
		image = 11;
		
		defaultAction = AC_OPEN;

		unique = true;
	}
	protected Dungeon.LimitedDrops limitedDrop = null;
	
	public Char owner;
	
	public ArrayList<Item> items = new ArrayList<>();
	
	public int size = 20;

	public void acquire() {
		if(limitedDrop != null) limitedDrop.drop();
	}
	public boolean dropped() {
		return limitedDrop.dropped();
	}

	@Override
	public Heap drop(int pos) {
		acquire();
		return super.drop(pos);
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_OPEN )) {
			
			GameScene.show( new WndBag( this, null, WndBag.Mode.ALL, null ) );
			
		}
	}
	
	@Override
	public boolean collect( Bag container ) {

		for (Item item : container.items.toArray( new Item[0] )) {
			if (grab( item )) {
				int slot = Dungeon.quickslot.getSlot(item);
				item.detachAll(container);
				if (!item.collect(this)) {
					item.collect(container);
				}
				if (slot != -1) {
					Dungeon.quickslot.setSlot(slot, item);
				}
			}
		}

		if (super.collect( container )) {
			
			owner = container.owner;
			acquire();
			
			Badges.validateAllBagsBought( this );
			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onDetach( ) {
		this.owner = null;
		for (Item item : items)
			Dungeon.quickslot.clearItem(item);
		updateQuickslot();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	public void clear() {
		items.clear();
	}
	
	public void resurrect() {
		for (Item item : items.toArray(new Item[0])){
			if (!item.unique) items.remove(item);
		}
	}
	
	private static final String ITEMS	= "inventory";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ITEMS, items );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		for (Bundlable item : bundle.getCollection( ITEMS )) {
			if (item != null) ((Item)item).collect( this );
		}
    }
	
	public boolean contains( Item item ) {
		for (Item i : items) {
			if (i == item) {
				return true;
			} else if (i instanceof Bag && ((Bag)i).contains( item )) {
				return true;
			}
		}
		return false;
	}
	
	public boolean grab( Item item ) {
		return false;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		private Iterator<Item> nested = null;
		
		@Override
		public boolean hasNext() {
			if (nested != null) {
				return nested.hasNext() || index < items.size();
			} else {
				return index < items.size();
			}
		}

		@Override
		public Item next() {
			if (nested != null && nested.hasNext()) {
				
				return nested.next();
				
			} else {
				
				nested = null;
				
				Item item = items.get( index++ );
				if (item instanceof Bag) {
					nested = ((Bag)item).iterator();
				}
				
				return item;
			}
		}

		@Override
		public void remove() {
			if (nested != null) {
				nested.remove();
			} else {
				items.remove( index );
			}
		}
	}
}
