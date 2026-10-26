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
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingListPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndSandboxTravel extends Window {

	public WndSandboxTravel() {
		int w = Sandbox.windowWidth();
		int h = Sandbox.windowHeight();

		IconTitle title = new IconTitle(Icons.get(Icons.STAIRS), Messages.get(WndSandbox.class, "travel"));
		title.setRect(0, 0, w, 0);
		add(title);

		ScrollingListPane list = new ScrollingListPane();
		add(list);
		list.setRect(0, title.bottom() + 2, w, h - title.bottom() - 2);

		section(list, "sewers", 1, 4, "sewers");
		floor(list, 5, 0, "goo");
		section(list, "prison", 6, 9, "prison");
		floor(list, 10, 0, "tengu");
		section(list, "caves", 11, 14, "caves");
		floor(list, 15, 0, "dm300");
		section(list, "metropolis", 16, 19, "metropolis");
		floor(list, 20, 0, "king");
		section(list, "halls", 21, 24, "halls");
		floor(list, 25, 0, "yog");
		floor(list, 26, 0, "amulet");
		list.addTitle(Messages.get(this, "special"));
		floor(list, 11, 1, "mining");
		floor(list, 16, 1, "vault");

		resize(w, h);
	}

	private void section(ScrollingListPane list, String titleKey, int from, int to, String nameKey) {
		list.addTitle(Messages.get(this, titleKey));
		for (int depth = from; depth <= to; depth++) {
			floor(list, depth, 0, nameKey);
		}
	}

	private void floor(ScrollingListPane list, int depth, int branch, String nameKey) {
		String label = Messages.get(this, "floor", depth, Messages.get(this, nameKey));
		list.addItem(new ScrollingListPane.ListItem(Icons.get(Icons.STAIRS), label) {
			@Override
			public boolean onClick(float x, float y) {
				if (!inside(x, y)) return false;
				Sandbox.travel(depth, branch);
				return true;
			}
		});
	}
}
