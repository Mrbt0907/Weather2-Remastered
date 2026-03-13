package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(GoalSelector.class)
public interface GoalSelectorAccessor {
    @Accessor("availableGoals")
    Set<PrioritizedGoal> getAvailableGoals();
}