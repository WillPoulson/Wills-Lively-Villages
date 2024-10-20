package uk.co.willpoulson.willslivelyvillages.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class NameTagMixin {

    @Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
    public void onUseOnEntity(
        PlayerEntity player,
        LivingEntity entity,
        Hand hand,
        CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack stack = (ItemStack) (Object) this;

        // Check if the item is a name tag and the entity is a villager
        if (stack.getItem() instanceof NameTagItem && entity instanceof VillagerEntity) {
            // Cancel the interaction, preventing the name tag from being applied
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}