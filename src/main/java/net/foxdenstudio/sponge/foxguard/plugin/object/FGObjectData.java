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

package net.foxdenstudio.sponge.foxguard.plugin.object;

import net.foxdenstudio.sponge.foxguard.plugin.FGManager;
import net.foxdenstudio.sponge.foxguard.plugin.object.path.owner.types.IOwner;

import java.util.UUID;

public class FGObjectData {

    protected String name;
    protected IOwner owner;
    protected boolean enabled;

    public FGObjectData(String name, IOwner owner, boolean enabled) {
        setName(name).setOwner(owner).setEnabled(enabled);
    }

    public FGObjectData() {
        this("", FGManager.SERVER_OWNER, true);
    }

    public String getName() {
        return name;
    }

    public FGObjectData setName(String name) {
        if (name == null) this.name = "";
        else this.name = name;
        return this;
    }

    public IOwner getOwner() {
        return owner;
    }

    public FGObjectData setOwner(IOwner owner) {
        if(owner == null) this.owner = FGManager.SERVER_OWNER;
        else this.owner = owner;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public FGObjectData setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
