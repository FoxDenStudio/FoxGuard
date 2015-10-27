package tk.elektrofuchse.fox.foxguard.commands;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.*;
import org.spongepowered.api.util.command.args.ArgumentParseException;
import org.spongepowered.api.util.command.source.ConsoleSource;
import tk.elektrofuchse.fox.foxguard.commands.util.FGHelper;

import java.util.List;
import java.util.Optional;

/**
 * Created by Fox on 8/20/2015.
 */
public class CommandPosition implements CommandCallable {
    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        if (source instanceof Player) {
            Player player = (Player) source;
            String[] args = {};
            if (!arguments.isEmpty()) args = arguments.split(" ");
            int x, y, z;
            Vector3i pPos = player.getLocation().getBlockPosition();
            if (args.length == 0) {
                x = pPos.getX();
                y = pPos.getY();
                z = pPos.getZ();
            } else if (args.length > 0 && args.length < 3) {
                throw new CommandException(Texts.of("Not enough arguments!"));
            } else if (args.length == 3) {
                try {
                    x = FGHelper.parseCoordinate(pPos.getX(), args[0]);
                } catch (NumberFormatException e) {
                    throw new ArgumentParseException(Texts.of("Unable to parse \"" + args[0] + "\"!"), e, args[0], 0);
                }
                try {
                    y = FGHelper.parseCoordinate(pPos.getY(), args[1]);
                } catch (NumberFormatException e) {
                    throw new ArgumentParseException(Texts.of("Unable to parse \"" + args[1] + "\"!"), e, args[1], 1);
                }
                try {
                    z = FGHelper.parseCoordinate(pPos.getZ(), args[2]);
                } catch (NumberFormatException e) {
                    throw new ArgumentParseException(Texts.of("Unable to parse \"" + args[2] + "\"!"), e, args[2], 2);
                }
            } else {
                throw new CommandException(Texts.of("Too many arguments!"));
            }
            FoxGuardCommandDispatcher.getInstance().getStateMap().get(player).positions.add(new Vector3i(x, y, z));
            player.sendMessage(Texts.of(TextColors.GREEN, "Successfully added position (" + x + ", " + y + ", " + z + ") to your state buffer!"));
        } else if (source instanceof ConsoleSource) {

        } else {
            throw new CommandPermissionException(Texts.of("You must be a player or console to use this command!"));
        }
        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return null;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("foxguard.command.state");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Text getUsage(CommandSource source) {

        if (source instanceof Player)
            return Texts.of("position [<x> <y> <z>]");
        else return Texts.of("position <x> <y> <z>");

    }

}