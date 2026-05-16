package arc.mysteryloot.commands.table;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.MysteryLootPlugin;
import arc.mysteryloot.managers.MysteryLootCommandPermissionManager;

public class RollTableCommand extends AbstractPlayerCommand {

  public RollTableCommand() {
    super("roll", "Open the loot table simulator");
    requirePermission(MysteryLootCommandPermissionManager.AdminCommandPermissions.MYSTERYLOOT_ADMIN);
  }

  @Override
  protected void execute(
    @Nonnull CommandContext commandContext,
    @Nonnull Store<EntityStore> store,
    @Nonnull Ref<EntityStore> ref,
    @Nonnull PlayerRef playerRef,
    @Nonnull World world
  ) {
    MysteryLootPlugin.INSTANCE.Manager.UI.OpenRollPage(ref, store, null);
  }
}
