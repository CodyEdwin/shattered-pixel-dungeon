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
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingListPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.utils.Reflection;

public class WndSandboxItem extends Window {

	private static int lastLevel = 0;
	private static int lastQty = 1;
	private static boolean lastIdentified = true;
	private static boolean lastCursed = false;
	private static Class<? extends Weapon.Enchantment> lastEnchant;
	private static Class<? extends Armor.Glyph> lastGlyph;

	private final Class<? extends Item> cls;
	private int maxLevel;
	private int maxQty;

	private RenderedTextBlock levelLabel;
	private RenderedTextBlock qtyLabel;
	private RedButton enchantBtn;
	private RedButton glyphBtn;
	private CheckBox cursedBox;

	public WndSandboxItem(Class<? extends Item> cls) {
		this.cls = cls;

		Item sample;
		try {
			sample = Reflection.newInstance(cls);
		} catch (Exception e) {
			sample = null;
		}
		if (sample == null) {
			resize(Sandbox.windowWidth(), 20);
			return;
		}

		maxLevel = sample instanceof Artifact ? 10 : 15;
		maxQty = sample.stackable ? 99 : 20;
		lastLevel = Math.max(0, Math.min(maxLevel, lastLevel));
		lastQty = Math.max(1, Math.min(maxQty, lastQty));

		int w = Sandbox.windowWidth();
		int maxH = Sandbox.windowHeight();
		boolean showLevel = sample instanceof Artifact || sample.isUpgradable();
		boolean showCurse = sample instanceof Weapon || sample instanceof Armor
				|| sample instanceof Wand || sample instanceof Ring || sample instanceof Artifact;
		boolean showEnchant = sample instanceof Weapon;
		boolean showGlyph = sample instanceof Armor;

		int blocks = 2 + (showLevel ? 1 : 0) + (showCurse ? 1 : 0) + (showEnchant ? 1 : 0) + (showGlyph ? 1 : 0);
		int row = maxH < 160 ? 15 : 16;
		if (18 + blocks * (row + 1) + 18 > maxH) row = 14;

		Image icon;
		try {
			icon = new ItemSprite(sample);
		} catch (Exception e) {
			icon = Icons.get(Icons.CATALOG);
		}
		IconTitle title = new IconTitle(icon, Messages.titleCase(sample.trueName()));
		title.setRect(0, 0, w, 0);
		add(title);

		float pos = title.bottom() + 2;
		if (showLevel) {
			levelLabel = PixelLabel();
			levelLabel.text(Messages.get(this, "level", lastLevel));
			pos = stepper(pos, w, row, new int[]{-5, -1, 1, 5}, true);
		}
		qtyLabel = PixelLabel();
		qtyLabel.text(Messages.get(this, "quantity", lastQty));
		pos = stepper(pos, w, row, new int[]{-10, -1, 1, 10}, false);

		CheckBox identified = new CheckBox(Messages.get(this, "identified")) {
			@Override
			protected void onClick() {
				super.onClick();
				lastIdentified = checked();
			}
		};
		identified.checked(lastIdentified);
		identified.setRect(0, pos, w, row);
		add(identified);
		pos = identified.bottom() + 1;

		if (showCurse) {
			cursedBox = new CheckBox(Messages.get(this, "cursed")) {
				@Override
				protected void onClick() {
					super.onClick();
					lastCursed = checked();
				}
			};
			cursedBox.checked(lastCursed);
			cursedBox.setRect(0, pos, w, row);
			add(cursedBox);
			pos = cursedBox.bottom() + 1;
		}

		if (showEnchant) {
			enchantBtn = new RedButton(Messages.get(this, "enchant"), 6) {
				@Override
				protected void onClick() {
					GameScene.show(new Picker(false));
				}
			};
			enchantBtn.setRect(0, pos, w, row);
			add(enchantBtn);
			pos = enchantBtn.bottom() + 1;
			refreshEnchant();
		}

		if (showGlyph) {
			glyphBtn = new RedButton(Messages.get(this, "glyph"), 6) {
				@Override
				protected void onClick() {
					GameScene.show(new Picker(true));
				}
			};
			glyphBtn.setRect(0, pos, w, row);
			add(glyphBtn);
			pos = glyphBtn.bottom() + 1;
			refreshGlyph();
		}

		RedButton spawn = new RedButton(Messages.get(this, "spawn")) {
			@Override
			protected void onClick() {
				Sandbox.give(WndSandboxItem.this.cls, lastLevel, lastQty, lastIdentified, lastCursed,
						showEnchant ? lastEnchant : null,
						showGlyph ? lastGlyph : null);
			}
		};
		spawn.setRect(0, pos, w, Math.max(row, 18));
		add(spawn);
		refreshLabels();
		resize(w, (int) spawn.bottom());
	}

	private RenderedTextBlock PixelLabel() {
		RenderedTextBlock label = com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.renderTextBlock(6);
		add(label);
		return label;
	}

