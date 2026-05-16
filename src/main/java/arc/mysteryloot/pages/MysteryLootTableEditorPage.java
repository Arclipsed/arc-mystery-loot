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
 * All-in-one loot table editor.
 * Top dropdown: "-- Create New --" or pick an existing table.
 * Selecting an existing table loads it for editing and shows the Delete button.
 * Save always says "Save Loot Table".
 */
public class MysteryLootTableEditorPage extends InteractiveCustomUIPage<MysteryLootTableEditorPage.MysteryLootTableEditorPageEventData> {

  // null = creating new, non-null = editing existing
  @Nullable private String SelectedTableId;

  @Nonnull private String EditingTableName = "";
  @Nonnull private MysteryLootTable EditingTable = new MysteryLootTable();

  // Saved snapshot for dirty-checking
  @Nonnull private String SavedTableName = "";
  @Nonnull private MysteryLootTable SavedTable = new MysteryLootTable();

  // Item form state
  @Nonnull private String ItemFormId = "";
  private int ItemFormAmount = 1;
  private double ItemFormDropWeight = 1.0;
  // -1 = create new item, >= 0 = editing existing item at index
  private int EditingItemIndex = -1;

  public MysteryLootTableEditorPage(@Nonnull PlayerRef playerRef, @Nullable String initialTableId) {
    super(playerRef, CustomPageLifetime.CanDismiss, MysteryLootTableEditorPageEventData.CODEC);

    var tables = MysteryLootPlugin.INSTANCE.Manager.GetAllLootTables();
    String targetId = (initialTableId != null && tables.containsKey(initialTableId))
      ? initialTableId
      : null; // default to create-new

    if (targetId != null) {
      SelectedTableId = targetId;
      EditingTableName = targetId;
      SavedTableName = targetId;
      MysteryLootTable existing = MysteryLootPlugin.INSTANCE.Manager.GetLootTable(targetId);
      if (existing != null) {
        EditingTable = new MysteryLootTable(existing);
        SavedTable = new MysteryLootTable(existing);
      }
    }
  }

  private boolean isEditing() {
    return SelectedTableId != null;
  }

  // ── Build ──────────────────────────────────────────────────────────────────

  @Override
  public void build(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull UICommandBuilder cmd,
    @Nonnull UIEventBuilder events,
    @Nonnull Store<EntityStore> store
  ) {
    cmd.append("MysteryLoot/Pages/MysteryLootTableEditorPage.ui");

    populateTableDropdown(cmd);
    loadForm(cmd);
    updateItemList(cmd);
    loadItemForm(cmd);

    // Top bar
    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#DeleteTableButton", EventData.of("Action", "DeleteTable"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveTableButton", EventData.of("Action", "SaveTable"), false);

    // Table dropdown
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TableDropdown",
      new EventData().append("Action", "SelectTable").append("@SelectedType", "#TableDropdown.Value"), false);

