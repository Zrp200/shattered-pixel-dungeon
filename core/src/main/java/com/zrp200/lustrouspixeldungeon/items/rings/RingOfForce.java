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

package com.zrp200.lustrouspixeldungeon.items.rings;

import com.watabou.utils.Random;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.actors.hero.Hero;
import com.zrp200.lustrouspixeldungeon.messages.Messages;

public class RingOfForce extends Ring {

	@Override
	protected RingBuff buff() {
		return new Force();
	}

	public static int armedDamageBonus(Char ch ){
		return getBonus( ch, Force.class);
	}
	
	
	// *** Weapon-like properties ***

	private static float tier(int str){
		float tier = Math.max(1, (str - 8)/2f);
		//each str point after 18 is half as effective
		if (tier > 5){
			tier = 5 + (tier - 5) / 2f;
		}
		return tier;
	}

	public static int damageRoll( Hero hero ){
		if (hero.buff(Force.class) != null) {
			int level = getBonus(hero,Force.class);
			float tier = tier(hero.STR());
			return Random.NormalIntRange(min(level, tier), max(level, tier));
		} else {
			//attack without any ring of force influence
			return Random.NormalIntRange(1, Math.max(hero.STR()-8, 1));
		}
	}

	//same as equivalent tier weapon
	private static int min(int lvl, float tier){
		return Math.round(
				tier +  //base
				lvl     //level scaling
		);
	}

	//same as equivalent tier weapon
	private static int max(int lvl, float tier){
		return Math.round(
				(tier+1)*(5+lvl)*0.5f
		);
	}

	@Override
	public String statsInfo() {
		float tier = tier(Dungeon.hero.STR());
		if (isIdentified()) {
			return Messages.get(this, "stats", min(soloBonus(), tier), max(soloBonus(), tier), soloBonus());
		} else {
			return Messages.get(this, "typical_stats", min(1, tier), max(1, tier), 1);
		}
	}

	private class Force extends RingBuff {	}
}

