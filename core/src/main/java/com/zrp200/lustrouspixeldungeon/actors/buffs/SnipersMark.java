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
import com.watabou.utils.Bundle;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.Actor;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.actors.hero.Hero;
import com.zrp200.lustrouspixeldungeon.items.weapon.SpiritBow;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.sprites.ItemSprite;
import com.zrp200.lustrouspixeldungeon.sprites.ItemSpriteSheet;
import com.zrp200.lustrouspixeldungeon.ui.ActionIndicator;
import com.zrp200.lustrouspixeldungeon.ui.BuffIndicator;
import com.zrp200.lustrouspixeldungeon.ui.QuickSlotButton;

public class SnipersMark extends FlavourBuff implements ActionIndicator.Action {

	public int object = 0;

	private static final String OBJECT    = "object";
	
	@Override
	public boolean attachTo(Char target) {
		ActionIndicator.setAction(this);
		return super.attachTo(target);
	}
	
	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( OBJECT, object );

	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		object = bundle.getInt( OBJECT );
	}

	@Override
	public int icon() {
		return BuffIndicator.MARK;
	}
	
	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}
	
	@Override
	public Image getIcon() {
		return new ItemSprite(ItemSpriteSheet.SPIRIT_BOW, null);
	}
	
	@Override
	public void doAction() {
		
		Hero hero = Dungeon.hero;
		if (hero == null) return;
		
		SpiritBow bow = hero.belongings.getItem(SpiritBow.class);
		if (bow == null) return;
		
		SpiritBow.SpiritArrow arrow = bow.knockArrow();
		if (arrow == null) return;
		
		Char ch = (Char) Actor.findById(object);
		if (ch == null) return;
		
		int cell = QuickSlotButton.autoAim(ch, arrow);
		if (cell == -1) return;
		
		bow.sniperSpecial = true;
		
		arrow.cast(hero, cell);
		detach();
		
	}
}
