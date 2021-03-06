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
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.zrp200.lustrouspixeldungeon.Assets;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.Actor;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.actors.mobs.Rat;
import com.zrp200.lustrouspixeldungeon.actors.mobs.npcs.NPC;
import com.zrp200.lustrouspixeldungeon.effects.CellEmitter;
import com.zrp200.lustrouspixeldungeon.effects.Effects;
import com.zrp200.lustrouspixeldungeon.effects.Speck;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.scenes.CellSelector;
import com.zrp200.lustrouspixeldungeon.scenes.GameScene;
import com.zrp200.lustrouspixeldungeon.ui.ActionIndicator;
import com.zrp200.lustrouspixeldungeon.ui.BuffIndicator;
import com.zrp200.lustrouspixeldungeon.utils.GLog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.zrp200.lustrouspixeldungeon.Dungeon.hero;

public class Preparation extends Buff implements ActionIndicator.Action {
	
	{
		//always acts after other buffs, so invisibility effects can process first
		actPriority = BUFF_PRIO - 1;
	}
	
	public enum AttackLevel{
		LVL_1( 1,  0.1f, 0.0f, 1, 0),
		LVL_2( 3,  0.2f, 0.0f, 1, 1),
		LVL_3( 6,  0.3f, 0.0f, 2, 3),
		LVL_4( 11, 0.4f, 0.6f, 2, 5),
		LVL_5( 16, 0.5f, 1.0f, 3, 7);
		
		final int turnsReq;
		final float baseDmgBonus, missingHPBonus;
		final int damageRolls, blinkDistance;
		
		AttackLevel( int turns, float base, float missing, int rolls, int dist){
			turnsReq = turns;
			baseDmgBonus = base; missingHPBonus = missing;
			damageRolls =rolls; blinkDistance = dist;
		}
		
		public boolean canInstakill(Char defender){
			return this == LVL_5
					&& !defender.properties().contains(Char.Property.MINIBOSS)
					&& !defender.properties().contains(Char.Property.BOSS);
		}
		
		public int damageRoll( Char attacker, Char defender){
			int dmg = attacker.damageRoll();
			for( int i = 1; i < damageRolls; i++){
				int newDmg = attacker.damageRoll();
				if (newDmg > dmg) dmg = newDmg;
			}
			float defenderHPPercent = 1f - (defender.HP / (float)defender.HT);
			return Math.round(dmg * (1f + baseDmgBonus + (missingHPBonus * defenderHPPercent)));
		}
		
		public static AttackLevel getLvl(int turnsInvis){
			List<AttackLevel> values = Arrays.asList(values());
			Collections.reverse(values);
			for ( AttackLevel lvl : values ){
				if (turnsInvis >= lvl.turnsReq){
					return lvl;
				}
			}
			return LVL_1;
		}
	}
	
	private int turnsInvis = 0;
	
