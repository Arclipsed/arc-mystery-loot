package arc.mysteryloot;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import arc.mysteryloot.configs.MysteryLootTablesConfig;
import arc.mysteryloot.managers.MysteryLootManager;
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

    this.Manager = new MysteryLootManager();
  }

  @Override
  protected void start() {
    return;
  }
}