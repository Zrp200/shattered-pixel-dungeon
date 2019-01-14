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

package com.zrp200.lustrouspixeldungeon.levels.rooms.special;

import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.lustrouspixeldungeon.Challenges;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.items.Generator;
import com.zrp200.lustrouspixeldungeon.items.Heap;
import com.zrp200.lustrouspixeldungeon.items.Item;
import com.zrp200.lustrouspixeldungeon.items.keys.CrystalKey;
import com.zrp200.lustrouspixeldungeon.items.keys.IronKey;
import com.zrp200.lustrouspixeldungeon.levels.Level;
import com.zrp200.lustrouspixeldungeon.levels.Terrain;
import com.zrp200.lustrouspixeldungeon.levels.painters.Painter;

import java.util.ArrayList;
import java.util.Arrays;

public class VaultRoom extends SpecialRoom {

	public void paint( Level level ) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );
		Painter.fill( level, this, 2, Terrain.EMPTY );
		
		int cx = (left + right) / 2;
		int cy = (top + bottom) / 2;
		int c = cx + cy * level.width();
		
		Random.shuffle(prizeClasses);
		
		Item i1, i2;
		i1 = prize( level );
		i2 = prize( level );
		level.drop( i1, c ).type = Heap.Type.CRYSTAL_CHEST;
		level.drop( i2, c + PathFinder.NEIGHBOURS8[Random.Int( 8 )]).type = Heap.Type.CRYSTAL_CHEST;
		level.addItemToSpawn( new CrystalKey( Dungeon.depth ) );
		
		entrance().set( Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey( Dungeon.depth ) );
	}
	
	private Item prize( Level level ) {
		Generator.Category cat = prizeClasses.remove(0);
		Item prize = null;
		do {
			prize = Generator.random(cat);
		} while (prize == null || Challenges.isItemBlocked(prize));
		return prize;
	}
	
	private ArrayList<Generator.Category> prizeClasses = new ArrayList<>(
			Arrays.asList(Generator.Category.WAND,
					Generator.Category.RING,
					Generator.Category.ARTIFACT));
}
