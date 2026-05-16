package arc.mysteryloot.interactions;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;

import arc.mysteryloot.configs.MysteryLootConfig;

public class MysteryLootInteraction extends SimpleInstantInteraction {
  @Nonnull
  public static final String InteractionId = "MysteryLoot_Interaction";
  @Nonnull
  public static final String CooldownId = "MysteryLoot_Interaction_Cooldown";

  @Nonnull
  public MysteryLootConfig Config = new MysteryLootConfig();

  @Override
  protected void firstRun(
    @Nonnull InteractionType interactionType,
    @Nonnull InteractionContext interactionContext,
    @Nonnull CooldownHandler cooldownHandler
  ) {
    // TODO: Implement Mystery Loot interaction logic.
    cancel(interactionContext);
  }

  protected void cancel(@Nonnull InteractionContext interactionContext) {
    interactionContext.getState().state = InteractionState.Failed;
  }

  @Nonnull
  public static final BuilderCodec<MysteryLootInteraction> CODEC = BuilderCodec.builder(
    MysteryLootInteraction.class,
    MysteryLootInteraction::new,
    MysteryLootInteraction.CODEC
  )
  .appendInherited(
    new KeyedCodec<>("Config", MysteryLootConfig.CODEC),
    (interaction, value) -> interaction.Config = value != null ? value : new MysteryLootConfig(),
    (interaction) -> interaction.Config,
    (interaction, parent) -> interaction.Config = parent.Config != null ? new MysteryLootConfig(parent.Config) : new MysteryLootConfig()
  )
  .add()
  .build();
}
