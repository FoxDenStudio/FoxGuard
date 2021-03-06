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

package net.foxdenstudio.foxsuite.foxguard.sponge.pluginold.util;

import net.foxdenstudio.foxsuite.foxguard.sponge.pluginold.FoxGuardMain;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public final class FGUtil {

    /*public static TextColor getColorForObject(IFGObject object) {
        if (object instanceof GlobalRegion) return TextColors.LIGHT_PURPLE;
        else if (object instanceof IGlobal) return TextColors.YELLOW;
        else if (!object.isEnabled()) return TextColors.GRAY;
        else if (object instanceof IController) return TextColors.GREEN;
        else if ((object instanceof IRegion) && !(object instanceof IWorldRegion)) return TextColors.AQUA;
        else return TextColors.WHITE;
    }


    @SuppressWarnings("unchecked")
    public static List<IRegion> getSelectedRegions(CommandSource source) {
        return ((RegionsStateField) FCStateManager.instance().getStateMap().get(source).getOrCreate(RegionsStateField.ID).get()).getList();
    }

    @SuppressWarnings("unchecked")
    public static List<IHandler> getSelectedHandlers(CommandSource source) {
        return ((HandlersStateField) FCStateManager.instance().getStateMap().get(source).getOrCreate(HandlersStateField.ID).get()).getList();
    }

    @SuppressWarnings("unchecked")
    public static List<IController> getSelectedControllers(CommandSource source) {
        return ((ControllersStateField) FCStateManager.instance().getStateMap().get(source).getOrCreate(ControllersStateField.ID).get()).getList();
    }

    public static String getRegionName(IRegion region, boolean dispWorld) {
        return region.getShortTypeName() + " : " + (dispWorld && region instanceof IWorldRegion ? ((IWorldRegion) region).getWorld().getName() + " : " : "") + region.getName();
    }

    public static String getCategory(IFGObject object) {
        if (object instanceof IRegion) {
            if (object instanceof IWorldRegion) return "worldregion";
            else return "region";
        } else if (object instanceof IHandler) {
            if (object instanceof IController) return "controller";
            else return "handler";
        } else return "object";
    }

    public static String genWorldFlag(IRegion region) {
        return region instanceof IWorldRegion ? "--w:" + ((IWorldRegion) region).getWorld().getName() + " " : "";
    }*/

    public static Text readableTristateText(Tristate state) {
        switch (state) {
            case UNDEFINED:
                return Text.of(TextColors.YELLOW, "Pass");
            case TRUE:
                return Text.of(TextColors.GREEN, "Allow");
            case FALSE:
                return Text.of(TextColors.RED, "Deny");
            default:
                return Text.of(TextColors.LIGHT_PURPLE, "Wait wat?");
        }
    }

    /*public static void markRegionDirty(IRegion region) {
        FGManager.getInstance().markDirty(region, RegionCache.DirtyType.MODIFIED);
        FGStorageManager.getInstance().defaultModifiedMap.put(region, true);
        Sponge.getGame().getEventManager().post(FGEventFactory.createFGUpdateObjectEvent(FoxGuardMain.getCause(), region));
    }

    public static void markHandlerDirty(IHandler handler) {
        FGStorageManager.getInstance().defaultModifiedMap.put(handler, true);
        Sponge.getGame().getEventManager().post(FGEventFactory.createFGUpdateObjectEvent(FoxGuardMain.getCause(), handler));
    }*/

    public static Optional<Location<World>> getLocation(Transaction<BlockSnapshot> transaction) {
        if (transaction == null) return Optional.empty();
        Optional<Location<World>> ret = transaction.getOriginal().getLocation();
        if (ret.isPresent()) return ret;
        ret = transaction.getFinal().getLocation();
        if (!ret.isPresent()) {
            Logger logger = FoxGuardMain.instance().getLogger();
            logger.warn("Encountered a block transaction with no location:");
            logger.warn(transaction.toString());
        }
        return ret;
    }
}
