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

package com.zrp200.lustrouspixeldungeon.actors.blobs;

import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Buff;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Shadows;
import com.zrp200.lustrouspixeldungeon.actors.hero.Hero;
import com.zrp200.lustrouspixeldungeon.effects.BlobEmitter;
import com.zrp200.lustrouspixeldungeon.effects.particles.ShaftParticle;
import com.zrp200.lustrouspixeldungeon.journal.Notes;
import com.zrp200.lustrouspixeldungeon.levels.Terrain;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.scenes.GameScene;

import static com.zrp200.lustrouspixeldungeon.Dungeon.level;

public class Foliage extends Blob {

	private boolean visible = false;
	@Override
	protected void evolve() {
		visible = false;
		applyToBlobArea(1, new EvolveCallBack() {
			@Override
			protected void call() {
				if (cur[cell] > 0) {

					off[cell] = cur[cell];
					volume += off[cell];

					if (level.map[cell] == Terrain.EMBERS) {
						level.map[cell] = Terrain.GRASS;
						GameScene.updateMap(cell);
					}
					visible = visible || level.heroFOV[cell];

				} else {
					off[cell] = 0;
				}
			}
		});
		
		Hero hero = Dungeon.hero;
		if (hero.isAlive() && hero.visibleEnemies() == 0 && cur[hero.pos] > 0) {
			Buff.affect( hero, Shadows.class ).prolong();
		}

		if (visible) {
			Notes.add( Notes.Landmark.GARDEN );
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.start( ShaftParticle.FACTORY, 0.9f, 0 );
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
