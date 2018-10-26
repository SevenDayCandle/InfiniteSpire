package infinitespire.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.green.Nightmare;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.dungeons.TheBeyond;
import com.megacrit.cardcrawl.map.DungeonMap;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import infinitespire.InfiniteSpire;
import infinitespire.helpers.QuestHelper;
import infinitespire.monsters.LordOfAnnihilation;
import infinitespire.quests.endless.EndlessQuestPart1;
import infinitespire.relics.BottledSoul;
import infinitespire.relics.HolyWater;
import infinitespire.rooms.NightmareEliteRoom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

public class AbstractDungeonPatch {

	public static final String CLS = "com.megacrit.cardcrawl.dungeons.AbstractDungeon";
	
	@SpirePatch(cls = CLS, method="closeCurrentScreen")
	public static class CloseCurrentScreen {
		public static void Prefix() {
			if(AbstractDungeon.screen == ScreenStatePatch.QUEST_LOG_SCREEN) {
					try {
						Method overlayReset = AbstractDungeon.class.getDeclaredMethod("genericScreenOverlayReset");
						overlayReset.setAccessible(true);
						overlayReset.invoke(AbstractDungeon.class);
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				AbstractDungeon.overlayMenu.hideBlackScreen();
				InfiniteSpire.questLogScreen.close();
			}
		}
	}
	
	@SpirePatch(cls = CLS, method = "nextRoomTransition")
	public static class NextRoomTransition {
		@SpireInsertPatch(rloc = 10) 
		public static SpireReturn<?> Insert(AbstractDungeon __instance){
			if(AbstractDungeon.getCurrRoom() instanceof NightmareEliteRoom && AbstractDungeon.eliteMonsterList.size() <= 0) {
				AbstractDungeon.eliteMonsterList.add(Nightmare.ID);
				return SpireReturn.Continue();
			}
			
			return SpireReturn.Continue();
		}
	}

	@SpirePatch(cls = CLS, method = "returnEndRandomRelicKey")
	public static class BottledSoulFilter{
		@SpirePostfixPatch
		public static String Postfix(String retVal, AbstractRelic.RelicTier tier) {
			if(retVal.equals(BottledSoul.ID)) {
				boolean hasExhaust = false;
				for(AbstractCard card : CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck).group) {
					if(card.exhaust){
						hasExhaust = true;
						break;
					}
				}
				if(!hasExhaust) return AbstractDungeon.returnEndRandomRelicKey(tier);

				return retVal;
			}
			return retVal;
		}
	}
	
	@SpirePatch(cls = CLS, method = "render")
	public static class Render {
		
		@SpireInsertPatch(rloc = 102) //112
		public static void Insert(AbstractDungeon __instance, SpriteBatch sb) {
			if(AbstractDungeon.screen == ScreenStatePatch.QUEST_LOG_SCREEN)
				InfiniteSpire.questLogScreen.render(sb);
		}
	}
	
	@SpirePatch(cls = CLS, method = "update")
	public static class Update {
		
		@SpireInsertPatch(rloc = 94)
		public static void Insert(AbstractDungeon __instance) {
			if(AbstractDungeon.screen == ScreenStatePatch.QUEST_LOG_SCREEN)
				InfiniteSpire.questLogScreen.update();
		}
	}

	@SpirePatch(cls = CLS, method = "setBoss")
	public static class SetBoss {

		@SpirePrefixPatch
		public static SpireReturn<?> LordOfAnnihilationSpawner(AbstractDungeon __instance, String key){
			InfiniteSpire.logger.info("bossCount: " + AbstractDungeon.bossCount);
			if(AbstractDungeon.bossCount >= 6 && AbstractDungeon.id.equals(TheBeyond.ID)){
				DungeonMap.boss = InfiniteSpire.getTexture("img/infinitespire/ui/map/bossIcon.png");
				DungeonMap.bossOutline = InfiniteSpire.getTexture("img/infinitespire/ui/map/bossIcon-outline.png");
				AbstractDungeon.bossKey = LordOfAnnihilation.ID;

				return SpireReturn.Return(null);
			}
			return SpireReturn.Continue();
		}
	}

