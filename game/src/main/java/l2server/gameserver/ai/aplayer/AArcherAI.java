/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.ai.aplayer;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.templates.skills.SkillTargetType;

/**
 * @author Pere
 */
public class AArcherAI extends APlayerAI {
	public static final int QUICK_EVASION = 10774;
	public static final int QUICK_CHARGE = 10805;
	
	public AArcherAI(Creature creature) {
		super(creature);
	}
	
	@Override
	protected int[] getRandomGear() {
		return new int[]{30250, 19912, 19913, 19914, 19915, 19916, 19464, 19463, 19458, 17623, 35570, 34860, 19462, 19454, 35890, 30313};
	}
	
	@Override
	protected boolean interactWith(Creature target) {
		if (super.interactWith(target)) {
			return true;
		}
		
		if (target != null) {
			if (player.isInsideRadius(target, 100, true, true)) {
				Skill skill = player.getKnownSkill(QUICK_EVASION);
				if (skill != null && player.useMagic(skill, true, false)) {
					return true;
				}
			} else if (!player.isInsideRadius(target, 1500, true, true)) {
				Skill skill = player.getKnownSkill(QUICK_CHARGE);
				if (skill != null && player.useMagic(skill, true, false)) {
					return true;
				}
			}
		}
		
		for (Creature attacker : player.getKnownList().getKnownCharactersInRadius(100)) {
			if (player.isEnemy(attacker) && attacker.isAttackingNow() && attacker.getTarget() == player) {
				Skill skill = player.getKnownSkill(QUICK_CHARGE);
				if (skill != null && player.useMagic(skill, true, false)) {
					return true;
				}
				
				skill = player.getKnownSkill(QUICK_EVASION);
				if (skill != null && player.useMagic(skill, true, false)) {
					return true;
				}
			}
		}
		
		if (player.getCurrentMp() > player.getMaxMp() * 0.7 || player.getCurrentHp() < player.getMaxHp() * 0.5 ||
				player.getTarget() instanceof Playable) {
			for (Skill skill : player.getAllSkills()) {
				if (!skill.isOffensive() || skill.getTargetType() != SkillTargetType.TARGET_ONE) {
					continue;
				}
				
				if (player.useMagic(skill, true, false)) {
					break;
				}
			}
		}
		
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		
		return true;
	}
	
	@Override
	protected void think() {
		super.think();
		
		Item arrows = player.getInventory().getItemByItemId(18550);
		if (arrows == null || arrows.getCount() < 1000) {
			player.getInventory().addItem("", 18550, 1000, player, player);
			Item bow = player.getActiveWeaponInstance();
			if (bow != null) {
				player.useEquippableItem(bow, false);
				player.useEquippableItem(bow, false);
			}
		}
	}
}
