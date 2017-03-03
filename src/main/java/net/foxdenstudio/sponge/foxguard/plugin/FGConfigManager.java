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

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

public final class FGConfigManager {

    private static FGConfigManager instance;

    private boolean cleanupFiles;
    private boolean saveWorldRegionsInWorldFolders;
    private boolean saveInWorldFolder;
    private boolean useConfigFolder;
    private boolean useCustomDirectory;
    private Path customDirectory;
    private boolean gcAndFinalize;
    private boolean lockDatabaseFiles;
    private boolean useMMappedFiles;
    private boolean gcCleanerHack;
    private int nameLengthLimit;

    private Map<Module, Boolean> modules = new EnumMap<>(Module.class);

    public FGConfigManager() {
        if (instance == null) instance = this;
        load();
    }

    public static FGConfigManager getInstance() {
        if (instance == null) new FGConfigManager();
        return instance;
    }

    public void save() {
        Path configFile =
                FoxGuardMain.instance().getConfigDirectory().resolve("foxguard.cfg");
        CommentedConfigurationNode root;
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configFile).build();
        if (Files.exists(configFile)) {
            try {
                root = loader.load();
            } catch (IOException e) {
                root = loader.createEmptyNode(ConfigurationOptions.defaults());
            }
        } else {
            root = loader.createEmptyNode(ConfigurationOptions.defaults());
        }
        //--------------------------------------------------------------------------------------------------------------

        root.getNode("storage", "cleanupFiles").setComment("Sets whether to aggressively delete files that are no longer used. Default: true\n" +
                "This is meant to keep the file store clean and free of clutter. It also improves load times.\n" +
                "The caveat is that objects that fail to load are deleted without warning. This normally isn't an issue, even in server crashes.\n" +
                "However, modifying databases and moving the files around can trigger the cleanup.\n" +
                "If plugin simply fails to load the database, it would just be discarded.\n" +
                "Setting this option to false will prevent databases from being deleted.\n" +
                "However, they will still be overwritten if a new database is made with the same name.")
                .setValue(cleanupFiles);

        root.getNode("storage", "location", "saveInWorldFolder").setComment("Whether or not FoxGuard should save object information in the world folder.\n Default: true" +
                "This includes super-regions, handlers, and controllers, but does not include world-regions.\n" +
                "If set to false, files will be placed in a folder in the server root directory.")
                .setValue(saveInWorldFolder);

        root.getNode("storage", "location", "saveWorldRegionsInWorldFolders").setComment("Whether or not FoxGuard should save world-region information in the world folder.\n" +
                "In this case, the files are kept with their corresponding world/dimension.\n" +
                "This makes it easier to copy and paste world data without causing de-synchronization between the world data and FoxGuard data.")
                .setValue(saveWorldRegionsInWorldFolders);
        root.getNode("storage", "location", "useConfigFolder").setComment("Whether or not to place the foxguard folder inside the config folder.\n" +
                "Only applies if files are not kept inside the world folder.")
                .setValue(useConfigFolder);
        root.getNode("storage", "gcAndFinalize").setComment("Whether to run try running gc and finalization when deleting things.\n" +
                "This may drastically slow down the deletion of objects.\n" +
                "Use only if you are having trouble deleting things from in game.\n" +
                "This really only makes a difference on Windows, so you can leave this alone on Unix based operating systems.")
                .setValue(gcAndFinalize);
        root.getNode("storage", "database", "lockDatabaseFiles").setComment("Whether to put a lock on database files while accessing them.\n" +
                "Locking is known to cause Java to hang on Unix based operating systems running on a NFS (Networked File System) that does not properly support locking.\n" +
                "This is often the case if you are using a server host, so be very cautious.\n" +
                "If your server hangs and crashes from the Minecraft watchdog, try setting this to false.")
                .setValue(lockDatabaseFiles);
        root.getNode("storage", "database", "useMMappedFiles").setComment("Whether to enable memory mapping for database files.\n" +
                "This has the potential to greatly speed up saving and loading from database files." +
                "This is known to cause some issues on Windows.\n" +
                "This may be correctable with gcCleanerHack.")
                .setValue(useMMappedFiles);
        root.getNode("storage", "database", "gcCleanerHack").setComment("Whether to enable MapDB's gcCleanerHack functionality.\n" +
                "This is meant for fixing issues with databases being un-deletable on Windows when memory mapping is enabled.\n" +
                "This only makes a difference if memory mapping is enabled, and can potentially decrease performance.")
                .setValue(gcCleanerHack);
        root.getNode("general", "nameLengthLimit").setComment("The length limit for object names. Use 0 or lower for no limit.\n" +
                "Extremely long names can cause a variety of unfixable issues. You have been warned.")
                .setValue(nameLengthLimit);

        for (Module m : Module.values()) {
            root.getNode("module", m.name).setValue(this.modules.get(m));
        }


        //--------------------------------------------------------------------------------------------------------------
        try {
            loader.save(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        Path configFile =
                FoxGuardMain.instance().getConfigDirectory().resolve("foxguard.cfg");
        CommentedConfigurationNode root;
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configFile).build();
        if (Files.exists(configFile)) {
            try {
                root = loader.load();
            } catch (IOException e) {
                root = loader.createEmptyNode(ConfigurationOptions.defaults());
            }
        } else {
            root = loader.createEmptyNode(ConfigurationOptions.defaults());
        }
        //--------------------------------------------------------------------------------------------------------------

        cleanupFiles = root.getNode("storage", "cleanupFiles").getBoolean(true);
        saveInWorldFolder = root.getNode("storage", "saveInWorldFolder").getBoolean(true);
        saveWorldRegionsInWorldFolders = root.getNode("storage", "saveWorldRegionsInWorldFolders").getBoolean(true);
        useConfigFolder = root.getNode("storage", "useConfigFolder").getBoolean(false);
        gcAndFinalize = root.getNode("storage", "gcAndFinalize").getBoolean(false);
        gcAndFinalize = root.getNode("storage", "database", "lockDatabaseFiles").getBoolean(false);
        gcAndFinalize = root.getNode("storage", "database", "useMMappedFiles").getBoolean(false);
        gcAndFinalize = root.getNode("storage", "database", "gcCleanerHack").getBoolean(false);
        nameLengthLimit = root.getNode("general", "nameLengthLimit").getInt(24);
        for (Module m : Module.values()) {
            this.modules.put(m, root.getNode("module", m.name).getBoolean(true));
        }

        //--------------------------------------------------------------------------------------------------------------

        Path path = Sponge.getGame().getSavesDirectory();
    }


    public boolean cleanupFiles() {
        return cleanupFiles;
    }

    public boolean saveWorldRegionsInWorldFolders() {
        return saveWorldRegionsInWorldFolders;
    }

    public boolean saveInWorldFolder() {
        return saveInWorldFolder;
    }

    public boolean useConfigFolder() {
        return useConfigFolder;
    }

    public boolean gcAndFinalize() {
        return gcAndFinalize;
    }

    public boolean lockDatabaseFiles() {
        return lockDatabaseFiles;
    }

    public boolean useMMappedFiles() {
        return useMMappedFiles;
    }

    public boolean gcCleanerHack() {
        return gcCleanerHack;
    }

    public int getNameLengthLimit() {
        return nameLengthLimit;
    }

    public Map<Module, Boolean> getModules() {
        return this.modules;
    }

    public enum Module {
        MOVEMENT("movement");

        String name;

        Module(String name) {
            this.name = name;
        }
    }

}
