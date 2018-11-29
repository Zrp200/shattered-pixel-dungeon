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

package com.zrp200.lustrouspixeldungeon.items.scrolls.exotic;

import com.watabou.noosa.audio.Sample;
import com.zrp200.lustrouspixeldungeon.Assets;
import com.zrp200.lustrouspixeldungeon.actors.buffs.ArtifactRecharge;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Buff;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Invisibility;
import com.zrp200.lustrouspixeldungeon.effects.SpellSprite;
import com.zrp200.lustrouspixeldungeon.items.scrolls.ScrollOfRecharging;

public class ScrollOfMysticalEnergy extends ExoticScroll {
	
	{
		initials = 6;
	}
	
	@Override
	public void doRead() {
		
		//append buff
		Buff.affect(curUser, ArtifactRecharge.class).set( 30 );
		
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel();
		
		SpellSprite.show( curUser, SpellSprite.CHARGE );
		setKnown();
		ScrollOfRecharging.charge(curUser);
		
		readAnimation();
	}
	
}