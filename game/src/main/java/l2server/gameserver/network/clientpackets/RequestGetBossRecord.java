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

import l2server.gameserver.instancemanager.RaidBossPointsManager;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ExGetBossRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Format: (ch) d
 *
 * @author -Wooden-
 */
public class RequestGetBossRecord extends L2GameClientPacket {
	private static Logger log = LoggerFactory.getLogger(RequestGetBossRecord.class.getName());

	private int bossId;
	
	@Override
	protected void readImpl() {
		bossId = readD();
	}
	
	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		if (bossId != 0) {
			log.info("C5: RequestGetBossRecord: d: " + bossId + " ActiveChar: " +
					activeChar); // should be always 0, log it if isnt 0 for furture research
		}
		
		int points = RaidBossPointsManager.getInstance().getPointsByOwnerId(activeChar.getObjectId());
		int ranking = RaidBossPointsManager.getInstance().calculateRanking(activeChar.getObjectId());
		
		Map<Integer, Integer> list = RaidBossPointsManager.getInstance().getList(activeChar);
		
		// trigger packet
		activeChar.sendPacket(new ExGetBossRecord(ranking, points, list));
	}
	
	@Override
	protected boolean triggersOnActionRequest() {
		return false;
	}
}