	private float stepper(float y, int w, int row, int[] deltas, boolean level) {
		float btnW = Math.min(26, Math.max(16, (w - 42) / (float) deltas.length));
		float labelW = w - deltas.length * btnW;
		int negatives = 0;
		for (int delta : deltas) if (delta < 0) negatives++;
		float x = 0;
		for (int i = 0; i < deltas.length; i++) {
			if (i == negatives) {
				RenderedTextBlock label = level ? levelLabel : qtyLabel;
				label.maxWidth((int) labelW);
				label.setPos(x + 1, y + (row - label.height()) / 2f);
				x += labelW;
			}
			final int delta = deltas[i];
			RedButton btn = new RedButton(stepText(delta), 6) {
				@Override
				protected void onClick() {
					if (level) {
						lastLevel = Math.max(0, Math.min(maxLevel, lastLevel + delta));
					} else {
						lastQty = Math.max(1, Math.min(maxQty, lastQty + delta));
					}
					refreshLabels();
				}
			};
			btn.setRect(x, y, btnW, row);
			add(btn);
			x += btnW;
		}
		return y + row + 1;
	}

	private void refreshLabels() {
		if (levelLabel != null) levelLabel.text(Messages.get(this, "level", lastLevel));
		if (qtyLabel != null) qtyLabel.text(Messages.get(this, "quantity", lastQty));
	}

	private void refreshEnchant() {
		if (enchantBtn == null) return;
		if (lastEnchant == null) {
			enchantBtn.text(Messages.get(this, "enchant"));
		} else {
			try {
				enchantBtn.text(Reflection.newInstance(lastEnchant).name());
			} catch (Exception e) {
				lastEnchant = null;
				enchantBtn.text(Messages.get(this, "enchant"));
			}
		}
	}

	private void refreshGlyph() {
		if (glyphBtn == null) return;
		if (lastGlyph == null) {
			glyphBtn.text(Messages.get(this, "glyph"));
		} else {
			try {
				glyphBtn.text(Reflection.newInstance(lastGlyph).name());
			} catch (Exception e) {
				lastGlyph = null;
				glyphBtn.text(Messages.get(this, "glyph"));
			}
		}
	}

	private static String stepText(int delta) {
		int abs = Math.abs(delta);
		String n = abs >= 1000 ? (abs / 1000) + "k" : Integer.toString(abs);
		return (delta > 0 ? "+" : "-") + n;
	}

	private class Picker extends Window {

		private Picker(boolean glyph) {
			int w = Sandbox.windowWidth();
			int h = Sandbox.windowHeight();

			IconTitle title = new IconTitle(
					Icons.get(Icons.CATALOG),
					Messages.get(WndSandboxItem.this, glyph ? "glyph" : "enchant"));
			title.setRect(0, 0, w, 0);
			add(title);

			ScrollingListPane list = new ScrollingListPane();
			add(list);
			list.setRect(0, title.bottom() + 2, w, h - title.bottom() - 2);

			list.addItem(choice(null, Messages.get(WndSandboxItem.this, "none"), glyph));
			if (glyph) {
				addGroup(list, "common", Armor.Glyph.common, true);
				addGroup(list, "uncommon", Armor.Glyph.uncommon, true);
				addGroup(list, "rare", Armor.Glyph.rare, true);
				addGroup(list, "curses", Armor.Glyph.curses, true);
			} else {
				addGroup(list, "common", Weapon.Enchantment.common, false);
				addGroup(list, "uncommon", Weapon.Enchantment.uncommon, false);
				addGroup(list, "rare", Weapon.Enchantment.rare, false);
				addGroup(list, "curses", Weapon.Enchantment.curses, false);
			}
			resize(w, h);
		}

		private void addGroup(ScrollingListPane list, String key, Class<?>[] classes, boolean glyph) {
			list.addTitle(Messages.get(WndSandboxItem.this, key));
			for (Class<?> cls : classes) {
				String name;
				try {
					Object inst = Reflection.newInstance((Class) cls);
					if (inst instanceof Armor.Glyph) name = ((Armor.Glyph) inst).name();
					else if (inst instanceof Weapon.Enchantment) name = ((Weapon.Enchantment) inst).name();
					else continue;
				} catch (Exception e) {
					continue;
				}
				list.addItem(choice(cls, name, glyph));
			}
		}

		private ScrollingListPane.ListItem choice(Class<?> cls, String label, boolean glyph) {
			return new ScrollingListPane.ListItem(Icons.get(Icons.STAIRS), label) {
				@Override
				public boolean onClick(float x, float y) {
					if (!inside(x, y)) return false;
					if (glyph) {
						lastGlyph = cls == null ? null : cls.asSubclass(Armor.Glyph.class);
						if (lastGlyph != null) {
							try {
								if (Reflection.newInstance(lastGlyph).curse()) lastCursed = true;
							} catch (Exception ignored) {
							}
						}
						if (cursedBox != null) cursedBox.checked(lastCursed);
						refreshGlyph();
					} else {
						lastEnchant = cls == null ? null : cls.asSubclass(Weapon.Enchantment.class);
						if (lastEnchant != null) {
							try {
								if (Reflection.newInstance(lastEnchant).curse()) lastCursed = true;
							} catch (Exception ignored) {
							}
						}
						if (cursedBox != null) cursedBox.checked(lastCursed);
						refreshEnchant();
					}
					hide();
					return true;
				}
			};
		}
	}
}
