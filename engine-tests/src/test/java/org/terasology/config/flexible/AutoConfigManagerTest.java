// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.config.flexible;

import com.google.gson.Gson;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataReader;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataWriter;

import java.nio.file.FileSystem;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutoConfigManagerTest {
    private static final Name PROVIDING_MODULE = new Name("unittest");
    private final Gson gson = new Gson();

    private final TypeHandlerLibrary typeHandlerLibrary = mock(TypeHandlerLibrary.class);
    private final AutoConfigManager autoConfigManager = new AutoConfigManager(
            typeHandlerLibrary, 
            new GsonPersistedDataWriter(gson),
            new GsonPersistedDataReader(gson),
            new GsonPersistedDataSerializer()
    );

    private final Context context = mock(Context.class);
    private final ModuleManager moduleManager = mock(ModuleManager.class);
    private final ModuleEnvironment environment = mock(ModuleEnvironment.class);

    @BeforeEach
    public void setUp() throws Exception {
        final JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
        final FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
        PathManager.getInstance().useOverrideHomePath(vfs.getPath(""));

        when(environment.getModuleProviding(any())).thenReturn(PROVIDING_MODULE);
        when(environment.getSubtypesOf(eq(AutoConfig.class))).thenReturn(Collections.singleton(TestAutoConfig.class));

        when(moduleManager.getEnvironment()).thenReturn(environment);

        when(context.get(eq(ModuleManager.class))).thenReturn(moduleManager);
    }

    @Test
    public void testLoad() {
        autoConfigManager.loadConfigsIn(context);

        ArgumentCaptor<TestAutoConfig> argumentCaptor = ArgumentCaptor.forClass(TestAutoConfig.class);
        verify(context).put(eq(TestAutoConfig.class), argumentCaptor.capture());

        TestAutoConfig value = argumentCaptor.getValue();
        assertEquals(new SimpleUri(PROVIDING_MODULE, TestAutoConfig.class.getName()), value.getId());
    }
}
