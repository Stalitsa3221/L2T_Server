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

package l2server.gameserver.datatables;

import l2server.Config;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.ShuttleInstance;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.CreatureTemplate;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pere
 */
public class ShuttleTable {
	private static Logger log = LoggerFactory.getLogger(ShuttleTable.class.getName());

	private Map<Integer, ShuttleInstance> shuttles = new HashMap<>();

	private static ShuttleTable instance;

	public static ShuttleTable getInstance() {
		if (instance == null) {
			instance = new ShuttleTable();
		}

		return instance;
	}

	private ShuttleTable() {
	}

	@Load(dependencies = DoorTable.class)
	public void readShuttles() {
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "shuttles.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode shuttleNode : doc.getChildren()) {
			if (shuttleNode.getName().equalsIgnoreCase("shuttle")) {
				int id = shuttleNode.getInt("id");

				StatsSet npcDat = new StatsSet();
				npcDat.set("npcId", id);
				npcDat.set("level", 0);

				npcDat.set("collisionRadius", 0);
				npcDat.set("collisionHeight", 0);
				npcDat.set("type", "");
				npcDat.set("walkSpd", 300);
				npcDat.set("eunSpd", 300);
				npcDat.set("name", "AirShip");
				npcDat.set("hpMax", 50000);
				npcDat.set("hpReg", 3.e-3f);
				npcDat.set("mpReg", 3.e-3f);
				npcDat.set("pDef", 100);
				npcDat.set("mDef", 100);

				ShuttleInstance shuttle = new ShuttleInstance(IdFactory.getInstance().getNextId(), new CreatureTemplate(npcDat), id);
				World.getInstance().storeObject(shuttle);

				for (XmlNode stopNode : shuttleNode.getChildren()) {
					if (stopNode.getName().equalsIgnoreCase("stop")) {
						int x = stopNode.getInt("x");
						int y = stopNode.getInt("y");
						int z = stopNode.getInt("z");
						int time = stopNode.getInt("time");
						int doorId = stopNode.getInt("doorId");
						int outerDoorId = stopNode.getInt("outerDoorId");
						int oustX = stopNode.getInt("oustX");
						int oustY = stopNode.getInt("oustY");
						int oustZ = stopNode.getInt("oustZ");

						shuttle.addStop(x, y, z, time, doorId, outerDoorId, oustX, oustY, oustZ);
					}
				}

				shuttle.moveToNextRoutePoint();
				shuttles.put(id, shuttle);
			}
		}
		log.info("ShuttleTable: Loaded " + shuttles.size() + " shuttles.");
	}
}
