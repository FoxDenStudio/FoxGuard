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

package net.foxdenstudio.sponge.foxguard.plugin.listener;

import com.flowpowered.math.vector.Vector3d;
import net.foxdenstudio.sponge.foxguard.plugin.FGManager;
import net.foxdenstudio.sponge.foxguard.plugin.FoxGuardMain;
import net.foxdenstudio.sponge.foxguard.plugin.flag.FlagSet;
import net.foxdenstudio.sponge.foxguard.plugin.handler.IHandler;
import net.foxdenstudio.sponge.foxguard.plugin.listener.util.EntityFlagCalculator;
import net.foxdenstudio.sponge.foxguard.plugin.listener.util.EventResult;
import net.foxdenstudio.sponge.foxguard.plugin.object.IGuardObject;
import net.foxdenstudio.sponge.foxguard.plugin.util.ExtraContext;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.foxdenstudio.sponge.foxguard.plugin.flag.Flags.*;

public class SpawnEntityListener implements EventListener<SpawnEntityEvent> {

    private static final EntityFlagCalculator ENTITY_FLAG_CALCULATOR = EntityFlagCalculator.getInstance();
    private static final boolean[] BASE_FLAG_SET = FlagSet.arrayFromFlags(ROOT, DEBUFF, SPAWN, ENTITY);

    @Override
    public void handle(SpawnEntityEvent event) throws Exception {
        if (event.isCancelled()) return;
        List<Entity> entities = event.getEntities();
        if (entities.isEmpty()) return;

        for (Entity entity : entities) {
            if (entity instanceof Player) return;
        }

        //Entity oneEntity = event.getEntities().get(0);
        /*if (oneEntity instanceof Arrow) {
            Optional<UUID> creator = oneEntity.getCreator(), notifier = oneEntity.getNotifier();

            System.out.println(creator + ", " + notifier);
            UserStorageService service = FoxGuardMain.instance().getUserStorage();
            if (creator.isPresent()){
                Optional<User> optional = service.get(creator.get());
                System.out.println("Creator: " + (optional.isPresent() ? optional.get().getName() : creator.get()));
            }

            if (notifier.isPresent()) {
                Optional<User> optional = service.get(notifier.get());
                System.out.println("Notifier: " + (optional.isPresent() ? optional.get().getName() : notifier.get()));
            }

        }*/

        Set<IHandler> handlerSet = new HashSet<>();

        for (Entity entity : entities) {
            Location<World> loc = entity.getLocation();
            Vector3d pos = loc.getPosition();
            World world = loc.getExtent();
            FGManager.getInstance().getRegionsInChunkAtPos(world, pos).stream()
                    .filter(region -> region.contains(pos, world))
                    .forEach(region -> region.getLinks().stream()
                            .filter(IGuardObject::isEnabled)
                            .forEach(handlerSet::add));
        }


        if (handlerSet.isEmpty()) {
            FoxGuardMain.instance().getLogger().warn("Handler set is empty for spawn entity listener!");
            return;
        }

        User user;
        if (event.getCause().containsType(Player.class)) {
            user = event.getCause().first(Player.class).get();
        } else if (event.getCause().containsType(User.class)) {
            user = event.getCause().first(User.class).get();
        } else {
            user = null;
        }

        boolean[] flags = BASE_FLAG_SET.clone();

        ENTITY_FLAG_CALCULATOR.applyEntityFlags(entities, flags);

        FlagSet flagSet = new FlagSet(flags);

        List<IHandler> handlerList = new ArrayList<>(handlerSet);
        handlerList.sort(IHandler.PRIORITY);
        int currPriority = handlerList.get(0).getPriority();
        Tristate flagState = Tristate.UNDEFINED;


        for (IHandler handler : handlerList) {
            if (handler.getPriority() < currPriority && flagState != Tristate.UNDEFINED) {
                break;
            }

            EventResult result = handler.handle(user, flagSet, ExtraContext.of(event));
            if (result != null) {
                flagState = flagState.and(result.getState());
            } else {
                FoxGuardMain.instance().getLogger().error("Handler \"" + handler.getName() + "\" returned null!");
            }

            currPriority = handler.getPriority();
        }

        if (flagState == Tristate.FALSE) {
            if (user instanceof Player)
                ((Player) user).sendMessage(ChatTypes.ACTION_BAR, Text.of("You don't have permission!"));
            event.setCancelled(true);
        } else {
            //makes sure that handlers are unable to cancel the event directly.
            event.setCancelled(false);
        }
    }

}
