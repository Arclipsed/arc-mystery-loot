package arc.mysteryloot.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

import arc.mysteryloot.commands.table.MysteryLootTableCommands;
import arc.mysteryloot.managers.MysteryLootCommandPermissionManager;

public class MysteryLootCommands extends AbstractCommandCollection {

  public MysteryLootCommands() {
    super("mysteryloot", "Mystery Loot Plugin");
    requirePermission(MysteryLootCommandPermissionManager.AdminCommandPermissions.MYSTERYLOOT_ADMIN);

    addSubCommand(new MysteryLootTableCommands());
  }
}

