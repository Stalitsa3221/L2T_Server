/*
 * $Header: PlayerClass.java, 24/11/2005 12:56:01 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 24/11/2005 12:56:01 $
 * $Revision: 1 $
 * $Log: PlayerClass.java,v $
 * Revision 1  24/11/2005 12:56:01  luisantonioa
 * Added copyright notice
 *
 *
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
package l2server.gameserver.model.base;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import l2server.gameserver.model.L2SkillLearn;
import l2server.gameserver.templates.item.L2Henna;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class PlayerClass
{
	private int _id;
	private String _name;
	private PlayerClass _parent;
	private int _awakensTo;
	private boolean _isMage;
	private Race _race;
	private int _level;
	private int _baseHp;
	private int _hpAdd;
	private int _hpMod;
	private int _baseMp;
	private int _mpAdd;
	private int _mpMod;
	private int _baseCp;
	private int _cpAdd;
	private int _cpMod;
	
	private Map<Long, L2SkillLearn> _skills = new LinkedHashMap<Long, L2SkillLearn>();
	
	private List<L2Henna> _allowedDyes = new ArrayList<L2Henna>();
	
	public PlayerClass(int id, String name, PlayerClass parent, int awakensTo, boolean isMage, int raceId, int level, int baseHp, int hpAdd, int hpMod, int baseMp, int mpAdd, int mpMod, int baseCp, int cpAdd, int cpMod)
	{
		_id = id;
		_name = name;
		_parent = parent;
		_awakensTo = awakensTo;
		_isMage = isMage;
		_race = raceId < 0 ? null : Race.values()[raceId];
		_level = level;
		_baseHp = baseHp;
		_hpAdd = hpAdd;
		_hpMod = hpMod;
		_baseMp = baseMp;
		_mpAdd = mpAdd;
		_mpMod = mpMod;
		_baseCp = baseCp;
		_cpAdd = cpAdd;
		_cpMod = cpMod;
	}
	
	public final int getId()
	{
		return _id;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final PlayerClass getParent()
	{
		return _parent;
	}
	
	public final int getAwakeningClassId()
	{
		return _awakensTo;
	}
	
	public final boolean isMage()
	{
		return _isMage;
	}
	
	public final int getLevel()
	{
		return _level;
	}
	
	public final Race getRace()
	{
		return _race;
	}
	
	public final int getBaseHp()
	{
		return _baseHp;
	}
	
	public final int getHpAdd()
	{
		return _hpAdd;
	}
	
	public final int getHpMod()
	{
		return _hpMod;
	}
	
	public final int getBaseMp()
	{
		return _baseMp;
	}
	
	public final int getMpAdd()
	{
		return _mpAdd;
	}
	
	public final int getMpMod()
	{
		return _mpMod;
	}
	
	public final int getBaseCp()
	{
		return _baseCp;
	}
	
	public final int getCpAdd()
	{
		return _cpAdd;
	}
	
	public final int getCpMod()
	{
		return _cpMod;
	}
	
	public final void addSkill(long hash, L2SkillLearn skill)
	{
		_skills.put(hash, skill);
	}
	
	public final Map<Long, L2SkillLearn> getSkills()
	{
		return _skills;
	}
	
	public void addAllowedDye(L2Henna henna)
	{
		_allowedDyes.add(henna);
	}
	
	public final List<L2Henna> getAllowedDyes()
	{
		return _allowedDyes;
	}
	
	public final boolean isSummoner()
	{
		return _id == 14 || _id == 28 || _id == 41 || _id == 96 || _id == 104
				|| _id == 111 || _id == 146 || _id == 176 || _id == 177 || _id == 178;
	}
	
	public final boolean childOf(PlayerClass cl)
	{
		if (_parent == null)
			return false;
		
		if (_parent == cl)
			return true;
		
		return _parent.childOf(cl);
		
	}
	
	public final boolean equalsOrChildOf(PlayerClass cl)
	{
		return this == cl || childOf(cl);
	}
	
	public final int level()
	{
		if (_parent == null)
			return 0;
		
		if (_id == 184 || _id == 185)
			return 2;
		
		return 1 + _parent.level();
	}
}