    // Table name
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TableNameInput",
      new EventData().append("Action", "NameChanged").append("@InputText", "#TableNameInput.Value"), false);

    // Item list
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ItemListDropdown",
      new EventData().append("Action", "SelectItem").append("@SelectedIndex", "#ItemListDropdown.Value"), false);

    // Item form
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ItemIdInput",
      new EventData().append("Action", "ItemIdChanged").append("@InputText", "#ItemIdInput.Value"), false);
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ItemAmountInput",
      new EventData().append("Action", "ItemAmountChanged").append("@Amount", "#ItemAmountInput.Value"), false);
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ItemDropChanceInput",
      new EventData().append("Action", "ItemDropChanceChanged").append("@Amount", "#ItemDropChanceInput.Value"), false);

    // Item actions
    events.addEventBinding(CustomUIEventBindingType.Activating, "#AddItemButton", EventData.of("Action", "AddItem"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#RemoveItemButton", EventData.of("Action", "RemoveItem"), false);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private void populateTableDropdown(UICommandBuilder cmd) {
    var tables = MysteryLootPlugin.INSTANCE.Manager.GetAllLootTables();
    List<DropdownEntryInfo> entries = new ArrayList<>();
    entries.add(new DropdownEntryInfo(LocalizableString.fromString("-- Create New --"), ""));
    tables.keySet().stream().sorted().forEach(key ->
      entries.add(new DropdownEntryInfo(LocalizableString.fromString(key), key))
    );
    cmd.set("#TableDropdown.Entries", entries);
    cmd.set("#TableDropdown.Value", SelectedTableId != null ? SelectedTableId : "");
    cmd.set("#DeleteTableButton.Visible", isEditing());
  }

  private void loadForm(UICommandBuilder cmd) {
    cmd.set("#TableNameInput.Value", EditingTableName);
    cmd.set("#ErrorLabel.Visible", false);
    boolean hasChanges = !EditingTableName.equals(SavedTableName) || !EditingTable.Matches(SavedTable);
    cmd.set("#SaveTableButton.Disabled", EditingTableName.isEmpty() || !hasChanges);
  }

  private void updateItemList(UICommandBuilder cmd) {
    List<DropdownEntryInfo> entries = new ArrayList<>();
    entries.add(new DropdownEntryInfo(LocalizableString.fromString("-- Create Item --"), "-1"));
    double totalWeight = EditingTable.Items.stream().mapToDouble(i -> i.DropWeight).sum();
    for (int i = 0; i < EditingTable.Items.size(); i++) {
      MysteryLootTableItem it = EditingTable.Items.get(i);
      String pct = totalWeight > 0
        ? String.format("%.1f%%", (it.DropWeight / totalWeight) * 100.0)
        : "0%";
      String label = it.ItemId + "  x" + it.Amount + "  (w:" + it.DropWeight + "  →  " + pct + ")";
      entries.add(new DropdownEntryInfo(LocalizableString.fromString(label), String.valueOf(i)));
    }
    cmd.set("#ItemListDropdown.Entries", entries);
    cmd.set("#ItemListDropdown.Value", EditingItemIndex >= 0 ? String.valueOf(EditingItemIndex) : "-1");
    cmd.set("#RemoveItemButton.Visible", EditingItemIndex >= 0);
  }

  private void loadItemForm(UICommandBuilder cmd) {
    cmd.set("#ItemIdInput.Value", ItemFormId);
    cmd.set("#ItemAmountInput.Value", (float) ItemFormAmount);
    cmd.set("#ItemDropChanceInput.Value", (float) ItemFormDropWeight);
    cmd.set("#AddItemButton.Disabled", ItemFormId.isEmpty());
    cmd.set("#AddItemButton.Text", EditingItemIndex >= 0 ? "Update Item" : "Add Item to Table");
  }

  private void resetItemForm() {
    ItemFormId = "";
    ItemFormAmount = 1;
    ItemFormDropWeight = 1.0;
    EditingItemIndex = -1;
  }

  // ── Events ─────────────────────────────────────────────────────────────────

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull MysteryLootTableEditorPageEventData data
  ) {
    if (data.Action == null) return;

    switch (data.Action) {

      case "Close":
        this.close();
        return;

      // ── Table selector ───────────────────────────────────────────────────
      case "SelectTable":
        var tables = MysteryLootPlugin.INSTANCE.Manager.GetAllLootTables();
        if (data.SelectedType == null || data.SelectedType.isEmpty()) {
          // Create-new mode
          SelectedTableId = null;
          EditingTableName = "";
          EditingTable = new MysteryLootTable();
          resetItemForm();
        } else {
          var selected = tables.get(data.SelectedType);
          if (selected == null) break;
          SelectedTableId = data.SelectedType;
          EditingTableName = data.SelectedType;
          SavedTableName = data.SelectedType;
          EditingTable = new MysteryLootTable(selected);
          SavedTable = new MysteryLootTable(selected);
          resetItemForm();
        }
        UICommandBuilder switchCmd = new UICommandBuilder();
        populateTableDropdown(switchCmd);
        loadForm(switchCmd);
        updateItemList(switchCmd);
        loadItemForm(switchCmd);
        sendUpdate(switchCmd);
        return;

      case "DeleteTable":
        if (SelectedTableId == null) break;
        MysteryLootPlugin.INSTANCE.Manager.DeleteMysteryLootTable(SelectedTableId);
        SelectedTableId = null;
        EditingTableName = "";
        SavedTableName = "";
        EditingTable = new MysteryLootTable();
        SavedTable = new MysteryLootTable();
        resetItemForm();
        UICommandBuilder deleteCmd = new UICommandBuilder();
        populateTableDropdown(deleteCmd);
        loadForm(deleteCmd);
        updateItemList(deleteCmd);
        loadItemForm(deleteCmd);
        sendUpdate(deleteCmd);
        return;

      case "SaveTable":
        if (EditingTableName.isEmpty()) break;
        EditingTableName = EditingTableName.trim().replace(" ", "_");
        if (isEditing()) {
          if (!EditingTableName.equals(SelectedTableId)) {
            MysteryLootPlugin.INSTANCE.Manager.RenameMysteryLootTable(SelectedTableId, EditingTableName, EditingTable);
          } else {
            MysteryLootPlugin.INSTANCE.Manager.UpdateMysteryLootTable(EditingTableName, EditingTable);
          }
          SelectedTableId = EditingTableName;
          SavedTableName = EditingTableName;
          SavedTable = new MysteryLootTable(EditingTable);
        } else {
          boolean created = MysteryLootPlugin.INSTANCE.Manager.CreateMysteryLootTable(EditingTableName);
          if (!created) {
            UICommandBuilder errorCmd = new UICommandBuilder();
            errorCmd.set("#ErrorLabel.Visible", true);
            errorCmd.set("#ErrorLabel.Text", "\"" + EditingTableName + "\" already exists!");
            sendUpdate(errorCmd);
            return;
          }
          if (!EditingTable.Items.isEmpty()) {
            MysteryLootPlugin.INSTANCE.Manager.UpdateMysteryLootTable(EditingTableName, EditingTable);
          }
          SelectedTableId = EditingTableName;
          SavedTableName = EditingTableName;
          SavedTable = new MysteryLootTable(EditingTable);
        }
        UICommandBuilder saveCmd = new UICommandBuilder();
        populateTableDropdown(saveCmd);
        loadForm(saveCmd);
        sendUpdate(saveCmd);
        return;

      // ── Table name ───────────────────────────────────────────────────────
      case "NameChanged":
        EditingTableName = data.InputText != null ? data.InputText.trim() : "";
        break;

      // ── Item selector ────────────────────────────────────────────────────
      case "SelectItem":
        int idx = -1;
        try { idx = data.SelectedIndex != null ? Integer.parseInt(data.SelectedIndex) : -1; }
        catch (Exception ignored) {}
        EditingItemIndex = (idx >= 0 && idx < EditingTable.Items.size()) ? idx : -1;
        if (EditingItemIndex >= 0) {
          MysteryLootTableItem sel = EditingTable.Items.get(EditingItemIndex);
          ItemFormId = sel.ItemId;
          ItemFormAmount = sel.Amount;
          ItemFormDropWeight = sel.DropWeight;
        } else {
          resetItemForm();
        }
        break;

      // ── Item form ────────────────────────────────────────────────────────
      case "ItemIdChanged":
        ItemFormId = data.InputText != null ? data.InputText.trim() : "";
        break;

      case "ItemAmountChanged":
        ItemFormAmount = data.Amount != null ? Math.max(1, data.Amount.intValue()) : 1;
        break;

      case "ItemDropChanceChanged":
        ItemFormDropWeight = data.Amount != null
          ? Math.max(0.01, Math.min(100.0, data.Amount.doubleValue()))
          : 1.0;
        break;

      case "AddItem":
        if (ItemFormId.isEmpty()) break;
        MysteryLootTableItem newItem = new MysteryLootTableItem();
        newItem.ItemId = ItemFormId;
        newItem.Amount = ItemFormAmount;
        newItem.DropWeight = ItemFormDropWeight;
        if (EditingItemIndex >= 0 && EditingItemIndex < EditingTable.Items.size()) {
          // Updating existing — stay on the same item
          EditingTable.Items.set(EditingItemIndex, newItem);
        } else {
          // Adding new — select the newly added item
          EditingTable.Items.add(newItem);
          EditingItemIndex = EditingTable.Items.size() - 1;
        }
        break;

      case "RemoveItem":
        if (EditingItemIndex >= 0 && EditingItemIndex < EditingTable.Items.size()) {
          EditingTable.Items.remove(EditingItemIndex);
          resetItemForm();
        }
        break;
    }

    UICommandBuilder update = new UICommandBuilder();
    loadForm(update);
    updateItemList(update);
    loadItemForm(update);
    sendUpdate(update);
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {}

  // ── Event Data ─────────────────────────────────────────────────────────────

  public static class MysteryLootTableEditorPageEventData {
    @Nullable public String Action;
    @Nullable public String InputText;
    @Nullable public String SelectedType;
    @Nullable public String SelectedIndex;
    @Nullable public Float Amount;

    public MysteryLootTableEditorPageEventData() {}

    @Nonnull
    public static final BuilderCodec<MysteryLootTableEditorPageEventData> CODEC = BuilderCodec
      .builder(MysteryLootTableEditorPageEventData.class, MysteryLootTableEditorPageEventData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.Action = v, d -> d.Action).add()
      .append(new KeyedCodec<>("@InputText", Codec.STRING), (d, v) -> d.InputText = v, d -> d.InputText).add()
      .append(new KeyedCodec<>("@SelectedType", Codec.STRING), (d, v) -> d.SelectedType = v, d -> d.SelectedType).add()
      .append(new KeyedCodec<>("@SelectedIndex", Codec.STRING), (d, v) -> d.SelectedIndex = v, d -> d.SelectedIndex).add()
      .append(new KeyedCodec<>("@Amount", Codec.FLOAT), (d, v) -> d.Amount = v, d -> d.Amount).add()
      .build();
  }
}
