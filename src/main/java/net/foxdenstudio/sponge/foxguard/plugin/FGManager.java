/*
 * This file is part of FoxGuard, licensed under the MIT License (MIT).
 *
 * Copyright (c) gravityfox - https://gravityfox.net/
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.foxdenstudio.sponge.foxguard.plugin;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import net.foxdenstudio.sponge.foxcore.common.util.CacheMap;
import net.foxdenstudio.sponge.foxcore.plugin.util.Aliases;
import net.foxdenstudio.sponge.foxguard.plugin.controller.IController;
import net.foxdenstudio.sponge.foxguard.plugin.event.util.FGEventFactory;
import net.foxdenstudio.sponge.foxguard.plugin.handler.GlobalHandler;
import net.foxdenstudio.sponge.foxguard.plugin.handler.IHandler;
import net.foxdenstudio.sponge.foxguard.plugin.object.IFGObject;
import net.foxdenstudio.sponge.foxguard.plugin.object.ILinkable;
import net.foxdenstudio.sponge.foxguard.plugin.region.GlobalRegion;
import net.foxdenstudio.sponge.foxguard.plugin.region.IRegion;
import net.foxdenstudio.sponge.foxguard.plugin.region.world.GlobalWorldRegion;
import net.foxdenstudio.sponge.foxguard.plugin.region.world.IWorldRegion;
import net.foxdenstudio.sponge.foxguard.plugin.util.RegionCache;
import org.lwjgl.openal.AL;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.stream.Collectors;

public final class FGManager {

    public static final String[] ILLEGAL_NAMES = {"all", "state", "full", "everything"};

    private static FGManager instance;
    private final Map<World, Set<IWorldRegion>> worldRegions;
    private final Set<IRegion> regions;
    private final Set<IHandler> handlers;
    private final GlobalRegion globalRegion;
    private final GlobalHandler globalHandler;

    private final RegionCache regionCache;

    private FGManager() {
        instance = this;
        worldRegions = new CacheMap<>((key, map) -> {
            if (key instanceof World) {
                Set<IWorldRegion> set = new HashSet<>();
                map.put((World) key, set);
                return set;
            } else return new HashSet<>();
        });
        regions = new HashSet<>();
        handlers = new HashSet<>();
        globalRegion = new GlobalRegion();
        globalHandler = new GlobalHandler();
        regions.add(globalRegion);
        handlers.add(globalHandler);
        globalRegion.addHandler(globalHandler);

        this.regionCache = new RegionCache(regions, worldRegions);
    }

    public static synchronized void init() {
        if (instance == null) instance = new FGManager();
    }

    public static FGManager getInstance() {
        return instance;
    }

    public boolean isRegistered(IHandler handler) {
        return handlers.contains(handler);
    }

    public boolean isRegionNameAvailable(String name) {
        if (getRegion(name) != null) return false;
        for (World world : worldRegions.keySet()) {
            if (getWorldRegion(world, name) != null) return false;
        }
        return true;
    }

    public boolean isWorldRegionNameAvailable(String name, World world) {
        if (getWorldRegion(world, name) != null || getRegion(name) != null) return false;
        else return true;
    }

    public Tristate isWorldRegionNameAvailable(String name) {
        Tristate available = null;
        for (World world : worldRegions.keySet()) {
            if (getWorldRegion(world, name) == null) {
                if (available == null) {
                    available = Tristate.TRUE;
                } else if (available == Tristate.FALSE) {
                    available = Tristate.UNDEFINED;
                }
            } else {
                if (available == null) {
                    available = Tristate.FALSE;
                } else if (available == Tristate.TRUE) {
                    available = Tristate.UNDEFINED;
                }
            }
        }
        return available;
    }

    public boolean addWorldRegion(World world, IWorldRegion region) {
        if (region == null || region.getWorld() != null ||
                !isWorldRegionNameAvailable(region.getName(), world) || !isNameValid(region.getName()))
            return false;
        region.setWorld(world);
        this.worldRegions.get(world).add(region);
        this.regionCache.markDirty(region, RegionCache.DirtyType.ADDED);
        FGStorageManager.getInstance().addObject(region);
        Sponge.getGame().getEventManager().post(FGEventFactory.createFGUpdateObjectEvent(FoxGuardMain.getCause(), region));
        return true;
    }

    public boolean addRegion(IRegion region) {
        if (region == null || !isRegionNameAvailable(region.getName()) || !isNameValid(region.getName())) return false;
        this.regions.add(region);
        this.regionCache.markDirty(region, RegionCache.DirtyType.ADDED);
        FGStorageManager.getInstance().addObject(region);
        Sponge.getGame().getEventManager().post(FGEventFactory.createFGUpdateObjectEvent(FoxGuardMain.getCause(), region));
        return true;
    }

    public boolean addRegion(IRegion region, World world) {
        if (region instanceof IWorldRegion) {
            return world != null && addWorldRegion(world, (IWorldRegion) region);
        } else return addRegion(region);
    }

    public IWorldRegion getWorldRegion(World world, String name) {
        for (IWorldRegion region : this.worldRegions.get(world)) {
            if (region.getName().equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }

    public IRegion getRegion(String name) {
        for (IRegion region : this.regions) {
            if (region.getName().equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }

    public IRegion getRegionFromWorld(World world, String name) {
        IRegion region = getWorldRegion(world, name);
        if (region == null) {
            return getRegion(name);
        } else return region;
    }

    public Set<IRegion> getRegions() {
        return ImmutableSet.copyOf(this.regions);
    }

    public Set<IWorldRegion> getWorldRegions(World world) {
        return ImmutableSet.copyOf(this.worldRegions.get(world));
    }

    public Set<IRegion> getAllRegions() {
        Set<IRegion> set = new HashSet<>();
        this.worldRegions.forEach((world, worldSet) -> worldSet.forEach(set::add));
        this.regions.forEach(set::add);
        return ImmutableSet.copyOf(set);
    }

    public Set<IRegion> getAllRegions(World world) {
        if (world == null) return getRegions();
        Set<IRegion> set = new HashSet<>();
        this.worldRegions.get(world).forEach(set::add);
        this.regions.forEach(set::add);
        return ImmutableSet.copyOf(set);
    }

    public Set<IRegion> getAllRegions(World world, Vector3i chunk) {
        return getAllRegions(world, chunk, false);
    }

    public Set<IRegion> getAllRegions(World world, Vector3i chunk, boolean includeDisabled) {
        return this.regionCache.getData(world, chunk).getRegions(includeDisabled);
    }

    public Set<IRegion> getRegionsAtPos(World world, Vector3i position) {
        return FGManager.getInstance().getRegionsInChunkAtPos(world, position).stream()
                .filter(region -> region.contains(position, world))
                .collect(Collectors.toSet());
    }

    public Set<IRegion> getRegionsAtPos(World world, Vector3i position, boolean includeDisabled) {
        return FGManager.getInstance().getRegionsInChunkAtPos(world, position, includeDisabled).stream()
                .filter(region -> region.contains(position, world))
                .collect(Collectors.toSet());
    }

    public Set<IRegion> getRegionsAtPos(World world, Vector3d position) {
        return FGManager.getInstance().getRegionsInChunkAtPos(world, position).stream()
                .filter(region -> region.contains(position, world))
                .collect(Collectors.toSet());
    }

    public Set<IRegion> getRegionsAtPos(World world, Vector3d position, boolean includeDisabled) {
        return FGManager.getInstance().getRegionsInChunkAtPos(world, position, includeDisabled).stream()
                .filter(region -> region.contains(position, world))
                .collect(Collectors.toSet());
    }

    public Set<IRegion> getRegionsAtMultiPosI(World world, Iterable<Vector3i> positions) {
        return getRegionsAtMultiPosI(world, positions, false);
    }

    public Set<IRegion> getRegionsAtMultiPosI(World world, Iterable<Vector3i> positions, boolean includeDisabled) {
        Set<IRegion> set = new HashSet<>();
        SetMultimap<Vector3i, Vector3i> chunkPosMap = HashMultimap.create();
        for (Vector3i pos : positions) {
            chunkPosMap.put(
                    new Vector3i(
                            pos.getX() >> 4,
                            pos.getY() >> 4,
                            pos.getZ() >> 4
                    ), pos
            );
        }
        for (Map.Entry<Vector3i, Collection<Vector3i>> entry : chunkPosMap.asMap().entrySet()) {
            RegionCache.ChunkData data = this.regionCache.getData(world, entry.getKey());
            Set<IRegion> candidates = new HashSet<>(data.getRegions(includeDisabled));
            candidates.removeAll(set);
            for (Vector3i pos : entry.getValue()) {
                if (candidates.isEmpty()) break;
                Iterator<IRegion> regionIterator = candidates.iterator();
                do {
                    IRegion region = regionIterator.next();
                    if (region.contains(pos, world)) {
                        set.add(region);
                        regionIterator.remove();
                    }
                } while (regionIterator.hasNext());
            }

        }
        return set;
    }

    public Set<IRegion> getRegionsAtMultiPosD(World world, Iterable<Vector3d> positions) {
        return getRegionsAtMultiPosD(world, positions, false);
    }

    public Set<IRegion> getRegionsAtMultiPosD(World world, Iterable<Vector3d> positions, boolean includeDisabled) {
        Set<IRegion> set = new HashSet<>();
        SetMultimap<Vector3i, Vector3d> chunkPosMap = HashMultimap.create();
        for (Vector3d pos : positions) {
            chunkPosMap.put(
                    new Vector3i(
                            GenericMath.floor(pos.getX()) >> 4,
                            GenericMath.floor(pos.getY()) >> 4,
                            GenericMath.floor(pos.getZ()) >> 4)
                    , pos
            );
        }
        for (Map.Entry<Vector3i, Collection<Vector3d>> entry : chunkPosMap.asMap().entrySet()) {
            RegionCache.ChunkData data = this.regionCache.getData(world, entry.getKey());
            Set<IRegion> candidates = new HashSet<>(data.getRegions(includeDisabled));
            candidates.removeAll(set);
            for (Vector3d pos : entry.getValue()) {
                if (candidates.isEmpty()) break;
                Iterator<IRegion> regionIterator = candidates.iterator();
                do {
                    IRegion region = regionIterator.next();
                    if (region.contains(pos, world)) {
                        set.add(region);
                        regionIterator.remove();
                    }
                } while (regionIterator.hasNext());
            }

        }
        return set;
    }

    public Set<IRegion> getRegionsInChunkAtPos(World world, Vector3i pos) {
        return getRegionsInChunkAtPos(world, pos, false);
    }

    public Set<IRegion> getRegionsInChunkAtPos(World world, Vector3i pos, boolean includeDisabled) {
        return this.regionCache.getData(world,
                new Vector3i(
                        pos.getX() >> 4,
                        pos.getY() >> 4,
                        pos.getZ() >> 4)
        ).getRegions(includeDisabled);
    }

    public Set<IRegion> getRegionsInChunkAtPos(World world, Vector3d pos) {
        return getRegionsInChunkAtPos(world, pos, false);
    }

    public Set<IRegion> getRegionsInChunkAtPos(World world, Vector3d pos, boolean includeDisabled) {
        return this.regionCache.getData(world,
                new Vector3i(
                        GenericMath.floor(pos.getX()) >> 4,
                        GenericMath.floor(pos.getY()) >> 4,
                        GenericMath.floor(pos.getZ()) >> 4)
        ).getRegions(includeDisabled);
    }

    public Set<IHandler> getHandlers() {
        return ImmutableSet.copyOf(this.handlers);
    }

    public Set<IHandler> getHandlers(boolean includeControllers) {
        if (includeControllers) {
            return ImmutableSet.copyOf(this.handlers);
        } else {
            return this.handlers.stream()
                    .filter(handler -> !(handler instanceof IController))
                    .collect(GuavaCollectors.toImmutableSet());
        }
    }

    public Set<IController> getControllers() {
        return this.handlers.stream()
                .filter(handler -> handler instanceof IController)
                .map(handler -> ((IController) handler))
                .collect(GuavaCollectors.toImmutableSet());
    }

    public boolean addHandler(IHandler handler) {
        if (handler == null) return false;
        if (gethandler(handler.getName()) != null) return false;
        handlers.add(handler);
        FGStorageManager.getInstance().addObject(handler);
        Sponge.getGame().getEventManager().post(FGEventFactory.createFGUpdateObjectEvent(FoxGuardMain.getCause(), handler));
        return true;
    }

    public IHandler gethandler(String name) {
        for (IHandler handler : handlers) {
            if (handler.getName().equalsIgnoreCase(name)) {
                return handler;
            }
        }
        return null;
    }

    public IController getController(String name) {
        for (IHandler handler : handlers) {
            if ((handler instanceof IController) && handler.getName().equalsIgnoreCase(name)) {
                return (IController) handler;
            }
        }
        return null;
    }

    public boolean removeHandler(IHandler handler) {
        if (handler == null || handler instanceof GlobalHandler) return false;
        this.worldRegions.forEach((world, set) -> {
            set.stream()
                    .filter(region -> region.getHandlers().contains(handler))
                    .forEach(region -> region.removeHandler(handler));
        });
        if (!this.handlers.contains(handler)) {
            return false;
        }
        FGStorageManager.getInstance().removeObject(handler);
        Sponge.getGame().getEventManager().post(FGEventFactory.createFGUpdateObjectEvent(FoxGuardMain.getCause(), handler));
        handlers.remove(handler);
        return true;
    }

    public boolean removeRegion(IRegion region) {
        if (region instanceof IWorldRegion) {
            return removeWorldRegion((IWorldRegion) region);
        } else {
            if (region == null) return false;
            if (!this.regions.contains(region)) return false;
            this.regions.remove(region);
            FGStorageManager.getInstance().removeObject(region);
            Sponge.getGame().getEventManager().post(FGEventFactory.createFGUpdateObjectEvent(FoxGuardMain.getCause(), region));
            this.regionCache.markDirty(region, RegionCache.DirtyType.REMOVED);
            return true;
        }
    }

    public boolean removeWorldRegion(IWorldRegion region) {
        if (region == null || region instanceof GlobalWorldRegion) return false;
        boolean removed = false;
        if (region.getWorld() != null) {
            if (!this.worldRegions.get(region.getWorld()).contains(region)) {
                return false;
            }
            this.worldRegions.get(region.getWorld()).remove(region);
            removed = true;
        } else {
            for (Set<IWorldRegion> set : this.worldRegions.values()) {
                if (set.contains(region)) {
                    set.remove(region);
                    removed = true;
                }
            }
        }
        if (removed) {
            FGStorageManager.getInstance().removeObject(region);
            Sponge.getGame().getEventManager().post(FGEventFactory.createFGUpdateObjectEvent(FoxGuardMain.getCause(), region));
            this.regionCache.markDirty(region, RegionCache.DirtyType.REMOVED);
        }
        return removed;
    }

    public boolean link(ILinkable linkable, IHandler handler) {
        if (linkable == null || handler == null || linkable.getHandlers().contains(handler)) return false;
        Sponge.getGame().getEventManager().post(FGEventFactory.createFGUpdateEvent(FoxGuardMain.getCause()));
        return !(handler instanceof GlobalHandler && !(linkable instanceof GlobalWorldRegion || linkable instanceof GlobalRegion)) && linkable.addHandler(handler);
    }

    public boolean unlink(ILinkable linkable, IHandler handler) {
        if (linkable == null || handler == null || !linkable.getHandlers().contains(handler)) return false;
        Sponge.getGame().getEventManager().post(FGEventFactory.createFGUpdateEvent(FoxGuardMain.getCause()));
        return !(handler instanceof GlobalHandler) && linkable.removeHandler(handler);
    }

    public boolean rename(IFGObject object, String newName) {
        if (object instanceof IWorldRegion) {
            IWorldRegion region = (IWorldRegion) object;
            if (this.getWorldRegion(region.getWorld(), newName) != null) return false;
        } else if (object instanceof IHandler) {
            if (this.gethandler(newName) != null) return false;
        }
        FGStorageManager.getInstance().removeObject(object);
        object.setName(newName);
        FGStorageManager.getInstance().addObject(object);
        return true;
    }

    public void initWorld(World world) {
        GlobalWorldRegion gwr = new GlobalWorldRegion();
        gwr.setWorld(world);
        this.worldRegions.get(world).add(gwr);
        this.regionCache.markDirty(gwr, RegionCache.DirtyType.ADDED);
    }

    public void unloadWorld(World world) {
        this.worldRegions.remove(world);
    }

    public GlobalHandler getGlobalHandler() {
        return globalHandler;
    }

    public static boolean isNameValid(String name) {
        return !name.matches("^.*[ :.=;\"\'\\\\/{}()\\[\\]<>#@|?*].*$") &&
                !Aliases.isIn(FGStorageManager.FS_ILLEGAL_NAMES, name) &&
                !Aliases.isIn(ILLEGAL_NAMES, name);
    }

    public void markDirty(IRegion region, RegionCache.DirtyType type) {
        regionCache.markDirty(region, type);
    }

    public void clearRegionCache() {
        this.regionCache.clearCaches();
    }
}
