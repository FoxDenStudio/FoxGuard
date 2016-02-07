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

import com.google.inject.Inject;
import net.foxdenstudio.sponge.foxcore.plugin.FoxCoreMain;
import net.foxdenstudio.sponge.foxcore.plugin.command.CommandAbout;
import net.foxdenstudio.sponge.foxcore.plugin.command.FCCommandDispatcher;
import net.foxdenstudio.sponge.foxcore.plugin.state.FCStateManager;
import net.foxdenstudio.sponge.foxcore.plugin.util.Aliases;
import net.foxdenstudio.sponge.foxguard.plugin.command.*;
import net.foxdenstudio.sponge.foxguard.plugin.command.handlers.CommandPriority;
import net.foxdenstudio.sponge.foxguard.plugin.listener.BlockEventListener;
import net.foxdenstudio.sponge.foxguard.plugin.listener.InteractListener;
import net.foxdenstudio.sponge.foxguard.plugin.listener.PlayerMoveListener;
import net.foxdenstudio.sponge.foxguard.plugin.listener.SpawnEntityEventListener;
import net.foxdenstudio.sponge.foxguard.plugin.state.HandlersStateField;
import net.foxdenstudio.sponge.foxguard.plugin.state.RegionsStateField;
import net.foxdenstudio.sponge.foxguard.plugin.state.factory.HandlersStateFieldFactory;
import net.foxdenstudio.sponge.foxguard.plugin.state.factory.RegionsStateFieldFactory;
import net.minecrell.mcstats.SpongeStatsLite;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static net.foxdenstudio.sponge.foxcore.plugin.util.Aliases.HANDLERS_ALIASES;

@Plugin(id = "foxguard", name = "FoxGuard", version = FoxGuardMain.PLUGIN_VERSION, dependencies = "required-after:foxcore")
public final class FoxGuardMain {

    /**
     * String object containing the current version of the plugin.
     */
    public static final String PLUGIN_VERSION = "0.16.1-SNAPSHOT";//VERSION

    /**
     * FoxGuardMain instance object.
     */
    private static FoxGuardMain instanceField;


    @Inject
    private Logger loggerField;
    @Inject
    private Game game;
    @Inject
    private EventManager eventManager;
    @Inject
    @ConfigDir(sharedRoot = true)
    private File configDirectory;

    @Inject
    private SpongeStatsLite stats;

    private SqlService sql;
    private UserStorageService userStorage;

    private boolean loaded = false;

    /**
     * @return The current instance of the FoxGuardMain object.
     */
    public static FoxGuardMain instance() {
        return instanceField;
    }

    //my uuid - f275f223-1643-4fac-9fb8-44aaf5b4b371

    /**
     * Used to create a new ReadWriteLock. Depending on config option, will either load a real one, or a "spoof" one.
     *
     * @return A ReadWriteLock Object that can be used.
     */


    @Listener
    public void gameConstruct(GameConstructionEvent event) {
        instanceField = this;
    }

    @Listener
    public void gamePreInit(GamePreInitializationEvent event) {
        loggerField.info("Beginning FoxGuard initialization");
        loggerField.info("Loading configs");
        new FGConfigManager();
        loggerField.info("Saving configs");
        FGConfigManager.getInstance().save();

        loggerField.info("Starting MCStats metrics extension");
        this.stats.start();
    }

    @Listener
    public void gameInit(GameInitializationEvent event) {
        loggerField.info("Getting User Storage");
        userStorage = game.getServiceManager().provide(UserStorageService.class).get();
        loggerField.info("Initializing FoxGuard Manager instance");
        FGManager.init();

        loggerField.info("Registering commands");
        registerCommands();
        loggerField.info("Registering event listeners");
        registerListeners();
        loggerField.info("Setting default player permissions");
        configurePermissions();
        loggerField.info("Registering Regions state field");
        FCStateManager.instance().registerStateFactory(new RegionsStateFieldFactory(), RegionsStateField.ID, RegionsStateField.ID, Aliases.REGIONS_ALIASES);
        loggerField.info("Registering Handlers state field");
        FCStateManager.instance().registerStateFactory(new HandlersStateFieldFactory(), HandlersStateField.ID, HandlersStateField.ID, Aliases.HANDLERS_ALIASES);
    }

