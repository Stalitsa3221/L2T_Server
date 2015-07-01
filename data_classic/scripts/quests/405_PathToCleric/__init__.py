# Made by Mr. Have fun! Version 0.2
# Shadow Weapon Coupons contributed by BiTi for the Official L2J Datapack Project
# Visit http://www.l2jdp.com/forum/ for more details
import sys
from l2server.gameserver.model.quest import State
from l2server.gameserver.model.quest import QuestState
from l2server.gameserver.model.quest.jython import QuestJython as JQuest
from l2server.gameserver.network.serverpackets import SocialAction

qn = "405_PathToCleric"

LETTER_OF_ORDER1 = 1191
LETTER_OF_ORDER2 = 1192
BOOK_OF_LEMONIELL = 1193
BOOK_OF_VIVI = 1194
BOOK_OF_SIMLON = 1195
BOOK_OF_PRAGA = 1196
CERTIFICATE_OF_GALLINT = 1197
PENDANT_OF_MOTHER = 1198
NECKLACE_OF_MOTHER = 1199
LEMONIELLS_COVENANT = 1200
MARK_OF_FAITH = 1201

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = range(1191,1201)

 def onEvent (self,event,st) :
    htmltext = event
    level = st.getPlayer().getLevel()
    classId = st.getPlayer().getClassId()
    if event == "1" :
        st.set("id","0")
        if level >= 18 and classId == 0x0a and st.getQuestItemsCount(MARK_OF_FAITH) == 0 :
          st.set("cond","1")
          st.setState(State.STARTED)
          st.playSound("ItemSound.quest_accept")
          st.giveItems(LETTER_OF_ORDER1,1)
          htmltext = "30022-05.htm"
        elif classId != 0x0a :
            if classId == 0x0f :
              htmltext = "30022-02a.htm"
            else:
              htmltext = "30022-02.htm"
        elif level<18 and classId == 0x0a :
            htmltext = "30022-03.htm"
        elif level >= 18 and classId == 0x0a and st.getQuestItemsCount(MARK_OF_FAITH) == 1 :
            htmltext = "30022-04.htm"
    return htmltext

 def onTalk (self,npc,player):
   htmltext = Quest.getNoQuestMsg(player)
   st = player.getQuestState(qn)
   if not st : return htmltext

   npcId = npc.getNpcId()
   id = st.getState()
   if npcId != 30022 and id != State.STARTED : return htmltext

   npcId = npc.getNpcId()
   id = st.getState()
   if npcId == 30022 and st.getInt("cond")==0 :
      if st.getQuestItemsCount(MARK_OF_FAITH) == 0 :
          htmltext = "30022-01.htm"
          return htmltext
      else:
          htmltext = "30022-04.htm"
   elif npcId == 30022 and st.getInt("cond") and st.getQuestItemsCount(LETTER_OF_ORDER2)==1 and st.getQuestItemsCount(LEMONIELLS_COVENANT)==0 :
        htmltext = "30022-07.htm"
   elif npcId == 30022 and st.getInt("cond") and st.getQuestItemsCount(LETTER_OF_ORDER2)==1 and st.getQuestItemsCount(LEMONIELLS_COVENANT)==1 :
        htmltext = "30022-09.htm"
        st.takeItems(LETTER_OF_ORDER2,1)
        st.takeItems(LEMONIELLS_COVENANT,1)
        st.giveItems(MARK_OF_FAITH,1)
        isFinished = st.getGlobalQuestVar("1ClassQuestFinished")
        if isFinished == "" : 
            if player.getLevel() >= 20 :
              st.addExpAndSp(32053, 2315)
            elif player.getLevel() == 19 :
              st.addExpAndSp(45612, 2863)
            else:
              st.addExpAndSp(59172, 3532)
            st.giveItems(57, 16380)
        st.set("cond","0")
        st.exitQuest(False)
        st.saveGlobalQuestVar("1ClassQuestFinished","1")
        st.playSound("ItemSound.quest_finish")
        player.sendPacket(SocialAction(player.getObjectId(),3))
   elif npcId == 30022 and st.getInt("cond") and st.getQuestItemsCount(LETTER_OF_ORDER1)==1 :
        if st.getQuestItemsCount(BOOK_OF_VIVI) == 1 and st.getQuestItemsCount(BOOK_OF_SIMLON)>0 and st.getQuestItemsCount(BOOK_OF_PRAGA) == 1 :
            htmltext = "30022-08.htm"
            st.takeItems(BOOK_OF_PRAGA,1)
            st.takeItems(BOOK_OF_VIVI,1)
            st.takeItems(BOOK_OF_SIMLON,3)
            st.takeItems(LETTER_OF_ORDER1,1)
            st.giveItems(LETTER_OF_ORDER2,1)
            st.set("cond","3")
        else:
            htmltext = "30022-06.htm"
   elif npcId == 30253 and st.getInt("cond") and st.getQuestItemsCount(LETTER_OF_ORDER1)==1 :
        if st.getQuestItemsCount(BOOK_OF_SIMLON) == 0 :
            htmltext = "30253-01.htm"
            st.giveItems(BOOK_OF_SIMLON,3)
        elif st.getQuestItemsCount(BOOK_OF_SIMLON)>0 :
            htmltext = "30253-02.htm"
   elif npcId == 30030 and st.getInt("cond") and st.getQuestItemsCount(LETTER_OF_ORDER1)==1 :
        if st.getQuestItemsCount(BOOK_OF_VIVI) == 0 :
            htmltext = "30030-01.htm"
            st.giveItems(BOOK_OF_VIVI,1)
        elif st.getQuestItemsCount(BOOK_OF_VIVI) == 1 :
            htmltext = "30030-02.htm"
   elif npcId == 30333 and st.getInt("cond") and st.getQuestItemsCount(LETTER_OF_ORDER1)==1 :
        if st.getQuestItemsCount(BOOK_OF_PRAGA) == 0 and st.getQuestItemsCount(NECKLACE_OF_MOTHER) == 0 :
            htmltext = "30333-01.htm"
            st.giveItems(NECKLACE_OF_MOTHER,1)
        elif st.getQuestItemsCount(BOOK_OF_PRAGA) == 0 and st.getQuestItemsCount(NECKLACE_OF_MOTHER) == 1 and st.getQuestItemsCount(PENDANT_OF_MOTHER) == 0 :
            htmltext = "30333-02.htm"
        elif st.getQuestItemsCount(BOOK_OF_PRAGA) == 0 and st.getQuestItemsCount(NECKLACE_OF_MOTHER) == 1 and st.getQuestItemsCount(PENDANT_OF_MOTHER) == 1 :
            htmltext = "30333-03.htm"
            st.takeItems(NECKLACE_OF_MOTHER,1)
            st.takeItems(PENDANT_OF_MOTHER,1)
            st.giveItems(BOOK_OF_PRAGA,1)
            st.set("cond","2")
        elif st.getQuestItemsCount(BOOK_OF_PRAGA)>0 :
            htmltext = "30333-04.htm"
   elif npcId == 30408 and st.getInt("cond") :
        if st.getQuestItemsCount(LETTER_OF_ORDER2) == 0 :
          htmltext = "30408-02.htm"
        elif st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 and st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 0 and st.getQuestItemsCount(LEMONIELLS_COVENANT) == 0 and st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 0 :
            htmltext = "30408-01.htm"
            st.giveItems(BOOK_OF_LEMONIELL,1)
            st.set("cond","4")
        elif st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 and st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 1 and st.getQuestItemsCount(LEMONIELLS_COVENANT) == 0 and st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 0 :
            htmltext = "30408-03.htm"
        elif st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 and st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 0 and st.getQuestItemsCount(LEMONIELLS_COVENANT) == 0 and st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 1 :
            htmltext = "30408-04.htm"
            st.takeItems(CERTIFICATE_OF_GALLINT,1)
            st.giveItems(LEMONIELLS_COVENANT,1)
            st.set("cond","6")
        elif st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 and st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 0 and st.getQuestItemsCount(LEMONIELLS_COVENANT) == 1 and st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 0 :
            htmltext = "30408-05.htm"
   elif npcId == 30017 and st.getInt("cond") and st.getQuestItemsCount(LETTER_OF_ORDER2)==1 and st.getQuestItemsCount(LEMONIELLS_COVENANT)==0 :
        if st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 1 and st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 0 :
            htmltext = "30017-01.htm"
            st.takeItems(BOOK_OF_LEMONIELL,1)
            st.giveItems(CERTIFICATE_OF_GALLINT,1)
            st.set("cond","5")
        elif st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 0 and st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 1 :
            htmltext = "30017-02.htm"
   return htmltext

 def onKill(self,npc,player,isPet):
   st = player.getQuestState(qn)
   if not st : return 
   if st.getState() != State.STARTED : return 
   
   npcId = npc.getNpcId()
   if npcId == 20026 :
        st.set("id","0")
        if st.getInt("cond") and st.getQuestItemsCount(PENDANT_OF_MOTHER) == 0 :
          st.giveItems(PENDANT_OF_MOTHER,1)
          st.playSound("ItemSound.quest_middle")
   elif npcId == 20029 :
        st.set("id","0")
        if st.getInt("cond") and st.getQuestItemsCount(PENDANT_OF_MOTHER) == 0 :
          st.giveItems(PENDANT_OF_MOTHER,1)
          st.playSound("ItemSound.quest_middle")
   return

QUEST       = Quest(405,qn,"Path To Cleric")

QUEST.addStartNpc(30022)

QUEST.addTalkId(30022)

QUEST.addTalkId(30017)
QUEST.addTalkId(30030)
QUEST.addTalkId(30253)
QUEST.addTalkId(30333)
QUEST.addTalkId(30408)

QUEST.addKillId(20026)
QUEST.addKillId(20029)