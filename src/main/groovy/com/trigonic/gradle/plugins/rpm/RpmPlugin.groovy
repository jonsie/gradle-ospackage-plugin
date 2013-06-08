/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trigonic.gradle.plugins.rpm

import org.gradle.api.internal.ConventionMapping
import org.gradle.api.internal.IConventionAware

import java.lang.reflect.Field

import org.freecompany.redline.Builder
import org.freecompany.redline.payload.Directive
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.file.copy.CopySpecImpl
import org.gradle.api.plugins.BasePlugin

class RpmPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply(BasePlugin.class)

        // CopySpec will nest in into() blocks, and Gradle will instaniate CopySpecImpl itself,
        // we have no ability to inject our own. Putting items here mean we won't have type safety.
        // When appending another copy spec to the task, it'll be created a WrapperCopySpec
        [CopySpecImpl, CopySpecImpl.WrapperCopySpec].each {
            it.metaClass.user = null
            it.metaClass.group = null
            it.metaClass.fileType = null
            it.metaClass.createDirectoryEntry = null
            it.metaClass.addParentDirs = true
        }

        Builder.metaClass.getDefaultSourcePackage() {
            format.getLead().getName() + "-src.rpm"
        }

        Directive.metaClass.or = { Directive other ->
            new Directive(delegate.flag | other.flag)
        }

        // Some defaults, if not set by the user
        project.tasks.withType(Rpm) { Rpm task ->
            task.applyConventions()
        }

    }
}