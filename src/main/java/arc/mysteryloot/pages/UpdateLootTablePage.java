package arc.mysteryloot.pages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.MysteryLootPlugin;
import arc.mysteryloot.classes.MysteryLootTable;

public class UpdateLootTablePage extends InteractiveCustomUIPage<UpdateLootTablePage.UpdateLootTablePageEventData> {

  @Nonnull
  private final String TableId;
  @Nonnull
  private String NewName = "";

  public UpdateLootTablePage(@Nonnull PlayerRef playerRef, @Nonnull String tableId) {
    super(playerRef, CustomPageLifetime.CanDismiss, UpdateLootTablePageEventData.CODEC);
    this.TableId = tableId;
    this.NewName = tableId;
  }

  @Override
  public void build(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull UICommandBuilder cmd,
    @Nonnull UIEventBuilder events,
    @Nonnull Store<EntityStore> store
  ) {
    cmd.append("MysteryLoot/Pages/UpdateLootTablePage.ui");

    cmd.set("#TableNameInput.Value", TableId);
    cmd.set("#TableIdLabel.Text", "Editing: " + TableId);
    cmd.set("#SaveButton.Disabled", false);

    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", EventData.of("Action", "Save"), false);

    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TableNameInput",
      new EventData().append("Action", "NameChanged").append("@InputText", "#TableNameInput.Value"), false);
  }

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull UpdateLootTablePageEventData data
  ) {
    if (data.Action == null) return;

    switch (data.Action) {
      case "Close":
        this.close();
        break;

      case "NameChanged":
        NewName = data.InputText != null ? data.InputText.trim() : "";
        UICommandBuilder stateCmd = new UICommandBuilder();
        stateCmd.set("#SaveButton.Disabled", NewName.isEmpty());
        sendUpdate(stateCmd);
        break;

      case "Save":
        if (NewName.isEmpty()) break;
        NewName = NewName.trim().replace(" ", "_");

        if (!NewName.equals(TableId)) {
          MysteryLootTable existing = MysteryLootPlugin.INSTANCE.Manager.GetLootTable(TableId);
          if (existing != null) {
            MysteryLootPlugin.INSTANCE.Manager.RenameMysteryLootTable(TableId, NewName, existing);
          }
        }

        this.close();
        break;
    }
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
  }

  public static class UpdateLootTablePageEventData {
    @Nullable public String Action;
    @Nullable public String InputText;

    public UpdateLootTablePageEventData() {}

    @Nonnull
    public static final BuilderCodec<UpdateLootTablePageEventData> CODEC = BuilderCodec
      .builder(UpdateLootTablePageEventData.class, UpdateLootTablePageEventData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.Action = v, d -> d.Action).add()
      .append(new KeyedCodec<>("@InputText", Codec.STRING), (d, v) -> d.InputText = v, d -> d.InputText).add()
      .build();
  }
}
