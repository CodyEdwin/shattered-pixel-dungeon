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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingListPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndSandboxTalents extends Window {

	private final RenderedTextBlock[] tierLabels = new RenderedTextBlock[4];
	private RedButton subclassBtn;
	private RedButton abilityBtn;

	public WndSandboxTalents() {
		int w = Sandbox.windowWidth();
		int maxH = Sandbox.windowHeight();

		IconTitle title = new IconTitle(Icons.get(Icons.TALENT), Messages.get(WndSandbox.class, "talents"));
		title.setRect(0, 0, w, 0);
		add(title);

		RenderedTextBlock hint = PixelScene.renderTextBlock(Messages.get(this, "hint"), 6);
		hint.maxWidth(w);
		hint.setPos(0, title.bottom() + 2);
		add(hint);

		int rows = 6;
		int gap = 1;
		int room = maxH - (int) hint.bottom() - 4 - (rows - 1) * gap;
		int rowH = Math.max(14, Math.min(16, room / rows));
		float pos = hint.bottom() + 2;

		for (int tier = 1; tier <= 4; tier++) {
			final int t = tier;
			tierLabels[tier - 1] = PixelScene.renderTextBlock(6);
			add(tierLabels[tier - 1]);
			pos = stepper(pos, w, rowH, tierLabels[tier - 1], delta -> Sandbox.addTalentPoints(t, delta));
		}

		subclassBtn = new RedButton("", 6) {
			@Override
			protected void onClick() {
				Hero hero = Dungeon.hero;
				HeroSubClass[] subs = hero.heroClass.subClasses();
				String[] labels = new String[subs.length + 1];
				labels[0] = Messages.get(WndSandboxTalents.this, "none");
				for (int i = 0; i < subs.length; i++) {
					labels[i + 1] = Messages.titleCase(subs[i].title());
				}
				GameScene.show(new ChoiceWindow(Messages.get(WndSandboxTalents.this, "subclass"), labels, index -> {
					setSubclass(index == 0 ? HeroSubClass.NONE : subs[index - 1]);
					refresh();
				}));
			}
		};
		subclassBtn.setRect(0, pos, w, rowH);
		add(subclassBtn);
		pos = subclassBtn.bottom() + gap;

		abilityBtn = new RedButton("", 6) {
			@Override
			protected void onClick() {
				Hero hero = Dungeon.hero;
				ArmorAbility[] abilities = hero.heroClass.armorAbilities();
				String[] labels = new String[abilities.length + 1];
				labels[0] = Messages.get(WndSandboxTalents.this, "none");
				for (int i = 0; i < abilities.length; i++) {
					labels[i + 1] = Messages.titleCase(abilities[i].name());
				}
				GameScene.show(new ChoiceWindow(Messages.get(WndSandboxTalents.this, "ability"), labels, index -> {
					setAbility(index == 0 ? null : abilities[index]);
					refresh();
				}));
			}
		};
		abilityBtn.setRect(0, pos, w, rowH);
		add(abilityBtn);

		refresh();
		resize(w, (int) abilityBtn.bottom());
	}

	private void refresh() {
		Hero hero = Dungeon.hero;
		for (int tier = 1; tier <= 4; tier++) {
			boolean locked = (tier == 3 && hero.subClass == HeroSubClass.NONE)
					|| (tier == 4 && hero.armorAbility == null);
			int shown = locked ? Sandbox.bonusTalents(tier) : hero.talentPointsAvailable(tier);
			tierLabels[tier - 1].text(Messages.get(this, locked ? "tier_locked" : "tier", tier, shown));
		}
		String sub = hero.subClass == null || hero.subClass == HeroSubClass.NONE
				? Messages.get(this, "none")
				: Messages.titleCase(hero.subClass.title());
		subclassBtn.text(Messages.get(this, "subclass") + ": " + sub);
		String ability = hero.armorAbility == null
				? Messages.get(this, "none")
				: Messages.titleCase(hero.armorAbility.name());
		abilityBtn.text(Messages.get(this, "ability") + ": " + ability);
	}

	private float stepper(float y, int w, int row, RenderedTextBlock label, IntOp apply) {
		float btnW = 22;
		label.maxWidth((int) (w - btnW * 2));
		label.setPos(btnW + 2, y + Math.max(0, (row - label.height()) / 2f));
		RedButton minus = new RedButton("-", 8) {
			@Override
			protected void onClick() {
				apply.apply(-1);
				refresh();
			}
		};
		RedButton plus = new RedButton("+", 8) {
			@Override
			protected void onClick() {
				apply.apply(1);
				refresh();
			}
		};
		minus.setRect(0, y, btnW, row);
		plus.setRect(w - btnW, y, btnW, row);
		add(minus);
		add(plus);
		return y + row + 1;
	}

	private static void ensureTiers(Hero hero) {
		while (hero.talents.size() < Talent.MAX_TALENT_TIERS) {
			hero.talents.add(new LinkedHashMap<>());
		}
	}

	static void setSubclass(HeroSubClass sub) {
		Hero hero = Dungeon.hero;
		ensureTiers(hero);
		ArrayList<LinkedHashMap<Talent, Integer>> fresh = new ArrayList<>();
		Talent.initClassTalents(hero.heroClass, fresh, hero.metamorphedTalents);
		hero.talents.get(2).clear();
		if (fresh.size() > 2) hero.talents.get(2).putAll(fresh.get(2));
		hero.subClass = sub == null ? HeroSubClass.NONE : sub;
		if (hero.subClass != HeroSubClass.NONE) {
			Talent.initSubclassTalents(hero);
		}
	}

	static void setAbility(ArmorAbility ability) {
		Hero hero = Dungeon.hero;
		ensureTiers(hero);
		hero.talents.get(3).clear();
		hero.armorAbility = ability;
		if (ability == null) return;
		Talent.initArmorTalents(hero);
		if (!(hero.belongings.armor instanceof ClassArmor)) {
			Armor base = hero.belongings.armor != null ? hero.belongings.armor : new ClothArmor();
			if (hero.belongings.armor == null) base.identify();
			ClassArmor classArmor = ClassArmor.upgrade(hero, base);
			classArmor.identify();
			hero.belongings.armor = classArmor;
			classArmor.activate(hero);
			if (hero.sprite instanceof HeroSprite) {
				((HeroSprite) hero.sprite).updateArmor();
			}
		}
	}

	private class ChoiceWindow extends Window {

		private ChoiceWindow(String titleText, String[] labels, IntOp pick) {
			int w = Sandbox.windowWidth();
			int rowH = ScrollingListPane.itemHeight();
			int h = Math.min(Sandbox.windowHeight(), 20 + labels.length * rowH);

			IconTitle title = new IconTitle(Icons.get(Icons.TALENT), titleText);
			title.setRect(0, 0, w, 0);
			add(title);

			ScrollingListPane list = new ScrollingListPane();
			add(list);
			list.setRect(0, title.bottom() + 2, w, h - title.bottom() - 2);
			for (int i = 0; i < labels.length; i++) {
				final int index = i;
				list.addItem(new ScrollingListPane.ListItem(Icons.get(Icons.TALENT), labels[i]) {
					@Override
					public boolean onClick(float x, float y) {
						if (!inside(x, y)) return false;
						pick.apply(index);
						hide();
						return true;
					}
				});
			}
			resize(w, h);
		}
	}

	private interface IntOp {
		void apply(int delta);
	}
}
