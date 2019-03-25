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

package com.zrp200.lustrouspixeldungeon.items.rings;

import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.items.Generator;
import com.zrp200.lustrouspixeldungeon.items.Gold;
import com.zrp200.lustrouspixeldungeon.items.Item;
import com.zrp200.lustrouspixeldungeon.items.armor.Armor;
import com.zrp200.lustrouspixeldungeon.items.potions.AlchemicalCatalyst;
import com.zrp200.lustrouspixeldungeon.items.potions.PotionOfExperience;
import com.zrp200.lustrouspixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.zrp200.lustrouspixeldungeon.items.spells.ArcaneCatalyst;
import com.zrp200.lustrouspixeldungeon.items.stones.StoneOfEnchantment;
import com.zrp200.lustrouspixeldungeon.items.weapon.Weapon;

import java.util.ArrayList;
import java.util.HashSet;

public class RingOfWealth extends Ring {

	private static final float BONUS_SCALING = 1.2f;
	private float triesToDrop = Float.MIN_VALUE;
	private int dropsToRare = Integer.MIN_VALUE;
	public static boolean latestDropWasRare = false;


	@Override
	protected RingBuff buff() {
		return new Wealth();
	}

	@Override
	protected String effect2Bonus() {
		return visualMultiplier(BONUS_SCALING);
	}
	
	public static float dropChanceMultiplier( Char target ){
		return (float)Math.pow(BONUS_SCALING,getBonus(target, Wealth.class)); // +4.3% compared to shattered
	}

	private static final String TRIES_TO_DROP = "tries_to_drop";
	private static final String DROPS_TO_RARE = "drops_to_rare";
	
	public static ArrayList<Item> tryForBonusDrop(Char target, int tries ){
		if (getBonus(target, Wealth.class) <= 0) return null;
		
		HashSet<Wealth> buffs = target.buffs(Wealth.class);
		float triesToDrop = Float.MIN_VALUE;
		int dropsToRare = Integer.MIN_VALUE;
		//find the largest count (if they aren't synced yet)
		for (Wealth w : buffs){
			if (w.triesToDrop() > triesToDrop){
				triesToDrop = w.triesToDrop();
				dropsToRare = w.dropsToRare();
			}
		}

		//reset (if needed), decrement, and store counts
		if (triesToDrop == Float.MIN_VALUE) {
			triesToDrop = Random.NormalInt(60);
			dropsToRare = Random.NormalInt(20);
		}

		//now handle reward logic
		ArrayList<Item> drops = new ArrayList<>();

		triesToDrop -= dropProgression(target, tries);
		while ( triesToDrop <= 0 ){
			if (dropsToRare <= 0){
				drops.add(genRareDrop());
				latestDropWasRare = true;
				dropsToRare = Random.NormalInt(20);
			} else {
				drops.add(genStandardDrop());
				dropsToRare--;
			}
			triesToDrop += Random.NormalIntRange(0, 60);
		}

		//store values back into rings
		for (Wealth w : buffs){
			w.setTriesToDrop(triesToDrop);
			w.setDropsToRare(dropsToRare);
		}
		
		return drops;
	}
	
	public static Item genStandardDrop(){
		float roll = Random.Float();
		if (roll < 0.3f){ //30% chance
			Item result = new Gold().random();
			result.quantity(Math.round(result.quantity() * Random.NormalFloat(0.33f, 1f)));
			return result;
		} else if (roll < 0.7f){ //40% chance
			return genBasicConsumable();
		} else if (roll < 0.9f){ //20% chance
			return genExoticConsumable();
		} else { //10% chance
			if (Random.Int(3) != 0){
				Weapon weapon = Generator.randomWeapon();
				weapon.enchant(null);
				weapon.cursed = false;
				weapon.cursedKnown = true;
				weapon.level(0);
				return weapon;
			} else {
				Armor armor = Generator.randomArmor();
				armor.inscribe(null);
				armor.cursed = false;
				armor.cursedKnown = true;
				armor.level(0);
				return armor;
			}
		}
	}
	
	private static Item genBasicConsumable(){
		float roll = Random.Float();
		if (roll < 0.4f){ //40% chance
			return Generator.random(Generator.Category.STONE);
		} else if (roll < 0.7f){ //30% chance
			return Generator.random(Generator.Category.POTION);
		} else { //30% chance
			return Generator.random(Generator.Category.SCROLL);
		}
	}
	
	private static Item genExoticConsumable(){
		float roll = Random.Float();
		if (roll < 0.3f){ //30% chance
			return Generator.random(Generator.Category.POTION);
		} else if (roll < 0.6f) { //30% chance
			return Generator.random(Generator.Category.SCROLL);
		} else { //40% chance
			return Random.Int(2) == 0 ? new AlchemicalCatalyst() : new ArcaneCatalyst();
		}
	}
	
	public static Item genRareDrop(){
		float roll = Random.Float();
		if (roll < 0.3f){ //30% chance
			Item result = new Gold().random();
			result.quantity(Math.round(result.quantity() * Random.NormalFloat(3f, 6f)));
			return result;
		} else if (roll < 0.7f){ //40% chance
			return genHighValueConsumable();
		} else if (roll < 0.9f){ //20% chance
			Item result = Random.Int(2) == 0 ? Generator.random(Generator.Category.ARTIFACT) : Generator.random(Generator.Category.RING);
			result.cursed = false;
			result.cursedKnown = true;
			return result;
		} else { //10% chance
			if (Random.Int(3) != 0){
				Weapon weapon = Generator.randomWeapon((Dungeon.depth / 5) + 1);
				weapon.upgrade(1);
				weapon.enchant(Weapon.Enchantment.random());
				weapon.cursed = false;
				weapon.cursedKnown = true;
				return weapon;
			} else {
				Armor armor = Generator.randomArmor((Dungeon.depth / 5) + 1);
				armor.upgrade();
				armor.inscribe(Armor.Glyph.random());
				armor.cursed = false;
				armor.cursedKnown = true;
				return armor;
			}
		}
	}
	
	private static Item genHighValueConsumable(){
		switch( Random.Int(4) ){ //25% chance each
			case 0: default:
				return new StoneOfEnchantment();
			case 1:
				return new StoneOfEnchantment().quantity(2);
			case 2:
				return new PotionOfExperience();
			case 3:
				return new ScrollOfTransmutation();
		}
	}
	
	private static float dropProgression( Char target, int tries ){
		return tries * (float)Math.pow(1.2f, getBonus(target, Wealth.class) );
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TRIES_TO_DROP, triesToDrop);
		bundle.put(DROPS_TO_RARE, dropsToRare);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		triesToDrop = bundle.getFloat(TRIES_TO_DROP);
		dropsToRare = bundle.getInt(DROPS_TO_RARE);
	}

	public class Wealth extends RingBuff {
		
		private float triesToDrop(){
			return triesToDrop;
		}

		private void setTriesToDrop( float val ){
			triesToDrop = val;
		}

		private void setDropsToRare( int val ) {
			dropsToRare = val;
		}

		private int dropsToRare() {
				return dropsToRare;
		}
		
	}
}
