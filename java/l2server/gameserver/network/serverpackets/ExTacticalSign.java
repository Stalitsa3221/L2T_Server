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

/**
 * @author Pere
 */
public final class ExTacticalSign extends L2GameServerPacket
{
	private int _objectId;
	private int _type;
	
	public ExTacticalSign(int objId, int type)
	{
		_objectId = objId;
		_type = type;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x100);
		writeD(_objectId);
		writeD(_type);
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "ExTacticalSign";
	}
}
