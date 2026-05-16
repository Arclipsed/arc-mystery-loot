package arc.mysteryloot.commands.table;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.core.components.Msg;
import arc.core.managers.NotificationManger;
import arc.mysteryloot.MysteryLootPlugin;
import arc.mysteryloot.managers.MysteryLootCommandPermissionManager;

public class UpdateTableCommand extends AbstractPlayerCommand {

  private final RequiredArg<String> TableId;

  public UpdateTableCommand(
    @Nonnull String name,
    @Nonnull String description
  ) {
    super(name, description);
    requirePermission(MysteryLootCommandPermissionManager.AdminCommandPermissions.MYSTERYLOOT_ADMIN);
    this.TableId = withRequiredArg("TableId", "The ID of the loot table to preview", ArgTypes.STRING);
  }

  @Override
  protected void execute(
    @Nonnull CommandContext commandContext,
    @Nonnull Store<EntityStore> store,
    @Nonnull Ref<EntityStore> ref,
    @Nonnull PlayerRef playerRef,
    @Nonnull World world
  ) {
    var tableId = this.TableId.get(commandContext);
    if (tableId == null) return;

    if (MysteryLootPlugin.INSTANCE.Manager.GetLootTable(tableId) == null) {
      NotificationManger.SendToPlayer(
        playerRef,
        new Msg().Translation("Arc.MysteryLoot.Table.NotFound.Title"),
        new Msg().Translation("Arc.MysteryLoot.Table.NotFound.Description").Param("tableId", tableId),
        NotificationStyle.Danger
      );
      return;
    }

    MysteryLootPlugin.INSTANCE.Manager.UI.OpenUpdateLootTablePage(ref, store, tableId);
  }
}
