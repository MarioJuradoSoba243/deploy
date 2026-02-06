package com.fs.pcmens2.deploy.bin;

import com.fs.pcmens2.deploy.cli.cmd.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "pcm-deploy",
        mixinStandardHelpOptions = true,
        version = {"pcm-deploy 0.2.0"},
        description = "CLI para despliegues locales de la plataforma PCM",
        subcommands = {
                PrepareCmd.class,
                InstallerCmd.class,
                RollbackCmd.class,
                StatusCmd.class,
                PlanCmd.class,
                MenuCmd.class
        }
)
public class Main implements Runnable {
    @Override public void run() { CommandLine.usage(this, System.out); }
    public static void main(String[] args) {
        int exit = new CommandLine(new Main()).execute(args);
        System.exit(exit);
    }
}
