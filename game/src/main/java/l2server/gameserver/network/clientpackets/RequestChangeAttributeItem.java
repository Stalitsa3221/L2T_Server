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

import l2server.Config;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Erlandys
 */
public class RequestChangeAttributeItem extends L2GameClientPacket {
	private static Logger log = LoggerFactory.getLogger(RequestChangeAttributeItem.class.getName());


	private int attributeOID, itemOID, newAttributeID;
	
	@Override
	protected void readImpl() {
		attributeOID = readD();
		itemOID = readD();
		newAttributeID = readD();
	}
	
	@Override
	protected void runImpl() {
		Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}
		log.info(itemOID + "");
		Item item = player.getInventory().getItemByObjectId(itemOID);
		
		if (player.getPrivateStoreType() != 0) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_CHANGE_AN_ATTRIBUTE_WHILE_USING_A_PRIVATE_SHOP_OR_WORKSHOP);
			return;
		}
		
		if (player.getActiveTradeList() != null) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_CHANGE_ATTRIBUTES_WHILE_EXCHANING);
			return;
		}
		
		if (!item.isWeapon()) {
			player.setActiveEnchantAttrItem(null);
			player.sendPacket(new ExChangeAttributeItemList(player, attributeOID));
			return;
		}
		
		if (newAttributeID == -1) {
			player.setActiveEnchantAttrItem(null);
			player.sendPacket(new ExChangeAttributeItemList(player, attributeOID));
			return;
		}
		Item attribute = player.getInventory().getItemByObjectId(attributeOID);
		player.getInventory().destroyItem("ChangingAttribute", attributeOID, 1, player, null);
		
		if (Rnd.get(100) < Config.CHANGE_CHANCE_ELEMENT) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_S2_ATTRIBUTE_HAS_SUCCESSFULLY_CHANGED_TO_S3_ATTRIBUTE);
			sm.addItemName(item);
			sm.addElemental(item.getAttackElementType());
			sm.addElemental(newAttributeID);
			
			item.changeAttribute((byte) newAttributeID, item.getAttackElementPower());
			if (item.isEquipped()) {
				item.updateElementAttrBonus(player);
			}
			
			player.sendPacket(sm);
			player.sendPacket(new ExChangeAttributeOk());
			player.sendPacket(new UserInfo(player));
		} else {
			player.sendPacket(new ExChangeAttributeFail());
			player.sendPacket(SystemMessageId.CHANGING_ATTRIBUTES_HAS_BEEN_FAILED);
		}
		
		// send packets
		player.sendPacket(new ExStorageMaxCount(player));
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(item);
		if (player.getInventory().getItemByObjectId(attributeOID) == null) {
			iu.addRemovedItem(attribute);
		} else {
			iu.addModifiedItem(attribute);
		}
		player.sendPacket(iu);
		
		player.setActiveEnchantAttrItem(null);
	}
}
