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
import arc.mysteryloot.classes.MysteryLootTableItem;

/**
 * Full loot table editor page.
 * Lets admins create a new table, name it, and add/remove items with ItemId, Amount and DropChance.
 */
public class CreateLootTablePage extends InteractiveCustomUIPage<CreateLootTablePage.CreateLootTablePageEventData> {

  @Nonnull
  private String TableName = "";
  @Nonnull
  private MysteryLootTable EditingTable = new MysteryLootTable();

  // Item form state
  @Nonnull  private String ItemFormId = "";
  private int ItemFormAmount = 1;
  private double ItemFormDropChance = 1.0;

  // -1 means we're adding a new item, >= 0 means editing existing
  private int EditingItemIndex = -1;

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

    updateSaveState(cmd);
    updateItemList(cmd);
    loadItemForm(cmd);

    // Top-level buttons
    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#CreateButton", EventData.of("Action", "Create"), false);

    // Table name input
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TableNameInput",
      new EventData().append("Action", "NameChanged").append("@InputText", "#TableNameInput.Value"), false);

    // Item form inputs
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ItemIdInput",
      new EventData().append("Action", "ItemIdChanged").append("@InputText", "#ItemIdInput.Value"), false);
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ItemAmountInput",
      new EventData().append("Action", "ItemAmountChanged").append("@Amount", "#ItemAmountInput.Value"), false);
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ItemDropChanceInput",
      new EventData().append("Action", "ItemDropChanceChanged").append("@Amount", "#ItemDropChanceInput.Value"), false);

    // Item form actions
    events.addEventBinding(CustomUIEventBindingType.Activating, "#AddItemButton", EventData.of("Action", "AddItem"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#RemoveItemButton", EventData.of("Action", "RemoveItem"), false);

    // Item list selection
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ItemListDropdown",
      new EventData().append("Action", "SelectItem").append("@SelectedIndex", "#ItemListDropdown.Value"), false);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private void updateSaveState(UICommandBuilder cmd) {
    boolean canCreate = !TableName.isEmpty();
    cmd.set("#CreateButton.Disabled", !canCreate);
    cmd.set("#ErrorLabel.Visible", false);
  }

  private void updateItemList(UICommandBuilder cmd) {
    List<DropdownEntryInfo> entries = new ArrayList<>();
    entries.add(new DropdownEntryInfo(LocalizableString.fromString("-- Select Item --"), "-1"));
    for (int i = 0; i < EditingTable.Items.size(); i++) {
      MysteryLootTableItem item = EditingTable.Items.get(i);
      String label = item.ItemId + "  x" + item.Amount + "  (" + item.DropChance + "%)";
      entries.add(new DropdownEntryInfo(LocalizableString.fromString(label), String.valueOf(i)));
    }
    cmd.set("#ItemListDropdown.Entries", entries);
    cmd.set("#ItemListDropdown.Value", EditingItemIndex >= 0 ? String.valueOf(EditingItemIndex) : "-1");
    cmd.set("#RemoveItemButton.Visible", EditingItemIndex >= 0);
  }

  private void loadItemForm(UICommandBuilder cmd) {
    cmd.set("#ItemIdInput.Value", ItemFormId);
    cmd.set("#ItemAmountInput.Value", (float) ItemFormAmount);
    cmd.set("#ItemDropChanceInput.Value", (float) ItemFormDropChance);
    boolean canAdd = !ItemFormId.isEmpty();
    cmd.set("#AddItemButton.Disabled", !canAdd);
  }

  // ── Event Handler ──────────────────────────────────────────────────────────

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
        return;

      case "NameChanged":
        TableName = data.InputText != null ? data.InputText.trim() : "";
        break;

      case "ItemIdChanged":
        ItemFormId = data.InputText != null ? data.InputText.trim() : "";
        break;

      case "ItemAmountChanged":
        ItemFormAmount = data.Amount != null ? Math.max(1, data.Amount.intValue()) : 1;
        break;

      case "ItemDropChanceChanged":
        ItemFormDropChance = data.Amount != null ? Math.max(0.0, Math.min(100.0, data.Amount.doubleValue())) : 1.0;
        break;

      case "SelectItem":
        int idx = -1;
        try { idx = data.SelectedIndex != null ? Integer.parseInt(data.SelectedIndex) : -1; } catch (Exception ignored) {}
        EditingItemIndex = (idx >= 0 && idx < EditingTable.Items.size()) ? idx : -1;
        if (EditingItemIndex >= 0) {
          MysteryLootTableItem selected = EditingTable.Items.get(EditingItemIndex);
          ItemFormId = selected.ItemId;
          ItemFormAmount = selected.Amount;
          ItemFormDropChance = selected.DropChance;
        } else {
          ItemFormId = "";
          ItemFormAmount = 1;
          ItemFormDropChance = 1.0;
        }
        break;

      case "AddItem":
        if (ItemFormId.isEmpty()) break;
        MysteryLootTableItem newItem = new MysteryLootTableItem();
        newItem.ItemId = ItemFormId;
        newItem.Amount = ItemFormAmount;
        newItem.DropChance = ItemFormDropChance;
        if (EditingItemIndex >= 0 && EditingItemIndex < EditingTable.Items.size()) {
          EditingTable.Items.set(EditingItemIndex, newItem);
        } else {
          EditingTable.Items.add(newItem);
          EditingItemIndex = EditingTable.Items.size() - 1;
        }
        ItemFormId = "";
        ItemFormAmount = 1;
        ItemFormDropChance = 1.0;
        EditingItemIndex = -1;
        break;

      case "RemoveItem":
        if (EditingItemIndex >= 0 && EditingItemIndex < EditingTable.Items.size()) {
          EditingTable.Items.remove(EditingItemIndex);
          EditingItemIndex = -1;
          ItemFormId = "";
          ItemFormAmount = 1;
          ItemFormDropChance = 1.0;
        }
        break;

      case "Create":
        if (TableName.isEmpty()) break;
        TableName = TableName.trim().replace(" ", "_");
        boolean created = MysteryLootPlugin.INSTANCE.Manager.CreateMysteryLootTable(TableName);
        if (!created) {
          UICommandBuilder errorCmd = new UICommandBuilder();
          errorCmd.set("#ErrorLabel.Visible", true);
          errorCmd.set("#ErrorLabel.Text", "A table named \"" + TableName + "\" already exists!");
          sendUpdate(errorCmd);
          return;
        }
        // Save items into the newly created table
        if (!EditingTable.Items.isEmpty()) {
          MysteryLootPlugin.INSTANCE.Manager.UpdateMysteryLootTable(TableName, EditingTable);
        }
        this.close();
        return;
    }

    UICommandBuilder update = new UICommandBuilder();
    updateSaveState(update);
    updateItemList(update);
    loadItemForm(update);
    sendUpdate(update);
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {}

  // ── Event Data ─────────────────────────────────────────────────────────────

  public static class CreateLootTablePageEventData {
    @Nullable public String Action;
    @Nullable public String InputText;
    @Nullable public String SelectedIndex;
    @Nullable public Float Amount;

    public CreateLootTablePageEventData() {}

    @Nonnull
    public static final BuilderCodec<CreateLootTablePageEventData> CODEC = BuilderCodec
      .builder(CreateLootTablePageEventData.class, CreateLootTablePageEventData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.Action = v, d -> d.Action).add()
      .append(new KeyedCodec<>("@InputText", Codec.STRING), (d, v) -> d.InputText = v, d -> d.InputText).add()
      .append(new KeyedCodec<>("@SelectedIndex", Codec.STRING), (d, v) -> d.SelectedIndex = v, d -> d.SelectedIndex).add()
      .append(new KeyedCodec<>("@Amount", Codec.FLOAT), (d, v) -> d.Amount = v, d -> d.Amount).add()
      .build();
  }
}
