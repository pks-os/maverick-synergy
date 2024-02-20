package com.sshtools.client.sftp;

/*-
 * #%L
 * Client API
 * %%
 * Copyright (C) 2002 - 2024 JADAPTIVE Limited
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;

import com.sshtools.client.tasks.Message;
import com.sshtools.common.util.ByteArrayReader;
import com.sshtools.synergy.ssh.ByteArrays;

public class SftpMessage extends ByteArrayReader implements Message {

      int type;
      int requestId;

      SftpMessage(byte[] msg) throws IOException {
          super(msg);
          type = read();
          requestId = (int) readInt();
      }

      public int getType() {
          return type;
      }

      public int getMessageId() {
          return requestId;
      }

      public void release() {
    	  ByteArrays.getInstance().releaseByteArray(super.buf);
    	  close();
      }
  }
