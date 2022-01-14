package douplo.mixins;

import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Mixin(StatType.class)
public class StatTypeMixin<T> {

    @Shadow @Final private Map<T, Stat<T>> stats = new HashMap<T, Stat<T>>();

}
