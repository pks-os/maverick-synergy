package com.sshtools.common.sftp.extensions.multipart;

import java.io.FileNotFoundException;
import java.io.IOException;
import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.sftp.AbstractFileSystem;
import com.sshtools.common.sftp.MultipartTransfer;
import com.sshtools.common.sftp.SftpExtension;
import com.sshtools.common.sftp.SftpSubsystem;
import com.sshtools.common.ssh.Packet;
import com.sshtools.common.util.ByteArrayReader;
import com.sshtools.common.util.Utils;

public class CreateMultipartFileExtension implements SftpExtension {

	public static final String EXTENSION_NAME = "create-multipart-file@sshtools.com";
	
	@Override
	public void processMessage(ByteArrayReader bar, int requestId, SftpSubsystem sftp) {
		
		String path = null;
		
		try {

			AbstractFileSystem fs = sftp.getFileSystem();
			
			path = sftp.checkDefaultPath(bar.readString(sftp.getCharsetEncoding()));
			AbstractFile targetFile = fs.getFileFactory().getFile(path);
			
			if(!targetFile.supportsMultipartTransfers()) {
				sftp.sendStatusMessage(requestId, SftpSubsystem.STATUS_FX_OP_UNSUPPORTED, "Path does not support multipart extensions");
				return;
			}
			
			MultipartTransfer t = fs.startMultipartUpload(targetFile);
			
			Packet reply = new Packet();
		       
	        try {
	        	reply.write(SftpSubsystem.SSH_FXP_EXTENDED_REPLY);
	        	reply.writeInt(requestId);
	        	reply.writeBinaryString(Utils.getUTF8Bytes(t.getUuid()));
	        	reply.writeInt(t.getMinimumPartSize());
	        	reply.write(0);
	       
	        	sftp.sendMessage(reply);
	        
	        } finally {
	        	reply.close();
	        }
		
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
