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

package com.zrp200.lustrouspixeldungeon.actors.buffs;

import com.watabou.noosa.Image;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.effects.CellEmitter;
import com.zrp200.lustrouspixeldungeon.effects.particles.EarthParticle;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.ui.BuffIndicator;

//pre-0.7.0, otherwise unused
public class EarthImbue extends FlavourBuff {
	
	{
		type = buffType.POSITIVE;
		announced = true;
	}

	public static final float DURATION	= 30f;

	public void proc(Char enemy){
		Buff.affect(enemy, Cripple.class, 2);
		CellEmitter.bottom(enemy.pos).start(EarthParticle.FACTORY, 0.05f, 8);
	}

	@Override
	public int icon() {
		return BuffIndicator.ROOTS;
	}
	
	@Override
	public void tintIcon(Image icon) {
		greyIcon(icon, 5f, cooldown());
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns());
	}
	
}