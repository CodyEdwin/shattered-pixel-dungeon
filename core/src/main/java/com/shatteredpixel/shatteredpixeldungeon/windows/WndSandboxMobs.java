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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingListPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.utils.Reflection;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class WndSandboxMobs extends Window {

	private static final Bestiary[] CATS = new Bestiary[]{
			Bestiary.REGIONAL, Bestiary.BOSSES, Bestiary.UNIVERSAL, Bestiary.RARE,
			Bestiary.QUEST, Bestiary.NEUTRAL, Bestiary.ALLY
	};

	private static final String[] KEYS = new String[]{
			"cat_regional", "cat_bosses", "cat_universal", "cat_rare",
			"cat_quest", "cat_neutral", "cat_ally"
	};

	private static int lastCat = 0;

	private final ScrollingListPane list;
	private final float[] jumps = new float[CATS.length];
	private final ArrayList<RedButton> tabs = new ArrayList<>();

	public WndSandboxMobs() {
		int w = Sandbox.windowWidth();
		int h = Sandbox.windowHeight();

		IconTitle title = new IconTitle(Icons.get(Icons.SKULL), Messages.get(WndSandbox.class, "mobs"));
		title.setRect(0, 0, w, 0);
		add(title);

		int cols = w >= 140 ? 4 : 3;
		int btnW = (w - (cols - 1) * 2) / cols;
		int btnH = h < 150 ? 14 : 16;
		float top = title.bottom() + 2;
		for (int i = 0; i < CATS.length; i++) {
			final int index = i;
			RedButton tab = new RedButton(Messages.get(this, KEYS[i]), 6) {
				@Override
				protected void onClick() {
					select(index);
				}
			};
			int col = i % cols;
			int row = i / cols;
			tab.setRect(col * (btnW + 2), top + row * (btnH + 2), btnW, btnH);
			add(tab);
			tabs.add(tab);
		}
		int tabRows = (CATS.length + cols - 1) / cols;
		top += tabRows * (btnH + 2);

		list = new ScrollingListPane();
		add(list);
		list.setRect(0, top, w, Math.max(40, h - top));
		populate();
		select(lastCat);
		resize(w, h);
	}

	private void populate() {
		int row = 0;
		int rowH = ScrollingListPane.itemHeight();
		for (int c = 0; c < CATS.length; c++) {
			jumps[c] = row * rowH;
			list.addTitle(Messages.titleCase(CATS[c].title()));
			row++;
			for (Class<?> cls : CATS[c].entities()) {
				if (!spawnable(cls)) continue;
				final Class<? extends Mob> mobCls = cls.asSubclass(Mob.class);
				String name = Messages.get(mobCls, "name");
				Image icon = Icons.get(Icons.SKULL);
				try {
					Mob mob = Reflection.newInstance(mobCls);
					name = mob.name();
					icon = iconFor(mob);
				} catch (Throwable ignored) {
				}
				if (name == null || name.equals(Messages.NO_TEXT_FOUND)) {
					name = mobCls.getSimpleName();
				}
				list.addItem(new ScrollingListPane.ListItem(icon, Messages.titleCase(name)) {
					@Override
					public boolean onClick(float x, float y) {
						if (!inside(x, y)) return false;
						Sandbox.spawnMob(mobCls);
						return true;
					}
				});
				row++;
			}
		}
	}

	private void select(int index) {
		if (index < 0 || index >= CATS.length) return;
		lastCat = index;
		for (int i = 0; i < tabs.size(); i++) {
			tabs.get(i).textColor(i == index ? 0xFF88FF : Window.WHITE);
		}
		list.scrollTo(0, jumps[index]);
	}

	private static boolean spawnable(Class<?> cls) {
		if (cls == null || !Mob.class.isAssignableFrom(cls)) return false;
		int mods = cls.getModifiers();
		if (Modifier.isAbstract(mods)) return false;
		return cls.getEnclosingClass() == null || Modifier.isStatic(mods);
	}

	private static Image iconFor(Mob mob) {
		if (mob.spriteClass == null) return Icons.get(Icons.SKULL);
		CharSprite sprite = null;
		try {
			sprite = Reflection.newInstance(mob.spriteClass);
			if (sprite.width() > 0 && sprite.width() <= 16 && sprite.height() <= 16) {
				Image icon = new Image();
				icon.copy(sprite);
				return icon;
			}
		} catch (Exception ignored) {
		} finally {
			if (sprite != null) {
				try {
					sprite.destroy();
				} catch (Exception ignored) {
				}
			}
		}
		return Icons.get(Icons.SKULL);
	}
}
