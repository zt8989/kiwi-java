package com.zt8989.util

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

/**
 * @author zhouteng* @Date 2022/4/7
 */
class OptionUtils {
    static CommandLine getOptions(String[] args) {
        CommandLineParser parser = new DefaultParser();
        def options = new Options()
        def config = Option.builder("config")
                .argName("config")
                .hasArg(true)
                .desc("config file path. default \${baseUrl}/kiwi.yaml")
                .build()

        options.addOption(config)
        def baseUrl = Option.builder("baseUrl")
                .argName("baseUrl")
                .hasArg(true)
                .desc("default current work dir")
                .build()

        def diffRef = Option.builder("diffRef")
                .argName("diffRef")
                .hasArgs()
                .desc("diff with branch[develop]")
                .build()

        def transformLog = Option.builder("transformLog")
                .argName("transformLog")
                .required(false)
                .desc("transform log params form concat string into format string")
                .build()

        options.addOption(config)
        options.addOption(baseUrl)
        options.addOption(diffRef)
        options.addOption(transformLog)
        def cmd = parser.parse(options, args)
        return cmd
    }
}
