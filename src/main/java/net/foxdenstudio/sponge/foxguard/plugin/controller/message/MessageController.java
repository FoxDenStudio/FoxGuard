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

package net.foxdenstudio.sponge.foxguard.plugin.controller.message;

import com.google.common.collect.ImmutableList;
import net.foxdenstudio.sponge.foxcore.plugin.command.util.AdvCmdParser;
import net.foxdenstudio.sponge.foxcore.plugin.command.util.ProcessResult;
import net.foxdenstudio.sponge.foxguard.plugin.controller.ControllerBase;
import net.foxdenstudio.sponge.foxguard.plugin.controller.util.HandlerWrapper;
import net.foxdenstudio.sponge.foxguard.plugin.flag.FlagSet;
import net.foxdenstudio.sponge.foxguard.plugin.handler.IHandler;
import net.foxdenstudio.sponge.foxguard.plugin.listener.util.EventResult;
import net.foxdenstudio.sponge.foxguard.plugin.object.factory.IHandlerFactory;
import net.foxdenstudio.sponge.foxguard.plugin.util.ExtraContext;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageController extends ControllerBase {

    private HandlerWrapper slot;
    private Map<String, ISendableMessage> messages;


    public MessageController(String name, boolean isEnabled, int priority) {
        super(name, isEnabled, priority);
        slot = HandlerWrapper.PASSTHROUGH;
        messages = new HashMap<>();
    }

    @Override
    public EventResult handle(@Nullable User user, FlagSet flags, ExtraContext extra) {
        //TODO: rewrite this class
        /*EventResult result = slot.handle(user, flags, extra);
        String messageName = configs.get(new Config(flag, result.getState()));
        if (messageName != null) {
            return result;
        } else {
            return result;
        }*/
        return EventResult.pass();
    }

    @Override
    public Text details(CommandSource source, String arguments) {
        Text.Builder builder = Text.builder();


        return builder.build();
    }

    @Override
    public List<String> detailsSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) {
        return ImmutableList.of();
    }

    @Override
    public void save(Path directory) {

    }

    @Override
    public void loadLinks(Path directory) {

    }

    @Override
    public int maxLinks() {
        return -1;
    }


    @Override
    public boolean addHandler(IHandler handler) {
        if (this.handlers.size() < 1) {
            slot = new HandlerWrapper(handler);
            return super.addHandler(handler);
        } else return false;
    }

    @Override
    public boolean removeHandler(IHandler handler) {
        if (slot != null && slot.handler == handler) slot = HandlerWrapper.PASSTHROUGH;
        return super.removeHandler(handler);
    }

    @Override
    public void clearHandlers() {
        slot = HandlerWrapper.PASSTHROUGH;
        super.clearHandlers();
    }

    public IHandler getHandler() {
        if (this.handlers.size() == 0) return null;
        else return this.handlers.get(0);
    }

    @Override
    public String getShortTypeName() {
        return "Msg";
    }

    @Override
    public String getLongTypeName() {
        return "Message";
    }

    @Override
    public String getUniqueTypeString() {
        return "message";
    }

    @Override
    public ProcessResult modify(CommandSource source, String arguments) throws CommandException {
        AdvCmdParser.ParseResult parse = AdvCmdParser.builder()
                .arguments(arguments).parse();

        return ProcessResult.failure();
    }

    @Override
    public List<String> modifySuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        return ImmutableList.of();
    }

    public HandlerWrapper getSlot() {
        return slot;
    }

    public void setSlot(HandlerWrapper slot) {
        this.slot = slot;
    }

    public Map<String, ISendableMessage> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, ISendableMessage> messages) {
        this.messages = messages;
    }

    public static class Factory implements IHandlerFactory {

        private static final String[] messageAliases = {"message", "mess", "msg"};

        @Override
        public IHandler create(String name, int priority, String arguments, CommandSource source) {
            return null;
        }

        @Override
        public IHandler create(Path directory, String name, int priority, boolean isEnabled) {
            return null;
        }

        @Override
        public String[] getAliases() {
            return messageAliases;
        }

        @Override
        public String getType() {
            return "message";
        }

        @Override
        public String getPrimaryAlias() {
            return "message";
        }

        @Override
        public List<String> createSuggestions(CommandSource source, String arguments, String type, @Nullable Location<World> targetPosition) throws CommandException {
            return ImmutableList.of();
        }
    }
}
