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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.InstanceType;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.knownlist.FriendlyMobKnownList;
import l2server.gameserver.templates.chars.NpcTemplate;

/**
 * This class represents Friendly Mobs lying over the world.
 * These friendly mobs should only attack players with karma > 0
 * and it is always aggro, since it just attacks players with karma
 *
 * @version $Revision: 1.20.4.6 $ $Date: 2005/07/23 16:13:39 $
 */
public class FriendlyMobInstance extends Attackable {
	public FriendlyMobInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2FriendlyMobInstance);
	}
	
	@Override
	public final FriendlyMobKnownList getKnownList() {
		return (FriendlyMobKnownList) super.getKnownList();
	}
	
	@Override
	public FriendlyMobKnownList initialKnownList() {
		return new FriendlyMobKnownList(this);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		if (attacker instanceof Player) {
			return ((Player) attacker).getReputation() < 0;
		}
		return false;
	}
	
	@Override
	public boolean isAggressive() {
		return true;
	}
}
