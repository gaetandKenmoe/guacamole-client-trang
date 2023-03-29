/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.timer;

import java.io.IOException;
import java.util.Arrays;
import java.util.TimerTask;
import javax.websocket.RemoteEndpoint;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.protocol.GuacamoleInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kenmoe
 */
public class GuacamoleConnectionTimeTask extends TimerTask {
    
    final static long SECS_MINUTE = 60;
    final static long SECS_HEURE = 60 * SECS_MINUTE;
    final static long SECS_DAY = 24 * SECS_HEURE;
    final static long SECS_MONTH = 30 * SECS_DAY;

    /**
     * Logger for this class.
     */
    private final Logger logger = LoggerFactory.getLogger(GuacamoleConnectionTimeTask.class);
    
    /**
     * The remaining time of the ActiveConnectionRecord.
     */
    private long[] remaining_times;
    
    /**
     * Remote (client) side of this connection. This value will always be
     * non-null if tunnel is non-null.
     */
    private RemoteEndpoint.Basic remote;
    
    /**
     * The underlying GuacamoleTunnel. WebSocket reads/writes will be handled
     * as reads/writes to this tunnel. This value may be null if no connection
     * has been established.
     */
    private GuacamoleTunnel tunnel;
    
    /**
     * Sends a Guacamole instruction along the outbound WebSocket connection to
     * the connected Guacamole client. If an instruction is already in the
     * process of being sent by another thread, this function will block until
     * in-progress instructions are complete.
     *
     * @param instruction
     *     The instruction to send.
     *
     * @throws IOException
     *     If an I/O error occurs preventing the given instruction from being
     *     sent.
     */
    private void sendInstruction(String instruction)
            throws IOException {

        // NOTE: Synchronization on the non-final remote field here is
        // intentional. The remote (the outbound websocket connection) is only
        // sensitive to simultaneous attempts to send messages with respect to
        // itself. If the remote changes, then the outbound websocket
        // connection has changed, and synchronization need only be performed
        // in context of the new remote.
        synchronized (remote) {
            remote.sendText(instruction);
        }

    }

    /**
     * Sends a Guacamole instruction along the outbound WebSocket connection to
     * the connected Guacamole client. If an instruction is already in the
     * process of being sent by another thread, this function will block until
     * in-progress instructions are complete.
     *
     * @param instruction
     *     The instruction to send.
     *
     * @throws IOException
     *     If an I/O error occurs preventing the given instruction from being
     *     sent.
     */
    private void sendInstruction(GuacamoleInstruction instruction)
            throws IOException {
        sendInstruction(instruction.toString());
    }

    /**
     * Creates a new task which automatically cleans up after the
     * connection associated with the given ActiveConnectionRecord.The
 connection and parent group will be removed from the maps of active
 connections and groups, and exclusive access will be released.
     *
     * @param remote
     * @param tunnel
     */
    public GuacamoleConnectionTimeTask(RemoteEndpoint.Basic remote, GuacamoleTunnel tunnel) {
        this.remaining_times = tunnel.getRemainingTime();
        this.tunnel = tunnel;
        this.remote = remote;
        if (this.remaining_times[0] == Long.MAX_VALUE && this.remaining_times[1] == Long.MAX_VALUE) {
            try {
                String message = "4.time," + tunnel.getUUID().toString().length() + "." + tunnel.getUUID() + ",9.Unlimited";
                sendInstruction(message);
            } catch (IOException ex) {
                logger.info(ex.getMessage());
            }
            this.cancel();
        }
    }
    
    private String formatToHour(long remaining_time) {
        remaining_time = remaining_time / 1000;
        String heure, min, sec;
        
        heure = Long.toString(remaining_time / SECS_HEURE);
        heure = heure.length() == 1 ? "0" + heure : heure;
        remaining_time %= SECS_HEURE;
        min = Long.toString(remaining_time / SECS_MINUTE);
        min = min.length() == 1 ? "0" + min : min;
        remaining_time %= SECS_MINUTE;
        sec = Long.toString(remaining_time);
        sec = sec.length() == 1 ? "0" + sec : sec;
        return heure + ":" + min + ":" + sec;
    }

    @Override
    public void run() {
        String message = "";
        long remaining_time;
        remaining_time = Arrays.stream(remaining_times).min().getAsLong();
        
        if (remaining_time <= 0) {
            try {
                if (remaining_times[0] == remaining_time)
                    message = "5.error,5.close,6.0x015A;";
                else
                    message = "5.error,5.close,6.0x015B;";
                sendInstruction(message);
            } catch (IOException ex) {
                logger.info(ex.getMessage());
            }
        } else {
            remaining_times[0] -= 1000;
            remaining_times[1] -= 1000;
            message = "4.time," + tunnel.getUUID().toString().length() + "." + tunnel.getUUID();
            String time = "";
            if (remaining_times[0] <= (SECS_DAY * 1000)) {
                String dayRemainingTime = formatToHour(remaining_times[0]);
                time += "Daily Time Left: " + dayRemainingTime;
            }
            
            if (remaining_times[1] <= (SECS_MONTH * 1000)) {
                String monthRemainingTime = formatToHour(remaining_times[1]);
                time += "".equals(time) ? "" : " - ";
                time += "Monthly Time Left: " + monthRemainingTime;
            }
            
            if ("".equals(time)) {
                message += ",9.Unlimited";
            } else {
                message += "," + time.length() + "." + time;
            }
            
            message += ";";
            
        }
        try {
            sendInstruction(message);
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
    }
}
