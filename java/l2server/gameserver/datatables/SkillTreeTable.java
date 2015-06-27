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

import gnu.trove.TIntObjectHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import l2server.Config;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2SkillLearn;
import l2server.gameserver.model.L2TransformSkillLearn;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.base.Race;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

public class SkillTreeTable
{
	private List<L2SkillLearn> _fishingSkillTrees = new ArrayList<L2SkillLearn>(); //all common skills (taught by Fisherman)
	private List<L2SkillLearn> _expandDwarfCraftSkillTrees = new ArrayList<L2SkillLearn>(); //list of special skill for dwarf (expand dwarf craft) learned by class teacher
	private List<L2TransformSkillLearn> _transformSkillTrees = new ArrayList<L2TransformSkillLearn>(); // Transform Skills (Test)
	private ArrayList<L2SkillLearn> _specialSkillTrees = new ArrayList<L2SkillLearn>();
	
	// checker, sorted arrays of hash codes
	private TIntObjectHashMap<long[]> _skillsByRaceHashCodes; // race-specific transformations
	private long[] _allSkillsHashCodes; // fishing, special and all races transformations
	
	private boolean _loading = true;
	
	public static SkillTreeTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private SkillTreeTable()
	{
		load();
	}
	
	/**
	 * Return the minimum level needed to have this Expertise.<BR><BR>
	 *
	 * @param grade The grade level searched
	 */
	public int getExpertiseLevel(int grade)
	{
		if (grade <= 0)
			return 0;
		
		if (Config.IS_CLASSIC && grade > 3)
			return 100;
		
		// since expertise comes at same level for all classes we use paladin for now
		Map<Long, L2SkillLearn> learnMap = PlayerClassTable.getInstance().getClassById(0).getSkills();
		
		long skillHashCode = SkillTable.getSkillHashCode(239, grade);
		if (learnMap.containsKey(skillHashCode))
			return learnMap.get(skillHashCode).getMinLevel();
		
		Log.severe("Expertise not found for grade " + grade);
		return 0;
	}
	
	private void load()
	{
		_loading = true;
		
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skillTrees/fishingSkillTree.xml");
		XmlDocument doc = new XmlDocument(file);

		int prevSkillId = -1;
		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (n.getName().equalsIgnoreCase("skill"))
			{
				int id = n.getInt("id");
				int lvl = n.getInt("level");
				int minLvl = n.getInt("minLevel");;
				int cost = n.getInt("spCost");;
				int costId = n.getInt("itemId");;
				int costCount = n.getInt("count");;
				boolean isDwarven = n.getBool("isForDwarf");
				boolean npc = n.getBool("learnedByNpc");
				boolean fs = n.getBool("learnedByFS");
				
				if (prevSkillId != id)
					prevSkillId = id;
				
				L2SkillLearn skill = new L2SkillLearn(id, lvl, cost, minLvl, 0, npc, fs, false, false);
				skill.addCostItem(costId, costCount);
				
				if (isDwarven)
					_expandDwarfCraftSkillTrees.add(skill);
				else
					_fishingSkillTrees.add(skill);
			}
		}

		file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skillTrees/transformSkillTree.xml");
		doc = new XmlDocument(file);

