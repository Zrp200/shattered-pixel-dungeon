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

package com.zrp200.lustrouspixeldungeon.actors.mobs;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.lustrouspixeldungeon.Assets;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.Actor;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Barrier;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Buff;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Charm;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Light;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Sleep;
import com.zrp200.lustrouspixeldungeon.effects.Speck;
import com.zrp200.lustrouspixeldungeon.items.scrolls.ScrollOfLullaby;
import com.zrp200.lustrouspixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.zrp200.lustrouspixeldungeon.mechanics.Ballistica;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.sprites.SuccubusSprite;

import java.util.ArrayList;
import java.util.HashMap;

public class Succubus extends Mob {
	public static Class<?extends Mob> random() {
		return Random.chances(new HashMap<Class<?extends Mob>, Float>() {
			{
				put(Succubus.class,			2.5f);
				put(Succubus.Winged.class, 	1f);
			}
		});
	}
	
	private static final int BLINK_DELAY	= 5;
	
	private int delay = 0;
	
	{
		spriteClass = SuccubusSprite.class;
		
		HP = HT = 80;
		defenseSkill = 25;
		viewDistance = Light.DISTANCE;
		
		EXP = 12;
		maxLvl = 25;
		
		loot = new ScrollOfLullaby();
		lootChance = 0.05f;

		properties.add(Property.DEMONIC);
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 22, 30 );
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		
		if ( enemy.isCharmedBy(this) ){
			int shield = (HP - HT) + (5 + damage);
			if (shield > 0){
				HP = HT;
				Buff.affect(this, Barrier.class).incShield(shield);
			} else {
				HP += 5 + damage;
			}
			sprite.emitter().burst( Speck.factory( Speck.HEALING ), 2 );
			Sample.INSTANCE.play( Assets.SND_CHARMS );
		} else if (Random.Int( 3 ) == 0) {
			//attack will reduce by 5 turns, so effectively 3-4 turns
			Buff.affect( enemy, Charm.class, Random.IntRange( 3, 4 ) + 5 ).object = id();
			enemy.sprite.centerEmitter().start( Speck.factory( Speck.HEART ), 0.2f, 5 );
			Sample.INSTANCE.play( Assets.SND_CHARMS );
		}
		
		return damage;
	}
	
	@Override
	protected boolean getCloser( int target ) {
		if (fieldOfView[target] && Dungeon.level.distance( pos, target ) > 2 && delay <= 0) {
			
			blink( target );
			spend( -1 / speed() );
			return true;
			
		} else {
			
			delay--;
			return super.getCloser( target );
			
		}
	}

	private void blink(int target ) {
		
		Ballistica route = new Ballistica( pos, target, Ballistica.PROJECTILE);
		int cell = route.collisionPos;

		//can't occupy the same cell as another char, so move back one.
		if (Actor.findChar( cell ) != null && cell != this.pos)
			cell = route.path.get(route.dist-1);

		if (Dungeon.level.avoid[ cell ]){
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int n : PathFinder.NEIGHBOURS8) {
				cell = route.collisionPos + n;
				if (Dungeon.level.passable[cell] && Actor.findChar( cell ) == null) {
					candidates.add( cell );
				}
			}
			if (candidates.size() > 0)
				cell = Random.element(candidates);
			else {
				delay = BLINK_DELAY;
				return;
			}
		}
		
		ScrollOfTeleportation.appear( this, cell );
		
		delay = BLINK_DELAY;
	}

	protected void zap() {
		sprite.zap(enemy.pos);
		for(Char enemy : findEnemies()) {
			Buff.affect( enemy, Charm.class, Random.IntRange( 3, 4 )).object = id();
			enemy.sprite.centerEmitter().start( Speck.factory( Speck.HEART ), 0.2f, 5 );
			Sample.INSTANCE.play( Assets.SND_CHARMS );
		}
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 40;
	}
	
	@Override
	public int drRoll() {
		return Random.NormalIntRange(0, 10);
	}
	
	{
		immunities.add( Sleep.class );
		immunities.add( Charm.class );
	}


	public static class Winged extends Succubus {
		{
			spriteClass = SuccubusSprite.Winged.class;
			HP = HT = 75;
			defenseSkill = 28; // +3
			baseSpeed = 2;
			flying = true;
		}

		@Override
		public int attackSkill(Char target) {
			return 37;
		} // -3

		@Override
		public String description() {
			return super.description() + "\n\n" + Messages.get(this,"variant_desc");
		}

		@Override
		public int drRoll() {
			return Math.max(super.drRoll()-2,0); // 8 armor
		}

		@Override
		public int damageRoll() {
			return super.damageRoll()-2; // 20-28
		}
	}
}
