/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2018 Evan Debenham
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

package com.zrp200.lustrouspixeldungeon.items.scrolls;

import com.watabou.noosa.audio.Sample;
import com.zrp200.lustrouspixeldungeon.Assets;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Amok;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Buff;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Invisibility;
import com.zrp200.lustrouspixeldungeon.actors.mobs.Mimic;
import com.zrp200.lustrouspixeldungeon.actors.mobs.Mob;
import com.zrp200.lustrouspixeldungeon.effects.Speck;
import com.zrp200.lustrouspixeldungeon.items.Heap;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.utils.GLog;

public class ScrollOfRage extends Scroll {

	{
		initials = 5;
	}

	@Override
	public void doRead() {

		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			mob.beckon( curUser.pos );
			if (Dungeon.level.heroFOV[mob.pos]) {
				Buff.prolong(mob, Amok.class, 5f);
			}
		}

		for (Heap heap : Dungeon.level.heaps.values()) {
			if (heap.type == Heap.Type.MIMIC) {
				Mimic m = Mimic.spawnAt( heap.pos, heap.items );
				if (m != null) {
					m.beckon( curUser.pos );
					heap.destroy();
				}
			}
		}

		GLog.w( Messages.get(this, "roar") );
		setKnown();
		
		curUser.sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
		Sample.INSTANCE.play( Assets.SND_CHALLENGE );
		Invisibility.dispel();

		readAnimation();
	}
	
	@Override
	public void empoweredRead() {
		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (Dungeon.level.heroFOV[mob.pos]) {
				Buff.prolong(mob, Amok.class, 5f);
			}
		}
		
		setKnown();
		
		curUser.sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel();
		
		readAnimation();
	}
	
	@Override
	public int price() {
		return isKnown() ? 40 * quantity : super.price();
	}
}