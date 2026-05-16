package arc.mysteryloot;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import arc.mysteryloot.configs.MysteryLootTablesConfig;
import arc.mysteryloot.managers.MysteryLootCommandPermissionManager;
import arc.mysteryloot.managers.MysteryLootManager;
import arc.mysteryloot.commands.MysteryLootCommands;
import arc.mysteryloot.components.LootRollViewer;
import arc.mysteryloot.systems.LootRollSystem;
import com.hypixel.hytale.server.core.util.Config;

public class MysteryLootPlugin extends JavaPlugin {
  public static MysteryLootPlugin INSTANCE;
  public final Config<MysteryLootTablesConfig> TablesConfig;

  public MysteryLootManager Manager;

  public MysteryLootPlugin(@Nonnull JavaPluginInit init) {
    super(init);
    INSTANCE = this;
    this.TablesConfig = this.withConfig("Arc-MysteryLoot-Tables", MysteryLootTablesConfig.CODEC);
  }

  @Override
  protected void setup() {
    super.setup();
    TablesConfig.get().Init();
    TablesConfig.save();

    this.Manager = new MysteryLootManager(TablesConfig);

    LootRollViewer.SetComponentType(
      this.getEntityStoreRegistry().registerComponent(LootRollViewer.class, "Arc:LootRollViewer", LootRollViewer.CODEC)
    );
    this.getEntityStoreRegistry().registerSystem(new LootRollSystem());

    this.getCommandRegistry().registerCommand(new MysteryLootCommands());
  }

  @Override
  protected void start() {
    MysteryLootCommandPermissionManager.InitUserGroupPermissions();
    MysteryLootCommandPermissionManager.InitAdminGroupPermissions();
    return;
  }
}