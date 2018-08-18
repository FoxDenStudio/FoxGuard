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

package net.foxdenstudio.sponge.foxguard.plugin.command;

import com.google.common.collect.ImmutableList;
import net.foxdenstudio.sponge.foxcore.plugin.command.FCCommandBase;
import net.foxdenstudio.sponge.foxcore.plugin.command.util.AdvCmdParser;
import net.foxdenstudio.sponge.foxguard.plugin.FGManager;
import net.foxdenstudio.sponge.foxguard.plugin.handler.GlobalHandler;
import net.foxdenstudio.sponge.foxguard.plugin.handler.IHandler;
import net.foxdenstudio.sponge.foxguard.plugin.object.IGuardObject;
import net.foxdenstudio.sponge.foxguard.plugin.object.IGlobal;
import net.foxdenstudio.sponge.foxguard.plugin.object.path.owner.types.UUIDOwner;
import net.foxdenstudio.sponge.foxguard.plugin.util.FGUtil;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.foxdenstudio.sponge.foxcore.plugin.util.Aliases.isIn;

public class CommandPriority extends FCCommandBase {

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        if (!testPermission(source)) {
            source.sendMessage(Text.of(TextColors.RED, "You don't have permission to use this command!"));
            return CommandResult.empty();
        }
        AdvCmdParser.ParseResult parse = AdvCmdParser.builder()
                .arguments(arguments)
                .parse();
        if (parse.args.length == 0) {
            source.sendMessage(Text.builder()
                    .append(Text.of(TextColors.GREEN, "Usage: "))
                    .append(getUsage(source))
                    .build());
            return CommandResult.empty();
        } else {
            int successes = 0;
            int failures = 0;
            List<IHandler> handlers = new ArrayList<>();
            handlers.addAll(FGUtil.getSelectedHandlers(source));
            PriorityMachine machine = null;
            for (String arg : parse.args) {
                try {
                    PriorityMachine temp = new PriorityMachine(arg);
                    if (machine == null) machine = temp;
                } catch (NumberFormatException ignored) {
                    try {
                        FGUtil.OwnerResult ownerResult = FGUtil.processUserInput(arg);
                        IHandler handler = FGUtil.getHandlerFromCommand(ownerResult);
                        handlers.add(handler);
                    } catch (CommandException ignored2) {
                        failures++;
                    }
                }
            }
            if (machine == null) {
                throw new CommandException(Text.of("You must specify a priority!"));
            }
            for (IHandler handler : handlers) {
                if (handler instanceof GlobalHandler) failures++;
                else {
                    handler.setPriority(machine.process(handler.getPriority()));
                    successes++;
                }
            }
            if (handlers.size() < 1) throw new CommandException(Text.of("You must specify at least one handler!"));
            source.sendMessage(Text.of(TextColors.GREEN, "Successfully changed priorities with "
                    + successes + " successes and " + failures + " failures!"));
            return CommandResult.builder().successCount(successes).build();
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        if (!testPermission(source)) return ImmutableList.of();
        AdvCmdParser.ParseResult parse = AdvCmdParser.builder()
                .arguments(arguments)
                .excludeCurrent(true)
                .autoCloseQuotes(true)
                .parse();
        if (parse.current.type.equals(AdvCmdParser.CurrentElement.ElementType.ARGUMENT)) {
            FGUtil.OwnerTabResult result = FGUtil.getOwnerSuggestions(parse.current.token);
            if (result.isComplete()) {
                return result.getSuggestions().stream()
                        .map(str -> parse.current.prefix + str)
                        .collect(GuavaCollectors.toImmutableList());
            }

            List<IHandler> selected = ImmutableList.copyOf(FGUtil.getSelectedHandlers(source));
            return FGManager.getInstance().getHandlers(new UUIDOwner(UUIDOwner.USER_GROUP, result.getOwner())).stream()
                    .filter(handler -> !selected.contains(handler) && !(handler instanceof IGlobal))
                    .map(IGuardObject::getName)
                    .filter(new StartsWithPredicate(parse.current.token))
                    .filter(alias -> !isIn(parse.args, alias))
                    .map(args -> parse.current.prefix + result.getPrefix() + args)
                    .collect(GuavaCollectors.toImmutableList());
        } else if (parse.current.type.equals(AdvCmdParser.CurrentElement.ElementType.COMPLETE))
            return ImmutableList.of(parse.current.prefix + " ");
        return ImmutableList.of();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("foxguard.command.modify.objects.handlers.priority");
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Sets or changes the priority of one or more handlers."));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("This command will modify the priorities of all handlers currently in your state buffer.\n" +
                "This command takes a minimum of one parameter, which is the priority that all handlers will be set to.\n" +
                "Prefixing this value with a tilde (\" ~ \") instead increments or decrements the priority of each handler by that value.\n" +
                "Any arguments after the priority are understood to be additional handler names not already in your state buffer."));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("priority <priority> [handlers...]");
    }

    private static class PriorityMachine {

        final int value;
        final boolean delta;

        public PriorityMachine(String arg) throws NumberFormatException {
            if (arg.startsWith("~")) {
                delta = true;
                value = Integer.parseInt(arg.substring(1));
            } else {
                delta = false;
                value = Integer.parseInt(arg);
            }
        }

        public int process(int input) {
            if (delta) return input + value;
            else return value;
        }
    }

}
