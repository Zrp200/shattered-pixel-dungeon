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

package com.zrp200.lustrouspixeldungeon.windows;

import com.watabou.noosa.Game;
import com.zrp200.lustrouspixeldungeon.Dungeon;
import com.zrp200.lustrouspixeldungeon.GamesInProgress;
import com.zrp200.lustrouspixeldungeon.LustrousPixelDungeon;
import com.zrp200.lustrouspixeldungeon.items.EquipableItem;
import com.zrp200.lustrouspixeldungeon.items.Item;
import com.zrp200.lustrouspixeldungeon.items.wands.Wand;
import com.zrp200.lustrouspixeldungeon.messages.Messages;
import com.zrp200.lustrouspixeldungeon.scenes.GameScene;
import com.zrp200.lustrouspixeldungeon.scenes.InterlevelScene;
import com.zrp200.lustrouspixeldungeon.scenes.RankingsScene;
import com.zrp200.lustrouspixeldungeon.scenes.TitleScene;
import com.zrp200.lustrouspixeldungeon.ui.RedButton;
import com.zrp200.lustrouspixeldungeon.ui.Window;

import java.io.IOException;

public class WndGame extends Window {

	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 20;
	private static final int GAP		= 2;
	
	private int pos;
	
	public WndGame() {

		addButton( new RedButton( Messages.get(this, "settings") ) {
			@Override
			protected void onClick() {
				hide();
				GameScene.show(new WndSettings());
			}
		});

		if (Dungeon.hero.isAlive()) {
			addButton(new RedButton("Rename Equipment") {
				protected void onClick() {
					WndGame.this.hide();
					GameScene.selectItem(new WndBag.Listener()  {
						public void onSelect(final Item item) {
							if (item instanceof EquipableItem || item instanceof Wand) {
								GameScene.show(new WndTextInput("Rename Item", item.name(),
                                        false, "Rename", "Revert") {
									@Override
									protected void onSelect(boolean z) {
									    if(z) item.rename(getText());
									}
								});
							}
						}
					}, WndBag.Mode.CURSABLE, "Select Item");
				}
			});
		}

		// Challenges window
		if (Dungeon.challenges > 0) {
			addButton( new RedButton( Messages.get(this, "challenges") ) {
				@Override
				protected void onClick() {
					hide();
					GameScene.show( new WndChallenges( Dungeon.challenges, false ) );
				}
			} );
		}

		// Restart
		if (!Dungeon.hero.isAlive()) {
			
			RedButton btnStart;
			addButton( btnStart = new RedButton( Messages.get(this, "start") ) {
				@Override
				protected void onClick() {
					GamesInProgress.selectedClass = Dungeon.hero.heroClass;
					InterlevelScene.noStory = true;
					GameScene.show(new WndStartGame(GamesInProgress.firstEmpty()));
				}
			} );
			btnStart.textColor(Window.TITLE_COLOR);
			
			addButton( new RedButton( Messages.get(this, "rankings") ) {
				@Override
				protected void onClick() {
					InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
					Game.switchScene( RankingsScene.class );
				}
			} );
		}

		addButtons(
				// Main menu
				new RedButton( Messages.get(this, "menu") ) {
					@Override
					protected void onClick() {
						try {
							Dungeon.saveAll();
						} catch (IOException e) {
							LustrousPixelDungeon.reportException(e);
						}
						Game.switchScene(TitleScene.class);
					}
				},
				// Quit
				new RedButton( Messages.get(this, "exit") ) {
					@Override
					protected void onClick() {
						try {
							Dungeon.saveAll();
						} catch (IOException e) {
							LustrousPixelDungeon.reportException(e);
						}
						Game.instance.finish();
					}
				}
		);

		// Cancel
		addButton( new RedButton( Messages.get(this, "return") ) {
			@Override
			protected void onClick() {
				hide();
			}
		} );
		
		resize( WIDTH, pos );
	}
	
	private void addButton( RedButton btn ) {
		add( btn );
		btn.setRect( 0, pos > 0 ? pos += GAP : 0, WIDTH, BTN_HEIGHT );
		pos += BTN_HEIGHT;
	}

	private void addButtons( RedButton btn1, RedButton btn2 ) {
		add( btn1 );
		btn1.setRect( 0, pos > 0 ? pos += GAP : 0, (WIDTH - GAP) / 2f, BTN_HEIGHT );
		add( btn2 );
		btn2.setRect( btn1.right() + GAP, btn1.top(), WIDTH - btn1.right() - GAP, BTN_HEIGHT );
		pos += BTN_HEIGHT;
	}
}
