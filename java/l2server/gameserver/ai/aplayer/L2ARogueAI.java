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
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Skill.SkillTargetType;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Character.AIAccessor;
import l2server.gameserver.model.actor.L2Playable;

/**
 * @author Pere
 *
 */
public class L2ARogueAI extends L2APlayerAI
{
	public L2ARogueAI(AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected int[] getRandomGear()
	{
		return new int[]{30268, 19704, 19705, 19706, 19707, 19708, 19464, 19463, 19458, 17623, 35570, 34860, 19462, 19454, 35890, 30312};
	}
	
	@Override
	protected boolean interactWith(L2Character target)
	{
		if (super.interactWith(target))
			return true;
		
		if (_player.getCurrentMp() > _player.getMaxMp() * 0.7
				|| _player.getCurrentHp() < _player.getMaxHp() * 0.5
				|| _player.getTarget() instanceof L2Playable)
		{
			for (L2Skill skill : _player.getAllSkills())
			{
				if (!skill.isOffensive() || skill.getTargetType() != SkillTargetType.TARGET_ONE)
					continue;

				//if (_player.useMagic(skill, true, false))
				//	break;
				_player.useMagic(skill, true, false);
			}
		}
		
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		
		return true;
	}
	
	@Override
	protected void think()
	{
		super.think();
	}
}
