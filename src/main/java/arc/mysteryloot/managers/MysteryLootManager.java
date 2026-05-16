package arc.mysteryloot.managers;

import javax.annotation.Nonnull;

public class MysteryLootManager {
  @Nonnull
  public final MysteryLootUIManager UI;

  public MysteryLootManager() {
    this.UI = new MysteryLootUIManager(this);
  }
}
