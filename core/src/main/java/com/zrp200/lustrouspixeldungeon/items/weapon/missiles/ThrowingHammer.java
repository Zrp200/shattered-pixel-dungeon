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

package com.zrp200.lustrouspixeldungeon.items.weapon.missiles;

import com.zrp200.lustrouspixeldungeon.sprites.ItemSpriteSheet;

public class ThrowingHammer extends MissileWeapon {
	
	{
		image = ItemSpriteSheet.THROWING_HAMMER;
		
		tier = 5;
		baseUses = 15;
		sticky = false;
	}

	@Override
	public int minBase() {
		return Math.round(1.6f * tier); // 8, down from 10
	}

	@Override
	public int maxBase() {
		return 4*tier; // 20, down from 25
	}
}
