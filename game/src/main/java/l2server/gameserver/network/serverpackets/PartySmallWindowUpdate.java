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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.instancemanager.PartySearchManager;
import l2server.gameserver.model.actor.instance.Player;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public final class PartySmallWindowUpdate extends L2GameServerPacket {
	private Player member;
	
	public PartySmallWindowUpdate(Player member) {
		this.member = member;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(member.getObjectId());
		//writeS(member.getName());
		writeH(0x03ff); // ???
		
		writeD((int) member.getCurrentCp()); //c4
		writeD(member.getMaxCp()); //c4
		
		writeD((int) member.getCurrentHp());
		writeD(member.getMaxVisibleHp());
		writeD((int) member.getCurrentMp());
		writeD(member.getMaxMp());
		writeC(member.getLevel());
		writeH(member.getCurrentClass().getId());
		writeC(PartySearchManager.getInstance().getWannaToChangeThisPlayer(member.getObjectId()) ? 0x01 : 0x00); // Party Searching
		writeD(member.getVitalityPoints());
	}
}
