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

package com.zrp200.lustrouspixeldungeon.items.bombs;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.lustrouspixeldungeon.Assets;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.LustSettings;
import com.zrp200.lustrouspixeldungeon.LustrousPixelDungeon;
import com.zrp200.lustrouspixeldungeon.actors.Actor;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.actors.hero.Hero;
import com.zrp200.lustrouspixeldungeon.effects.CellEmitter;
import com.zrp200.lustrouspixeldungeon.effects.particles.BlastParticle;
import com.zrp200.lustrouspixeldungeon.effects.particles.SmokeParticle;
import com.zrp200.lustrouspixeldungeon.items.Heap;
import com.zrp200.lustrouspixeldungeon.items.Item;
import com.zrp200.lustrouspixeldungeon.items.Recipe;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfFrost;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfHealing;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfInvisibility;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfLiquidFlame;
import com.zrp200.lustrouspixeldungeon.items.quest.GooBlob;
import com.zrp200.lustrouspixeldungeon.items.quest.MetalShard;
import com.zrp200.lustrouspixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.zrp200.lustrouspixeldungeon.items.scrolls.ScrollOfRage;
import com.zrp200.lustrouspixeldungeon.items.scrolls.ScrollOfRecharging;
import com.zrp200.lustrouspixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.zrp200.lustrouspixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.zrp200.lustrouspixeldungeon.messages.Languages;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.scenes.GameScene;
import com.zrp200.lustrouspixeldungeon.sprites.CharSprite;
import com.zrp200.lustrouspixeldungeon.sprites.ItemSprite;
import com.zrp200.lustrouspixeldungeon.sprites.ItemSpriteSheet;
import com.zrp200.lustrouspixeldungeon.utils.GLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Bomb extends Item {
	
	{
		image = ItemSpriteSheet.BOMB;

		defaultAction = AC_LIGHTTHROW;
		usesTargeting = true;

		stackable = true;
	}

	public Fuse fuse;

	//FIXME using a static variable for this is kinda gross, should be a better way
	private static boolean lightingFuse = false;

	private static final String AC_LIGHTTHROW = "LIGHTTHROW";

	@Override
	public boolean isSimilar(Item item) {
		return super.isSimilar(item) && this.fuse == ((Bomb) item).fuse;
	}
	
	public boolean explodesDestructively(){
		return true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		actions.add ( AC_LIGHTTHROW );
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {

		if (action.equals(AC_LIGHTTHROW)) {
			lightingFuse = true;
			action = AC_THROW;
		} else
			lightingFuse = false;

		super.execute(hero, action);
	}

	@Override
	protected void afterThrow( int cell ) {
		if (!Dungeon.level.pit[ cell ] && lightingFuse) {
			Actor.addDelayed(fuse = new Fuse().ignite(this), 2);
		}
		if (Actor.findChar( cell ) != null && !(Actor.findChar( cell ) instanceof Hero) ){
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8)
				if (Dungeon.level.passable[cell + i])
					candidates.add(cell + i);
			int newCell = candidates.isEmpty() ? cell : Random.element(candidates);
			drop(newCell).sprite.drop( cell );
		} else
			super.afterThrow( cell );
	}

	@Override
	public boolean doPickUp(Hero hero) {
		if (fuse != null) {
			GLog.w( Messages.get(this, "snuff_fuse") );
			fuse = null;
		}
		return super.doPickUp(hero);
	}

	public void explode(int cell){
		//We're blowing up, so no need for a fuse anymore.
		this.fuse = null;

		Sample.INSTANCE.play( Assets.SND_BLAST );

		if (explodesDestructively()) {
			
			ArrayList<Char> affected = new ArrayList<>();
			
			if (Dungeon.level.heroFOV[cell]) {
				CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
			}
			
			boolean terrainAffected = false;
			for (int n : PathFinder.NEIGHBOURS9) {
				int c = cell + n;
				if (c >= 0 && c < Dungeon.level.length()) {
					if (Dungeon.level.heroFOV[c]) {
						CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
					}
					
					if (Dungeon.level.flamable[c]) {
						Dungeon.level.destroy(c);
						GameScene.updateMap(c);
						terrainAffected = true;
					}
					
					//destroys items / triggers bombs caught in the blast.
					Heap heap = Dungeon.level.heaps.get(c);
					if (heap != null)
						heap.explode();
					
					Char ch = Actor.findChar(c);
					if (ch != null) {
						affected.add(ch);
					}
				}
			}
			
			for (Char ch : affected){
				int dmg = Random.NormalIntRange(5 + Dungeon.depth, 10 + Dungeon.depth*2);

				//those not at the center of the blast take less damage
				if (ch.pos != cell){
					dmg = Math.round(dmg*0.67f);
				}

				dmg -= ch.drRoll();

				if (dmg > 0) {
					ch.damage(dmg, this);
				}
				
				if (ch == Dungeon.hero && !ch.isAlive()) {
					Dungeon.fail(Bomb.class);
				}
			}
			
			if (terrainAffected) {
				Dungeon.observe();
			}
		}
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public Item random() {
		switch(Random.Int( 4 )){
			case 0:
				return new DoubleBomb();
			default:
				return this;
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return fuse != null ? new ItemSprite.Glowing( 0xFF0000, 0.6f) : null;
	}

	@Override
	public int price() {
		return 20 * quantity;
	}
	
	@Override
	public String desc() {
		if (fuse == null)
			return super.desc()+ "\n\n" + Messages.get(this, "desc_fuse");
		else
			return super.desc() + "\n\n" + Messages.get(this, "desc_burning");
	}

	private static final String FUSE = "fuse";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( FUSE, fuse );
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains( FUSE ))
			Actor.add( fuse = ((Fuse)bundle.get(FUSE)).ignite(this) );
	}


	public static class Fuse extends Actor{

		{
			actPriority = BLOB_PRIO+1; //after hero, before other actors
		}

		private Bomb bomb;

		public Fuse ignite(Bomb bomb){
			this.bomb = bomb;
			return this;
		}

		@Override
		protected boolean act() {

			//something caused our bomb to explode early, or be defused. Do nothing.
			if (bomb.fuse != this){
				Actor.remove( this );
				return true;
			}

			//look for our bomb, remove it from its heap, and blow it up.
			for (Heap heap : Dungeon.level.heaps.values()) {
				if (heap.items.contains(bomb)) {

					//FIXME this is a bit hacky, might want to generalize the functionality
					//of bombs that don't explode instantly when their fuse ends
					if (bomb instanceof Noisemaker){

						((Noisemaker) bomb).setTrigger(heap.pos);

					} else {

						heap.items.remove(bomb);
						if (heap.items.isEmpty()) {
							heap.destroy();
						}

						Actor.remove(this); // Before bomb stuff happens and we know that the bomb is still there.
					bomb.explode(heap.pos);}

					diactivate();
					Actor.remove(this);
					return true;
				}
			}

			//can't find our bomb, something must have removed it, do nothing.
			bomb.fuse = null;
			Actor.remove( this );
			return true;
		}
	}


	public static class DoubleBomb extends Bomb{

		{
			image = ItemSpriteSheet.DBL_BOMB;
			stackable = false;
		}

		@Override
		public boolean doPickUp(Hero hero) {
			Bomb bomb = new Bomb();
			bomb.quantity(2);
			if (bomb.doPickUp(hero)) {
				//isaaaaac.... (don't bother doing this when not in english)
				if (LustSettings.language() == Languages.ENGLISH)
					hero.sprite.showStatus(CharSprite.NEUTRAL, "1+1 free!");
				return true;
			}
			return false;
		}
	}
	
	public static class EnhanceBomb extends Recipe {
		
		public static final LinkedHashMap<Class<?extends Item>, Class<?extends Bomb>> validIngredients = new LinkedHashMap<>();
		static {
			validIngredients.put(PotionOfFrost.class,           FrostBomb.class);
			validIngredients.put(ScrollOfMirrorImage.class,     WoollyBomb.class);
			
			validIngredients.put(PotionOfLiquidFlame.class,     Firebomb.class);
			validIngredients.put(ScrollOfRage.class,            Noisemaker.class);
			
			validIngredients.put(PotionOfInvisibility.class,    Flashbang.class);
			validIngredients.put(ScrollOfRecharging.class,      ShockBomb.class);
			validIngredients.put(ScrollOfTeleportation.class,			TeleportationBomb.class);

			validIngredients.put(PotionOfHealing.class,         RegrowthBomb.class);
			validIngredients.put(ScrollOfRemoveCurse.class,     HolyBomb.class);


			validIngredients.put(GooBlob.class,                 ArcaneBomb.class);
			validIngredients.put(MetalShard.class,              ShrapnelBomb.class);
		}
		
		private static final HashMap<Class<?extends Bomb>, Integer> bombCosts = new HashMap<>();
		static {
			bombCosts.put(FrostBomb.class,      2);
			bombCosts.put(WoollyBomb.class,     2);
			
			bombCosts.put(Firebomb.class,       4);
			bombCosts.put(Noisemaker.class,     4);
			
			bombCosts.put(Flashbang.class,      6);
			bombCosts.put(ShockBomb.class,      6);
bombCosts.put(TeleportationBomb.class, 5);

			bombCosts.put(RegrowthBomb.class,   8);
			bombCosts.put(HolyBomb.class,       8);
			
			bombCosts.put(ArcaneBomb.class,     10);
			bombCosts.put(ShrapnelBomb.class,   10);
		}
		
		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			boolean bomb = false;
			boolean ingredient = false;
			
			for (Item i : ingredients){
				if (!i.isIdentified()) return false;
				if (i.getClass().equals(Bomb.class)){
					bomb = true;
				} else if (validIngredients.containsKey(i.getClass())){
					ingredient = true;
				}
			}
			
			return bomb && ingredient;
		}
		
		@Override
		public int cost(ArrayList<Item> ingredients) {
			for (Item i : ingredients){
				if (validIngredients.containsKey(i.getClass())){
					return (bombCosts.get(validIngredients.get(i.getClass())));
				}
			}
			return 0;
		}
		
		@Override
		public Item brew(ArrayList<Item> ingredients) {
			Item result = null;
			
			for (Item i : ingredients){
				i.quantity(i.quantity()-1);
				if (validIngredients.containsKey(i.getClass())){
					try {
						result = validIngredients.get(i.getClass()).newInstance();
					} catch (Exception e) {
						LustrousPixelDungeon.reportException(e);
					}
				}
			}
			
			return result;
		}
		
		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			for (Item i : ingredients){
				if (validIngredients.containsKey(i.getClass())){
					try {
						return validIngredients.get(i.getClass()).newInstance();
					} catch (Exception e) {
						LustrousPixelDungeon.reportException(e);
					}
				}
			}
			return null;
		}
	}
}