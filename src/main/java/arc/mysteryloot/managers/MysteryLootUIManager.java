package arc.mysteryloot.managers;

import javax.annotation.Nonnull;

public class MysteryLootUIManager {
  @Nonnull
  private final MysteryLootManager Manager;

  public MysteryLootUIManager(@Nonnull MysteryLootManager manager) {
    this.Manager = manager;
  }
}
