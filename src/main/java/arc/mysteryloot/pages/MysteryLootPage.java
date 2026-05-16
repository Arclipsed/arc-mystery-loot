package arc.mysteryloot.pages;

import java.util.ArrayList;
import java.util.List;

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
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.MysteryLootPlugin;
import arc.mysteryloot.classes.MysteryLootTable;
import arc.mysteryloot.managers.MysteryLootManager;

public class MysteryLootPage extends InteractiveCustomUIPage<MysteryLootPage.MysteryLootPageEventData> {

  @Nonnull
  private final MysteryLootManager Manager;

  @Nullable
  private String SelectedTableId;
  @Nonnull
  private String EditingTableName = "";
  @Nonnull
  private MysteryLootTable EditingTable = new MysteryLootTable();
  @Nullable
  private MysteryLootTable SavedTable = null;
  private boolean IsCreatingNew = false;

  public MysteryLootPage(
    @Nonnull PlayerRef playerRef,
    @Nullable String initialTableId
  ) {
    super(playerRef, CustomPageLifetime.CanDismiss, MysteryLootPageEventData.CODEC);
    this.Manager = MysteryLootPlugin.INSTANCE.Manager;

    var tables = Manager.GetAllLootTables();

    String targetId = (initialTableId != null && tables.containsKey(initialTableId))
      ? initialTableId
      : (!tables.isEmpty() ? tables.keySet().iterator().next() : null);

    if (targetId != null) {
      SelectedTableId = targetId;
      EditingTableName = targetId;
      EditingTable = new MysteryLootTable(tables.get(targetId));
      SavedTable = new MysteryLootTable(tables.get(targetId));
    }
  }