		prevSkillId = -1;
		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (n.getName().equalsIgnoreCase("skill"))
			{
				int id = n.getInt("id");
				int lvl = n.getInt("level");
				int minLvl = n.getInt("minLevel");
				int cost = n.getInt("spCost");
				int itemId = n.getInt("itemId");
				int raceId = n.getInt("raceId");
				
				if (prevSkillId != id)
					prevSkillId = id;
				
				L2TransformSkillLearn skill = new L2TransformSkillLearn(raceId, id, itemId, lvl, cost, minLvl);
				
				_transformSkillTrees.add(skill);
			}
		}

		file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skillTrees/specialSkillTree.xml");
		doc = new XmlDocument(file);

		prevSkillId = -1;
		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (n.getName().equalsIgnoreCase("skill"))
			{
				int id = n.getInt("id");
				int lvl = n.getInt("level");
				int costId = n.getInt("itemId");
				int costCount = n.getInt("count");;
				boolean npc = n.getBool("learnedByNpc");
				boolean fs = n.getBool("learnedByFS");
				
				if (prevSkillId != id)
					prevSkillId = id;
				
				L2SkillLearn skill = new L2SkillLearn(id, lvl, 0, 0, 0, npc, fs, false, false);
				skill.addCostItem(costId, costCount);
				
				_specialSkillTrees.add(skill);
			}
		}
		
		generateCheckArrays();
		
		Log.info("FishingSkillTreeTable: Loaded " + _fishingSkillTrees.size() + " general skills.");
		Log.info("DwarvenCraftSkillTreeTable: Loaded " + _expandDwarfCraftSkillTrees.size() + " dwarven skills.");
		Log.info("TransformSkillTreeTable: Loaded " + _transformSkillTrees.size() + " transform skills");
		Log.info("SpecialSkillTreeTable: Loaded " + _specialSkillTrees.size() + " special skills");
		_loading = false;
	}
	
	private void generateCheckArrays()
	{
		int i;
		long[] array;
		
		// race-specific skills including dwarven (obtained by fishing)
		ArrayList<Long> list = new ArrayList<Long>();
		TIntObjectHashMap<long[]> result = new TIntObjectHashMap<long[]>(Race.values().length);
		for (Race r : Race.values())
		{
			for (L2TransformSkillLearn s : _transformSkillTrees)
			{
				if (s.getRace() == r.ordinal())
					list.add(SkillTable.getSkillHashCode(s.getId(), s.getLevel()));
			}
			
			if (r == Race.Dwarf)
			{
				for (L2SkillLearn s : _expandDwarfCraftSkillTrees)
					list.add(SkillTable.getSkillHashCode(s.getId(), s.getLevel()));
			}
			
			i = 0;
			array = new long[list.size()];
			for (long s : list)
				array[i++] = s;
			Arrays.sort(array);
			result.put(r.ordinal(), array);
			list.clear();
		}
		_skillsByRaceHashCodes = result;
		
		// skills available for all classes and races
		for (L2SkillLearn s : _fishingSkillTrees)
			list.add(SkillTable.getSkillHashCode(s.getId(), s.getLevel()));
		
		for (L2TransformSkillLearn s : _transformSkillTrees)
			if (s.getRace() == -1)
				list.add(SkillTable.getSkillHashCode(s.getId(), s.getLevel()));
		
		for (L2SkillLearn s : _specialSkillTrees)
			list.add(SkillTable.getSkillHashCode(s.getId(), s.getLevel()));
		
		i = 0;
		array = new long[list.size()];
		for (long s : list)
			array[i++] = s;
		Arrays.sort(array);
		_allSkillsHashCodes = array;
	}
	
	public L2SkillLearn[] getAvailableClassSkills(L2PcInstance cha)
	{
		List<L2SkillLearn> result = new ArrayList<L2SkillLearn>();
		Set<Long> skillIds = cha.getCurrentClass().getSkills().keySet();
		
		if (skillIds == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			Log.warning("Skilltree for class " + cha.getCurrentClass().getName() + " is not defined!");
			return new L2SkillLearn[0];
		}
		
		L2Skill[] oldSkills = cha.getAllSkills();
		
		for (long skillId : skillIds)
		{
			L2SkillLearn temp = cha.getCurrentClass().getSkills().get(skillId);
			//Let's get all auto-get skills and all skill learn from npc, but transfer skills.
			if (((temp.isAutoGetSkill() && temp.getMinLevel() <= cha.getLevel() && temp.getMinDualLevel() <= cha.getDualLevel())
					|| (temp.isLearnedFromPanel() && !temp.isTransferSkill()))
					&& (!temp.isRemember() || cha.isRememberSkills()))
			{
				boolean knownSkill = false;
				
				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						
						if (oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}
				
				if (!knownSkill && (temp.getLevel() == 1 || temp.getId() == 248))
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}
		
		return result.toArray(new L2SkillLearn[result.size()]);
	}
	
	// Very inefficient function but works
	public boolean hasNewSkillsToLearn(L2PcInstance cha, PlayerClass cl)
	{
		List<L2SkillLearn> result1 = new ArrayList<L2SkillLearn>();
		List<L2SkillLearn> result2 = new ArrayList<L2SkillLearn>();
		Collection<L2SkillLearn> skills = PlayerClassTable.getInstance().getClassById(cl.getId()).getSkills().values();
		
		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			Log.warning("Skilltree for class " + cl.getName() + " is not defined!");
			return false;
		}
		
		L2Skill[] oldSkills = cha.getAllSkills();
		
		for (L2SkillLearn temp : skills)
		{
			//Let's get all auto-get skills and all skill learn from npc, but transfer skills.
			if ((temp.isAutoGetSkill() || (temp.isLearnedFromPanel() && !temp.isTransferSkill()))
					&& temp.getMinLevel() <= cha.getLevel() && temp.getMinDualLevel() <= cha.getDualLevel())
			{
				boolean knownSkill = false;
				
				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						
						if (oldSkills[j].getLevelHash() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result1.add(temp);
						}
					}
				}
				
				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result1.add(temp);
				}
			}
		}
		
		for (L2SkillLearn temp : skills)
		{
			//Let's get all auto-get skills and all skill learn from npc, but transfer skills.
			if ((temp.isAutoGetSkill() || (temp.isLearnedFromPanel() && !temp.isTransferSkill()))
					&& temp.getMinLevel() <= cha.getLevel() - 1 && temp.getMinDualLevel() <= cha.getDualLevel() - 1)
			{
				boolean knownSkill = false;
				
				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						
						if (oldSkills[j].getLevelHash() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result2.add(temp);
						}
					}
				}
				
				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result2.add(temp);
				}
			}
		}
		
		return result1.size() > result2.size();
	}
	
	public L2SkillLearn[] getAvailableSkills(L2PcInstance cha)
	{
		List<L2SkillLearn> result = new ArrayList<L2SkillLearn>();
		List<L2SkillLearn> skills = new ArrayList<L2SkillLearn>();
		
		skills.addAll(_fishingSkillTrees);
		
		if (skills.size() < 1)
		{
			// the skilltree for this class is undefined, so we give an empty list
			Log.warning("Skilltree for fishing is not defined!");
			return new L2SkillLearn[0];
		}
		
		if (cha.hasDwarvenCraft() && _expandDwarfCraftSkillTrees != null)
		{
			skills.addAll(_expandDwarfCraftSkillTrees);
		}
		
		L2Skill[] oldSkills = cha.getAllSkills();
		
		for (L2SkillLearn temp : skills)
		{
			if (temp.isLearnedFromPanel() && temp.getMinLevel() <= cha.getLevel() && temp.getMinDualLevel() <= cha.getDualLevel())
			{
				boolean knownSkill = false;
				
				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						
						if (oldSkills[j].getLevelHash() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}
				
				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}
		
		return result.toArray(new L2SkillLearn[result.size()]);
	}
	
	public L2SkillLearn[] getAvailableSpecialSkills(L2PcInstance cha)
	{
		List<L2SkillLearn> result = new ArrayList<L2SkillLearn>();
		List<L2SkillLearn> skills = new ArrayList<L2SkillLearn>();
		
		skills.addAll(_specialSkillTrees);
		
		if (skills.size() < 1)
		{
			// the skilltree for this class is undefined, so we give an empty list
			Log.warning("Skilltree for special is not defined!");
			return new L2SkillLearn[0];
		}
		
		L2Skill[] oldSkills = cha.getAllSkills();
		
		for (L2SkillLearn temp : skills)
		{
			boolean knownSkill = false;
			
			for (int j = 0; j < oldSkills.length && !knownSkill; j++)
			{
				if (oldSkills[j].getId() == temp.getId())
				{
					knownSkill = true;
					
					if (oldSkills[j].getLevelHash() == temp.getLevel() - 1)
					{
						// this is the next level of a skill that we know
						result.add(temp);
					}
				}
			}
			
			if (!knownSkill && temp.getLevel() == 1)
			{
				// this is a new skill
				result.add(temp);
			}
		}
		
		return result.toArray(new L2SkillLearn[result.size()]);
	}
	
	public L2TransformSkillLearn[] getAvailableTransformSkills(L2PcInstance cha)
	{
		List<L2TransformSkillLearn> result = new ArrayList<L2TransformSkillLearn>();
		List<L2TransformSkillLearn> skills = _transformSkillTrees;
		
		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			
			Log.warning("No Transform skills defined!");
			return new L2TransformSkillLearn[0];
		}
		
		L2Skill[] oldSkills = cha.getAllSkills();
		
		for (L2TransformSkillLearn temp : skills)
		{
			if (temp.getMinLevel() <= cha.getLevel() && (temp.getRace() == cha.getRace().ordinal() || temp.getRace() == -1))
			{
				boolean knownSkill = false;
				
				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						
						if (oldSkills[j].getLevelHash() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}
				
				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}
		
		return result.toArray(new L2TransformSkillLearn[result.size()]);
	}
	
	public int getMinLevelForNewSkill(L2PcInstance cha)
	{
		int minLevel = 0;
		List<L2SkillLearn> skills = new ArrayList<L2SkillLearn>();
		
		skills.addAll(_fishingSkillTrees);
		
		if (skills.size() < 1)
		{
			// the skilltree for this class is undefined, so we give an empty list
			Log.warning("SkillTree for fishing is not defined!");
			return minLevel;
		}
		
		if (cha.hasDwarvenCraft() && _expandDwarfCraftSkillTrees != null)
		{
			skills.addAll(_expandDwarfCraftSkillTrees);
		}
		
		for (L2SkillLearn s : skills)
		{
			if (s.getMinLevel() > cha.getLevel())
				if (minLevel == 0 || s.getMinLevel() < minLevel)
					minLevel = s.getMinLevel();
		}
		
		return minLevel;
	}
	
	public int getMinLevelForNewTransformSkill(L2PcInstance cha)
	{
		int minLevel = 0;
		List<L2TransformSkillLearn> skills = new ArrayList<L2TransformSkillLearn>();
		
		skills.addAll(_transformSkillTrees);
		
		if (skills.size() < 1)
		{
			// the skilltree for this class is undefined, so we give an empty list
			Log.warning("SkillTree for fishing is not defined!");
			return minLevel;
		}
		
		for (L2TransformSkillLearn s : skills)
		{
			if ((s.getMinLevel() > cha.getLevel()) && (s.getRace() == cha.getRace().ordinal()))
				if (minLevel == 0 || s.getMinLevel() < minLevel)
					minLevel = s.getMinLevel();
		}
		
		return minLevel;
	}
	
	public int getSkillCost(L2PcInstance player, L2Skill skill)
	{
		int skillCost = 100000000;
		long skillHashCode = SkillTable.getSkillHashCode(skill);
		
		if (player.getCurrentClass().getSkills().containsKey(skillHashCode))
		{
			L2SkillLearn skillLearn = player.getCurrentClass().getSkills().get(skillHashCode);
			if (skillLearn.getMinLevel() <= player.getLevel())
			{
				skillCost = skillLearn.getSpCost();
			}
		}
		
		return skillCost;
	}
	
	public L2SkillLearn getSkillLearnBySkillIdLevel(PlayerClass cl, int skillId, int skillLvl)
	{
		for (L2SkillLearn sl : PlayerClassTable.getInstance().getClassById(cl.getId()).getSkills().values())
		{
			if (sl.getId() == skillId && sl.getLevel() == skillLvl)
			{
				return sl; // found skill learn
			}
		}
		return null;
	}
	
	public List<Integer> getAllAllowedSkillId(L2PcInstance player)
	{
		ArrayList<Integer> skills = new ArrayList<Integer>();
		
		for (L2SkillLearn tmp : player.getCurrentClass().getSkills().values())
		{
			if (skills.contains(tmp.getId()))
				skills.add(tmp.getId());
		}
		
		return skills;
	}
	
	public boolean isSkillAllowed(L2PcInstance player, L2Skill skill)
	{
		if (skill.isExcludedFromCheck())
			return true;
		
		if (player.isGM() && skill.isGMSkill())
			return true;
		
		if (_loading) // prevent accidental skill remove during reload
			return true;
		
		final int maxLvl = SkillTable.getInstance().getMaxLevel(skill.getId());
		final long hashCode = SkillTable.getSkillHashCode(skill.getId(), Math.min(skill.getLevel(), maxLvl));
		
		if (player.getCurrentClass().getSkills().containsKey(hashCode))
			return true;
		
		if (player.getTemplate().getSkillIds().contains(skill.getId()))
			return true;
		
		if (Arrays.binarySearch(_skillsByRaceHashCodes.get(player.getRace().ordinal()), hashCode) >= 0)
			return true;
		
		if (Arrays.binarySearch(_allSkillsHashCodes, hashCode) >= 0)
			return true;
		
		return false;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SkillTreeTable _instance = new SkillTreeTable();
	}
	
	public void reload()
	{
		load();
	}
}
