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

package net.foxdenstudio.sponge.foxguard.plugin.storage;

import net.foxdenstudio.sponge.foxguard.plugin.handler.IHandler;
import net.foxdenstudio.sponge.foxguard.plugin.object.IGuardObject;
import net.foxdenstudio.sponge.foxguard.plugin.object.ILinkable;
import net.foxdenstudio.sponge.foxguard.plugin.object.path.owner.types.IOwner;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Fox on 7/9/2017.
 * Project: SpongeForge
 */
public class FGSObjectIndex extends FGSObjectMeta {

    Boolean enabled;
    Integer priority;
    List<FGSObjectPath> links;

    public FGSObjectIndex(String name, IOwner owner, String category, String type, Boolean enabled, Integer priority, List<FGSObjectPath> links) {
        super(name, owner, category, type);
        this.enabled = enabled;
        this.priority = priority;
        this.links = links;
    }

    public FGSObjectIndex() {
    }

    public FGSObjectIndex(IGuardObject object) {
        super(object);
        this.enabled = object.isEnabled();
        if (object instanceof IHandler) {
            this.priority = ((IHandler) object).getPriority();
        }
        if (object instanceof ILinkable && ((ILinkable) object).saveLinks()) {
            this.links = ((ILinkable) object).getLinks().stream()
                    .map(FGSObjectPath::new)
                    .collect(Collectors.toList());
        }
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<FGSObjectPath> getLinks() {
        return links;
    }

    public void setLinks(List<FGSObjectPath> links) {
        this.links = links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FGSObjectIndex that = (FGSObjectIndex) o;
        return Objects.equals(enabled, that.enabled) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), enabled, priority, links);
    }
}