  @Override
  public void build(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull UICommandBuilder cmd,
    @Nonnull UIEventBuilder events,
    @Nonnull Store<EntityStore> store
  ) {
    cmd.append("MysteryLoot/Pages/MysteryLootPage.ui");

    populateTableList(cmd);
    loadForm(cmd);
    updateSaveButtonState(cmd);

    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#DeleteTableButton", EventData.of("Action", "DeleteTable"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveTableButton", EventData.of("Action", "SaveTable"), false);

    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TableListDropdown",
      new EventData().append("Action", "SelectTable").append("@SelectedType", "#TableListDropdown.Value"), false);

    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TableNameInput",
      new EventData().append("Action", "TableNameChanged").append("@InputText", "#TableNameInput.Value"), false);
  }

  private void populateTableList(UICommandBuilder cmd) {
    var tables = Manager.GetAllLootTables();
    List<DropdownEntryInfo> entries = new ArrayList<>();
    entries.add(new DropdownEntryInfo(LocalizableString.fromString("-- Create New --"), ""));
    tables.keySet().stream().sorted().forEach(key ->
      entries.add(new DropdownEntryInfo(LocalizableString.fromString(key), key))
    );
    cmd.set("#TableListDropdown.Entries", entries);
    cmd.set("#TableListDropdown.Value", SelectedTableId != null ? SelectedTableId : "");
    cmd.set("#DeleteTableButton.Visible", SelectedTableId != null);
  }

  private void loadForm(UICommandBuilder cmd) {
    cmd.set("#TableNameInput.Value", EditingTableName);
  }

  private void updateSaveButtonState(UICommandBuilder cmd) {
    boolean disabled;
    if (IsCreatingNew || SavedTable == null) {
      disabled = EditingTableName == null || EditingTableName.isEmpty();
    } else {
      boolean nameChanged = !EditingTableName.equals(SelectedTableId);
      disabled = EditingTableName == null || EditingTableName.isEmpty();
    }
    cmd.set("#SaveTableButton.Disabled", disabled);
  }

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull MysteryLootPageEventData data
  ) {
    if (data.Action == null) return;

    switch (data.Action) {
      case "Close":
        this.close();
        break;

      case "SelectTable":
        var tables = Manager.GetAllLootTables();
        if (data.SelectedType == null || data.SelectedType.isEmpty()) {
          IsCreatingNew = true;
          SelectedTableId = null;
          EditingTableName = "";
          EditingTable = new MysteryLootTable();
          SavedTable = null;
          UICommandBuilder clearCmd = new UICommandBuilder();
          clearCmd.set("#DeleteTableButton.Visible", false);
          loadForm(clearCmd);
          updateSaveButtonState(clearCmd);
          sendUpdate(clearCmd);
          break;
        }
        var selected = tables.get(data.SelectedType);
        if (selected == null) break;
        IsCreatingNew = false;
        SelectedTableId = data.SelectedType;
        EditingTableName = data.SelectedType;
        EditingTable = new MysteryLootTable(selected);
        SavedTable = new MysteryLootTable(selected);
        UICommandBuilder selectCmd = new UICommandBuilder();
        selectCmd.set("#DeleteTableButton.Visible", true);
        loadForm(selectCmd);
        updateSaveButtonState(selectCmd);
        sendUpdate(selectCmd);
        break;

      case "DeleteTable":
        if (SelectedTableId == null) break;
        Manager.DeleteMysteryLootTable(SelectedTableId);
        var remaining = Manager.GetAllLootTables();
        SelectedTableId = remaining.isEmpty() ? null : remaining.keySet().iterator().next();
        EditingTableName = SelectedTableId != null ? SelectedTableId : "";
        EditingTable = SelectedTableId != null ? new MysteryLootTable(remaining.get(SelectedTableId)) : new MysteryLootTable();
        SavedTable = SelectedTableId != null ? new MysteryLootTable(EditingTable) : null;
        IsCreatingNew = false;
        UICommandBuilder deleteCmd = new UICommandBuilder();
        populateTableList(deleteCmd);
        loadForm(deleteCmd);
        updateSaveButtonState(deleteCmd);
        sendUpdate(deleteCmd);
        break;

      case "SaveTable":
        if (EditingTableName == null || EditingTableName.isEmpty()) break;
        EditingTableName = EditingTableName.trim().replace(" ", "_");
        
        if (!IsCreatingNew && SelectedTableId != null && !EditingTableName.equals(SelectedTableId)) {
          Manager.RenameMysteryLootTable(SelectedTableId, EditingTableName, EditingTable);
        } else {
          if (IsCreatingNew) {
            Manager.CreateMysteryLootTable(EditingTableName);
          }
          Manager.UpdateMysteryLootTable(EditingTableName, EditingTable);
        }
        
        SelectedTableId = EditingTableName;
        IsCreatingNew = false;
        SavedTable = new MysteryLootTable(EditingTable);
        UICommandBuilder saveCmd = new UICommandBuilder();
        saveCmd.set("#TableNameInput.Value", EditingTableName);
        saveCmd.set("#DeleteTableButton.Visible", true);
        populateTableList(saveCmd);
        updateSaveButtonState(saveCmd);
        sendUpdate(saveCmd);
        break;

      case "TableNameChanged":
        EditingTableName = data.InputText != null ? data.InputText.trim() : "";
        break;
    }

    UICommandBuilder stateCmd = new UICommandBuilder();
    updateSaveButtonState(stateCmd);
    sendUpdate(stateCmd);
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
  }

  @Override
  public void close() {
    super.close();
  }

  public static class MysteryLootPageEventData {
    @Nullable public String Action;
    @Nullable public String InputText;
    @Nullable public String SelectedType;

    public MysteryLootPageEventData() {}

    @Nonnull
    public static final BuilderCodec<MysteryLootPageEventData> CODEC = BuilderCodec
      .builder(MysteryLootPageEventData.class, MysteryLootPageEventData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.Action = v, d -> d.Action).add()
      .append(new KeyedCodec<>("@InputText", Codec.STRING), (d, v) -> d.InputText = v, d -> d.InputText).add()
      .append(new KeyedCodec<>("@SelectedType", Codec.STRING), (d, v) -> d.SelectedType = v, d -> d.SelectedType).add()
      .build();
  }
}
