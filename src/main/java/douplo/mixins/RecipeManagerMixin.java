package douplo.mixins;

import com.google.gson.JsonElement;
import douplo.RpgMod;
import douplo.resource.ReloadManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements IdentifiableResourceReloadListener {

    private Collection<Identifier> fabric$dependencies;

    @Override
    public Identifier getFabricId() {
        return ResourceReloadListenerKeys.RECIPES;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        List<Identifier> ls = new ArrayList<>(IdentifiableResourceReloadListener.super.getFabricDependencies());
        ls.add(new Identifier(RpgMod.MODID, "reload_manager"));
        return ls;
    }

    @Inject(method = "apply", at=@At(value="HEAD"))
    public void applyInject(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
        ReloadManager.reloadItems(resourceManager);
    }

}