	@SpirePatch(cls = "com.megacrit.cardcrawl.dungeons.TheBeyond", method = "initializeBoss")
	public static class InitBoss {

		@SpirePrefixPatch
		public static SpireReturn<Void> LordOfAnnihilationInitBoss(TheBeyond __instance){
			InfiniteSpire.logger.info("bossCount: " + AbstractDungeon.bossCount);
			if(AbstractDungeon.bossCount >= 6 && AbstractDungeon.id.equals(TheBeyond.ID)){
				if(AbstractDungeon.bossCount > 8 && AbstractDungeon.miscRng.randomBoolean(1 / (TheBeyond.bossList.size() + 1))){
					return SpireReturn.Continue();
				}

				TheBeyond.bossList.clear();
				TheBeyond.bossList.add(LordOfAnnihilation.ID);
				return SpireReturn.Return(null);
			}
			return SpireReturn.Continue();
		}
	}

	@SpirePatch(cls = CLS, method = "generateMap")
	public static class GenerateMap {
									//SL:637 = 36 right now
		@SpireInsertPatch(rloc = 37)// after AbstractDungeon.map = (ArrayList<ArrayList<MapRoomNode>>)RoomTypeAssigner.distributeRoomsAcrossMap(AbstractDungeon.mapRng, (ArrayList)AbstractDungeon.map, (ArrayList)roomList);
		public static void Insert() {
			Settings.isEndless = InfiniteSpire.isEndless;
			addHolyWaterToRareRelicPool();
			addInitialQuests();
			//number of nightmares increases with number of bosses beaten (max 3), is increased by 1 if the "kill a nightmare" quest has not been completed or discarded.
			for(int i = 0; i < Math.min(AbstractDungeon.bossCount + ((InfiniteSpire.questLog.get(0) instanceof EndlessQuestPart1) ? 1 : 0), 3); i++) {
				insertNightmareNode();
			}
		}
		
		private static void addInitialQuests() {
			InfiniteSpire.logger.info("adding initial quests");
			if(AbstractDungeon.floorNum <= 1 &&  InfiniteSpire.questLog.isEmpty()) {
				if(InfiniteSpire.startWithEndlessQuest)	InfiniteSpire.questLog.add(new EndlessQuestPart1().createNew());
				InfiniteSpire.questLog.addAll(QuestHelper.getRandomQuests(9));
				InfiniteSpire.questLog.markAllQuestsAsSeen();
				QuestHelper.saveQuestLog();
			}
		}
		
		private static void addHolyWaterToRareRelicPool() {
			AbstractDungeon.rareRelicPool.remove(HolyWater.ID);
			if(AbstractDungeon.bossCount >= 3 && AbstractDungeon.id.equals(Exordium.ID)) {
				AbstractDungeon.rareRelicPool.add(HolyWater.ID);
				Collections.shuffle(AbstractDungeon.rareRelicPool, new java.util.Random(AbstractDungeon.relicRng.randomLong()));
			}
		}
		
		private static void insertNightmareNode() {
			if(AbstractDungeon.bossCount < 1) return;
			
			int rand;
			ArrayList<MapRoomNode> eliteNodes = new ArrayList<MapRoomNode>();
			
			for(ArrayList<MapRoomNode> rows : AbstractDungeon.map) {
				for(MapRoomNode node : rows) {
					if(node.room != null && node.room instanceof MonsterRoomElite) {
						eliteNodes.add(node);
					}
				}
			}
			rand = AbstractDungeon.mapRng.random(eliteNodes.size() - 1);
			eliteNodes.get(rand).setRoom(new NightmareEliteRoom());
		}
	}
}
