package com.moji.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.BiFunction;

@Slf4j
public class LocalExectionUtils {
    public String execuFunction(Executor exec, CommandLine command) {
        String res = "";
        try {
            ByteArrayOutputStream ouputStream = new ByteArrayOutputStream();
            exec.setExitValues(null);
            exec.setStreamHandler(new PumpStreamHandler(ouputStream));
            exec.execute(command);
            res = ouputStream.toString("utf-8");
        } catch (IOException e) {
            log.error("execute command error,msg={}", e.getMessage());
        }
        return res;
    }

    public String execCommand(String commandStr, Executor exec, BiFunction<Executor, CommandLine, String> func) {
        return func.apply(exec, CommandLine.parse(commandStr));
    }

    public String exec(String command) {
        DefaultExecutor exec = new DefaultExecutor();
        return execCommand(command, exec, (c, e) -> execuFunction(c, e));
    }
}
