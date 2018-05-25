package infinitespire.perks.red;

import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import infinitespire.perks.AbstractPerk;

public class Strengthen extends AbstractPerk {
	private static final String NAME = "Strengthen";
    private static final String ID = "Strengthen";
    private static final String DESCRIPTION = "At the start of combat, gain 2 Strength.";
    private static final int TIER = 0;
    private static final PerkTreeColor TREE_COLOR = PerkTreeColor.RED;
    
    
    public Strengthen() {
        super(NAME, ID, DESCRIPTION, TIER, TREE_COLOR);
    }
    
    @Override
    public void onCombatStart() {
        AbstractPlayer player = AbstractDungeon.player;
        AbstractDungeon.actionManager.addToBottom(new DrawCardAction(player, 1));
    }
}
