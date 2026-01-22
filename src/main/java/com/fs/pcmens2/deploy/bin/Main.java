package com.fs.pcmens2.deploy.bin;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import com.fs.pcmens2.deploy.cli.cmd.*;


@Command(
        name = "pcm-deploy",
        mixinStandardHelpOptions = true,
        version = {"pcm-deploy 0.1.0"},
        description = "CLI para despliegues locales de una plataforma Java",
        subcommands = {
                InstallCmd.class, ActivateCmd.class, RollbackCmd.class,
                VerifyCmd.class, StatusCmd.class, MenuCmd.class
        }
)
public class Main implements Runnable {
    @Override public void run() { CommandLine.usage(this, System.out); }
    public static void main(String[] args) {
        int exit = new CommandLine(new Main()).execute(args);
        System.exit(exit);
    }
}

