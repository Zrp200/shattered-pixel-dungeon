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

package com.zrp200.lustrouspixeldungeon.levels.traps;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.zrp200.lustrouspixeldungeon.Assets;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.LustrousPixelDungeon;
import com.zrp200.lustrouspixeldungeon.actors.Actor;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.items.weapon.missiles.darts.Dart;
import com.zrp200.lustrouspixeldungeon.sprites.MissileSprite;

public class DartTrap extends AimingTrap {

	{
		color = GREY;
		shape = CROSSHAIR;
	}

	public void onHit(Char target) {}

	@Override
	public void shoot(final Char target) {
		final DartTrap trap = this;
		if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[target.pos]) {
			Actor.add(new Actor() {

				{
					//it's a visual effect, gets priority no matter what
					actPriority = VFX_PRIO;
				}
					
				@Override
				protected boolean act() {
					final Actor toRemove = this;
					((MissileSprite) LustrousPixelDungeon.scene().recycle(MissileSprite.class)).
							reset(pos, target.sprite, new Dart(), new Callback() {
								@Override
								public void call() {
									int dmg = Random.NormalIntRange(1, 4) - target.drRoll();
									target.damage(dmg, trap);
									if (target == Dungeon.hero && !target.isAlive()){
										Dungeon.fail( trap.getClass()  );
									}
									onHit(target);
									Sample.INSTANCE.play(Assets.SND_HIT, 1, 1, Random.Float(0.8f, 1.25f));
									target.sprite.bloodBurstA(target.sprite.center(), dmg);
									target.sprite.flash();
									Actor.remove(toRemove);
									next();
								}
							});
					return false;
				}
			});
		} else {
			target.damage(Random.NormalIntRange(1, 4) - target.drRoll(), trap);
			onHit(target);
		}
	}
}
