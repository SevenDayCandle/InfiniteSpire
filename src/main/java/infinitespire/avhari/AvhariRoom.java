package infinitespire.avhari;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.NlothsGift;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import infinitespire.InfiniteSpire;

public class AvhariRoom extends AbstractRoom {

	public static Vector2 portalPosition = new Vector2((Settings.WIDTH / 3f) * 2f  + 75f * Settings.scale, Settings.HEIGHT / 2f);
	public static Vector2 randRelicPos = new Vector2(830f * Settings.scale,660f * Settings.scale);
	public static Vector2 removeCardPos = new Vector2( 630f * Settings.scale, 660f * Settings.scale);
	public static AvhariHelper.SpinningCardItems cards;
	public static AvhariHelper.SpinningRelicItems relics;
	public static AvhariHelper.RandomRelicItem randRelic;
	public static AvhariHelper.RemoveCardItem removeCard;
	public static final Texture portal = InfiniteSpire.Textures.getUITexture("avhari/portal.png");
	private float portalRotation = 0.0f;
	private static final Avhari avhari = new Avhari(0,0);

	public AvhariRoom() {
		this.phase = RoomPhase.COMPLETE;
		this.mapSymbol = "AVR";
		this.mapImg = ImageMaster.MAP_NODE_MERCHANT;
		this.mapImgOutline = ImageMaster.MAP_NODE_MERCHANT_OUTLINE;
	}

	@Override
	public void onPlayerEntry() {
		cards = new AvhariHelper.SpinningCardItems(portalPosition.x, portalPosition.y);
		relics = new AvhariHelper.SpinningRelicItems(portalPosition.x, portalPosition.y);
		randRelic = new AvhariHelper.RandomRelicItem(randRelicPos);
		removeCard = new AvhariHelper.RemoveCardItem(removeCardPos);
		this.playBGM("SHOP");
		AbstractDungeon.overlayMenu.proceedButton.setLabel(ShopRoom.TEXT[0]);
	}

	@Override
	public AbstractCard.CardRarity getCardRarity(int roll) {
		int rareRate;
		// Start Nloths Gift Effect
		if (AbstractDungeon.player.hasRelic(NlothsGift.ID)) {
			rareRate = (int) (Settings.NORMAL_RARE_DROP_RATE * NlothsGift.MULTIPLIER);
		} else {
			rareRate = Settings.NORMAL_RARE_DROP_RATE;
		}
		// End Nloths Gift Effect

		if (roll < rareRate) {
			return AbstractCard.CardRarity.RARE;
		} else if (roll < Settings.NORMAL_UNCOMMON_DROP_RATE) {
			return AbstractCard.CardRarity.UNCOMMON;
		}
		return AbstractCard.CardRarity.COMMON;

	}

	@Override
	public void render(SpriteBatch sb) {
		super.render(sb);
		renderPortal(sb);
		avhari.render(sb);
		if(cards != null) cards.render(sb);
		if(relics != null) relics.render(sb);
		if(randRelic != null) randRelic.render(sb);
		if(removeCard != null) removeCard.render(sb);
	}

	public void renderPortal(SpriteBatch sb) {
		sb.setColor(Color.WHITE.cpy());
		sb.setBlendFunction(770, 1);
		TextureRegion portalRegion = new TextureRegion(portal);

		sb.draw(
			portalRegion,
			portalPosition.x - portal.getWidth() / 2f,
			portalPosition.y - portal.getHeight() / 2f,
			portal.getWidth() / 2f,
			portal.getHeight() / 2f,
			portal.getWidth(),
			portal.getHeight(),
			Settings.scale,
			Settings.scale,
			portalRotation);
		sb.setBlendFunction(770, 771);
	}

	@Override
	public void update() {
		super.update();
		if(cards != null) cards.update();
		if(relics != null) relics.update();
		if(randRelic != null) randRelic.update();
		if(removeCard != null) removeCard.update();

		portalRotation += 8 * Gdx.graphics.getDeltaTime();

		if(portalRotation >= 360f) {
			portalRotation = 0.0f;
		}
	}
}