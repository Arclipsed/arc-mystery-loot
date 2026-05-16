package arc.mysteryloot.commands.table;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

import arc.mysteryloot.managers.MysteryLootCommandPermissionManager;

public class MysteryLootTableCommands extends AbstractCommandCollection {

  public MysteryLootTableCommands() {
    super("table", "Manage Mystery Loot tables");
    requirePermission(MysteryLootCommandPermissionManager.AdminCommandPermissions.MYSTERYLOOT_ADMIN);

    addSubCommand(new CreateTableCommand("create", "Create a new loot table"));
    addSubCommand(new UpdateTableCommand("update", "Update an existing loot table"));
  }
}
