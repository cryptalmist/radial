package dev.velolib.radial.util;

import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class EncoderUtils {

    /**
     * Converts an ItemStack into a valid /give command string.
     *
     * @param stack      The ItemStack to convert.
     * @param registries The registry access (e.g., from world.getRegistryManager()).
     * @return A string formatted for the /give command.
     */
    @SuppressWarnings("unchecked")
    public static String toGiveCommandString(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        if (stack.isEmpty()) {
            return "minecraft:air";
        }

        // 1. Get the base Item ID
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        StringBuilder command = new StringBuilder(itemId.toString());

        // 2. Fetch the patch containing all modifications made to this specific item vs its default state
        ComponentChanges patch = stack.getComponentChanges();

        // 3. Serialize the component patch if any modifications exist
        if (!patch.isEmpty()) {
            command.append("[");

            // Create a registry-aware NBT ops context
            RegistryOps<NbtElement> registryOps = registries.getOps(NbtOps.INSTANCE);
            boolean first = true;

            for (Map.Entry<ComponentType<?>, Optional<?>> entry : patch.entrySet()) {
                ComponentType<?> type = entry.getKey();
                Identifier typeId = Registries.DATA_COMPONENT_TYPE.getId(type);
                Optional<?> value = entry.getValue();

                if (value.isPresent()) {
                    // Added or modified component
                    Codec<Object> codec = (Codec<Object>) type.getCodec();

                    // Transient components lacking a codec cannot be expressed in a command
                    if (codec != null) {
                        if (!first) command.append(",");
                        first = false;

                        command.append(Objects.requireNonNull(typeId)).append("=");

                        // Encode the component object back to an NBT Tag
                        NbtElement tag =
                                codec.encodeStart(registryOps, value.get()).getOrThrow();

                        // NbtElement#toString natively produces a compliant SNBT string in 1.21
                        command.append(tag);
                    }
                } else {
                    // A default component that was explicitly removed is prefixed with an exclamation mark
                    if (!first) command.append(",");
                    first = false;

                    command.append("!").append(Objects.requireNonNull(typeId).toString());
                }
            }
            command.append("]");
        }

        return command.toString();
    }
}
