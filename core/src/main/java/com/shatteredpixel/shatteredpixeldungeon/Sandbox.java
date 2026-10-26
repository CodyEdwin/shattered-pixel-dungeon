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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * Optional cheat mode for a single run. Normal games leave {@link #active} false
 * and never consult the toggles below.
 */
public class Sandbox {

	public static boolean active;

	public static boolean godMode;
	public static boolean oneHit;
	public static boolean noHunger;
	public static boolean infiniteCharges;
	public static boolean fullMap;
	public static boolean identifyAll;

	public static int[] bonusTalents = new int[4];

	private static int revealedDepth = Integer.MIN_VALUE;
	private static int revealedBranch = Integer.MIN_VALUE;

	private static final String ACTIVE = "sandbox";
	private static final String GOD = "sandbox_god";
	private static final String ONE_HIT = "sandbox_one_hit";
	private static final String NO_HUNGER = "sandbox_no_hunger";
	private static final String CHARGES = "sandbox_charges";
	private static final String FULL_MAP = "sandbox_full_map";
	private static final String IDENTIFY = "sandbox_identify";
	private static final String TALENTS = "sandbox_talents";

	public static void beginNewGame() {
		reset();
		active = !Dungeon.daily && SPDSettings.sandbox();
	}

	public static void reset() {
		active = false;
		godMode = false;
		oneHit = false;
		noHunger = false;
		infiniteCharges = false;
		fullMap = false;
		identifyAll = false;
		bonusTalents = new int[4];
		revealedDepth = Integer.MIN_VALUE;
		revealedBranch = Integer.MIN_VALUE;
	}

	public static void store( Bundle bundle ) {
		bundle.put( ACTIVE, active );
		bundle.put( GOD, godMode );
		bundle.put( ONE_HIT, oneHit );
		bundle.put( CHARGES, infiniteCharges );
		bundle.put( NO_HUNGER, noHunger );
		bundle.put( FULL_MAP, fullMap );
		bundle.put( IDENTIFY, identifyAll );
		bundle.put( TALENTS, bonusTalents );
	}

	public static void restore( Bundle bundle ) {
		reset();
		if (!bundle.contains( ACTIVE )) return;
		active = bundle.getBoolean( ACTIVE );
		if (!active) return;
		godMode = bundle.getBoolean( GOD );
		oneHit = bundle.getBoolean( ONE_HIT );
		noHunger = bundle.getBoolean( NO_HUNGER );
		infiniteCharges = bundle.getBoolean( CHARGES );
		fullMap = bundle.getBoolean( FULL_MAP );
		identifyAll = bundle.getBoolean( IDENTIFY );
		if (bundle.contains( TALENTS )) {
			int[] stored = bundle.getIntArray( TALENTS );
			if (stored != null && stored.length == bonusTalents.length) {
				bonusTalents = stored;
			}
		}
	}

	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		info.sandbox = bundle.contains( ACTIVE ) && bundle.getBoolean( ACTIVE );
	}

	public static boolean blocksProgress() {
		return active;
	}

	public static int bonusTalents( int tier ) {
		if (!active || tier < 1 || tier > bonusTalents.length) return 0;
		return bonusTalents[tier - 1];
	}

	public static void addTalentPoints( int tier, int delta ) {
		if (!active || Dungeon.hero == null || tier < 1 || tier > bonusTalents.length) return;
		if (delta < 0 && bonusTalents[tier - 1] <= 0) return;
		boolean locked = (tier == 3 && Dungeon.hero.subClass == HeroSubClass.NONE)
				|| (tier == 4 && Dungeon.hero.armorAbility == null);
		if (delta < 0 && !locked && Dungeon.hero.talentPointsAvailable( tier ) <= 0) return;
		int next = bonusTalents[tier - 1] + delta;
		if (next < 0) next = 0;
		if (next > 20) next = 20;
		bonusTalents[tier - 1] = next;
	}

	public static boolean godMode() {
		return active && godMode;
	}

	public static boolean fullMap() {
		return active && fullMap;
	}

	/** Hero melee, thrown attacks, and wand zaps. */
	public static boolean oneHitKills( Char target, Object src ) {
		if (!active || !oneHit || target == null || target == Dungeon.hero) return false;
		if (src == Dungeon.hero) return true;
		return src instanceof Wand && ((Wand) src).usedByHero();
	}

	public static void onHeroTurn() {
		if (!active || Dungeon.hero == null) return;
		if (infiniteCharges) refillCharges();
		if (identifyAll) identifyEverything();
		if (fullMap) revealIfNeeded();
	}

	public static void applyToggles() {
		if (!active || Dungeon.hero == null) return;
		if (noHunger) {
			Hunger hunger = Dungeon.hero.buff( Hunger.class );
			if (hunger != null) hunger.satisfy( Hunger.STARVING );
		}
		if (infiniteCharges) refillCharges();
		if (identifyAll) identifyEverything();
		if (fullMap) {
			revealedDepth = Integer.MIN_VALUE;
			revealIfNeeded();
			Dungeon.observe();
		}
	}

	private static void revealIfNeeded() {
		if (Dungeon.level == null) return;
		if (revealedDepth == Dungeon.depth && revealedBranch == Dungeon.branch) return;
		revealLevel();
		revealedDepth = Dungeon.depth;
		revealedBranch = Dungeon.branch;
	}

	public static void revealLevel() {
		Level level = Dungeon.level;
		if (level == null) return;
		int length = level.length();
		for (int i = 0; i < length; i++) {
			level.mapped[i] = true;
			level.visited[i] = true;
			if ((Terrain.flags[level.map[i]] & Terrain.SECRET) != 0) {
				level.discover( i );
			}
		}
		GameScene.updateFog();
	}

	public static void identifyEverything() {
		if (Dungeon.hero == null) return;
		for (Item item : Dungeon.hero.belongings) {
			if (!item.isIdentified()) item.identify( false );
		}
		if (Dungeon.level != null) {
			for (Heap heap : Dungeon.level.heaps.valueList()) {
				for (Item item : heap.items) {
					if (!item.isIdentified()) item.identify( false );
				}
			}
		}
		Item.updateQuickslot();
	}

	public static void refillCharges() {
		Hero hero = Dungeon.hero;
		if (hero == null) return;
		for (Item item : hero.belongings) {
			if (item instanceof Wand) {
				Wand wand = (Wand) item;
				wand.curCharges = wand.maxCharges;
				wand.partialCharge = 0;
			} else if (item instanceof MagesStaff) {
				((MagesStaff) item).refillCharges();
			} else if (item instanceof Artifact) {
				((Artifact) item).fullyCharge();
			} else if (item instanceof ClassArmor) {
				((ClassArmor) item).charge = 100;
			}
		}
		Item.updateQuickslot();
	}

	public static void fullHeal() {
		Hero hero = Dungeon.hero;
		if (hero == null) return;
		ArrayList<Buff> buffs = new ArrayList<>( hero.buffs() );
		for (Buff buff : buffs) {
			if (buff.type == Buff.buffType.NEGATIVE) {
				buff.detach();
			}
		}
		hero.HP = hero.HT;
		GLog.p( Messages.get( Sandbox.class, "healed" ) );
	}

	public static void fillHunger() {
		if (Dungeon.hero == null) return;
		Buff.affect( Dungeon.hero, Hunger.class ).satisfy( Hunger.STARVING );
	}

	public static void starve() {
		if (Dungeon.hero == null) return;
		Buff.affect( Dungeon.hero, Hunger.class ).setHunger( Hunger.STARVING );
	}

	public static void killEnemies() {
		if (Dungeon.level == null) return;
		int killed = 0;
		ArrayList<Mob> mobs = new ArrayList<>( Dungeon.level.mobs );
		for (Mob mob : mobs) {
			if (!mob.isAlive() || mob.alignment != Char.Alignment.ENEMY) continue;
			try {
				mob.die( Sandbox.class );
				killed++;
			} catch (Exception e) {
				ShatteredPixelDungeon.reportException( e );
			}
		}
		GLog.p( Messages.get( Sandbox.class, "killed", killed ) );
	}

	public static void giveGold( int amount ) {
		if (amount == 0) return;
		Dungeon.gold = Math.max( 0, Dungeon.gold + amount );
		if (amount > 0) Statistics.goldCollected += amount;
	}

	public static void giveEnergy( int amount ) {
		Dungeon.energy = Math.max( 0, Dungeon.energy + amount );
	}

	public static void give( Class<? extends Item> cls, int level, int quantity,
							 boolean identified, boolean cursed,
							 Class<? extends Weapon.Enchantment> enchantment,
							 Class<? extends Armor.Glyph> glyph ) {
		Hero hero = Dungeon.hero;
		if (hero == null || Dungeon.level == null) return;

		Item probe;
		try {
			probe = Reflection.newInstance( cls );
		} catch (Exception e) {
			GLog.w( Messages.get( Sandbox.class, "spawn_fail" ) );
			return;
		}

		quantity = Math.max( 1, Math.min( quantity, probe.stackable ? 99 : 20 ) );
		int copies = probe.stackable ? 1 : quantity;
		int each = probe.stackable ? quantity : 1;
		int stored = 0;
		int dropped = 0;

		for (int i = 0; i < copies; i++) {
			Item item = Reflection.newInstance( cls );
			if (item instanceof Artifact) {
				((Artifact) item).transferUpgrade( Math.max( 0, Math.min( 10, level ) ) );
			} else if (item.isUpgradable()) {
				item.level( Math.max( 0, Math.min( 15, level ) ) );
			}
			if (item.stackable) item.quantity( each );
			if (item instanceof Weapon && enchantment != null) {
				Weapon.Enchantment ench = Reflection.newInstance( enchantment );
				((Weapon) item).enchant( ench );
				if (ench.curse()) item.cursed = true;
			}
			if (item instanceof Armor && glyph != null) {
				Armor.Glyph applied = Reflection.newInstance( glyph );
				((Armor) item).inscribe( applied );
				if (applied.curse()) item.cursed = true;
			}
			if (cursed) item.cursed = true;
			if (identified) item.identify( false );

			if (item instanceof Gold) {
				giveGold( item.quantity() );
				stored++;
				continue;
			}
			if (item instanceof EnergyCrystal) {
				giveEnergy( item.quantity() );
				stored++;
				continue;
			}
			if (item.collect()) {
				stored++;
			} else {
				Dungeon.level.drop( item, hero.pos ).sprite.drop();
				dropped++;
			}
		}

		Item.updateQuickslot();
		String name = probe.trueName();
		if (dropped > 0 && stored == 0) {
			GLog.w( Messages.get( Sandbox.class, "dropped", name ) );
		} else if (dropped > 0) {
			GLog.p( Messages.get( Sandbox.class, "spawned_both", name, stored, dropped ) );
		} else {
			GLog.p( Messages.get( Sandbox.class, "spawned", name ) );
		}
	}

	public static boolean spawnMob( Class<? extends Mob> cls ) {
		if (Dungeon.hero == null || Dungeon.level == null) return false;
		Mob mob;
		try {
			mob = Reflection.newInstance( cls );
		} catch (Exception e) {
			GLog.w( Messages.get( Sandbox.class, "spawn_fail" ) );
			return false;
		}
		int cell = spawnCell( mob );
		if (cell == -1) {
			GLog.w( Messages.get( Sandbox.class, "no_space" ) );
			return false;
		}
		mob.pos = cell;
		GameScene.add( mob );
		if (mob.alignment == Char.Alignment.ENEMY) {
			mob.beckon( Dungeon.hero.pos );
			mob.aggro( Dungeon.hero );
		}
		GLog.p( Messages.get( Sandbox.class, "spawned_mob", mob.name() ) );
		return true;
	}

	private static int spawnCell( Char ch ) {
		Level level = Dungeon.level;
		boolean large = Char.hasProp( ch, Char.Property.LARGE );
		int best = -1;
		int bestDist = Integer.MAX_VALUE;
		for (int i = 0; i < level.length(); i++) {
			if (i == Dungeon.hero.pos || Actor.findChar( i ) != null) continue;
			if (level.solid[i] || level.pit[i]) continue;
			if (!level.passable[i] && !level.avoid[i]) continue;
			if (large && !level.openSpace[i]) continue;
			int dist = level.distance( Dungeon.hero.pos, i );
			if (dist < 1 || dist >= bestDist) continue;
			best = i;
			bestDist = dist;
			if (bestDist == 1) break;
		}
		return best;
	}

	public static void travel( int depth, int branch ) {
		if (Dungeon.hero == null || !Dungeon.hero.isAlive()) return;
		if (branch == 0 && depth > Statistics.deepestFloor) {
			Statistics.deepestFloor = depth;
		}
		InterlevelScene.mode = InterlevelScene.Mode.RETURN;
		InterlevelScene.returnDepth = depth;
		InterlevelScene.returnBranch = branch;
		InterlevelScene.returnPos = -1;
		Game.switchScene( InterlevelScene.class );
	}

	public static int windowWidth() {
		int max = (int) PixelScene.uiCamera.width - 16;
		int pref = PixelScene.landscape() ? 144 : 120;
		return Math.max( 104, Math.min( pref, max ) );
	}

	public static int windowHeight() {
		int max = (int) PixelScene.uiCamera.height - 22;
		int pref = PixelScene.landscape() ? 136 : 184;
		return Math.max( 112, Math.min( pref, max ) );
	}

}