    @Listener
    public void serverStarted(GameStartedServerEvent event) {
        loggerField.info("Initializing Handlers database");
        FGStorageManager.getInstance().initHandlers();
        loggerField.info("Loading Handlers");
        FGStorageManager.getInstance().loadHandlers();
        FGStorageManager.getInstance().loadGlobalHandler();

        if (FGConfigManager.getInstance().forceLoad()) {
            loggerField.info("Resolving deferred objects");
            FGStorageManager.getInstance().resolveDeferredObjects();
        }
        loaded = true;
        loggerField.info("Loading linkages");
        FGStorageManager.getInstance().loadLinks();
        loggerField.info("Saving Handlers");
        FGStorageManager.getInstance().writeHandlers();
        loggerField.info("Saving World data");
        for (World world : game.getServer().getWorlds()) {
            loggerField.info("Saving data for World: \"" + world.getName() + "\"");
            FGStorageManager.getInstance().writeWorld(world);
        }

    }

    @Listener
    public void serverStopping(GameStoppingServerEvent event) {
        loggerField.info("Saving Handlers");
        FGStorageManager.getInstance().writeHandlers();
    }

    @Listener
    public void serverStopped(GameStoppedServerEvent event) {
        if (FGConfigManager.getInstance().purgeDatabases()) {
            FoxGuardMain.instance().logger().info("Purging databases");
            FGStorageManager.getInstance().purgeDatabases();
        }
        loggerField.info("Saving configs");
        FGConfigManager.getInstance().save();
    }

    @Listener
    public void worldUnload(UnloadWorldEvent event) {
        loggerField.info("Saving data for World: \"" + event.getTargetWorld().getName() + "\"");
        FGStorageManager.getInstance().writeWorld(event.getTargetWorld());
        FGManager.getInstance().unloadWorld(event.getTargetWorld());
    }

    @Listener
    public void worldLoad(LoadWorldEvent event) {
        loggerField.info("Initializing Regions database for World: \"" + event.getTargetWorld().getName() + "\"");
        FGStorageManager.getInstance().initWorld(event.getTargetWorld());
        loggerField.info("Constructing Region list for World: \"" + event.getTargetWorld().getName() + "\"");
        FGManager.getInstance().createLists(event.getTargetWorld());
        loggerField.info("Loading Regions for World: \"" + event.getTargetWorld().getName() + "\"");
        FGStorageManager.getInstance().loadWorldRegions(event.getTargetWorld());
        loggerField.info("Initializing Global Region for World: \"" + event.getTargetWorld().getName() + "\"");
        FGManager.getInstance().initWorld(event.getTargetWorld());
        if (loaded) {
            if (FGConfigManager.getInstance().forceLoad()) {
                loggerField.info("Resolving deferred objects for World: " + event.getTargetWorld().getName() + "\"");
                FGStorageManager.getInstance().resolveDeferredObjects();
            }
            loggerField.info("Loading links for World: \"" + event.getTargetWorld().getName() + "\"");
            FGStorageManager.getInstance().loadWorldLinks(event.getTargetWorld());
        }
    }

    /**
     * @param jdbcUrl A String representation of the connection url for the database.
     * @return DataSource Object that is retrieved based off of the url from the SqlService.
     * @throws SQLException
     */
    public DataSource getDataSource(String jdbcUrl) throws SQLException {
        if (sql == null) {
            sql = game.getServiceManager().provide(SqlService.class).get();
        }
        try {
            return sql.getDataSource(jdbcUrl);
        } catch (SQLException e) {
            e.printStackTrace();
            File file = new File(jdbcUrl.split(":", 3)[2]);
            if (file.exists()) {
                if (!file.delete()) {
                    file.deleteOnExit();
                    throw e;
                } else {
                    return sql.getDataSource(jdbcUrl);
                }
            } else {
                throw e;
            }
        }
    }

