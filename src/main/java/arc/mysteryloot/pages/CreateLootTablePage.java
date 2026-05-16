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

public class CreateLootTablePage extends InteractiveCustomUIPage<CreateLootTablePage.CreateLootTablePageEventData> {

  @Nonnull
  private String TableName = "";

  public CreateLootTablePage(@Nonnull PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismiss, CreateLootTablePageEventData.CODEC);
  }

  @Override
  public void build(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull UICommandBuilder cmd,
    @Nonnull UIEventBuilder events,
    @Nonnull Store<EntityStore> store
  ) {
    cmd.append("MysteryLoot/Pages/CreateLootTablePage.ui");

    cmd.set("#CreateButton.Disabled", true);

    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#CreateButton", EventData.of("Action", "Create"), false);

    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TableNameInput",
      new EventData().append("Action", "NameChanged").append("@InputText", "#TableNameInput.Value"), false);
  }

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull CreateLootTablePageEventData data
  ) {
    if (data.Action == null) return;

    switch (data.Action) {
      case "Close":
        this.close();
        break;

      case "NameChanged":
        TableName = data.InputText != null ? data.InputText.trim() : "";
        UICommandBuilder stateCmd = new UICommandBuilder();
        stateCmd.set("#CreateButton.Disabled", TableName.isEmpty());
        sendUpdate(stateCmd);
        break;

      case "Create":
        if (TableName.isEmpty()) break;
        TableName = TableName.trim().replace(" ", "_");
        boolean created = MysteryLootPlugin.INSTANCE.Manager.CreateMysteryLootTable(TableName);
        if (created) {
          this.close();
        } else {
          UICommandBuilder errorCmd = new UICommandBuilder();
          errorCmd.set("#ErrorLabel.Visible", true);
          errorCmd.set("#ErrorLabel.Text", "A table with that name already exists!");
          sendUpdate(errorCmd);
        }
        break;
    }
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
  }

  public static class CreateLootTablePageEventData {
    @Nullable public String Action;
    @Nullable public String InputText;

    public CreateLootTablePageEventData() {}

    @Nonnull
    public static final BuilderCodec<CreateLootTablePageEventData> CODEC = BuilderCodec
      .builder(CreateLootTablePageEventData.class, CreateLootTablePageEventData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.Action = v, d -> d.Action).add()
      .append(new KeyedCodec<>("@InputText", Codec.STRING), (d, v) -> d.InputText = v, d -> d.InputText).add()
      .build();
  }
}
