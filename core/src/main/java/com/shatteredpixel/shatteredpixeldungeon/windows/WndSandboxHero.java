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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Sandbox;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndSandboxHero extends Window {

	private RenderedTextBlock hpLabel;
	private RenderedTextBlock htLabel;
	private RenderedTextBlock strLabel;
	private RenderedTextBlock lvlLabel;
	private RenderedTextBlock expLabel;
	private RenderedTextBlock goldLabel;
	private RenderedTextBlock energyLabel;

	public WndSandboxHero() {
		int w = Sandbox.windowWidth();
		int maxH = Sandbox.windowHeight();

		IconTitle title = new IconTitle(Icons.get(Icons.TALENT), Messages.get(WndSandbox.class, "hero"));
		title.setRect(0, 0, w, 0);
		add(title);

		hpLabel = line();
		htLabel = line();
		strLabel = line();
		lvlLabel = line();
		expLabel = line();
		goldLabel = line();
		energyLabel = line();
		refresh();

		int rows = 8;
		int gap = 1;
		int room = maxH - (int) title.bottom() - 2 - (rows - 1) * gap;
		int rowH = Math.max(14, Math.min(18, room / rows));
		float pos = title.bottom() + 2;

		pos = stepper(pos, w, rowH, hpLabel, new int[]{-10, -1, 1, 10}, delta -> {
			Hero hero = Dungeon.hero;
			hero.setSandboxHP(hero.HP + delta, hero.HT);
		});
		pos = stepper(pos, w, rowH, htLabel, new int[]{-10, -1, 1, 10}, delta -> {
			Hero hero = Dungeon.hero;
			hero.setSandboxHP(hero.HP, hero.HT + delta);
		});
		pos = stepper(pos, w, rowH, strLabel, new int[]{-5, -1, 1, 5}, delta -> {
			Hero hero = Dungeon.hero;
			hero.setSandboxSTR(hero.STR + delta);
		});
		pos = stepper(pos, w, rowH, lvlLabel, new int[]{-5, -1, 1, 5}, delta -> {
			Hero hero = Dungeon.hero;
			hero.setSandboxLevel(hero.lvl + delta, hero.exp);
		});
		pos = stepper(pos, w, rowH, expLabel, new int[]{-50, -1, 1, 50}, delta -> {
			Hero hero = Dungeon.hero;
			hero.setSandboxLevel(hero.lvl, hero.exp + delta);
		});
		pos = stepper(pos, w, rowH, goldLabel, new int[]{-1000, -100, 100, 1000}, Sandbox::giveGold);
		pos = stepper(pos, w, rowH, energyLabel, new int[]{-100, -10, 10, 100}, Sandbox::giveEnergy);

		float btnW = (w - 2) / 2f;
		RedButton fill = new RedButton(Messages.get(this, "full")) {
			@Override
			protected void onClick() {
				Sandbox.fillHunger();
			}
		};
		RedButton starve = new RedButton(Messages.get(this, "starve")) {
			@Override
			protected void onClick() {
				Sandbox.starve();
			}
		};
		fill.setRect(0, pos, btnW, rowH);
		starve.setRect(btnW + 2, pos, w - btnW - 2, rowH);
		add(fill);
		add(starve);
		resize(w, (int) fill.bottom());
	}

	private RenderedTextBlock line() {
		RenderedTextBlock label = PixelScene.renderTextBlock(6);
		add(label);
		return label;
	}

	private void refresh() {
		Hero hero = Dungeon.hero;
		hpLabel.text(Messages.get(this, "hp", hero.HP));
		htLabel.text(Messages.get(this, "ht", hero.HT));
		strLabel.text(Messages.get(this, "str", hero.STR()));
		lvlLabel.text(Messages.get(this, "level", hero.lvl));
		expLabel.text(Messages.get(this, "exp", hero.exp, Hero.maxExp(hero.lvl)));
		goldLabel.text(Messages.get(this, "gold", Dungeon.gold));
		energyLabel.text(Messages.get(this, "energy", Dungeon.energy));
	}

	private float stepper(float y, int w, int row, RenderedTextBlock label, int[] deltas, IntOp apply) {
		float btnW = Math.min(26, Math.max(16, (w - 40) / (float) deltas.length));
		float labelW = w - deltas.length * btnW;
		int negatives = 0;
		for (int delta : deltas) if (delta < 0) negatives++;
		float x = 0;
		for (int i = 0; i < deltas.length; i++) {
			if (i == negatives) {
				label.maxWidth((int) labelW);
				label.setPos(x + 1, y + Math.max(0, (row - label.height()) / 2f));
				PixelScene.align(label);
				x += labelW;
			}
			final int delta = deltas[i];
			RedButton btn = new RedButton(stepText(delta), 6) {
				@Override
				protected void onClick() {
					apply.apply(delta);
					refresh();
				}
			};
			btn.setRect(x, y, btnW, row);
			add(btn);
			x += btnW;
		}
		return y + row + 1;
	}

	private static String stepText(int delta) {
		int abs = Math.abs(delta);
		String n = abs >= 1000 ? (abs / 1000) + "k" : Integer.toString(abs);
		return (delta > 0 ? "+" : "-") + n;
	}

	private interface IntOp {
		void apply(int delta);
	}
}
