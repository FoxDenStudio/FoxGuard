package net.foxdenstudio.sponge.foxguard.plugin.object.path.owner;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import net.foxdenstudio.sponge.foxguard.plugin.object.path.owner.types.BaseOwner;
import net.foxdenstudio.sponge.foxguard.plugin.object.path.owner.types.IOwner;

import javax.annotation.Nonnull;

public abstract class OwnerTypeAdapter<T extends BaseOwner> extends TypeAdapter<T> {

    public final String group;
    protected final Gson gson;

    protected OwnerTypeAdapter(@Nonnull String group, @Nonnull Gson gson) {
        this.group = group;
        this.gson = gson;
    }



}
