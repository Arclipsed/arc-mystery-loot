package arc.mysteryloot.pages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.configs.MysteryLootConfig;

public class MysteryLootPage extends InteractiveCustomUIPage<MysteryLootPage.MysteryLootPageEventData> {

  @Nonnull
  private MysteryLootConfig Config;

  public MysteryLootPage(
    @Nonnull PlayerRef playerRef,
    @Nonnull MysteryLootConfig config
  ) {
    super(playerRef, CustomPageLifetime.CanDismiss, MysteryLootPageEventData.CODEC);
    this.Config = config;
  }

  @Override
  public void build(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull UICommandBuilder cmd,
    @Nonnull UIEventBuilder events,
    @Nonnull Store<EntityStore> store
  ) {
    // TODO: Append the Mystery Loot UI file and set up bindings.
  }

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull MysteryLootPageEventData data
  ) {
    // TODO: Handle UI events from the Mystery Loot page.
    this.close();
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    // TODO: Cleanup on dismiss.
  }

  @Override
  public void close() {
    super.close();
  }

  // â”€â”€ Event data â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  public static class MysteryLootPageEventData {
    @Nullable
    public String Action;

    public MysteryLootPageEventData() {}

    @Nonnull
    public static final BuilderCodec<MysteryLootPageEventData> CODEC = BuilderCodec
      .builder(MysteryLootPageEventData.class, MysteryLootPageEventData::new)
      .append(
        new KeyedCodec<>("Action", Codec.STRING),
        (data, value) -> data.Action = value,
        data -> data.Action
      )
      .add()
      .build();
  }
}
