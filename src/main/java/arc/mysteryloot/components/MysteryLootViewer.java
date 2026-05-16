package arc.mysteryloot.components;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Attached to a player while they have a Mystery Loot screen open.
 */
public class MysteryLootViewer implements Component<EntityStore> {

  private static ComponentType<EntityStore, MysteryLootViewer> TYPE;

  public MysteryLootViewer() {}

  public static void SetComponentType(ComponentType<EntityStore, MysteryLootViewer> type) {
    TYPE = type;
  }

  public static ComponentType<EntityStore, MysteryLootViewer> GetComponentType() {
    return TYPE;
  }

  @Override
  public MysteryLootViewer clone() {
    return new MysteryLootViewer();
  }

  @Override
  public String toString() {
    return "MysteryLootViewer{}";
  }

  @Nonnull
  public static final BuilderCodec<MysteryLootViewer> CODEC = BuilderCodec
    .builder(MysteryLootViewer.class, MysteryLootViewer::new)
    .build();
}
