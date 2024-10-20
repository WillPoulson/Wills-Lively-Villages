package uk.co.willpoulson.willslivelyvillages.mixin;

import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VillagerEntity.class)
public class VillagerHealGolemMixin {

    private long lastHealTime = 0;
    private static final int HEAL_COOLDOWN_TICKS = 6000;
    private IronGolemEntity targetGolem = null;

    @Inject(method = "tick", at = @At("HEAD"))
    public void healNearbyGolem(CallbackInfo info) {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        World world = villager.getWorld();

        // Exit if not on the server or the world is not a ServerWorld
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        if (villager.isBaby() || villager.isSleeping()) {
            return;
        }

        // Check if cooldown has expired
        if (world.getTime() - lastHealTime < HEAL_COOLDOWN_TICKS) {
            return;
        }

        if (targetGolem != null) {
            if (targetGolem.isDead() || targetGolem.getHealth() == targetGolem.getMaxHealth()) {
                targetGolem = null;
            }
        }

        if (targetGolem == null) {
            List<IronGolemEntity> nearbyDamagedGolems = world.getEntitiesByClass(
                IronGolemEntity.class,
                villager.getBoundingBox().expand(10),
                golem -> golem.getHealth() < golem.getMaxHealth()
            );

            if (nearbyDamagedGolems.isEmpty()) {
                return;
            }

            targetGolem = nearbyDamagedGolems.getFirst();
        }

        double distanceToGolem = villager.squaredDistanceTo(targetGolem);

        if (distanceToGolem > 4) {
            villager.getNavigation().startMovingTo(targetGolem, 0.5);
            return;
        }

        targetGolem.heal(5f);
        spawnHealParticles(serverWorld, targetGolem.getBlockPos());
        villager.playSound(SoundEvents.BLOCK_ANVIL_USE, 0.3F, 1.0F);

        lastHealTime = serverWorld.getTime();
        targetGolem = null;
    }

    @Unique
    private void spawnHealParticles(ServerWorld world, BlockPos pos) {
        for (int i = 0; i < 5; i++) {
            double offsetX = world.random.nextDouble() * 1.2;
            double offsetY = world.random.nextDouble() * 0.5 + 0.5;
            double offsetZ = world.random.nextDouble() * 1.2;
            double deltaY = world.random.nextDouble() * 0.1;
            double speed = world.random.nextDouble() * 0.05;

            Vec3d particlePos = new Vec3d(pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ);

            world.spawnParticles(ParticleTypes.HEART, particlePos.x, particlePos.y, particlePos.z, 1, 0, deltaY, 0, speed);
        }
    }
}