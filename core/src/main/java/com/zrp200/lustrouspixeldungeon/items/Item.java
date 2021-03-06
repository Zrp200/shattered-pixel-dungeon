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

package com.zrp200.lustrouspixeldungeon.items;

import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.zrp200.lustrouspixeldungeon.Assets;
import com.zrp200.lustrouspixeldungeon.Badges;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.LustrousPixelDungeon;
import com.zrp200.lustrouspixeldungeon.actors.Actor;
import com.zrp200.lustrouspixeldungeon.actors.Char;
import com.zrp200.lustrouspixeldungeon.actors.hero.Hero;
import com.zrp200.lustrouspixeldungeon.effects.Speck;
import com.zrp200.lustrouspixeldungeon.items.bags.Bag;
import com.zrp200.lustrouspixeldungeon.journal.Catalog;
import com.zrp200.lustrouspixeldungeon.mechanics.Ballistica;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.scenes.CellSelector;
import com.zrp200.lustrouspixeldungeon.scenes.GameScene;
import com.zrp200.lustrouspixeldungeon.sprites.ItemSprite;
import com.zrp200.lustrouspixeldungeon.sprites.MissileSprite;
import com.zrp200.lustrouspixeldungeon.ui.ItemSlot;
import com.zrp200.lustrouspixeldungeon.ui.QuickSlotButton;
import com.zrp200.lustrouspixeldungeon.utils.GLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.zrp200.lustrouspixeldungeon.ui.Window.TITLE_COLOR;

public class Item implements Bundlable, Cloneable {

	private static final String TXT_TO_STRING_LVL		= "%s %+d";
	private static final String TXT_TO_STRING_X		= "%s x%d";
	
	private static final float TIME_TO_THROW		= 1.0f;
	protected static final float TIME_TO_PICK_UP	= 1.0f;
	private static final float TIME_TO_DROP		= 1.0f;
	
	private static final String AC_DROP		= "DROP";
	protected static final String AC_THROW		= "THROW";

	public String defaultAction;
	public boolean usesTargeting;
	
	private String name;
	protected String trueName = Messages.get(this, "name");
	public int image = 0;

	public ItemSprite sprite() {
		return new ItemSprite(this);
	}

	public boolean stackable = false;
	protected int quantity = 1;
	
	private int level = 0;

	public boolean levelKnown = false;
	
	public boolean cursed;
	public boolean cursedKnown;
	
	// Unique items persist through revival
	public boolean unique = false;

	// whether an item can be included in heroes remains
	public boolean bones = false;

	public Item() { reset(); }

