package arc.mysteryloot.managers;

import java.util.Set;

import arc.core.managers.PermissionManager;

public class MysteryLootCommandPermissionManager {
  public class PermissionGroups {
    public static final String User = "Arc.MysteryLoot.Command.User";
    public static final String Admin = "Arc.MysteryLoot.Command.Admin";
  }

  public class AdminCommandPermissions {
    public static final String MYSTERYLOOT_ADMIN = "arc.mysteryloot.command.admin";
  }

  public class UserCommandPermissions {
  }

  public MysteryLootCommandPermissionManager() {}

  public static void InitUserGroupPermissions() {
  }

  public static void InitAdminGroupPermissions() {
    var initAdminPermissions = Set.of(
      AdminCommandPermissions.MYSTERYLOOT_ADMIN
    );

    PermissionManager.AddPermisionToGroup(PermissionGroups.Admin, initAdminPermissions);
  }
}
