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

import l2server.gameserver.datatables.HennaTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.HennaItemRemoveInfo;
import l2server.gameserver.templates.item.L2Henna;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public final class RequestHennaItemRemoveInfo extends L2GameClientPacket
{
	private static final String _C__BB_RequestHennaItemRemoveInfo = "[C] 71 RequestHennaItemRemoveInfo";
	
	private int _symbolId;
	// format  cd
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);
		if (template == null)
			return;
		
		activeChar.sendPacket(new HennaItemRemoveInfo(template, activeChar));
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__BB_RequestHennaItemRemoveInfo;
	}
}
