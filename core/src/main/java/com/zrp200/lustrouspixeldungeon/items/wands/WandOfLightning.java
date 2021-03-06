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

package com.zrp200.lustrouspixeldungeon.items.wands;

import com.watabou.noosa.Camera;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.Actor;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.effects.CellEmitter;
import com.zrp200.lustrouspixeldungeon.effects.Lightning;
import com.zrp200.lustrouspixeldungeon.effects.particles.SparkParticle;
import com.zrp200.lustrouspixeldungeon.items.weapon.enchantments.Shocking;
import com.zrp200.lustrouspixeldungeon.items.weapon.melee.MagesStaff;
import com.zrp200.lustrouspixeldungeon.mechanics.Ballistica;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.sprites.ItemSpriteSheet;
import com.zrp200.lustrouspixeldungeon.tiles.DungeonTilemap;
import com.zrp200.lustrouspixeldungeon.utils.BArray;
import com.zrp200.lustrouspixeldungeon.utils.GLog;

import java.util.ArrayList;

public class WandOfLightning extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_LIGHTNING;
	}
	
	private ArrayList<Char> affected = new ArrayList<>();

	private ArrayList<Lightning.Arc> arcs = new ArrayList<>();

	public int min(int lvl){
		return 5+lvl;
	}

	public int max(int lvl){
		return 10+5*lvl;
	}
	
	@Override
	protected void onZap( Ballistica bolt ) {

		//lightning deals less damage per-target, the more targets that are hit.
		float multipler = 0.4f + (0.6f/affected.size());
		//if the main target is in water, all affected take full damage
		if (Dungeon.level.water[bolt.collisionPos]) multipler = 1f;

		for (Char ch : affected){
			processSoulMark(ch, chargesPerCast());
			ch.damage(Math.round(damageRoll() * multipler), this);

			if (ch == Dungeon.hero) Camera.main.shake( 2, 0.3f );
			ch.sprite.centerEmitter().burst( SparkParticle.FACTORY, 3 );
			ch.sprite.flash();
		}

		if (!curUser.isAlive()) {
			Dungeon.fail( getClass() );
			GLog.n(Messages.get(this, "ondeath"));
		}
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		//acts like shocking enchantment
		new Shocking().proc(staff, attacker, defender, damage);
	}

	private void arc( Char ch ) {
		
		affected.add( ch );

		int dist;
		dist = Dungeon.level.water[ch.pos] && !ch.flying ? 2 : 1;

		PathFinder.buildDistanceMap( ch.pos, BArray.not( Dungeon.level.solid, null ), dist );
		ArrayList<Integer>[] map = PathFinder.sortedMap();
		for(int range=0; range < map.length; range++) {
			Random.shuffle(map[range]); // can't be doing predictable things, can we?
			for(int cell : map[range]) {
				Char n = Actor.findChar(cell);
				if (n != null && (n.alignment == Char.Alignment.ENEMY || range <= 1)) {
					//the hero, as well as allied characters, are only zapped if they are adjacent
					if ( !affected.contains(n) ) {
						arcs.add(new Lightning.Arc(ch.sprite.center(), n.sprite.center()));
						arc(n);
					}
				}
			}
		}
	}
	
	@Override
	protected void fx( Ballistica bolt, Callback callback ) {

		affected.clear();
		arcs.clear();

		int cell = bolt.collisionPos;

		Char ch = Actor.findChar( cell );
		if (ch != null) {
			arcs.add( new Lightning.Arc(curUser.sprite.center(), ch.sprite.center()));
			arc(ch);
		} else {
			arcs.add( new Lightning.Arc(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(bolt.collisionPos)));
			CellEmitter.center( cell ).burst( SparkParticle.FACTORY, 3 );
		}

		//don't want to wait for the effect before processing damage.
		curUser.sprite.parent.addToFront( new Lightning( arcs, null ) );
		callback.call();
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0xFFFFFF);
		particle.am = 0.6f;
		particle.setLifespan(0.6f);
		particle.acc.set(0, +10);
		particle.speed.polar(-Random.Float(3.1415926f), 6f);
		particle.setSize(0f, 1.5f);
		particle.sizeJitter = 1f;
		particle.shuffleXY(1f);
		float dst = Random.Float(1f);
		particle.x -= dst;
		particle.y += dst;
	}
	
}
