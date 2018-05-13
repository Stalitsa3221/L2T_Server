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
import l2server.gameserver.templates.chars.NpcTemplate;

/**
 * This class ...
 *
 * @author LBaldi
 * @version $Revision: $ $Date: $
 */
public class AdventurerInstance extends NpcInstance {
	public AdventurerInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2AdventurerInstance);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val) {
		String pom = "";
		
		if (val == 0) {
			pom = "" + npcId;
		} else {
			pom = npcId + "-" + val;
		}
		
		return "adventurer_guildsman/" + pom + ".htm";
	}
}