    /**
     * A private method that registers the list of commands, their aliases, and the command class.
     */
    private void registerCommands() {
        FCCommandDispatcher fgDispatcher = new FCCommandDispatcher("/foxguard",
                "FoxGuard commands for managing world protection. Use /help foxguard for subcommands.");
        //FCCommandDispatcher fgRegionDispatcher = new FCCommandDispatcher("/foxguard regions");
        FCCommandDispatcher fgHandlerDispatcher = new FCCommandDispatcher("/foxguard handlers",
                "Commands spcifically meant for managing Handlers.");

        registerCommonCommands(fgDispatcher);
        fgDispatcher.register(new CommandCreate(), "create", "construct", "new", "make", "define", "mk", "cr");
        fgDispatcher.register(new CommandDelete(), "delete", "del", "remove", "rem", "rm", "destroy");
        fgDispatcher.register(new CommandModify(), "modify", "mod", "change", "edit", "update", "md", "ch");
        fgDispatcher.register(new CommandRename(), "rename", "name", "rn");
        fgDispatcher.register(new CommandLink(), "link", "connect", "attach");
        fgDispatcher.register(new CommandUnlink(), "unlink", "disconnect", "detach");
        fgDispatcher.register(new CommandEnableDisable(true), "enable", "activate", "engage", "on");
        fgDispatcher.register(new CommandEnableDisable(false), "disable", "deactivate", "disengage", "off");
        fgDispatcher.register(new CommandList(), "list", "ls");
        fgDispatcher.register(new CommandHere(), "here", "around");
        fgDispatcher.register(new CommandDetail(), "detail", "det", "show");
        fgDispatcher.register(new CommandSave(), "save", "saveall", "save-all");

        fgHandlerDispatcher.register(new CommandPriority(), "priority", "prio", "level", "rank");

        fgDispatcher.register(fgHandlerDispatcher, HANDLERS_ALIASES);

        game.getCommandManager().register(this, fgDispatcher, "foxguard", "foxg", "fguard", "fg");
    }

    private void registerCommonCommands(FCCommandDispatcher dispatcher) {
        Text.Builder builder = Text.builder();
        builder.append(Text.of(TextColors.GOLD, "FoxGuard World Protection Plugin\n"));
        builder.append(Text.of("Version: " + FoxGuardMain.PLUGIN_VERSION + "\n"));
        builder.append(Text.of("Author: gravityfox\n"));

        for (CommandMapping mapping : FoxCoreMain.instance().getFCDispatcher().getCommands()) {
            Set<String> secondary = new HashSet<>(mapping.getAllAliases());
            secondary.remove(mapping.getPrimaryAlias());
            CommandCallable callable = mapping.getCallable();
            if (callable instanceof CommandAbout) ((CommandAbout) callable).addText(builder.build());
            dispatcher.register(callable, mapping.getPrimaryAlias(), new ArrayList<>(secondary));
        }
    }

    /**
     * A private method that registers the Listener class and the corresponding event class.
     */
    private void registerListeners() {
        eventManager.registerListener(this, ChangeBlockEvent.class, new BlockEventListener());
        eventManager.registerListener(this, InteractEvent.class, new InteractListener());
        eventManager.registerListener(this, SpawnEntityEvent.class, new SpawnEntityEventListener());
        if (FGConfigManager.getInstance().getModules().get(FGConfigManager.Module.MOVEMENT)) {
            PlayerMoveListener pml = new PlayerMoveListener();
            eventManager.registerListener(this, DisplaceEntityEvent.class, pml);
            eventManager.registerListeners(this, pml.new Listeners());
        }
    }

    /**
     * A private method that sets up the permissions.
     */
    private void configurePermissions() {
        game.getServiceManager().provide(PermissionService.class).get()
                .getDefaultData().setPermission(SubjectData.GLOBAL_CONTEXT, "foxguard.override", Tristate.FALSE);
    }

    /**
     * @return A Logger instance for this plugin.
     */
    public Logger logger() {
        return loggerField;
    }

    /**
     * Method that when called will return a UserStorageService object that can be used to store or retrieve data corresponding to a specific player.
     *
     * @return UserStorageService object.
     */
    public UserStorageService getUserStorage() {
        return userStorage;
    }

    /**
     * @return A File object corresponding to the config of the plugin.
     */
    public File getConfigDirectory() {
        return configDirectory;
    }

    /**
     * Will return true or false depending on if the plugin has loaded properly or not.
     *
     * @return Depending on the loaded variable
     */
    public boolean isLoaded() {
        return loaded;
    }
}
