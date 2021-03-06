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

import l2server.gameserver.model.Skill;
import l2server.util.loader.annotations.Load;

/**
 * @author BiTi
 */
public class HeroSkillTable {
	private static final Skill[] heroSkills = new Skill[5];
	private static final int[] heroSkillsId = {395, 396, 1374, 1375, 1376};

	private HeroSkillTable() {
	}
	
	@Load(dependencies = SkillTable.class)
	private void initialize() {
		for (int i = 0; i < heroSkillsId.length; i++) {
			heroSkills[i] = SkillTable.getInstance().getInfo(heroSkillsId[i], 1);
		}
	}
	
	public static HeroSkillTable getInstance() {
		return SingletonHolder.instance;
	}

	public static Skill[] getHeroSkills() {
		return heroSkills;
	}

	public static boolean isHeroSkill(int skillid) {
		/*
		 * Do not perform checks directly on Skill array,
		 * it will cause errors due to SkillTable not initialized
		 */
		for (int id : heroSkillsId) {
			if (id == skillid) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final HeroSkillTable instance = new HeroSkillTable();
	}
}
