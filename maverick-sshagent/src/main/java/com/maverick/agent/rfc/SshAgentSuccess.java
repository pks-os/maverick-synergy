package com.maverick.agent.rfc;

import com.maverick.agent.AgentMessage;
import com.maverick.agent.exceptions.InvalidMessageException;
import com.sshtools.common.util.ByteArrayReader;
import com.sshtools.common.util.ByteArrayWriter;




public class SshAgentSuccess extends AgentMessage {
    

    /**
     * Creates a new SshAgentSuccess object.
     */
    public SshAgentSuccess() {
        super(RFCAgentMessages.SSH_AGENT_SUCCESS);
    }

    /**
     *
     *
     * @return
     */
    public String getMessageName() {
        return "SSH_AGENT_SUCCESS";
    }

    /**
     *
     *
     * @param baw
     *
     * @throws java.io.IOException
     * @throws com.sshtools.j2ssh.transport.InvalidMessageException DOCUMENT
     *         ME!
     */
    public void constructByteArray(ByteArrayWriter baw)
        throws java.io.IOException, 
            InvalidMessageException {
    }

    /**
     *
     *
     * @param bar
     *
     * @throws java.io.IOException
     * @throws com.sshtools.j2ssh.transport.InvalidMessageException DOCUMENT
     *         ME!
     */
    public void constructMessage(ByteArrayReader bar)
        throws java.io.IOException, 
            InvalidMessageException {
    }
}
