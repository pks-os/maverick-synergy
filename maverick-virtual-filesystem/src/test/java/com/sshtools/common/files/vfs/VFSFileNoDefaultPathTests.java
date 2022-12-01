/*
 *    _           _             _   _
 *   (_) __ _  __| | __ _ _ __ | |_(_)_   _____
 *   | |/ _` |/ _` |/ _` | '_ \| __| \ \ / / _ \
 *   | | (_| | (_| | (_| | |_) | |_| |\ V /  __/
 *  _/ |\__,_|\__,_|\__,_| .__/ \__|_| \_/ \___|
 * |__/                  |_|
 *
 * This file is part of the Maverick Synergy Hotfixes Java SSH API
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *
 * Copyright (C) 2002-2021 JADAPTIVE Limited - All Rights Reserved
 *
 * Use of this software may also be covered by third-party licenses depending on the choices you make about what features to use.
 *
 * Please visit the link below to see additional third-party licenses and copyrights
 *
 * https://www.jadaptive.com/app/manpage/en/article/1565029/What-third-party-dependencies-does-the-Maverick-Synergy-API-have
 */

package com.sshtools.common.files.vfs;
import java.io.IOException;

import org.apache.commons.vfs2.VFS;

import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.tests.DirectFileTests;
import com.sshtools.common.util.FileUtils;

public class VFSFileNoDefaultPathTests extends DirectFileTests {

	VFSFileFactory factory; 
		
	protected void setup() throws IOException {
		System.setProperty("maverick.vfsDefaultPath", getBaseFolder().getAbsoluteFile().toURI().toASCIIString());
		factory = new VFSFileFactory();
	}
	
	protected void clean() throws IOException {
		FileUtils.deleteFolder(getBaseFolder());
	}
	
	
	@Override
	protected AbstractFile getFile(String path) throws PermissionDeniedException, IOException {
		return factory.getFile(path);
	}


	@Override
	protected String getBasePath() throws IOException {
		return VFS.getManager().resolveFile(getBaseFolder().getAbsolutePath()).getName().getURI();
	}


	@Override
	protected String getCanonicalPath() throws IOException {
		return VFS.getManager().resolveFile(getBaseFolder().getAbsolutePath()).getName().getURI();
	}

	
}