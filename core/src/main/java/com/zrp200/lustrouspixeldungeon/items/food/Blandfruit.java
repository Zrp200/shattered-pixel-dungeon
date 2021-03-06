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

package com.zrp200.lustrouspixeldungeon.items.food;

import com.watabou.utils.Bundle;
import com.zrp200.lustrouspixeldungeon.Challenges;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.LustrousPixelDungeon;
import com.zrp200.lustrouspixeldungeon.actors.buffs.Hunger;
import com.zrp200.lustrouspixeldungeon.actors.hero.Hero;
import com.zrp200.lustrouspixeldungeon.items.Item;
import com.zrp200.lustrouspixeldungeon.items.Recipe;
import com.zrp200.lustrouspixeldungeon.items.potions.Potion;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfExperience;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfFrost;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfHaste;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfHealing;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfInvisibility;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfLevitation;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfLiquidFlame;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfMindVision;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfParalyticGas;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfPurity;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfStrength;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfToxicGas;
import com.zrp200.lustrouspixeldungeon.levels.Terrain;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.plants.Plant.Seed;
import com.zrp200.lustrouspixeldungeon.plants.Sungrass;
import com.zrp200.lustrouspixeldungeon.sprites.ItemSprite;
import com.zrp200.lustrouspixeldungeon.sprites.ItemSpriteSheet;
import com.zrp200.lustrouspixeldungeon.utils.GLog;

import java.util.ArrayList;

public class Blandfruit extends Food {

	public Potion potionAttrib = null;
	public ItemSprite.Glowing potionGlow = null;

	{
		stackable = true;
		image = ItemSpriteSheet.BLANDFRUIT;

		//only applies when blandfruit is cooked
		energy = Hunger.STARVING;

		bones = true;
	}

	@Override
	public boolean isSimilar( Item item ) {
		if ( super.isSimilar(item) ){
			Blandfruit other = (Blandfruit) item;
			if (potionAttrib == null && other.potionAttrib == null) {
					return true;
			} else return potionAttrib != null && other.potionAttrib != null
                    && potionAttrib.isSimilar(other.potionAttrib);
		}
		return false;
	}

	@Override
	public void execute( Hero hero, String action ) {

		if (action.equals( AC_EAT ) && potionAttrib == null) {

			GLog.w( Messages.get(this, "raw"));
			return;

		}

		super.execute(hero, action);

		if (action.equals( AC_EAT ) && potionAttrib != null){

			potionAttrib.apply(hero);

		}
	}

	@Override
	public String desc() {
		if (potionAttrib== null) {
			return super.desc();
		} else {
			String desc = Messages.get(this, "desc_cooked") + "\n\n";
			if (potionAttrib instanceof PotionOfFrost
				|| potionAttrib instanceof PotionOfLiquidFlame
				|| potionAttrib instanceof PotionOfToxicGas
				|| potionAttrib instanceof PotionOfParalyticGas) {
				desc += Messages.get(this, "desc_throw");
			} else {
				desc += Messages.get(this, "desc_eat");
			}
			return desc;
		}
	}

	@Override
	public int price() {
		return 20 * quantity;
	}

	public Item cook(Seed seed){

		try {
			return imbuePotion(Potion.SeedToPotion.types.get(seed.getClass()).newInstance());
		} catch (Exception e) {
			LustrousPixelDungeon.reportException(e);
			return null;
		}

	}

