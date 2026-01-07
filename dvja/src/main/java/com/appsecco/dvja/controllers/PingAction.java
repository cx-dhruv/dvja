package com.appsecco.dvja.controllers;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class PingAction extends BaseController {

    private String address;
    private String commandOutput;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCommandOutput() {
        return commandOutput;
    }

    public void setCommandOutput(String commandOutput) {
        this.commandOutput = commandOutput;
    }

    public String execute() {
        if(StringUtils.isEmpty(getAddress()))
            return INPUT;

        // Validate address input to prevent command injection
        if(!isValidAddress(getAddress())) {
            addActionError("Invalid address format. Please enter a valid IP address or hostname.");
            return INPUT;
        }

        try {
            doExecCommand();
        } catch (Exception e) {
            addActionMessage("Error running command: " + e.getMessage());
        }

        return SUCCESS;
    }

    /**
     * Validates that the address is a valid IP address or hostname
     * Prevents command injection by restricting input format
     */
    private boolean isValidAddress(String address) {
        if(address == null || address.trim().isEmpty()) {
            return false;
        }
        
        // Allow IPv4 addresses, IPv6 addresses, and valid hostnames
        // Pattern allows:
        // - IPv4: digits (0-255) separated by dots
        // - IPv6: hex digits with colons
        // - Hostname: alphanumeric, dots, and hyphens
        String pattern = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)*[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?$|^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4})$";
        return Pattern.matches(pattern, address.trim());
    }

    private void doExecCommand() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        // Use array form of exec() to prevent command injection
        // Pass address as a separate argument without string concatenation
        String[] command = { "/bin/ping", "-c", "5", getAddress().trim() };
        Process process = runtime.exec(command);

        BufferedReader  stdinputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        String output = "Output:\n\n";

        while((line = stdinputReader.readLine()) != null)
            output += line + "\n";

        output += "\n";
        output += "Error:\n\n";

        stdinputReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while((line = stdinputReader.readLine()) != null)
            output += line + "\n";

        setCommandOutput(output);
    }
}
