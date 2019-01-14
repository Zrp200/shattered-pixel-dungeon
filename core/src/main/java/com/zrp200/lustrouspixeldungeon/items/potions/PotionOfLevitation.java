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

package com.zrp200.lustrouspixeldungeon.items.potions;

import com.watabou.noosa.audio.Sample;
import com.zrp200.lustrouspixeldungeon.Assets;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.blobs.Blob;
import com.zrp200.lustrouspixeldungeon.actors.blobs.ConfusionGas;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Buff;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Levitation;
import com.zrp200.lustrouspixeldungeon.actors.hero.Hero;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.scenes.GameScene;
import com.zrp200.lustrouspixeldungeon.utils.GLog;

public class PotionOfLevitation extends Potion {

	{
		initials = 5;
	}

	@Override
	public void shatter( int cell ) {

		if (Dungeon.level.heroFOV[cell]) {
			setKnown();

			splash( cell );
			Sample.INSTANCE.play( Assets.SND_SHATTER );
		}

		GameScene.add( Blob.seed( cell, 1000, ConfusionGas.class ) );
	}
	
	@Override
	public void apply( Hero hero ) {
		setKnown();
		Buff.affect( hero, Levitation.class, Levitation.DURATION );
		GLog.i( Messages.get(this, "float") );
	}
	
	@Override
	public int price() {
		return isKnown() ? 40 * quantity : super.price();
	}
}
