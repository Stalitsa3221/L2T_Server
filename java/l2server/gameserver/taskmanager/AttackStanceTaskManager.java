/*
 * $HeadURL: $
 *
 * $Author: $ $Date: $ $Revision: $
 *
 *
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

package l2server.gameserver.taskmanager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2CubicInstance;
import l2server.gameserver.model.actor.instance.L2MobSummonInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.serverpackets.AutoAttackStop;
import l2server.log.Log;

/**
 * This class ...
 *
 * @version $Revision: $ $Date: $
 * @author Luca Baldi
 */
public class AttackStanceTaskManager
{
	
	protected Map<L2Character, Long> _attackStanceTasks = new ConcurrentHashMap<L2Character, Long>();
	
	private AttackStanceTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FightModeScheduler(), 0, 1000);
	}
	
	public static AttackStanceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void addAttackStanceTask(L2Character actor)
	{
		if (actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) actor;
			actor = summon.getOwner();
		}
		if (actor instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) actor;
			player.setFightStanceTime(System.currentTimeMillis());
			player.onCombatStanceStart();
			for (L2CubicInstance cubic : player.getCubics().values())
			{
				if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
					cubic.doAction();
			}
			
			for (L2SummonInstance summon : player.getSummons())
			{
				if (summon instanceof L2MobSummonInstance)
					summon.unSummon(player);
			}
		}
		_attackStanceTasks.put(actor, System.currentTimeMillis());
	}
	
	public void removeAttackStanceTask(L2Character actor)
	{
		if (actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) actor;
			actor = summon.getOwner();
		}
		if (actor instanceof L2PcInstance)
			((L2PcInstance) actor).onCombatStanceEnd();
		
		_attackStanceTasks.remove(actor);
	}
	
	public boolean getAttackStanceTask(L2Character actor)
	{
		if (actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) actor;
			actor = summon.getOwner();
		}
		
		return _attackStanceTasks.containsKey(actor);
	}
	
	private class FightModeScheduler implements Runnable
	{
		protected FightModeScheduler()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			Long current = System.currentTimeMillis();
			try
			{
				if (_attackStanceTasks != null)
				{
					synchronized (this)
					{
						for (Entry<L2Character, Long> entry : _attackStanceTasks.entrySet())
						{
							L2Character actor = entry.getKey();
							if (current - entry.getValue() > 15000)
							{
								actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
								if (actor instanceof L2PcInstance)
								{
									if (((L2PcInstance) actor).getPet() != null)
										((L2PcInstance) actor).getPet().broadcastPacket(new AutoAttackStop(((L2PcInstance) actor).getPet().getObjectId()));
									
									if (((L2PcInstance) actor).getSummons() != null)
									{
										for (L2SummonInstance summon : ((L2PcInstance) actor).getSummons())
										{
											if (summon != null)
												summon.broadcastPacket(new AutoAttackStop(summon.getObjectId()));
										}
									}
								}
								actor.getAI().setAutoAttacking(false);
								removeAttackStanceTask(actor);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "Error in FightModeScheduler: " + e.getMessage(), e);
			}
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager _instance = new AttackStanceTaskManager();
	}
}
