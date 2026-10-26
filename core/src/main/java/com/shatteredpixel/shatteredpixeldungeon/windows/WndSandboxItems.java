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
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingListPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.lang.reflect.Modifier;

public class WndSandboxItems extends Window {

	private static final Catalog[] CATS = new Catalog[]{
			Catalog.MELEE_WEAPONS, Catalog.ARMOR, Catalog.THROWN_WEAPONS, Catalog.WANDS,
			Catalog.RINGS, Catalog.ARTIFACTS, Catalog.TRINKETS, Catalog.MISC_EQUIPMENT,
			Catalog.POTIONS, Catalog.EXOTIC_POTIONS, Catalog.SCROLLS, Catalog.EXOTIC_SCROLLS,
			Catalog.SEEDS, Catalog.STONES, Catalog.FOOD, Catalog.BOMBS, Catalog.TIPPED_DARTS,
			Catalog.BREWS_ELIXIRS, Catalog.SPELLS, Catalog.MISC_CONSUMABLES
	};

	private static int lastCat = 0;

	private final ScrollingListPane list;
	private final float[] jumps;
	private final ArrayList<CatButton> tabs = new ArrayList<>();
	private RenderedTextBlock catTitle;

	public WndSandboxItems() {
		int w = Sandbox.windowWidth();
		int h = Sandbox.windowHeight();
		jumps = new float[CATS.length];

		IconTitle title = new IconTitle(Icons.get(Icons.BACKPACK), Messages.get(WndSandbox.class, "items"));
		title.setRect(0, 0, w, 0);
		add(title);

		int cols = Math.max(4, w / 18);
		int rows = (CATS.length + cols - 1) / cols;
		int btn = Math.min(18, w / cols);
		int gridH = rows * (btn + 1);
		boolean compact = gridH > (h - title.height() - 8) * 0.45f;

		float top = title.bottom() + 2;
		if (compact) {
			IconButton prev = new IconButton(Icons.get(Icons.LEFTARROW)) {
				@Override
				protected void onClick() {
					select(lastCat - 1);
				}
			};
			IconButton next = new IconButton(Icons.get(Icons.RIGHTARROW)) {
				@Override
				protected void onClick() {
					select(lastCat + 1);
				}
			};
			prev.setRect(0, top, 16, 16);
			next.setRect(w - 16, top, 16, 16);
			add(prev);
			add(next);
			catTitle = PixelScene.renderTextBlock(6);
			catTitle.hardlight(TITLE_COLOR);
			catTitle.maxWidth(w - 36);
			catTitle.setPos(18, top + 4);
			add(catTitle);
			top += 18;
		} else {
			for (int i = 0; i < CATS.length; i++) {
				int col = i % cols;
				int row = i / cols;
				CatButton tab = new CatButton(i, iconFor(CATS[i]));
				tab.setRect(col * (btn + 1), top + row * (btn + 1), btn, btn);
				add(tab);
				tabs.add(tab);
			}
			top += gridH + 1;
		}

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

			ArrayList<Item> items = new ArrayList<>();
			for (Class<?> cls : CATS[c].items()) {
				Item item = instantiate(cls);
				if (item != null) items.add(item);
			}
			Collections.sort(items, new Comparator<Item>() {
				@Override
				public int compare(Item a, Item b) {
					return a.trueName().compareToIgnoreCase(b.trueName());
				}
			});
			for (Item item : items) {
				final Class<? extends Item> cls = item.getClass();
				Image icon;
				try {
					icon = new ItemSprite(item);
				} catch (Exception e) {
					icon = Icons.get(Icons.CATALOG);
				}
				list.addItem(new ScrollingListPane.ListItem(icon, Messages.titleCase(item.trueName())) {
					@Override
					public boolean onClick(float x, float y) {
						if (!inside(x, y)) return false;
						GameScene.show(new WndSandboxItem(cls));
						return true;
					}
				});
				row++;
			}
		}
	}

	private void select(int index) {
		if (index < 0) index = CATS.length - 1;
		if (index >= CATS.length) index = 0;
		lastCat = index;
		for (int i = 0; i < tabs.size(); i++) {
			tabs.get(i).select(i == index);
		}
		if (catTitle != null) {
			catTitle.text(Messages.titleCase(CATS[index].title()));
			PixelScene.align(catTitle);
		}
		list.scrollTo(0, jumps[index]);
	}

	private static boolean spawnable(Class<?> cls) {
		if (cls == null || !Item.class.isAssignableFrom(cls)) return false;
		int mods = cls.getModifiers();
		if (Modifier.isAbstract(mods)) return false;
		return cls.getEnclosingClass() == null || Modifier.isStatic(mods);
	}

	private static Image iconFor(Catalog cat) {
		for (Class<?> cls : cat.items()) {
			Item item = instantiate(cls);
			if (item == null) continue;
			try {
				return new ItemSprite(item);
			} catch (Exception ignored) {
			}
		}
		return Icons.get(Icons.CATALOG);
	}

	@SuppressWarnings("unchecked")
	private static Item instantiate(Class<?> cls) {
		if (!spawnable(cls)) return null;
		try {
			Item item = Reflection.newInstance((Class<? extends Item>) cls);
			item.trueName();
			return item;
		} catch (Throwable e) {
			return null;
		}
	}

	private class CatButton extends IconButton {

		private final int index;
		private boolean selected;

		private CatButton(int index, Image icon) {
			super(icon);
			this.index = index;
		}

		@Override
		protected void onClick() {
			WndSandboxItems.this.select(index);
		}

		@Override
		protected void onPointerUp() {
			super.onPointerUp();
			tint();
		}

		@Override
		protected String hoverText() {
			return Messages.titleCase(CATS[index].title());
		}

		private void select(boolean value) {
			selected = value;
			tint();
		}

		private void tint() {
			if (icon == null) return;
			if (selected) icon.hardlight(1.5f, 0.55f, 1.7f);
			else icon.resetColor();
		}
	}
}
