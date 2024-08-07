package ru.MjKey.sponger;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class Sponger implements ModInitializer {

    private static SpongerConfig config;

    @Override
    public void onInitialize() {
        AutoConfig.register(SpongerConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(SpongerConfig.class).getConfig();
        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
    }

    private void onWorldTick(ServerWorld world) {
        Set<BlockPos> absorbedPositions = new HashSet<>();

        for (ItemEntity item : world.getEntitiesByType(net.minecraft.entity.EntityType.ITEM, entity -> true)) {
            ItemStack stack = item.getStack();
            BlockPos pos = item.getBlockPos();

            if (stack.getItem() == Items.SPONGE && world.getRegistryKey() == World.OVERWORLD && config.enableInOverworld) {
                if (isInWater(world, pos)) {
                    if (absorbWater(world, pos, absorbedPositions)) {
                        convertToWetSponge(world, item, pos);
                    }
                }
            } else if (stack.getItem() == Items.WET_SPONGE && world.getRegistryKey() == World.NETHER && config.enableInNether) {
                convertToDrySponge(world, item, pos);
            }
        }
    }

    private boolean isInWater(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == Blocks.WATER;
    }

    private boolean absorbWater(ServerWorld world, BlockPos center, Set<BlockPos> absorbedPositions) {
        boolean abs = false;
        for (int x = -config.spongeRadius; x <= config.spongeRadius; x++) {
            for (int y = -config.spongeRadius; y <= config.spongeRadius; y++) {
                for (int z = -config.spongeRadius; z <= config.spongeRadius; z++) {
                    if (x*x + y*y + z*z <= config.spongeRadius*config.spongeRadius) {
                        BlockPos pos = center.add(x, y, z);
                        if (!absorbedPositions.contains(pos)) {
                            BlockState state = world.getBlockState(pos);
                            if (state.getBlock() == Blocks.WATER) {
                                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                                absorbedPositions.add(pos);
                                abs = true;
                            }
                        }
                    }
                }
            }
        }
        return abs;
    }

    private void convertToWetSponge(ServerWorld world, ItemEntity item, BlockPos pos) {
        ItemStack stack = item.getStack();
        ItemStack wetSpongeStack = new ItemStack(Items.WET_SPONGE, 1);
        world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), wetSpongeStack));
        stack.decrement(1);
        if (stack.isEmpty()) {
            item.discard();
        } else {
            item.setStack(stack);
        }
    }

    private void convertToDrySponge(ServerWorld world, ItemEntity item, BlockPos pos) {
        ItemStack stack = item.getStack();
        ItemStack drySpongeStack = new ItemStack(Items.SPONGE, 1);
        world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), drySpongeStack));
        stack.decrement(1);
        if (stack.isEmpty()) {
            item.discard();
        } else {
            item.setStack(stack);
        }
    }
}