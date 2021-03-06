package instances.GrandBosses.Istina;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LasTravel
 * <p>
 * Istina Boss - Normal/Extreme mode
 * <p>
 * Source:
 * - http://www.youtube.com/watch?v=f2O97hNztBs
 * - http://l2wiki.com/Istina
 * - http://tw.myblog.yahoo.com/l2_friend/article?mid=28883&next=28764&l=f&fid=148
 */

public class Istina extends L2AttackableAIScript {
	//Quest
	private static final boolean debug = false;
	private static final String qn = "Istina";

	//Id's
	private static final int acidDummyNpc = 18919;
	private static final int boxgMagicPower = 30371;
	private static final int istinaCrystal = 37506;
	private static final int sealingEnergy = 19036;
	private static final int energyDevice = 17608;
	private static final int istinasCreationId = 23125;
	//private static final int	failedCreation		= 23037;
	private static final int taklacanId = 23030;
	private static final int torumbaId = 23031;
	private static final int dopagen = 23032;
	private static final int effectRedCircle = 14220101;
	private static final int effectBlueCircle = 14220102;
	private static final int effectGreenCircle = 14220103;
	private static final int ballistaId = 19021;
	private static final int rumieseEnterId = 33293;
	private static final int rumieseInnerId = 33151;
	private static final int[] _all_mobs = {29195, 29196, ballistaId, sealingEnergy};
	private static final int[] minionIds = {taklacanId, torumbaId, dopagen};
	private static final int[] templates = {169, 170};
	private static final int _ZONE_BLUE_ID = 60021;
	private static final int _ZONE_RED_ID = 60022;
	private static final int _ZONE_GREEN_ID = 60020;
	private static final int _manifestation_red_id = 14212;
	private static final int _manifestation_blue_id = 14213;
	private static final int _manifestarion_green_id = 14214;

	//Zones
	private static final ZoneType _ZONE_BLUE = ZoneManager.getInstance().getZoneById(_ZONE_BLUE_ID);
	private static final ZoneType _ZONE_RED = ZoneManager.getInstance().getZoneById(_ZONE_RED_ID);
	private static final ZoneType _ZONE_GREEN = ZoneManager.getInstance().getZoneById(_ZONE_GREEN_ID);

	//Skills
	private static final Skill energyControlDevice = SkillTable.getInstance().getInfo(14224, 1);
	private static final Skill flood = SkillTable.getInstance().getInfo(14220, 1);
	private static final Skill deathBlow = SkillTable.getInstance().getInfo(14219, 1);
	private static final Skill dummyAcidEruption = SkillTable.getInstance().getInfo(14222, 1);
	private static final Skill dummyEndAcidEruption = SkillTable.getInstance().getInfo(14223, 1);
	private static final Skill _manifestation_red = SkillTable.getInstance().getInfo(_manifestation_red_id, 1);
	private static final Skill _manifestation_blue = SkillTable.getInstance().getInfo(_manifestation_blue_id, 1);
	private static final Skill _manifestation_green = SkillTable.getInstance().getInfo(_manifestarion_green_id, 1);
	private static final Skill[] _manifestation_of_authority = {_manifestation_red, _manifestation_blue, _manifestation_green};

	//Cords
	private static final Location[] playerEnter =
			{new Location(-177100, 141730, -11264), new Location(-176802, 142267, -11269), new Location(-177235, 142597, -11264),
					new Location(-177133, 142992, -11269), new Location(-177124, 142264, -11269)};

	private static final Location[] minionLocs =
			{new Location(-178695, 147188, -11391, 3992), new Location(-175462, 147184, -11391, 27931), new Location(-176437, 149527, -11391, 44860)};

	//Others
	private enum zoneInUse {
		USE_ZONE_BLUE,
		USE_ZONE_RED,
		USE_ZONE_GREEN,
		NONE
	}

	;

	private class IstinaWorld extends InstanceWorld {
		private int IstinaId;
		private double maxBallistDamage;
		private double currBallistDamage;
		private int ballistaSeconds;
		private Npc Istina;
		private Npc Ballista;
		private zoneInUse zone;
		private Skill zoneDebuff;
		private boolean isHardMode;
		private ArrayList<Player> rewardedPlayers;