	@Override
	public boolean act() {
		if (target.invisible > 0){
			turnsInvis++;
			if (AttackLevel.getLvl(turnsInvis).blinkDistance > 0 && target == hero){
				ActionIndicator.setAction(this);
			}
			BuffIndicator.refreshHero();
			spend(TICK);
		} else {
			detach();
		}
		return true;
	}
	
	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}
	
	public int damageRoll(Char attacker, Char defender ){
		AttackLevel lvl = AttackLevel.getLvl(turnsInvis);
		if (lvl.canInstakill(defender)){
			int dmg = lvl.damageRoll(attacker, defender);
			defender.damage( Math.max(defender.HT, dmg), attacker );
			//even though the defender is dead, other effects should still proc (enchants, etc.)
			return Math.max( defender.HT, dmg);
		} else {
			return lvl.damageRoll(attacker, defender);
		}
	}
	
	@Override
	public int icon() {
		return BuffIndicator.PREPARATION;
	}
	
	@Override
	public void tintIcon(Image icon) {
		switch (AttackLevel.getLvl(turnsInvis)){
			case LVL_1:
				icon.hardlight(1f, 1f, 1f);
				break;
			case LVL_2:
				icon.hardlight(0f, 1f, 0f);
				break;
			case LVL_3:
				icon.hardlight(1f, 1f, 0f);
				break;
			case LVL_4:
				icon.hardlight(1f, 0.6f, 0f);
				break;
			case LVL_5:
				icon.hardlight(1f, 0f, 0f);
				break;
		}
	}
	
	@Override
	public String toString() {
		return Messages.get(this, "name");
	}
	
	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		
		AttackLevel lvl = AttackLevel.getLvl(turnsInvis);
		
		if (lvl.canInstakill(new Rat())){
			desc += "\n\n" + Messages.get(this, "desc_dmg_instakill",
					(int)(lvl.baseDmgBonus*100),
					(int)(lvl.baseDmgBonus*100 + lvl.missingHPBonus*100));
		} else if (lvl.missingHPBonus > 0){
			desc += "\n\n" + Messages.get(this, "desc_dmg_scale",
					(int)(lvl.baseDmgBonus*100),
					(int)(lvl.baseDmgBonus*100 + lvl.missingHPBonus*100));
		} else {
			desc += "\n\n" + Messages.get(this, "desc_dmg", (int)(lvl.baseDmgBonus*100));
		}
		
		if (lvl.damageRolls > 1){
			desc += " " + Messages.get(this, "desc_dmg_likely");
		}
		
		if (lvl.blinkDistance > 0){
			desc += "\n\n" + Messages.get(this, "desc_blink", lvl.blinkDistance);
		}
		
		desc += "\n\n" + Messages.get(this, "desc_invis_time", turnsInvis);
		
		if (lvl.ordinal() != AttackLevel.values().length-1){
			AttackLevel next = AttackLevel.values()[lvl.ordinal()+1];
			desc += "\n" + Messages.get(this, "desc_invis_next", next.turnsReq);
		}
		
		return desc;
	}
	
	private static final String TURNS = "turnsInvis";
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turnsInvis = bundle.getInt(TURNS);
		if (AttackLevel.getLvl(turnsInvis).blinkDistance > 0){
			ActionIndicator.setAction(this);
		}
	}
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TURNS, turnsInvis);
	}
	
	@Override
	public Image getIcon() {
		Image actionIco = Effects.get(Effects.Type.WOUND);
		tintIcon(actionIco);
		return actionIco;
	}
	
	@Override
	public void doAction() {
		GameScene.selectCell(attack);
	}
	
	private CellSelector.Listener attack = new CellSelector.Listener() {
		
		@Override
		public void onSelect(Integer cell) {
			if (cell == null) return;
			final Char enemy = Actor.findChar( cell );
			if (enemy == null || hero.isCharmedBy(enemy) || enemy instanceof NPC){
				GLog.w(Messages.get(Preparation.class, "no_target"));
			} else {
				
				//just attack them then!
				if (hero.canAttack(enemy)){
					if (hero.handle( cell )) {
						hero.next();
					}
					return;
				}
				
				AttackLevel lvl = AttackLevel.getLvl(turnsInvis);
				
				boolean[] passable = Dungeon.level.passable.clone();
				//need to consider enemy cell as passable in case they are on a trap or chasm
				passable[cell] = true;
				PathFinder.buildDistanceMap(hero.pos, passable, lvl.blinkDistance+hero.weapon().reachFactor(hero));
				if (PathFinder.distance[cell] == Integer.MAX_VALUE){
					GLog.w(Messages.get(Preparation.class, "out_of_reach"));
					return;
				}
				
				//we can move through enemies when determining blink distance,
				// but not when actually jumping to a location
				for (Char ch : Actor.chars()){
					if (ch != hero)  passable[ch.pos] = false;
				}
				
				PathFinder.Path path = PathFinder.find(hero.pos, cell, passable);
				int attackPos = -1;
				int originalPos = hero.pos;
				try {
					if(path != null) for( int pos : path.subList(0, path.size() - 2) ) {
						hero.pos = pos;
						if(hero.canAttack(enemy)) {
							attackPos = pos;
							break;
						}
					}
				} finally { hero.pos = originalPos; }
				
				if (attackPos == -1 || Dungeon.level.distance(attackPos, hero.pos) > lvl.blinkDistance){
					GLog.w(Messages.get(Preparation.class, "out_of_reach"));
					return;
				}
				
				hero.pos = attackPos;
				Dungeon.level.press(hero.pos, hero);
				//prevents the hero from being interrupted by seeing new enemies
				Dungeon.observe();
				hero.checkVisibleMobs();
				
				hero.sprite.place( hero.pos );
				hero.sprite.turnTo( hero.pos, cell);
				CellEmitter.get( hero.pos ).burst( Speck.factory( Speck.WOOL ), 6 );
				Sample.INSTANCE.play( Assets.SND_PUFF );
				
				if (hero.handle( cell )) {
					hero.next();
				}
			}
		}
		
		@Override
		public String prompt() {
			return Messages.get(Preparation.class, "prompt", AttackLevel.getLvl(turnsInvis).blinkDistance);
		}
	};
}
