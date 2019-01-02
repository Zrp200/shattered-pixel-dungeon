package com.zrp200.lustrouspixeldungeon.actors.buffs;

import com.watabou.utils.Bundle;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.ui.BuffIndicator;

public class ActiveBuff extends Buff {
    protected float left, initial;

    public void set( float duration ) {
        left = Math.max(left, duration);
        initial = Math.max(initial, left);
    }

    public void extend( float duration ) {
        left += duration;
        initial = Math.max(initial, left);
    }

    @Override
    public boolean act() {
        spend(TICK);
        left -= TICK;
        if (left <= 0){
            detach();
        } else if (left < 5){
            BuffIndicator.refreshHero();
        }
        return true;
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns(left));
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( "initial", initial );
        bundle.put( "left", left );

    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        initial = bundle.getFloat( "initial" );
        left = bundle.getFloat( "left" );
    }


}
