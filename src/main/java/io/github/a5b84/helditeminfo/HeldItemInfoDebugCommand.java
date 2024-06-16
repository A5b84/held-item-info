package io.github.a5b84.helditeminfo;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class HeldItemInfoDebugCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("helditeminfo").then(
                literal("give")
                        .executes(HeldItemInfoDebugCommand::executeGive)
                        .then(argument("row", IntegerArgumentType.integer(1, 1000))
                                .executes(HeldItemInfoDebugCommand::executeGiveRow))
                        .then(literal("gen")
                                .executes(HeldItemInfoDebugCommand::executeGenerate))
        ));
    }

    private static int executeGive(CommandContext<FabricClientCommandSource> context) {
        return executeGive(context, 0, Integer.MAX_VALUE);
    }

    private static int executeGiveRow(CommandContext<FabricClientCommandSource> context) {
        int row = IntegerArgumentType.getInteger(context, "row");
        int start = (row - 1) * 9;
        return executeGive(context, start, start + 9);
    }

    private static int executeGive(CommandContext<FabricClientCommandSource> context, int start, int end) {
        String[] items = {
                "minecraft:diamond_sword[minecraft:enchantments={levels: {\"minecraft:looting\": 3, \"minecraft:sharpness\": 5, \"minecraft:fire_aspect\": 2}}]",
                "minecraft:potion[minecraft:potion_contents={potion: \"minecraft:strong_turtle_master\"}]",
                "minecraft:ominous_bottle",
                "minecraft:shulker_box[minecraft:container=[{item: {count: 32, id: \"minecraft:redstone_ore\"}, slot: 0}, {item: {count: 32, id: \"minecraft:stone\"}, slot: 1}, {item: {count: 1, components: {\"minecraft:item_name\": '{\"extra\":[{\"color\":\"#FF00FF\",\"text\":\"def\",\"underlined\":true}],\"text\":\"abc \"}'}, id: \"minecraft:golden_sword\"}, slot: 2}, {item: {count: 32, id: \"minecraft:stone\"}, slot: 3}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 4}, {item: {count: 64, id: \"minecraft:redstone_ore\"}, slot: 5}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 6}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 7}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 8}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 9}, {item: {count: 1, components: {\"minecraft:enchantments\": {levels: {\"minecraft:sharpness\": 5}}}, id: \"minecraft:iron_sword\"}, slot: 10}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 11}, {item: {count: 1, components: {\"minecraft:enchantments\": {levels: {\"minecraft:sharpness\": 5}}}, id: \"minecraft:iron_sword\"}, slot: 12}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 13}, {item: {count: 32, id: \"minecraft:stone\"}, slot: 14}, {item: {count: 64, id: \"minecraft:redstone_ore\"}, slot: 18}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 19}, {item: {count: 64, id: \"minecraft:stone\"}, slot: 20}, {item: {count: 1, components: {\"minecraft:item_name\": '{\"extra\":[{\"color\":\"#FF00FF\",\"text\":\"def\",\"underlined\":true}],\"text\":\"abc \"}'}, id: \"minecraft:golden_sword\"}, slot: 21}, {item: {count: 64, id: \"minecraft:diamond_ore\"}, slot: 22}, {item: {count: 64, id: \"minecraft:diamond_ore\"}, slot: 23}, {item: {count: 64, id: \"minecraft:diamond_ore\"}, slot: 24}]]",
                "minecraft:chiseled_bookshelf[minecraft:container=[{item: {count: 1, id: \"minecraft:book\"}, slot: 0}, {item: {count: 1, id: \"minecraft:book\"}, slot: 2}, {item: {count: 1, id: \"minecraft:book\"}, slot: 4}]]",
                "minecraft:bundle[minecraft:bundle_contents=[{count: 3, id: \"minecraft:gold_block\"}, {count: 2, id: \"minecraft:iron_block\"}]]",
                "minecraft:suspicious_sand[minecraft:block_entity_data={item: {count: 2, id: \"minecraft:gold_block\"}, id: \"minecraft:suspicious_sand\"}]",
                "minecraft:firework_rocket[minecraft:fireworks={explosions: [{fade_colors: [I; 11743532], shape: \"large_ball\", colors: [I; 14188952]}], flight_duration: 2b}]",
                "minecraft:command_block[minecraft:block_entity_data={conditionMet: 0b, auto: 0b, powered: 0b, Command: \"say ハローエブリニャン Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\", id: \"minecraft:command_block\", SuccessCount: 0, TrackOutput: 1b, UpdateLastExecution: 1b}]",
                "minecraft:bee_nest[minecraft:bees=[{min_ticks_in_hive: 200, ticks_in_hive: 0, entity_data: {id: \"minecraft:bee\"}}, {min_ticks_in_hive: 200, ticks_in_hive: 0, entity_data: {id: \"minecraft:bee\"}}, {min_ticks_in_hive: 200, ticks_in_hive: 0, entity_data: {id: \"minecraft:bee\"}}]]",
                "minecraft:crossbow[minecraft:charged_projectiles=[{count: 1, components: {\"minecraft:intangible_projectile\": {}}, id: \"minecraft:spectral_arrow\"}]]",
                "minecraft:book[minecraft:lore=['{\"color\":\"blue\",\"extra\":[\"consectetur adipiscing elit.\"],\"text\":\"Lorem ipsum dolor sit amet, \"}', '{\"italic\":true,\"text\":\"Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\"}'],minecraft:unbreakable={}]",
                "minecraft:acacia_sign[minecraft:block_entity_data={back_text: {has_glowing_text: 1b, color: \"yellow\", messages: ['\"\"', '\"back line 2\"', '\"\"', '{\"color\":\"gold\",\"text\":\"back line 4\"}']}, is_waxed: 0b, id: \"minecraft:sign\", front_text: {has_glowing_text: 0b, color: \"black\", messages: ['\"front line 1\"', '\"\"', '{\"extra\":[{\"bold\":true,\"color\":\"light_purple\",\"text\":\"3\"}],\"text\":\"front line \"}', '\"\"']}}]",
                "minecraft:music_disc_13",
                "minecraft:disc_fragment_5",
                "minecraft:painting[minecraft:entity_data={variant: \"minecraft:kebab\", id: \"minecraft:painting\"}]",
                "minecraft:goat_horn[minecraft:instrument=\"minecraft:ponder_goat_horn\"]",
                "minecraft:written_book[minecraft:written_book_content={generation: 3, title: {raw: \"Title title\"}, author: \"@me\"}]",
                "minecraft:filled_map",
                "minecraft:filled_map[minecraft:map_id=0]",
                "minecraft:creeper_banner_pattern",
                "minecraft:tropical_fish_bucket[minecraft:bucket_entity_data={BucketVariantTag: 185466881}]",
                "minecraft:spawner[minecraft:block_entity_data={SpawnData: {entity: {id: \"minecraft:pig\"}}, id: \"minecraft:spawner\"}]",
                "minecraft:trial_spawner[block_entity_data={id:\"minecraft:trial_spawner\",spawn_data:{entity:{id:\"minecraft:pig\"}}}]",
        };

        ClientPlayNetworkHandler networkHandler = context.getSource().getPlayer().networkHandler;
        end = Math.min(end, items.length);

        for (int i = start; i < end; i++) {
            networkHandler.sendChatCommand("give @s " + items[i]);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int executeGenerate(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        ClientPlayerEntity player = source.getPlayer();
        ItemStack stack = player.getMainHandStack();
        NbtCompound nbt = (NbtCompound) stack.encode(player.getRegistryManager());
        MutableText result = Text.literal(nbt.getString("id"));
        NbtCompound components = nbt.getCompound("components");

        if (!components.isEmpty()) {
            boolean isFirstComponent = true;

            result.append("[");

            for (String key : components.getKeys()) {
                NbtElement component = components.get(key);
                result.append((isFirstComponent ? "" : ",") + key + "=");
                result.append(NbtHelper.toPrettyPrintedText(component));
                isFirstComponent = false;
            }

            result.append("]");
        }

        source.sendFeedback(result);
        return Command.SINGLE_SUCCESS;
    }
}
