package arc.mysteryloot.interactions;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import arc.core.components.Msg;
import arc.core.loggers.Logger;
import arc.core.managers.NotificationManger;
import arc.mysteryloot.MysteryLootPlugin;


public class OpenMysteryLootTableInteraction extends SimpleInstantInteraction {
  @Nonnull
  public static final String InteractionId = "Open_MysteryLoot_Table_Interaction";

  @Nonnull
  public String TableId = null;

  @Override
  protected void firstRun(
    @Nonnull InteractionType interactionType,
    @Nonnull InteractionContext interactionContext,
    @Nonnull CooldownHandler cooldownHandler
  ) {
    String resolvedTableId = this.TableId;
    if (resolvedTableId == null || resolvedTableId.isEmpty()) {
      resolvedTableId = interactionContext.getInteractionVars() != null ? 
        interactionContext.getInteractionVars().get("TableId") : null;
    }
    
    if (resolvedTableId == null || resolvedTableId.isEmpty()) {
      cancel(interactionContext);
      return;
    }

    var rootInteractionId = interactionContext.getRootInteractionId(interactionType);
    var rootInteraction = RootInteraction.getAssetMap().getAsset(rootInteractionId);
    if (rootInteraction == null) { cancel(interactionContext); return; }

    var commandBuffer = interactionContext.getCommandBuffer();
    if (commandBuffer == null) { cancel(interactionContext); return; }

    var ref = interactionContext.getEntity();
    if (ref == null || !ref.isValid()) { cancel(interactionContext); return; }

    var playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef == null || !playerRef.isValid()) { cancel(interactionContext); return; }

    var player = commandBuffer.getComponent(ref, Player.getComponentType());
    if (player == null) {
      cancel(interactionContext);
      return;
    };
   
    if (MysteryLootPlugin.INSTANCE.Manager.GetLootTable(resolvedTableId) == null) {
      NotificationManger.SendToPlayer(
        playerRef,
        new Msg().Translation("Arc.MysteryLoot.Table.NotFound.Title"),
        new Msg().Translation("Arc.MysteryLoot.Table.NotFound.Description").Param("tableId", resolvedTableId),
        NotificationStyle.Danger
      );
      cancel(interactionContext);
      return;
    }

    final var store    = commandBuffer.getStore();
    final var world    = store.getExternalData().getWorld();
    final String finalTableId = resolvedTableId;

    world.execute(() -> {
      MysteryLootPlugin.INSTANCE.Manager.UI.OpenMysteryLootTablePage(ref, store, finalTableId);
    });
    
  }

  protected void cancel(@Nonnull InteractionContext interactionContext) {
    interactionContext.getState().state = InteractionState.Failed;
  }

  @Nonnull
  public static final BuilderCodec<OpenMysteryLootTableInteraction> CODEC = BuilderCodec.builder(
    OpenMysteryLootTableInteraction.class,
    OpenMysteryLootTableInteraction::new,
    OpenMysteryLootTableInteraction.CODEC
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
