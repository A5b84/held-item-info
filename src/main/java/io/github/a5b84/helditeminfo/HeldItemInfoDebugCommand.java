package io.github.a5b84.helditeminfo;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Optional;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.storage.TagValueOutput;

public class HeldItemInfoDebugCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        literal("helditeminfo")
            .then(
                literal("give")
                    .executes(HeldItemInfoDebugCommand::executeGive)
                    .then(
                        argument("row", IntegerArgumentType.integer(1, 1000))
                            .executes(HeldItemInfoDebugCommand::executeGiveRow))
                    .then(literal("gen").executes(HeldItemInfoDebugCommand::executeGenerate)))
            .then(literal("item").executes(HeldItemInfoDebugCommand::executeShowItem))
            .then(literal("trace").executes(HeldItemInfoDebugCommand::executeTraceTooltipLines)));
  }

  private static int executeGive(CommandContext<FabricClientCommandSource> context) {
    return executeGive(context, 0, Integer.MAX_VALUE);
  }

  private static int executeGiveRow(CommandContext<FabricClientCommandSource> context) {
    int row = IntegerArgumentType.getInteger(context, "row");
    int start = (row - 1) * 9;
    return executeGive(context, start, start + 9);
  }

  private static int executeGive(
      CommandContext<FabricClientCommandSource> context, int start, int end) {
    String[] items = {
      "minecraft:diamond_sword[minecraft:enchantments={\"minecraft:sharpness\": 5, \"minecraft:looting\": 3, \"minecraft:fire_aspect\": 2}]",
      "minecraft:potion[minecraft:potion_contents={potion: \"minecraft:strong_turtle_master\"}]",
      "minecraft:ominous_bottle",
      "minecraft:shulker_box[minecraft:container=[{item: {count: 32, id: \"minecraft:redstone_ore\"}, slot: 0}, {item: {count: 32, id: \"minecraft:stone\"}, slot: 1}, {item: {components: {\"minecraft:item_name\": {extra: [{color: \"#FF00FF\", underlined: 1b, text: \"def\"}], text: \"abc \"}}, count: 1, id: \"minecraft:golden_sword\"}, slot: 2}, {item: {count: 32, id: \"minecraft:stone\"}, slot: 3}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 4}, {item: {count: 64, id: \"minecraft:redstone_ore\"}, slot: 5}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 6}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 7}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 8}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 9}, {item: {components: {\"minecraft:enchantments\": {\"minecraft:sharpness\": 5}}, count: 1, id: \"minecraft:iron_sword\"}, slot: 10}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 11}, {item: {components: {\"minecraft:enchantments\": {\"minecraft:sharpness\": 5}}, count: 1, id: \"minecraft:iron_sword\"}, slot: 12}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 13}, {item: {count: 32, id: \"minecraft:stone\"}, slot: 14}, {item: {count: 64, id: \"minecraft:redstone_ore\"}, slot: 18}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 19}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 20}, {item: {components: {\"minecraft:item_name\": {extra: [{color: \"#FF00FF\", underlined: 1b, text: \"def\"}], text: \"abc \"}}, count: 1, id: \"minecraft:golden_sword\"}, slot: 21}, {item: {count: 64, id: \"minecraft:diamond_ore\"}, slot: 22}, {item: {count: 64, id: \"minecraft:diamond_ore\"}, slot: 23}, {item: {count: 64, id: \"minecraft:diamond_ore\"}, slot: 24}]]",
      "minecraft:chiseled_bookshelf[minecraft:container=[{item: {count: 1, id: \"minecraft:book\"}, slot: 0}, {item: {count: 1, id: \"minecraft:book\"}, slot: 2}, {item: {count: 1, id: \"minecraft:book\"}, slot: 4}]]",
      "minecraft:bundle[minecraft:bundle_contents=[{count: 3, id: \"minecraft:gold_block\"}, {count: 2, id: \"minecraft:iron_block\"}]]",
      "minecraft:suspicious_sand[minecraft:block_entity_data={id: \"brushable_block\", item: {count: 2, id: \"minecraft:gold_block\"}}]",
      "minecraft:firework_rocket[minecraft:fireworks={explosions: [{fade_colors: [I; 11743532], shape: \"large_ball\", colors: [I; 14188952]}], flight_duration: 2b}]",
      "minecraft:command_block[minecraft:block_entity_data={conditionMet: 0b, auto: 0b, powered: 0b, Command: \"say ハローエブリニャン Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\", id: \"minecraft:command_block\", SuccessCount: 0, TrackOutput: 1b, UpdateLastExecution: 1b}]",
      "minecraft:bee_nest[minecraft:bees=[{ticks_in_hive: 0, entity_data: {id: \"minecraft:bee\"}, min_ticks_in_hive: 200}],minecraft:block_state={honey_level: \"3\"}]",
      "minecraft:crossbow[minecraft:charged_projectiles=[{count: 1, components: {\"minecraft:intangible_projectile\": {}}, id: \"minecraft:spectral_arrow\"}]]",
      "minecraft:book[minecraft:lore=[{color: \"blue\", extra: [\"consectetur adipiscing elit.\"], text: \"Lorem ipsum dolor sit amet, \"}, {text: \"Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\", italic: 1b}],minecraft:unbreakable={}]",
      "minecraft:acacia_sign[minecraft:block_entity_data={back_text: {has_glowing_text: 1b, color: \"yellow\", messages: [\"\", \"back line 2\", \"\", {color: \"gold\", text: \"back line 4\"}]}, is_waxed: 0b, id: \"minecraft:sign\", front_text: {has_glowing_text: 0b, color: \"black\", messages: [\"front line 1\", \"\", {extra: [{color: \"light_purple\", bold: 1b, text: \"3\"}], text: \"front line \"}, \"\"]}}]",
      "minecraft:music_disc_13",
      "minecraft:disc_fragment_5",
      "minecraft:painting[minecraft:painting/variant=\"minecraft:kebab\"]",
      "minecraft:goat_horn[minecraft:instrument=\"minecraft:ponder_goat_horn\"]",
      "minecraft:written_book[minecraft:written_book_content={generation: 3, title: {raw: \"Title title\"}, author: \"@me\"}]",
      "minecraft:filled_map[minecraft:map_id=0]",
      "minecraft:tropical_fish_bucket[minecraft:tropical_fish/pattern_color=\"blue\",minecraft:tropical_fish/base_color=\"red\",minecraft:tropical_fish/pattern=\"flopper\"]",
      "minecraft:spawner[minecraft:block_entity_data={SpawnData: {entity: {id: \"minecraft:pig\"}}, id: \"minecraft:mob_spawner\"}]",
      "minecraft:trial_spawner[block_entity_data={id:\"minecraft:trial_spawner\",spawn_data:{entity:{id:\"minecraft:pig\"}}}]",
    };

    ClientPacketListener networkHandler = context.getSource().getPlayer().connection;
    end = Math.min(end, items.length);

    for (int i = start; i < end; i++) {
      networkHandler.sendCommand("give @s " + items[i]);
    }

    return Command.SINGLE_SUCCESS;
  }

  private static int executeGenerate(CommandContext<FabricClientCommandSource> context) {
    FabricClientCommandSource source = context.getSource();
    LocalPlayer player = source.getPlayer();
    ItemStack stack = player.getMainHandItem();
    CompoundTag nbt = toNbt(stack, player.registryAccess());
    MutableComponent result = Component.literal(nbt.getString("id").orElseThrow());
    Optional<CompoundTag> components = nbt.getCompound("components");

    if (components.isPresent()) {
      boolean isFirstComponent = true;

      result.append("[");

      for (String key : components.get().keySet()) {
        Tag component = components.get().get(key);
        result.append((isFirstComponent ? "" : ",") + key + "=");
        result.append(NbtUtils.toPrettyComponent(component));
        isFirstComponent = false;
      }

      result.append("]");
    }

    source.sendFeedback(result);
    return Command.SINGLE_SUCCESS;
  }

  private static CompoundTag toNbt(ItemStack stack, Provider registries) {
    try (ProblemReporter.ScopedCollector reporter =
        new ProblemReporter.ScopedCollector(HeldItemInfo.LOGGER)) {
      TagValueOutput view = TagValueOutput.createWithContext(reporter, registries);
      view.store("stack", ItemStack.CODEC, stack);
      return view.buildResult().getCompound("stack").orElseThrow();
    }
  }

  private static int executeShowItem(CommandContext<FabricClientCommandSource> context) {
    FabricClientCommandSource source = context.getSource();
    Item item = source.getPlayer().getMainHandItem().getItem();
    source.sendFeedback(Component.literal(item.getClass().getName()));
    return Command.SINGLE_SUCCESS;
  }

  private static int executeTraceTooltipLines(CommandContext<FabricClientCommandSource> context) {
    FabricClientCommandSource source = context.getSource();
    ItemStack stack = source.getPlayer().getMainHandItem();

    stack.addDetailsToTooltip(
        Item.TooltipContext.of(source.getWorld()),
        TooltipDisplay.DEFAULT,
        source.getPlayer(),
        TooltipFlag.NORMAL,
        line -> {
          source.sendFeedback(line);
          StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
          String endClassName = HeldItemInfoDebugCommand.class.getName();

          for (int i = 2; i < stackTraceElements.length; i++) {
            StackTraceElement element = stackTraceElements[i];
            String className = element.getClassName();
            if (className.equals(endClassName)) {
              break;
            } else {
              className = className.substring(className.lastIndexOf('.') + 1);
              source.sendFeedback(
                  Component.literal(
                      "  at "
                          + className
                          + '.'
                          + element.getMethodName()
                          + ':'
                          + element.getLineNumber()));
            }
          }
        });

    return Command.SINGLE_SUCCESS;
  }
}
