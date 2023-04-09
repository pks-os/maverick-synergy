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

package com.sshtools.common.sftp.extensions.multipart;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.sftp.AbstractFileSystem;
import com.sshtools.common.sftp.Multipart;
import com.sshtools.common.sftp.SftpExtension;
import com.sshtools.common.sftp.SftpSubsystem;
import com.sshtools.common.util.ByteArrayReader;
import com.sshtools.common.util.UnsignedInteger64;

public class CreateMultipartFileExtension implements SftpExtension {

	public static final String EXTENSION_NAME = "create-multipart-file@sshtools.com";
	
	@Override
	public void processMessage(ByteArrayReader bar, int requestId, SftpSubsystem sftp) {
		
		String path = null;
		String transactionId = null;
		
		Date started = new Date();
		
		try {

			AbstractFileSystem fs = sftp.getFileSystem();
			
			path = sftp.checkDefaultPath(bar.readString(sftp.getCharsetEncoding()));
			AbstractFile targetFile = fs.getFileFactory().getFile(path);
			
			if(!targetFile.supportsMultipartTransfers()) {
				sftp.sendStatusMessage(requestId, SftpSubsystem.STATUS_FX_OP_UNSUPPORTED, "Path does not support multipart extensions");
				return;
			}
			
			int parts = (int) bar.readInt();

			long expectedStart = 0;
			List<Multipart> multiparts = new ArrayList<>();
			
			for(int part = 0; part < parts; part++) {
			
				String partId = bar.readString();
				UnsignedInteger64 position = bar.readUINT64();
				UnsignedInteger64 length = bar.readUINT64();
			
				if(expectedStart!=position.longValue()) {
					sftp.sendStatusMessage(requestId, 9999, "Expected start position of " + expectedStart + " for part " + part + " but got " + position.toString());
					return;
				}
				
				expectedStart = expectedStart += length.longValue();
				Multipart multipart = new Multipart();
				multipart.setStartPosition(position);
				multipart.setLength(length);
				multipart.setPartIdentifier(partId);
				multipart.setTransaction(transactionId);
				multipart.setTargetFile(targetFile);
				
				multiparts.add(multipart);
		
			}
			
			byte[] handle = fs.startMultipartUpload(path, multiparts);
			sftp.sendHandleMessage(requestId, handle);
		
		} catch (FileNotFoundException ioe) {
			sftp.sendStatusMessage(requestId, SftpSubsystem.STATUS_FX_NO_SUCH_FILE, ioe.getMessage());
		} catch (IOException ioe2) {
			sftp.sendStatusMessage(requestId, SftpSubsystem.STATUS_FX_FAILURE, ioe2.getMessage());
		} catch (PermissionDeniedException pde) {
			sftp.sendStatusMessage(requestId, SftpSubsystem.STATUS_FX_PERMISSION_DENIED,
					pde.getMessage());
		} finally {
			bar.close();
		}
	}
	
	@Override
	public boolean supportsExtendedMessage(int messageId) {
		return false;
	}

	@Override
	public void processExtendedMessage(ByteArrayReader msg, SftpSubsystem sftp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDeclaredInVersion() {
		return true;
	}

	@Override
	public byte[] getDefaultData() {
		return new byte[] { };
	}


	@Override
	public String getName() {
		return EXTENSION_NAME;
	}

}
