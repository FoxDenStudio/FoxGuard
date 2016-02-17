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

package net.foxdenstudio.sponge.foxguard.plugin.util;

import net.foxdenstudio.sponge.foxcore.plugin.state.FCStateManager;
import net.foxdenstudio.sponge.foxguard.plugin.Flag;
import net.foxdenstudio.sponge.foxguard.plugin.controller.IController;
import net.foxdenstudio.sponge.foxguard.plugin.handler.IHandler;
import net.foxdenstudio.sponge.foxguard.plugin.object.IFGObject;
import net.foxdenstudio.sponge.foxguard.plugin.object.IGlobal;
import net.foxdenstudio.sponge.foxguard.plugin.region.IRegion;
import net.foxdenstudio.sponge.foxguard.plugin.state.HandlersStateField;
import net.foxdenstudio.sponge.foxguard.plugin.state.RegionsStateField;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FGHelper {

    public static TextColor getColorForObject(IFGObject object) {
        if (object instanceof IGlobal) return TextColors.YELLOW;
        else if (!object.isEnabled()) return TextColors.GRAY;
        else if (object instanceof IController) return TextColors.GREEN;
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

    public static String getRegionName(IRegion region, boolean dispWorld) {
        return region.getShortTypeName() + " : " + (dispWorld ? region.getWorld().getName() + " : " : "") + region.getName();
    }

    public static Flag nearestParent(Flag child, Set<Flag> set) {
        for(Set<Flag> level : child.getHiearchy()){
            Set<Flag> matches = new HashSet<>(set);
            matches.retainAll(level);
            //if(matches)
        }
        return null;
    }
}
