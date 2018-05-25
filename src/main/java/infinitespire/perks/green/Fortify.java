package infinitespire.perks.green;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.DexterityPower;

import infinitespire.perks.AbstractPerk;

public class Fortify extends AbstractPerk{
	private static final String NAME = "Fortify";
    private static final String ID = "Fortify";
    private static final String DESCRIPTION = "At the start of combat, gain 2 Dexterity.";
    private static final int TIER = 0;
    private static final PerkTreeColor TREE_COLOR = PerkTreeColor.RED;
    
    
    public Fortify() {
        super(NAME, ID, DESCRIPTION, TIER, TREE_COLOR);
    }
    
    @Override
    public void onCombatStart() {
        AbstractPlayer player = AbstractDungeon.player;
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(player, player, new DexterityPower(player, 2), 2));
    }
}