		public IstinaWorld() {
			isHardMode = false;
			ballistaSeconds = 30;
			zone = zoneInUse.NONE;
			rewardedPlayers = new ArrayList<Player>();
		}
	}

	public Istina(int questId, String name, String descr) {
		super(questId, name, descr);

		addEnterZoneId(_ZONE_BLUE_ID);
		addEnterZoneId(_ZONE_RED_ID);
		addEnterZoneId(_ZONE_GREEN_ID);
		addTalkId(rumieseEnterId);
		addStartNpc(rumieseEnterId);
		addTalkId(rumieseInnerId);
		addStartNpc(rumieseInnerId);
		addFirstTalkId(rumieseInnerId);
		addSpellFinishedId(acidDummyNpc);

		for (int mob : _all_mobs) {
			addAttackId(mob);
			addSpellFinishedId(mob);
			addSkillSeeId(mob);
		}
	}

	@Override
	public String onFirstTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onFirstTalk: " + player.getName());
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		}

		if (wrld != null && wrld instanceof IstinaWorld) {
			IstinaWorld world = (IstinaWorld) wrld;
			if (npc.getNpcId() == rumieseInnerId) {
				if (world.status == 8) {
					return "RumieseInnerBallistaLoaded.html";
				} else if (world.status >= 5 && world.status < 7) {
					return "RumieseInnerBallistaPreLoaded.html";
				}
			}
		}
		return super.onFirstTalk(npc, player);
	}

	@Override
	public String onSkillSee(Npc npc, Player player, Skill skill, WorldObject[] targets, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onSkillSee: " + skill.getName());
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else if (player != null) {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		} else {
			log.warn(getName() + ": onSpellFinished: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof IstinaWorld) {
			IstinaWorld world = (IstinaWorld) wrld;
			switch (skill.getId()) {
				case 14224: //Energy Control Device
					int casterCount = 0;
					for (Player players : npc.getKnownList().getKnownPlayers().values()) {
						if (players.getTarget() == npc && players.getLastSkillCast() == energyControlDevice) {
							casterCount++;
						}
					}

					if (npc == world.Istina) {
						if (casterCount >= 7) {
							Abnormal eff = npc.getFirstEffect(flood);
							if (eff != null) {
								eff.exit();
							}

							InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1811175, 0, true, 5000));
						}
					} else if (npc.getNpcId() == sealingEnergy) {
						if (npc.isCastingNow() && casterCount > 0) //Npc should be casting
						{
							npc.doDie(player);
						}
					}
					break;
			}
		}
		return super.onSkillSee(npc, player, skill, targets, isPet);
	}

	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill) {
		if (debug) {
			log.warn(getName() + ": onSpellFinished: " + skill.getName());
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else if (player != null) {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		} else {
			log.warn(getName() + ": onSpellFinished: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof IstinaWorld) {
			IstinaWorld world = (IstinaWorld) wrld;
			if (npc.getNpcId() == world.IstinaId) {
				switch (skill.getId()) {
					case 14215: //Barrier of Reflection
						InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1811148, 0, true, 5000));

						world.Istina.broadcastPacket(new PlaySound(3, "Npcdialog1.istina_voice_02", 0, 0, 0, 0, 0));
						break;

					case 14220: //Flood
						InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1811141, 0, true, 5000));

						world.Istina.broadcastPacket(new PlaySound(3, "Npcdialog1.istina_voice_05", 0, 0, 0, 0, 0));
						break;

					case 14221: //Acid Eruption
						InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1811156, 0, true, 5000));

						List<Player> instPlayers = InstanceManager.getInstance().getPlayers(world.instanceId);
						if (instPlayers.isEmpty()) {
							break;
						}

						for (int i = 1; i <= (world.isHardMode ? Rnd.get(2, 3) : Rnd.get(2, 5)); i++) {
							Player target = instPlayers.get(Rnd.get(instPlayers.size()));
							if (debug) {
								log.warn(getName() + ": Acid Target: " + target.getName());
							}

							Npc dummyAcid =
									addSpawn(acidDummyNpc, target.getX(), target.getY(), target.getZ(), -1, true, 0, false, world.instanceId);
							dummyAcid.setTarget(dummyAcid);
							dummyAcid.doCast(dummyAcidEruption);
						}
						break;

					case 14218: //Istina's Mark
						istinasMarkAndDeathBlow(world);
						break;
				}
			} else if (npc.getNpcId() == acidDummyNpc) {
				if (skill.getId() == 14222)//Acid Eruption
				{
					npc.setTarget(npc);
					npc.doCast(dummyEndAcidEruption);
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}

	private static void istinasMarkAndDeathBlow(IstinaWorld world) {
		Collection<Creature> players = world.Istina.getKnownList().getKnownCharacters();
		if (players == null || players.isEmpty()) {
			return;
		}

		for (Creature player : players) {
			if (player == null) {
				continue;
			}

			if (player.isAlikeDead()) {
				continue;
			}

			if (player.getFirstEffect(14218) != null) //Istina Mark
			{
				player.sendPacket(new ExShowScreenMessage(1811187, 0, true, 5000)); //Istina's Mark shines above the head.

				world.Istina.setTarget(player);
				world.Istina.doCast(deathBlow);
				break;
			}
		}
	}

	@Override
	public final String onAdvEvent(String event, Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else if (player != null) {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		} else {
			log.warn(getName() + ": onAdvEvent: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof IstinaWorld) {
			final IstinaWorld world = (IstinaWorld) wrld;
			if (event.equalsIgnoreCase("stage_0_open_doors")) {
				world.status = 1;
				for (DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors()) {
					door.openMe();
				}
				startQuestTimer("stage_1_intro", debug ? 60000 : 5 * 60000, null, player);
			} else if (event.equalsIgnoreCase("stage_1_intro")) {
				world.status = 2;
				for (DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors()) {
					door.closeMe();
				}

				//Kick retards
				ArrayList<Integer> allowedPlayers = new ArrayList<Integer>(world.allowed);
				for (int objId : allowedPlayers) {
					Player pl = World.getInstance().getPlayer(objId);
					if (pl != null && pl.isOnline() && pl.getInstanceId() == world.instanceId) {
						if (pl.getY() < 145039) {
							world.allowed.remove((Integer) pl.getObjectId());
							pl.logout(true);
						}
					}
				}

				InstanceManager.getInstance().showVidToInstance(31, world.instanceId); //intro

				startQuestTimer("stage_1_begin", ScenePlayerDataTable.getInstance().getVideoDuration(31), null, player);
			} else if (event.equalsIgnoreCase("stage_1_begin")) {
				world.status = 3;

				world.Istina = addSpawn(world.IstinaId, -177119, 147857, -11385, 49511, false, 0, false, world.instanceId);
				world.Istina.setMortal(false);

				startQuestTimer("stage_all_manifestation_of_authority", Rnd.get(60000 / 2, 60000), world.Istina, null);

				if (world.isHardMode) {
					startQuestTimer("stage_all_epic_sealing_energy_task", 60000, world.Istina, null);
					startQuestTimer("stage_all_epic_minions_task", 90000, world.Istina, null);
				}
			} else if (event.equalsIgnoreCase("stage_all_manifestation_of_authority")) {
				if (world.status < 4) {
					int delay = 0;
					if (world.Istina.isInCombat()) {
						//Cast a random Manifestation
						world.Istina.broadcastPacket(new PlaySound(3, "Npcdialog1.istina_voice_01", 0, 0, 0, 0, 0));

						final Skill randomSkill = _manifestation_of_authority[Rnd.get(_manifestation_of_authority.length)];
						switch (randomSkill.getId()) {
							case 14212: //Manifestation of Authority (Red)
								if (!world.isHardMode) {
									InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1811138, 0, true, 5000));
								}

								InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectBlueCircle, true));
								InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectGreenCircle, true));

								world.zone = zoneInUse.USE_ZONE_RED;
								break;

							case 14213: //Manifestation of Authority (Blue)
								if (!world.isHardMode) {
									InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1811139, 0, true, 5000));
								}

								InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectRedCircle, true));
								InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectGreenCircle, true));

								world.zone = zoneInUse.USE_ZONE_BLUE;
								break;

							case 14214: //Manifestation of Authority (Green)
								if (!world.isHardMode) {
									InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1811140, 0, true, 5000));
								}

								InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectRedCircle, true));
								InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectBlueCircle, true));

								world.zone = zoneInUse.USE_ZONE_GREEN;
								break;
						}

						startQuestTimer("stage_all_turnOffCircleEffect", 15000, npc, null);

						if (debug) {
							log.warn(getName() + ": onSpellFinished: Zone in use is: " + world.zone);
						}

						revalidateZone(world);

						if (world.Istina.isCastingNow()) {
							delay += 5000;
						}

						delay += Rnd.get(3, 5) * 1000;

						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
							@Override
							public void run() {
								world.Istina.doCast(randomSkill);
							}
						}, delay);
					}
					startQuestTimer("stage_all_manifestation_of_authority", Rnd.get(68000 + delay, 68000 + delay * 2), world.Istina, null);
				}
			} else if (event.equalsIgnoreCase("stage_all_epic_sealing_energy_task")) {
				if (world.status < 4) {
					int knownPlayers = world.Istina.getKnownList().getKnownPlayers().size();
					int knownChars = world.Istina.getKnownList().getKnownCharacters().size();
					if (knownPlayers > 1 && knownChars - knownPlayers < 20 && world.Istina.isInCombat()) {
						for (int a = 0; a < Rnd.get(1, 3); a++) {
							Npc sealingEnergy = addSpawn(Istina.sealingEnergy,
									world.Istina.getX(),
									world.Istina.getY(),
									world.Istina.getZ(),
									0,
									true,
									180000,
									true,
									world.instanceId);
							sealingEnergy.setInvul(true);
						}
						world.Istina.broadcastPacket(new PlaySound(3, "Npcdialog1.istina_voice_04", 0, 0, 0, 0, 0));
					}
					startQuestTimer("stage_all_epic_sealing_energy_task", Rnd.get(2, 3) * 60000, world.Istina, null);
				}
			} else if (event.equalsIgnoreCase("stage_all_epic_minions_task")) {
				if (world.status < 4) {
					int knownPlayers = world.Istina.getKnownList().getKnownPlayers().size();
					int knownChars = world.Istina.getKnownList().getKnownCharacters().size();
					if (knownPlayers > 1 && knownChars - knownPlayers < 20 && world.Istina.isInCombat()) {
						if (world.Istina.getCurrentHp() < world.Istina.getMaxHp() * 0.75) //only if have less than 75%
						{
							InstanceManager.getInstance()
									.sendPacket(world.instanceId,
											new ExShowScreenMessage(1811144, 0, true, 2000)); //Istina calls her creatures with tremendous anger.

							//Taklacan, Torumba, Dopagen
							for (Location minionLoc : minionLocs) {
								for (int a = 0; a < Rnd.get(2, 3); a++) {
									addSpawn(minionIds[Rnd.get(minionIds.length)],
											minionLoc.getX(),
											minionLoc.getY(),
											minionLoc.getZ(),
											minionLoc.getHeading(),
											false,
											0,
											true,
											world.instanceId);
								}
							}
							world.Istina.broadcastPacket(new PlaySound(3, "Npcdialog1.istina_voice_03", 0, 0, 0, 0, 0));
						}

						//Istina's Creation
						for (int a = 0; a < Rnd.get(3, 5); a++) {
							addSpawn(istinasCreationId,
									world.Istina.getX(),
									world.Istina.getY(),
									world.Istina.getZ(),
									0,
									true,
									0,
									true,
									world.instanceId);
						}
					}
					startQuestTimer("stage_all_epic_minions_task", Rnd.get(2, 3) * 60000, world.Istina, null);
				}
			} else if (event.equalsIgnoreCase("stage_last_spawns")) {
				world.status = 5;

				world.Ballista = addSpawn(ballistaId, -177119, 146889, -11384, 16571, false, 0, false, world.instanceId);
				world.Ballista.disableCoreAI(true);
				world.Ballista.setInvul(true);
				world.Ballista.setParalyzed(true);

				addSpawn(rumieseInnerId, -177028, 146879, -11384, 22754, false, 0, false, world.instanceId);

				//Spam messages
				InstanceManager.getInstance()
						.sendDelayedPacketToInstance(world.instanceId,
								3,
								new ExShowScreenMessage(1,
										-1,
										2,
										0,
										0,
										0,
										0,
										true,
										1000,
										0,
										"After 5 seconds, the charging magic Ballistas starts."));
				InstanceManager.getInstance()
						.sendDelayedPacketToInstance(world.instanceId,
								4,
								new ExShowScreenMessage(1,
										-1,
										2,
										0,
										0,
										0,
										0,
										true,
										1000,
										0,
										"After 4 seconds, the charging magic Ballistas starts."));
				InstanceManager.getInstance()
						.sendDelayedPacketToInstance(world.instanceId,
								5,
								new ExShowScreenMessage(1,
										-1,
										2,
										0,
										0,
										0,
										0,
										true,
										1000,
										0,
										"After 3 seconds, the charging magic Ballistas starts."));
				InstanceManager.getInstance()
						.sendDelayedPacketToInstance(world.instanceId,
								6,
								new ExShowScreenMessage(1,
										-1,
										2,
										0,
										0,
										0,
										0,
										true,
										1000,
										0,
										"After 2 seconds, the charging magic Ballistas starts."));
				InstanceManager.getInstance()
						.sendDelayedPacketToInstance(world.instanceId,
								7,
								new ExShowScreenMessage(1,
										-1,
										2,
										0,
										0,
										0,
										0,
										true,
										1000,
										0,
										"After 1 seconds, the charging magic Ballistas starts."));

				startQuestTimer("stage_last_start_message", 8000, npc, null);
			} else if (event.equalsIgnoreCase("stage_last_start_message")) {
				InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1811172, 0, true, 2000));

				world.status = 6;

				startQuestTimer("stage_last_check_ballista", 1000, npc, null); //1sec
			} else if (event.equalsIgnoreCase("stage_last_check_ballista")) {
				if (world.ballistaSeconds > 0) {
					double calculation = world.currBallistDamage / world.maxBallistDamage * 100;

					InstanceManager.getInstance()
							.sendPacket(world.instanceId, new ExSendUIEvent(2, world.ballistaSeconds, (int) calculation, 1811347));

					world.ballistaSeconds -= 1;

					startQuestTimer("stage_last_check_ballista", 1000, npc, null); //1sec
				} else {
					//End here
					world.status = 7;

					InstanceManager.getInstance().sendPacket(world.instanceId, new ExSendUIEventRemove());

					int time = 0;
					double chanceToKillIstina = world.currBallistDamage * 100 / world.maxBallistDamage;

					if (debug) {
						log.warn(getName() + ": Chance to kill istina: " + chanceToKillIstina);
					}

					if (chanceToKillIstina > 40 && Rnd.get(101) <= chanceToKillIstina)//We want at least 40% on the ballista in order to kill istina
					{
						//Success, kill istina
						world.status = 8;

						time = ScenePlayerDataTable.getInstance().getVideoDuration(32);

						//Let's shot
						InstanceManager.getInstance().showVidToInstance(32, world.instanceId); //End ok
					} else {
						world.status = 9;

						time = ScenePlayerDataTable.getInstance().getVideoDuration(33);

						InstanceManager.getInstance().showVidToInstance(33, world.instanceId); //End fail
					}

					InstanceManager.getInstance()
							.setInstanceReuse(world.instanceId, world.templateId, world.templateId == templates[0] ? false : true);
					InstanceManager.getInstance().finishInstance(world.instanceId, true);

					startQuestTimer("stage_last_boss_drop", time - 1200, npc, null);
				}
			} else if (event.equalsIgnoreCase("stage_all_turnOffCircleEffect")) {
				switch (world.zone) {
					case USE_ZONE_RED:
						InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectBlueCircle, false));
						InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectGreenCircle, false));
						break;

					case USE_ZONE_GREEN:
						InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectBlueCircle, false));
						InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectRedCircle, false));
						break;

					case USE_ZONE_BLUE:
						InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectRedCircle, false));
						InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(effectGreenCircle, false));
						break;
					default:
				}
			} else if (event.equalsIgnoreCase("stage_last_boss_drop")) {
				//Spawn Rumiese
				addSpawn(rumieseInnerId, -177120, 147860, -11388, 49201, false, 0, false, world.instanceId);

				if (world.status == 8) //Only if istina is killed
				{
					Player randomPlayer = null;

					int x = world.allowed.size();
					if (x > 0) {
						for (int i = 0; i < x; i++) {
							int objId = world.allowed.get(Rnd.get(world.allowed.size()));
							randomPlayer = World.getInstance().getPlayer(objId);
							if (randomPlayer != null && randomPlayer.getInstanceId() == world.instanceId) {
								if (debug) {
									log.warn(getName() + ": " + randomPlayer.getName() + " will be used as a killer!");
								}

								world.Istina = addSpawn(world.IstinaId, -177120, 148794, -11229, 49488, false, 0, false, world.instanceId);
								world.Istina.reduceCurrentHp(world.Istina.getMaxHp(), randomPlayer, null);
								world.Istina.deleteMe();
								break;
							}
						}
					}

					if (randomPlayer == null) {
						log.warn(getName() + ": Cant found an instanced player for kill Istina.");
					}
				}
			} else if (event.equalsIgnoreCase("tryGetReward")) {
				if (world.status == 8) {
					synchronized (world.rewardedPlayers) {
						if (InstanceManager.getInstance().canGetUniqueReward(player, world.rewardedPlayers)) {
							world.rewardedPlayers.add(player);

							player.addItem(qn, boxgMagicPower, 1, npc, true);

							if (world.isHardMode) {
								player.addItem(qn, istinaCrystal, 1, npc, true);
							}
						} else {
							player.sendMessage("Nice attempt, but you already got a reward!");
						}
					}
				}
			}
		}

		if (npc != null && npc.getNpcId() == rumieseEnterId && Util.isDigit(event) && Util.contains(templates, Integer.valueOf(event))) {
			try {
				enterInstance(player, Integer.valueOf(event));
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		return null;
	}

	@Override
	public final String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		if (npc == null || attacker == null) {
			return null;
		}

		if (debug) {
			log.warn(getName() + ": onAttack: " + attacker.getName());
		}

		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof IstinaWorld) {
			final IstinaWorld world = (IstinaWorld) tmpWorld;

			if (npc.getNpcId() == world.IstinaId) {
				if (world.status == 3 && npc.getCurrentHp() < npc.getMaxHp() * 0.04) //4%?
				{
					world.status = 4;

					world.zone = zoneInUse.NONE;

					//Delete all spawned npcs before ballista spawn
					InstanceManager.getInstance().despawnAll(world.instanceId);
					InstanceManager.getInstance().showVidToInstance(34, world.instanceId);

					startQuestTimer("stage_last_spawns", ScenePlayerDataTable.getInstance().getVideoDuration(34) + 2000, npc, null);
				}
			} else if (npc.getNpcId() == ballistaId) {
				if (world.status == 6) {
					if (world.currBallistDamage == world.maxBallistDamage) {
						return super.onAttack(npc, attacker, damage, isPet);
					}

					if (world.currBallistDamage + damage > world.maxBallistDamage) {
						world.currBallistDamage = world.maxBallistDamage;
					} else {
						world.currBallistDamage += damage;
					}
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public final String onTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onTalk: " + player.getName());
		}

		if (npc.getNpcId() == rumieseEnterId) {
			return "Rumiese.html";
		}

		return super.onTalk(npc, player);
	}

	@Override
	public String onEnterZone(Creature character, ZoneType zone) {
		if (debug) {
			log.warn(getName() + ": onEnterZone: " + character.getName());
		}

		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(character.getInstanceId());
		if (tmpWorld instanceof IstinaWorld) {
			final IstinaWorld world = (IstinaWorld) tmpWorld;
			if (world.zone == zoneInUse.NONE || !(character instanceof Playable)) {
				return super.onEnterZone(character, zone);
			}

			switch (zone.getId()) {
				case _ZONE_GREEN_ID: //Green, center
					switch (world.zone) {
						case USE_ZONE_BLUE:
						case USE_ZONE_RED:
							world.zoneDebuff.getEffects(character, character);
							break;
						default:
					}
					break;

				case _ZONE_BLUE_ID: //Blue, second
					switch (world.zone) {
						case USE_ZONE_RED:
						case USE_ZONE_GREEN:
							world.zoneDebuff.getEffects(character, character);
							break;
						default:
					}
					break;

				case _ZONE_RED_ID: //Red, last
					switch (world.zone) {
						case USE_ZONE_BLUE:
						case USE_ZONE_GREEN:
							world.zoneDebuff.getEffects(character, character);
							break;
						default:
					}
					break;
				default:
			}
		}
		return super.onEnterZone(character, zone);
	}

	private void setupIDs(IstinaWorld world, int template_id) {
		if (template_id == 170) //Hard
		{
			world.IstinaId = 29196;
			world.maxBallistDamage = 1600000;
			world.zoneDebuff = SkillTable.getInstance().getInfo(14289, 2);
			world.isHardMode = true;
		} else {
			world.IstinaId = 29195;
			world.maxBallistDamage = 800000;
			world.zoneDebuff = SkillTable.getInstance().getInfo(14289, 1);
		}
	}

	private final synchronized void enterInstance(Player player, int template_id) {
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null) {
			if (!(world instanceof IstinaWorld)) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}

			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null) {
				if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId())) {
					player.setInstanceId(world.instanceId);
					player.teleToLocation(-177107, 146576, -11392, true);
				}
			}
			return;
		} else {
			int minPlayers = template_id == 170 ? Config.ISTINA_MIN_PLAYERS : Config.ISTINA_MIN_PLAYERS / 2;
			int maxLevel = template_id == 170 ? Config.MAX_LEVEL : 99;
			if (!debug && !InstanceManager.getInstance().checkInstanceConditions(player, template_id, minPlayers, 35, 92, maxLevel)) {
				return;
			}

			final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");
			world = new IstinaWorld();
			world.instanceId = instanceId;
			world.templateId = template_id;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			setupIDs((IstinaWorld) world, template_id);

			List<Player> allPlayers = new ArrayList<Player>();
			if (debug) {
				allPlayers.add(player);
			} else {
				allPlayers.addAll(minPlayers > Config.MAX_MEMBERS_IN_PARTY ? player.getParty().getCommandChannel().getMembers() :
						player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel().getMembers() :
								player.getParty().getPartyMembers());
			}

			for (Player enterPlayer : allPlayers) {
				if (enterPlayer == null) {
					continue;
				}

				world.allowed.add(enterPlayer.getObjectId());

				if (enterPlayer.getInventory().getItemByItemId(energyDevice) == null) {
					enterPlayer.addItem(qn, energyDevice, 1, enterPlayer, true);
				}

				enterPlayer.stopAllEffectsExceptThoseThatLastThroughDeath();
				enterPlayer.setInstanceId(instanceId);
				enterPlayer.teleToLocation(playerEnter[Rnd.get(0, playerEnter.length - 1)], true);
			}

			startQuestTimer("stage_0_open_doors", 5000, null, player);

			log.debug(getName() + ": [" + template_id + "] instance started: " + instanceId + " created by player: " + player.getName());
			return;
		}
	}

	private void revalidateZone(IstinaWorld world) {
		for (int objId : world.allowed) {
			Player player = World.getInstance().getPlayer(objId);
			if (player != null && player.isOnline() && player.getInstanceId() == world.instanceId) {
				if (_ZONE_BLUE.isCharacterInZone(player)) {
					notifyEnterZone(player, _ZONE_BLUE);
				} else if (_ZONE_GREEN.isCharacterInZone(player)) {
					notifyEnterZone(player, _ZONE_GREEN);
				} else if (_ZONE_RED.isCharacterInZone(player)) {
					notifyEnterZone(player, _ZONE_RED);
				}
			}
		}
	}

	public static void main(String[] args) {
		new Istina(-1, qn, "instances/GrandBosses");
	}
}
