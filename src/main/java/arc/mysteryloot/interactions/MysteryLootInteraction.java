package arc.mysteryloot.interactions;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;


public class MysteryLootInteraction extends SimpleInstantInteraction {
  @Nonnull
  public static final String InteractionId = "MysteryLoot_Interaction";
  @Nonnull
  public static final String CooldownId = "MysteryLoot_Interaction_Cooldown";

  @Nonnull
  public String TableId = null;

  @Override
  protected void firstRun(
    @Nonnull InteractionType interactionType,
    @Nonnull InteractionContext interactionContext,
    @Nonnull CooldownHandler cooldownHandler
  ) {
    if (TableId == null) return;

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
    new KeyedCodec<>("TableId", Codec.STRING),
    (interaction, value) -> interaction.TableId = value != null ? value : "",
    (interaction) -> interaction.TableId,
    (interaction, parent) -> interaction.TableId = parent.TableId != null ? parent.TableId : ""
  )
  .add()
  .build();
}