	private static Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare( Item lhs, Item rhs ) {
			return Generator.Category.order( lhs ) - Generator.Category.order( rhs );
		}
	};
	
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = new ArrayList<String>();
		actions.add( AC_DROP );
		actions.add( AC_THROW );
		return actions;
	}

	public boolean doPickUp(Hero hero ) {
		if (collect( hero.belongings.backpack )) {
			
			GameScene.pickUp( this, hero.pos );
			Sample.INSTANCE.play( Assets.SND_ITEM );
			hero.spendAndNext( TIME_TO_PICK_UP );
			return true;
			
		} else {
			return false;
		}
	}
	
	public void doDrop( Hero hero ) {
		hero.spendAndNext(TIME_TO_DROP);
		detachAll(hero.belongings.backpack).drop(hero.pos);
	}

	//resets an item's properties, to ensure consistency between runs
	public void reset(){
		//resets the name incase the language has changed.
		trueName = Messages.get(this, "name");
	}

	public void doThrow( Hero hero ) {
		GameScene.selectCell(thrower);
	}
	
	public void execute( Hero hero, String action ) {
		
		curUser = hero;
		curItem = this;
		
		if (action.equals( AC_DROP )) {
			
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doDrop(hero);
			}
			
		} else if (action.equals( AC_THROW )) {
			
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doThrow(hero);
			}
			
		}
	}
	
	public void execute( Hero hero ) {
		execute( hero, defaultAction );
	}

	// when overriding, call super.onThrow (or #onThrowComplete) after the method.
    // This should be overridden when the logic won't affect whether or not it drops.
	// Calling super.onThrow before logic will cause the logic to run after everything has occurred, including #afterThrow.
	protected void onThrow(int cell) { onThrowComplete(cell); }

	// this method exists to allow logic after the initial throw logic. notably sidestepping having to drop the item.
	protected void afterThrow(int cell) { drop(cell); }

	// this prevents the game from getting hung up.
    protected final void onThrowComplete(int cell) {
        afterThrow(cell);
        curUser.spendAndNext(castDelay); // this is so important that I made #afterThrow to ease overriding.
    }
    //takes two items and merges them (if possible)
	public Item merge( Item other ){
		if (isSimilar( other ) && other != this){
			quantity += other.quantity;
			other.quantity = 0;
		}
		return this;
	}

	public Item transmute(boolean dry) {
		return null;
	}
	public final Item transmute() {
		return transmute(false);
	}

	public synchronized Heap drop(int pos) {
		Heap heap = Dungeon.level.drop(quantity > 0 ? this : null , pos);
		if(!heap.isEmpty()) heap.sprite.drop();
		return heap;
	}

	public boolean collect( Bag container ) {
		ArrayList<Item> items = container.items;
		
		if (items.contains( this )) {
			return false;
		}
		
		for (Item item:items) {
			if (item instanceof Bag && ((Bag)item).grab( this ) && ((Bag)item).size > ((Bag)item).items.size()) {
				return collect( (Bag)item );
			}
		}
		
		if (stackable) {
			for (Item item:items) {
				if (isSimilar( item )) {
					item.merge( this );
					updateQuickslot();
					return true;
				}
			}
		}
		
		if (items.size() < container.size) {
			
			if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
				Badges.validateItemLevelAquired( this );
			}
			
			items.add( this );
			Dungeon.quickslot.replacePlaceholder(this);
			updateQuickslot();
			Collections.sort( items, itemComparator );
			return true;
			
		} else {
			GLog.n( Messages.get(Item.class, "pack_full", name()) );
			return false;
			
		}
	}
	
	public final boolean collect() {
		return collect( Dungeon.hero.belongings.backpack );
	}


    // basically a copy constructor, but it can write over pretty much anything...
	public Item emulate(Item item) {
	    Bundle copy = new Bundle();
	    item.storeInBundle(copy);
	    restoreFromBundle(copy);
        return this;
    }

    public Item(Item item) {
		emulate(item);
	}

    @SuppressWarnings("MethodDoesntCallSuperMethod")
	public Item clone() {
        try {
            return getClass().newInstance().emulate(this); // everything of value is already kept here.
        } catch (Exception e) {
            LustrousPixelDungeon.reportException(e);
            return null;
        }
    }

	//returns a new item if the split was sucessful and there are now 2 items, otherwise null
	public Item split( int amount ){
		if (amount <= 0 || amount >= quantity()) {
			return null;
		} else {
			try {
				
				//pssh, who needs copy constructors?
				Item split = clone();
				split.quantity(amount);
				quantity -= amount;
				
				return split;
			} catch (Exception e){
				LustrousPixelDungeon.reportException(e);
				return null;
			}
		}
	}
	
	public final Item detach( Bag container ) {
		
		if (quantity <= 0) {
			
			return null;
			
		} else
		if (quantity == 1) {

			if (stackable){
				Dungeon.quickslot.convertToPlaceholder(this);
			}

			return detachAll( container );
			
		} else {
			
			
			Item detached = split(1);
			updateQuickslot();
			if (detached != null) detached.onDetach( );
			return detached;
			
		}
	}
	
	public final Item detachAll( Bag container ) {
		Dungeon.quickslot.clearItem( this );
		updateQuickslot();

		for (Item item : container.items) {
			if (item == this) {
				container.items.remove(this);
				item.onDetach();
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag)item;
				if (bag.contains( this )) {
					return detachAll( bag );
				}
			}
		}
		
		return this;
	}
	
	public boolean isSimilar( Item item ) {
		return level == item.level && getClass() == item.getClass();
	}

	protected void onDetach(){}

	public int level(){
		return level;
	}

	public void level( int value ){
		level = value;

		updateQuickslot();
	}
	
	public Item upgrade() {
		
		this.level++;

		updateQuickslot();
		
		return this;
	}
	
	final public Item upgrade( int n ) {
		for (int i=0; i < n; i++) {
			upgrade();
		}
		
		return this;
	}
	
	public Item degrade() {
		
		this.level--;
		
		return this;
	}
	
	final public Item degrade( int n ) {
		for (int i=0; i < n; i++) {
			degrade();
		}
		
		return this;
	}
	
	public int visiblyUpgraded() {
		return levelKnown ? level() : 0;
	}
	
	public boolean visiblyCursed() {
		return cursed && cursedKnown;
	}
	
	public boolean isUpgradable() {
		return true;
	}

	public boolean isEnchantable() { return false; }

	public boolean isDestroyable() {
		return !(unique || level() > 0);
	}
	public boolean isIdentified() {
		return levelKnown && cursedKnown;
	}
	
	public boolean isEquipped( Hero hero ) {
		return false;
	}
	
	public Item identify() {
		
		levelKnown = true;
		cursedKnown = true;
		
		if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
			Catalog.setSeen(getClass());
		}
		
		return this;
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		//do nothing by default
	}
	
	public static void evoke( Hero hero ) {
		hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
	}
	
	@Override
	public String toString() {

		String name = name();

		if (visiblyUpgraded() != 0)
			name = Messages.format( TXT_TO_STRING_LVL, name, visiblyUpgraded()  );

		if (quantity > 1)
			name = Messages.format( TXT_TO_STRING_X, name, quantity );

		return name;
	}

	protected boolean hasCustomName() {
		return !( name == null || name.equals("") );
	}

	public int nameColor() {
		if( hasCustomName() )
			return 0x3399FF;
		if(levelKnown && level() != 0)
			return level() > 0 ? ItemSlot.UPGRADED : ItemSlot.DEGRADED;
		return TITLE_COLOR;
	}
	
	public String name() {
		return hasCustomName() ? name : trueName;
	}

	public void rename(String name) {
		if(!trueName.equals(name))
			this.name = name;
	}
	
	public final String trueName() {
		return trueName;
	}
	
	public int image() {
		return image;
	}
	
	public ItemSprite.Glowing glowing() {
		return null;
	}

	public Emitter emitter() { return null; }
	
	public String info() {
		return desc();
	}
	
	public String desc() {
		return Messages.get(this, "desc");
	}
	
	public int quantity() {
		return quantity;
	}
	
	public Item quantity( int value ) {
		quantity = value;
		return this;
	}

	protected float value;
	public int price() {
		return Math.max(Math.round(value*quantity),1);
	}
	
	public Item virtual(){
		try {
			Item item = getClass().newInstance();
			item.quantity = 0;
			item.level = level;
			return item;
			
		} catch (Exception e) {
			LustrousPixelDungeon.reportException(e);
			return null;
		}
	}
	
	public Item random() {
		if(isUpgradable() && Dungeon.depth > 22) level(level()+1); // free upgrade for demon halls
		return this;
	}
	
	public String status() {
		return quantity != 1 ? Integer.toString( quantity ) : null;
	}
	
	public static void updateQuickslot() {
			QuickSlotButton.refresh();
	}
	
	private static final String QUANTITY		= "quantity";
	private static final String LEVEL			= "level";
	private static final String LEVEL_KNOWN		= "levelKnown";
	private static final String CURSED			= "cursed";
	private static final String CURSED_KNOWN	= "cursedKnown";
	private static final String QUICKSLOT		= "quickslotpos";
	private static final String NAME			= "name";

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( NAME, name);
		bundle.put( QUANTITY, quantity );
		bundle.put( LEVEL, level );
		bundle.put( LEVEL_KNOWN, levelKnown );
		bundle.put( CURSED, cursed );
		bundle.put( CURSED_KNOWN, cursedKnown );
		if (Dungeon.quickslot.contains(this)) {
			bundle.put( QUICKSLOT, Dungeon.quickslot.getSlot(this) );
		}
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		quantity	= bundle.getInt( QUANTITY );
		levelKnown	= bundle.getBoolean( LEVEL_KNOWN );
		cursedKnown	= bundle.getBoolean( CURSED_KNOWN );
		if(bundle.contains(NAME)) name = bundle.getString(NAME);

		int level = bundle.getInt( LEVEL );
		if (level > 0) {
			upgrade( level );
		} else if (level < 0) {
			degrade( -level );
		}
		
		cursed	= bundle.getBoolean( CURSED );

		//only want to populate slot on first load.
		if (Dungeon.hero == null) {
			if (bundle.contains(QUICKSLOT)) {
				Dungeon.quickslot.setSlot(bundle.getInt(QUICKSLOT), this);
			}
		}
	}

	//FIXME: this is currently very expensive, should either optimize Ballistica or this, or both
	public static int throwPos( int src, int dst, boolean assist){
		int collisionPos = new Ballistica( src, dst, Ballistica.PROJECTILE ).collisionPos; //first try to directly target
		if(collisionPos != dst && assist) for (int i = 0; i < PathFinder.distance.length; i++) 	//Otherwise pick nearby tiles to try and 'angle' the shot, auto-aim basically.
			if (PathFinder.distance[i] < Integer.MAX_VALUE
					&& throwPos(src, i, false) == dst)
				return dst;
		return collisionPos;
	}

	public int throwPos( Hero hero, int dst, boolean assist) {
	    return throwPos(hero.pos,dst,assist);
    }
	public int throwPos(Hero user, int dst) {
		return throwPos(user,dst,true);
	}
	
	public void cast( final Hero user, final int dst ) {
		
		final int cell = throwPos( user, dst );
		user.sprite.zap( cell );
		user.busy();

		Sample.INSTANCE.play( Assets.SND_MISS, 0.6f, 0.6f, 1.5f );

		Char enemy = Actor.findChar( cell );
		QuickSlotButton.target(enemy);

		Callback onCastComplete = new Callback() {
			@Override
			public void call() {
				curUser = user;
				Item thrown = detach(curUser.belongings.backpack);
				thrown.castDelay = castDelay(curUser, dst);
				thrown.onThrow( cell );
			}
		};

		MissileSprite projectile = (MissileSprite) user.sprite.parent.recycle(MissileSprite.class);

		if (enemy != null) {
			projectile.reset(user.sprite, enemy.pos, this, onCastComplete);
		} else {
			projectile.reset(user.sprite, cell, this, onCastComplete);
		}
	}

	protected float castDelay;
	public float castDelay( Char user, int dst ){
		return TIME_TO_THROW;
	}
	
	protected static Hero curUser = null;
	protected static Item curItem = null;
	protected static CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				curItem.cast( curUser, target );
			}
		}
		@Override
		public String prompt() {
			return Messages.get(Item.class, "prompt");
		}
	};
}