	public Item imbuePotion(Potion potion){

		potionAttrib = potion;
		potionAttrib.anonymize();

		potionAttrib.image = ItemSpriteSheet.BLANDFRUIT;

		if (potionAttrib instanceof PotionOfHealing){
			trueName = Messages.get(this, "sunfruit");
			potionGlow = new ItemSprite.Glowing( 0x2EE62E );
		} else if (potionAttrib instanceof PotionOfStrength){
			trueName = Messages.get(this, "rotfruit");
			potionGlow = new ItemSprite.Glowing( 0xCC0022 );
		} else if (potionAttrib instanceof PotionOfParalyticGas){
			trueName = Messages.get(this, "earthfruit");
			potionGlow = new ItemSprite.Glowing( 0x67583D );
		} else if (potionAttrib instanceof PotionOfInvisibility){
			trueName = Messages.get(this, "blindfruit");
			potionGlow = new ItemSprite.Glowing( 0xD9D9D9 );
		} else if (potionAttrib instanceof PotionOfLiquidFlame){
			trueName = Messages.get(this, "firefruit");
			potionGlow = new ItemSprite.Glowing( 0xFF7F00 );
		} else if (potionAttrib instanceof PotionOfFrost){
			trueName = Messages.get(this, "icefruit");
			potionGlow = new ItemSprite.Glowing( 0x66B3FF );
		} else if (potionAttrib instanceof PotionOfMindVision){
			trueName = Messages.get(this, "fadefruit");
			potionGlow = new ItemSprite.Glowing( 0x919999 );
		} else if (potionAttrib instanceof PotionOfToxicGas){
			trueName = Messages.get(this, "sorrowfruit");
			potionGlow = new ItemSprite.Glowing( 0xA15CE5 );
		} else if (potionAttrib instanceof PotionOfLevitation) {
			trueName = Messages.get(this, "stormfruit");
			potionGlow = new ItemSprite.Glowing( 0x1B5F79 );
		} else if (potionAttrib instanceof PotionOfPurity) {
			trueName = Messages.get(this, "dreamfruit");
			potionGlow = new ItemSprite.Glowing( 0xC152AA );
		} else if (potionAttrib instanceof PotionOfExperience) {
			trueName = Messages.get(this, "starfruit");
			potionGlow = new ItemSprite.Glowing( 0x404040 );
		} else if (potionAttrib instanceof PotionOfHaste) {
			trueName = Messages.get(this, "swiftfruit");
			potionGlow = new ItemSprite.Glowing( 0xCCBB00 );
		}

		return this;
	}

	public static final String POTIONATTRIB = "potionattrib";
	
	@Override
	protected void afterThrow(int cell) {
		if (Dungeon.level.map[cell] == Terrain.WELL || Dungeon.level.pit[cell]) {
			super.onThrow( cell );
			
		} else if (potionAttrib instanceof PotionOfLiquidFlame ||
				potionAttrib instanceof PotionOfToxicGas ||
				potionAttrib instanceof PotionOfParalyticGas ||
				potionAttrib instanceof PotionOfFrost ||
				potionAttrib instanceof PotionOfLevitation ||
				potionAttrib instanceof PotionOfPurity) {

			potionAttrib.shatter( cell );
			new Chunks().drop(cell);
			
		} else {
			super.afterThrow(cell);
		}
	}
	
	@Override
	public void reset() {
		if (potionAttrib != null)
			imbuePotion(potionAttrib);
		else
			super.reset();
	}
	
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put( POTIONATTRIB , potionAttrib);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(POTIONATTRIB)) {
			imbuePotion((Potion) bundle.get(POTIONATTRIB));
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return potionGlow;
	}
	
	public static class CookFruit extends Recipe {
		
		@Override
		//also sorts ingredients if it can
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (ingredients.size() != 2) return false;
			
			if (ingredients.get(0) instanceof Blandfruit){
				if (!(ingredients.get(1) instanceof Seed)){
					return false;
				}
			} else if (ingredients.get(0) instanceof Seed){
				if (ingredients.get(1) instanceof Blandfruit){
					Item temp = ingredients.get(0);
					ingredients.set(0, ingredients.get(1));
					ingredients.set(1, temp);
				} else {
					return false;
				}
			} else {
				return false;
			}
			
			Blandfruit fruit = (Blandfruit) ingredients.get(0);
			Seed seed = (Seed) ingredients.get(1);
			
			if (fruit.quantity() >= 1 && fruit.potionAttrib == null
				&& seed.quantity() >= 1){

                return !Dungeon.isChallenged(Challenges.NO_HEALING)
                        || !(seed instanceof Sungrass.Seed);
            }
			
			return false;
		}
		
		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 3;
		}
		
		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			
			ingredients.get(0).quantity(ingredients.get(0).quantity() - 1);
			ingredients.get(1).quantity(ingredients.get(1).quantity() - 1);
			
			
			return new Blandfruit().cook((Seed) ingredients.get(1));
		}
		
		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			
			return new Blandfruit().cook((Seed) ingredients.get(1));
		}
	}

	public static class Chunks extends Food {

		{
			stackable = true;
			image = ItemSpriteSheet.BLAND_CHUNKS;

			energy = Hunger.STARVING;

			bones = true;
		}

	}

}
