/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Sandbox;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

public class WndSandbox extends Window {

	public WndSandbox() {
		int w = Sandbox.windowWidth();

		Image skull = Icons.get(Icons.SKULL);
		skull.hardlight(1f, 0.45f, 0.55f);
		IconTitle title = new IconTitle(skull, Messages.get(this, "title"));
		title.setRect(0, 0, w, 0);
		add(title);

		RedButton[] buttons = new RedButton[]{
				button("items", WndSandboxItems::new),
				button("mobs", WndSandboxMobs::new),
				button("hero", WndSandboxHero::new),
				button("talents", WndSandboxTalents::new),
				button("travel", WndSandboxTravel::new),
				button("toggles", Toggles::new),
				button("actions", Actions::new)
		};

		int maxH = Sandbox.windowHeight();
		int gap = 2;
		int n = buttons.length;
		float top = title.bottom() + gap;
		int room = maxH - (int) top - (n - 1) * gap;
		int btnH = Math.max(14, Math.min(20, room / n));
		float pos = top;
		for (RedButton btn : buttons) {
			btn.setRect(0, pos, w, btnH);
			add(btn);
			pos = btn.bottom() + gap;
		}
		resize(w, (int) (pos - gap));
	}

	private RedButton button(String key, WindowMaker maker) {
		return new RedButton(Messages.get(this, key)) {
			@Override
			protected void onClick() {
				hide();
				GameScene.show(maker.make());
			}
		};
	}

	private interface WindowMaker {
		Window make();
	}

	public static class Toggles extends Window {

		public Toggles() {
			int w = Sandbox.windowWidth();
			IconTitle title = new IconTitle(Icons.get(Icons.PREFS), Messages.get(WndSandbox.class, "toggles"));
			title.setRect(0, 0, w, 0);
			add(title);

			float pos = title.bottom() + 2;
			int row = Sandbox.windowHeight() < 150 ? 15 : 16;
			pos = toggle(pos, w, row, "god", Sandbox.godMode, value -> Sandbox.godMode = value);
			pos = toggle(pos, w, row, "one_hit", Sandbox.oneHit, value -> Sandbox.oneHit = value);
			pos = toggle(pos, w, row, "no_hunger", Sandbox.noHunger, value -> Sandbox.noHunger = value);
			pos = toggle(pos, w, row, "charges", Sandbox.infiniteCharges, value -> Sandbox.infiniteCharges = value);
			pos = toggle(pos, w, row, "full_map", Sandbox.fullMap, value -> Sandbox.fullMap = value);
			pos = toggle(pos, w, row, "identify", Sandbox.identifyAll, value -> Sandbox.identifyAll = value);
			resize(w, (int) pos);
		}

		private float toggle(float pos, int w, int row, String key, boolean start, Flag flag) {
			CheckBox box = new CheckBox(Messages.get(this, key)) {
				@Override
				protected void onClick() {
					super.onClick();
					flag.set(checked());
					Sandbox.applyToggles();
				}
			};
			box.checked(start);
			box.setRect(0, pos, w, row);
			add(box);
			return box.bottom() + 2;
		}

		private interface Flag {
			void set(boolean value);
		}
	}

	public static class Actions extends Window {

		public Actions() {
			int w = Sandbox.windowWidth();
			IconTitle title = new IconTitle(Icons.get(Icons.ENTER), Messages.get(WndSandbox.class, "actions"));
			title.setRect(0, 0, w, 0);
			add(title);

			RedButton[] buttons = new RedButton[]{
					action("heal", Sandbox::fullHeal),
					action("fill", Sandbox::fillHunger),
					action("identify", Sandbox::identifyEverything),
					action("recharge", Sandbox::refillCharges),
					action("kill", Sandbox::killEnemies),
					action("gold", () -> Sandbox.giveGold(1000)),
					action("energy", () -> Sandbox.giveEnergy(50))
			};

			int maxH = Sandbox.windowHeight();
			int gap = 2;
			float top = title.bottom() + gap;
			int room = maxH - (int) top - (buttons.length - 1) * gap;
			int btnH = Math.max(14, Math.min(18, room / buttons.length));
			float pos = top;
			for (RedButton btn : buttons) {
				btn.setRect(0, pos, w, btnH);
				add(btn);
				pos = btn.bottom() + gap;
			}
			resize(w, (int) (pos - gap));
		}

		private RedButton action(String key, Runnable run) {
			return new RedButton(Messages.get(this, key)) {
				@Override
				protected void onClick() {
					run.run();
				}
			};
		}
	}
}
