package net.foxdenstudio.sponge.foxguard.plugin.object.path.element.owner;

import com.google.common.collect.ImmutableMap;
import net.foxdenstudio.sponge.foxguard.plugin.object.IGuardObject;
import net.foxdenstudio.sponge.foxguard.plugin.object.path.PathManager;
import net.foxdenstudio.sponge.foxguard.plugin.object.path.owner.provider.PathOwnerProvider;
import net.foxdenstudio.sponge.foxguard.plugin.object.path.owner.types.BaseOwner;
import net.foxdenstudio.sponge.foxguard.plugin.object.path.owner.types.IOwner;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class OwnerPathElement<P extends PathOwnerProvider<? extends IOwner>> implements IOwnerPathElement {

    private static final PathManager MANAGER = PathManager.getInstance();

    public final String prefix;
    protected final OwnerPathElement<P> parent;
    protected final List<String> currentPath;
    protected P provider;

    public OwnerPathElement(String prefix) {
        this.prefix = prefix;
        this.currentPath = new ArrayList<>();
        this.parent = null;
        this.provider = null;
    }

    private OwnerPathElement(OwnerPathElement<P> parent, String next) {
        this.prefix = parent.prefix;
        this.parent = parent;
        this.currentPath = new ArrayList<>(parent.currentPath);
        this.currentPath.add(next);
    }

    @Override
    public Optional<IGuardObject> get(@Nonnull String name, @Nullable World world) {
        if (name.isEmpty() || !this.provider.isValid()) return Optional.empty();

        if (this.parent != null) {
            if (name.startsWith(".")) {
                String current = this.currentPath.get(this.currentPath.size() - 1);
                return this.parent.get(current + name, null);
            }
        }
        IOwner owner = provider.getOwner().orElse(null);


        // TODO actually lookup the object

        return Optional.empty();
    }


    @Override
    public abstract Optional<? extends OwnerPathElement> resolve(String name);

    @Override
    public Collection<String> getPathSuggestions() {
        return this.provider.getSuggestions();
    }

    @Override
    public Map<String, IGuardObject> getObjects() {
        if (!this.provider.isValid()) return ImmutableMap.of();

        // TODO actually lookup the object

        return ImmutableMap.of();
    }

    @Override
    public boolean isFinished() {
        return this.provider != null && this.provider.isFinished();
    }

    @Override
    public boolean isValid() {
        return this.provider != null && this.provider.isValid();
    }

    @Override
    public Optional<? extends IOwner> getOwner() {
        return provider.getOwner();
    }

    public static class Literal extends OwnerPathElement<PathOwnerProvider.Literal<? extends BaseOwner>> {

        public final String group;
        public final String type;

        public Literal(String prefix) {
            super(prefix);
            this.group = null;
            this.type = null;
        }

        private Literal(Literal parent, String next) {
            super(parent, next);
            int size = this.currentPath.size();
            this.group = size > 0 ? this.currentPath.get(0) : null;
            this.type = size > 1 ? this.currentPath.get(1) : null;
            if (size > 1) {
                PathOwnerProvider.Literal.Factory<? extends BaseOwner> providerFactory = MANAGER.getLiteralPathOwnerProvider(this.type);
                if (providerFactory != null) {
                    this.provider = providerFactory.get();
                    this.provider.setGroup(group);
                    for (int i = 2; i < size; i++) {
                        String element = this.currentPath.get(i);
                        this.provider.apply(element);
                    }
                }
            } else this.provider = null;
        }

        @Override
        public Optional<Literal> resolve(String name) {
            //System.out.println(name);
            if (name == null || name.isEmpty()) return Optional.empty();
            else return Optional.of(new Literal(this, name));
        }
    }

    public static class Dynamic extends OwnerPathElement<PathOwnerProvider.Dynamic<? extends IOwner>> {

        public final String type;
        public final CommandSource source;

        public Dynamic(String prefix, CommandSource source) {
            super(prefix);
            this.type = null;
            this.source = source;
        }

        private Dynamic(Dynamic parent, String next) {
            super(parent, next);
            this.source = parent.source;
            int size = this.currentPath.size();

            this.type = size > 0 ? this.currentPath.get(0) : null;

            if (size > 0) {
                PathOwnerProvider.Dynamic.Factory<? extends IOwner> providerFactory = MANAGER.getDynamicPathOwnerProvider(this.type);
                if (providerFactory != null) {
                    this.provider = providerFactory.get();
                    this.provider.setSource(source);
                    for (int i = 1; i < size; i++) {
                        String element = this.currentPath.get(i);
                        this.provider.apply(element);
                    }
                }
            } else this.provider = null;
        }

        @Override
        public Optional<Dynamic> resolve(String name) {
            if (name == null || name.isEmpty()) return Optional.empty();
            else return Optional.of(new Dynamic(this, name));
        }
    }
}
