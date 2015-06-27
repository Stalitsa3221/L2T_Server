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
package l2server.gameserver.network.clientpackets;

import l2server.gameserver.instancemanager.PartySearchManager;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExRegistWaitingSubstituteOk;
import l2server.gameserver.network.serverpackets.PartySmallWindowUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.UserInfo;

/**
 * @author Erlandys
 *
 */
public class RequestRegistPartySubstitute extends L2GameClientPacket
{
	private static final String _C__D0_A8_REQUESTREGISTPARTYSUBSTITUTE = "[C] D0:A8 RequestRegistPartySubstitute";

	int _objId;

	@Override
	protected void readImpl()
	{
		_objId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance leader = getClient().getActiveChar();
		PartySearchManager psm = PartySearchManager.getInstance();
		SystemMessage sm1;
		if (L2World.getInstance().getPlayer(_objId) != null)
		{
			L2PcInstance activeChar = L2World.getInstance().getPlayer(_objId);
			if (psm.getWannaToChangeThisPlayer(activeChar.getLevel(), activeChar.getClassId()) == null)
			{
				sm1 = SystemMessage.getSystemMessage(SystemMessageId.LOOKING_FOR_A_PLAYER_WHO_WILL_REPLACE_S1);
				sm1.addCharName(activeChar);
				leader.sendPacket(sm1);
				psm.addChangeThisPlayer(activeChar);
			}

			leader.getParty().broadcastToPartyMembers(activeChar, new PartySmallWindowUpdate(activeChar));
			activeChar.sendPacket(new UserInfo(activeChar));

			L2PcInstance changingPlayer = psm.getLookingForParty(activeChar.getLevel(), activeChar.getClassId());
			if (changingPlayer != null)
			{
				changingPlayer.sendPacket(new ExRegistWaitingSubstituteOk(changingPlayer.getClassId(), activeChar));
				changingPlayer.closeWaitingSubstitute();
				changingPlayer.setPlayerForChange(activeChar);
				psm.removeChangeThisPlayer(activeChar);

				leader.sendMessage(changingPlayer.getName() + " meets the requirements to change " + activeChar.getName() + "."); // Not retail thing, need something to do with that......
			}
		}
		else
			leader.sendPacket(SystemMessageId.THE_PLAYER_TO_BE_REPLACED_DOES_NOT_EXIST);
	}

	@Override
	public String getType()
	{
		return _C__D0_A8_REQUESTREGISTPARTYSUBSTITUTE;
	}
}
